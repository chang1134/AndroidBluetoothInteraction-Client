package com.biggerchang.bluetoothclientv2.bluetooth.connect;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.biggerchang.bluetoothclientv2.SysDefine;
import com.biggerchang.bluetoothclientv2.bluetooth.interactive.BluetoothClient;
import com.biggerchang.bluetoothclientv2.bluetooth.utils.ClsUtils;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 负责蓝牙连接和通讯
 */
public class BluetoothConnector {

    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private Context mContext;
    private BluetoothClient mBluetoothClient;
    private static final String TAG = "BluetoothConnector";
    private static final String PIN = "1234";
    private BluetoothDevice mFoundDevice;
    private ExecutorService mCatchThreadPool;

    public BluetoothConnector(Context context, Handler handler) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //获得数据时的接口回调
        mHandler = handler;
        initReceiver();
        mCatchThreadPool = Executors.newCachedThreadPool();
    }

    //设置可见性
    public void setDiscoverable() {
        //启动修改蓝牙可见性的Intent
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        //设置蓝牙可见性的时间，方法本身规定最多可见300秒,0表示永久开启
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        mContext.startActivity(intent);
    }

    //开始连接蓝牙服务端
    private void connect(BluetoothDevice device) {
        if (device == null) return;
        mBluetoothClient = new BluetoothClient(device, mHandler,mCatchThreadPool);
        mCatchThreadPool.execute(mBluetoothClient);
    }

    //开启蓝牙
    public void setAdapterEnable() {
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }

    //连接已经配对的设备
    public void connectPairedDevice() {
        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : bondedDevices) {
            if (device != null && SysDefine.BOX_BLUETOOTH_DEVICE_NAME.equals(device.getName())) {
                //尝试连接
                connect(device);
                break;
            }
        }
    }

    //匹配蓝牙设备
    public void pairDevice() {
        if (mFoundDevice == null) {
            Toast.makeText(mContext, "无法配对，没有找到指定设备", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            //通过工具类ClsUtils,调用createBond方法
            ClsUtils.createBond(mFoundDevice.getClass(), mFoundDevice);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //连接刚找到的设备
    public void connectFoundDevice() {
        //获取到搜索的到蓝牙设备
        connect(mFoundDevice);
    }

    //开始搜索
    public void startDiscovery() {
        mBluetoothAdapter.startDiscovery();
    }

    //发送数据
    public void send(Object data) {
        if (mBluetoothClient != null) {
            mBluetoothClient.send(data);
        }
    }

    //断开连接
    public void close() {
        if (mBluetoothClient != null) {
            mBluetoothClient.close();
        }
        mContext.unregisterReceiver(mReceive);
    }

    private void initReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SysDefine.BLUETOOTH_DEVICE_ACTION_PAIRING_REQUEST);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(mReceive, filter);
    }

    private BroadcastReceiver mReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 从Intent中获取设备对象
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            switch (intent.getAction()) {
                case BluetoothDevice.ACTION_FOUND: {
                    if (device != null && SysDefine.BOX_BLUETOOTH_DEVICE_NAME.equals(device.getName())) {
                        if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                            mFoundDevice = device;
                            //停止搜索
                            mBluetoothAdapter.cancelDiscovery();
                        }
                    }
                }
                break;
                case SysDefine.BLUETOOTH_DEVICE_ACTION_PAIRING_REQUEST: {
                    if (device != null) {
                        try {
                            //1.确认配对
                            ClsUtils.setPairingConfirmation(device.getClass(), device, true);
                            //2.终止有序广播
                            abortBroadcast();//如果没有将广播终止，则会出现一个一闪而过的配对框。
                            //3.调用setPin方法进行配对...
                            ClsUtils.setPin(device.getClass(), device, PIN);
                            Toast.makeText(mContext, "匹配成功", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else
                        Log.e("提示信息", "这个设备不是目标蓝牙设备");
                }
                break;
                default:
                    Log.d(TAG, "onReceive: ERROR ACTION :" + intent.getAction());
                    break;
            }
        }
    };
}
