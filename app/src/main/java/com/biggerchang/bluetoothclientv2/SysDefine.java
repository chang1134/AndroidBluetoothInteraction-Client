package com.biggerchang.bluetoothclientv2;

/**
 * Created by user on 2017/11/1.
 */

public class SysDefine {
    public static final String SOCKET_UUID = "3949bfbf-439e-456b-8058-e04a357bc10a";
    public static final String BLUETOOTH_DEVICE_ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
    public static final String MESSAGE_END_MARK = "#!#";
    public static final int MSG_WHAT_BLUETOOTH_CONNECT_SUCCESS = 100;
    public static final int MSG_WHAT_BLUETOOTH_CONNECT_FAILED = 99;
    public static final int MSG_WHAT_BLUETOOTH_RECONNECT = 98;
    public static final int MSG_WHAT_BLUETOOTH_RECEIVE_DATA = 97;
    //盒子端的蓝牙设备的名称 TODO 暂用小手机进行测试
    public static final String BOX_BLUETOOTH_DEVICE_NAME = "dcage_vivosmall";
}
