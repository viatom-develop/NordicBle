package com.lepu.nordicble.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import androidx.annotation.NonNull
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.nordicble.ble.cmd.KcaBleCmd
import com.lepu.nordicble.ble.cmd.KcaBleCmd.*
import com.lepu.nordicble.ble.cmd.KcaBleResponse
import com.lepu.nordicble.objs.Bluetooth
import com.lepu.nordicble.utils.HexString
import com.lepu.nordicble.utils.add
import com.lepu.nordicble.utils.toHex
import com.lepu.nordicble.vals.EventMsgConst
import com.lepu.nordicble.vals.kcaBattery
import com.lepu.nordicble.vals.kcaSn
import com.lepu.nordicble.viewmodel.KcaViewModel
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.observer.ConnectionObserver

class KcaBleInterface : ConnectionObserver, KcaBleManger.onNotifyListener {

    private var er1DeviceName: String? = null
    private var oxyDeviceName: String? = null
    private var kcaDeviceName: String? = null

    private var continueScan = false

    private lateinit var model: KcaViewModel
    public fun setViewModel(viewModel: KcaViewModel) {
        this.model = viewModel
    }

    lateinit var manager: KcaBleManger

    lateinit var mydevice: BluetoothDevice

    private var pool: ByteArray? = null

    private var count: Long = 0L

    /**
     * interface
     * state
     * connect
     * disconnect
     * getInfo
     * getRtData
     */
    public var state = false
    private var connecting = false

    public fun connect(context: Context, @NonNull device: BluetoothDevice) {
        if (connecting || state) {
            return
        }
        manager = KcaBleManger(context)
        mydevice = device
        manager.setConnectionObserver(this)
        manager.setNotifyListener(this)
        manager.connect(device)
            .useAutoConnect(false)
            .timeout(10000)
            .retry(3, 100)
            .done {
                LogUtils.d("Device Init")
//                runRtTask()
            }
            .enqueue()

    }

    public fun disconnect() {
        manager.disconnect()
        manager.close()

        this.onDeviceDisconnected(mydevice, ConnectionObserver.REASON_SUCCESS)
    }


    private fun sendCmd(cmd : Int, bs: ByteArray) {
//        val bleJob = BleJobController.BleJob(cmd, bs, timeout)
        manager.sendCmd(bs)
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(pkg: KcaPackage) {
//        controller.onBleResponseReceived(response)
        val kcaContent = KcaContent(pkg.content)
//        LogUtils.d("received cmd: ${kcaContent.cmd}")
//        for (key in kcaContent.keyObjs) {
//            LogUtils.d("received key: ${kcaContent.cmd} -> ${key.key} ~ ${key.`val`.toHex()}")
//        }

        // broadcast
        when(kcaContent.cmd) {
            KcaBleCmd.CMD_CONFIG -> {
                val key = kcaContent.keyObjs[0] as KeyObj
                when(key.key) {
                    KEY_TIME_RES -> {
                        LogUtils.d("设置时间成功")
                    }
                }
            }
            KcaBleCmd.CMD_STATE -> {
                val key : KcaBleCmd.KeyObj = kcaContent.keyObjs[0]
                model.measureState.value = key.key

                when(key.key) {
                    KEY_MEASURE_START -> {
                        LiveEventBus.get(EventMsgConst.EventKcaMeasureState)
                            .postAcrossProcess(KcaBleResponse.KcaBpState(KEY_MEASURE_START, 0))
                    }
                    KEY_MEASURING -> {
                        val bp: Int = ((key.`val`[0].toUInt() and 0xFFu) shl 8 or (key.`val`[1].toUInt() and 0xFFu)).toInt()
                        model.rtBp.value = bp

                        LiveEventBus.get(EventMsgConst.EventKcaMeasureState)
                            .postAcrossProcess(KcaBleResponse.KcaBpState(KEY_MEASURING, bp))
                    }
                    KEY_MEASURE_RESULT -> {
                        val result: KcaBleResponse.KcaBpResult =
                            KcaBleResponse.KcaBpResult(
                                key.`val`
                            )
                        model.bpResult.value = result

                        LiveEventBus.get(EventMsgConst.EventKcaMeasureState)
                            .postAcrossProcess(KcaBleResponse.KcaBpState(KEY_MEASURE_RESULT, result.sys))

                        LiveEventBus.get(EventMsgConst.EventKcaBpResult)
                            .postAcrossProcess(result)
                    }
                }
            }
            KcaBleCmd.CMD_DATA -> {
                val key = kcaContent.keyObjs[0] as KeyObj

                when(key.key) {
                    KEY_SN_RES -> {
                        val sn = HexString.trimStr(String(key.`val`))
                        LogUtils.d("获取到SN: $sn")
                        kcaSn = sn
                        LiveEventBus.get(EventMsgConst.EventKcaSn)
                            .postAcrossProcess(sn)
                    }
                    KEY_BATTERY_RES -> {
                        val battery = key.`val`[0].toUInt().toInt()
                        model.battery.value = battery
                        kcaBattery = battery
                        LogUtils.d("获取到电量: $battery")
                    }
                }
            }

        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 8) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size-7) {
            if (bytes[i] != 0x5A.toByte()) {
                continue@loop
            }

            // need content length
            val len = ((bytes[i+2].toUInt() and 0xFFu) shl 8 or (bytes[i+3].toUInt() and 0xFFu)).toInt()
//            LogUtils.d("want bytes length: $len")
            if (i+8+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i+8+len)

            val res = KcaPackage(temp)
            if (!res.crcHasErr) {
                onResponseReceived(res)

                val tempBytes: ByteArray? = if (i+8+len == bytes.size) null else bytes.copyOfRange(i+8+len, bytes.size)
                return hasResponse(tempBytes)
            }

        }

        return bytesLeft
    }

    override fun onNotify(device: BluetoothDevice?, data: Data?) {
        data?.value?.apply {
            pool = add(pool, this)
//            LogUtils.d(pool!!.toHex())
        }
        pool?.apply {
            pool = hasResponse(pool)
        }
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        state = true
        model.connect.value = state

        connecting = false
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
        state = false
        model.connect.value = state

        connecting = true
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        state = false
        model.connect.value = state
        LiveEventBus.get(EventMsgConst.EventDeviceDisconnect).postAcrossProcess(Bluetooth.MODEL_KCA)

        connecting = false
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        state = false
        model.connect.value = state

        connecting = false
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        state = false
        model.connect.value = state

        connecting = false
    }

    override fun onDeviceReady(device: BluetoothDevice) {

//        LogUtils.d(mydevice.name)

        connecting = false
    }
}