package com.lepu.nordicble.utils

import android.app.Activity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.arialyy.aria.core.Aria
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.*
import com.lepu.nordicble.R
import com.lepu.nordicble.annotation.CheckVersionType
import com.lepu.nordicble.bean.CheckVersionBean
import com.lepu.nordicble.views.NormalDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_setting_about.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class NetObserver(private var lifecycleOwner: LifecycleOwner) : LifecycleObserver {

    var checkVerBean: CheckVersionBean? = null

    private var taskId: Long? = null
    private val compositeDis by lazy { CompositeDisposable() }

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onLifecycleCreate() {
        LogUtils.i("${lifecycleOwner.javaClass.simpleName}  onLifecycleCreate")
        Aria.download(lifecycleOwner).register()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onLifecycleDestory() {
        LogUtils.i("${lifecycleOwner.javaClass.simpleName} onLifecycleDestory")
        compositeDis.takeIf { it.isDisposed.not() }?.let {
            it.clear()
            it.dispose()
        }

        cancelDownloadTask()
        Aria.download(lifecycleOwner).unRegister()

    }

    private fun cancelDownloadTask() {
        taskId?.let { taskId ->
            Aria.download(lifecycleOwner).load(taskId).cancel()
        }
    }

    fun checkVersion(@CheckVersionType key: String = CheckVersionType.WIRELESS) {
        NetUtils.retrofit.create(NetInterface::class.java)
            .checkVersion(NetUtils.getCheckVersion(key))
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(Consumer { bean ->
                checkVerBean = bean
                LogUtils.json(GsonUtils.toJson(bean))
                var currentCode = AppUtils.getAppVersionCode()
                var cloundCode = bean.version
                if (currentCode < cloundCode) {
                    LogUtils.i("服务器版本高于本地版本,需要更新")

                    PermissionUtils.permission(
                        PermissionConstants.LOCATION,
                        PermissionConstants.STORAGE
                    ).callback(object : PermissionUtils.SimpleCallback {
                        override fun onGranted() {

                            NormalDialog().also {
                                it.setMsgContent(
                                    String.format(
                                        Utils.getApp().getString(R.string.update_version_tip),
                                        AppUtils.getAppName(), bean.versionName
                                    )
                                )
                                it.setSureAction {
                                    taskId = Aria.download(lifecycleOwner)
                                        .load(bean.fileUrl)
                                        .setFilePath(createApkStorePath(bean.versionName))
                                        .create()
                                }
                            }.show()
                        }
                        override fun onDenied() {

                        }
                    }).request()
                } else {
                    ToastUtils.showShort(R.string.newest_app_version)
                }

            }, Consumer { error ->
                error.printStackTrace()
                LogUtils.i("error->${error.message}")
            })?.let {
                compositeDis.add(it)
            }
    }


    private fun createApkStorePath(versionName: String): String {
        var temPath = PathUtils.getExternalStoragePath() + "/nordicble/apk"
        FileUtils.createOrExistsDir(temPath)

        var apkFile = File(temPath + "nordicBle${versionName}Version.apk")
        if (apkFile.exists()) {
            apkFile.delete()
        }
        return apkFile.absolutePath
    }


}