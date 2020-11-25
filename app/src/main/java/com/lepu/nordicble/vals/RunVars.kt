package com.lepu.nordicble.vals

import android.icu.text.IDNA
import com.lepu.nordicble.ble.obj.Er1Device

/**
 * relay info
 */
var relayId: String = "123456"

/**
 * ER1
 */
public var hasEr1 = false
public var er1Name: String? = null
public var er1Sn: String? = null
public var er1Battery = 0
public var er1BleError = 0
public var er1RecordTime = 0
public var er1Conn = false

/**
 * Oxy
 */
public var hasOxy = false
public var oxyName: String? = null
public var oxySn: String? = null
public var oxyBattery = 0
public var oxyBleError = 0
public var oxyRecordTime = 0
public var oxyConn = false

/**
 * KCA
 */
public var hasKca = false
public var kcaName: String? = null
public var kcaSn: String? = null
public var kcaBattery = 0
public var kcaBleError = 0
public var kcaRecordTime = 0
public var kcaConn = false

/**
 * 收发器电池数组
 */
val relayBatArr = arrayListOf<Int>(0,0,1,1,1,1,2,2,2,3,3,3,3,4,4,4,4,5,5,5,6,6,6,6,7,7,7,8,8,8,8,9,9,9,10,10,10,10,11,11,11,11,12,12,12,13,13,13,13,14,14,14,15,15,15,15,16,16,16,17,17,17,17,18,18,18,18,19,19,19,20,20,20,20,21,21,21,22,22,22,22,23,23,23,24,24,24,24,25,25,25,25,26,26,26,27,27,27,27,28,28)

/**
 * er1 电池数组
 */
val erBatArr =  arrayListOf<Int>(0,0,0,2,3,4,4,5,6,7,7,8,9,9,10,11,11,12,12,13,13,14,15,15,15,16,16,17,17,18,18,18,19,21,21,22,22,22,22,23,23,24,24,24,25,25,26,26,27,27,28,29,29,30,31,31,32,33,33,34,35,36,37,38,38,39,40,42,43,43,44,45,46,47,48,49,50,51,52,53,54,55,55,56,58,59,60,61,62,63,64,65,66,67,68,69,70,72,73,74,75)

var hostIp: String? = null
var hostPort: Int? = null

var hostNeedConnect : Boolean = false

/**
 * phone
 */

var id : String? = null

// wifi
var wifiState = false
// -45 ~ -100, 加上100
var wifiRssi = 100
var wifiSsid = ""
//var hostState = false

/**
 * 0x00 : 电池供电
 * 0x10 : 低电量
 * 0x01 : 充电中
 */
var batteryState : Int = 0
var battery = 0

var socketState = false
var socketToken: ByteArray? = null

fun clearSocketVars() {
    socketState = false
    socketToken = null
}

var networkErrors = 0
var longitude = 0
var latitude = 0

/**
 * ECG module
 */
// lead info
var lead: Int = 0x02

// ble 是否打开
var er1Bond = false
var bleState = false
var bleName: String? =null
var bleSN: String? = null
var bleMacAddr: String? = null
// ble 是否连接
var bleConnected: Boolean = false
var bleBattery = 0
var bleErrors = 0
var recordTime = 0
var curHr = 0
// 获取到设备信息
var gotInfo = false



var curER1 : Er1Device? = null

fun clearBleVars() {
    er1Bond = false
    bleState = false
    bleName =null
    bleSN = null
    bleMacAddr = null
    bleConnected = false
    bleBattery = 0
    bleErrors = 0
    recordTime = 0
    curHr = 0
    gotInfo = false
}

fun clearER1Vars() {
    recordTime = 0
    bleBattery = 0
    curHr = 0
    gotInfo = false
}