package com.lepu.nordicble.activity

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.text.TextUtils
import android.widget.AdapterView
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.nordicble.R
import com.lepu.nordicble.ble.BleService
import com.lepu.nordicble.const.BleConst
import com.lepu.nordicble.objs.BleAdapter
import com.lepu.nordicble.objs.Bluetooth
import com.lepu.nordicble.objs.BluetoothController
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    private lateinit var adapter: BleAdapter
    private var currentModel: Int = Bluetooth.MODEL_ER1
    private lateinit var list : ArrayList<Bluetooth>


    lateinit var bleService: BleService

    private val bleConn = object : ServiceConnection  {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            bleService = (p1 as BleService.BleBinder).getService()
            bleService.startDiscover()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            TODO("Not yet implemented")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initService()
        currentModel = intent.extras?.get("TYPE") as Int
        setContentView(R.layout.activity_search)
        initUI()
    }

    private fun initService() {
        BleService.startService(this)

        Intent(this, BleService::class.java).also {
            intent -> bindService(intent, bleConn, Context.BIND_AUTO_CREATE)
        }
    }

    private fun initUI() {
        when(currentModel) {
            Bluetooth.MODEL_ER1 -> {
                toolbar_title.text = getString(R.string.name_er1)
            }
            Bluetooth.MODEL_CHECKO2 -> {
                toolbar_title.text = getString(R.string.name_o2)
            }
            Bluetooth.MODEL_KCA -> {
                toolbar_title.text = getString(R.string.name_kca)
            }
        }

        setAdapter()

        action_back.setOnClickListener {
            this.finish()
        }
        refresh.setOnClickListener {
            bleService.startDiscover()
        }
    }

    private fun setAdapter() {
        list = BluetoothController.getDevices(currentModel)
        adapter = BleAdapter(this, list)
        ble_list.adapter = adapter
        ble_list.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
//                connect(BluetoothController.getDevices()[position])
                val b = BluetoothController.getDevices(currentModel)[position]
                LogUtils.d("clicked: ${b.name}")
                when(currentModel) {
                    Bluetooth.MODEL_ER1 -> {
                        LiveEventBus.get(BleConst.EventBindEr1Device)
                                .postAcrossProcess(b)
                        this.finish()
                    }
                    Bluetooth.MODEL_O2MAX -> {
                        LiveEventBus.get(BleConst.EventBindO2Device)
                                .postAcrossProcess(b)
                        this.finish()
                    }
                    Bluetooth.MODEL_KCA -> {
                        LiveEventBus.get(BleConst.EventBindKcaDevice)
                                .postAcrossProcess(b)
                        this.finish()
                    }
                }
            }

    }



    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        bleService.stopDiscover()
    }
}