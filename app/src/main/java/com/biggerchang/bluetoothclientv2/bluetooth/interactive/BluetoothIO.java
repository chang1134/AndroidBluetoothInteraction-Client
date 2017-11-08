package com.biggerchang.bluetoothclientv2.bluetooth.interactive;


import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.biggerchang.bluetoothclientv2.SysDefine;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class BluetoothIO extends Thread {

    private BluetoothSocket mSocket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private boolean mIsClose = false;
    private Handler mHandler;
    private static final String TAG = "BluetoothIO";

    public BluetoothIO(BluetoothSocket socket, Handler handler) {
        mHandler = handler;
        mSocket = socket;
        if (mSocket == null) {
            Log.d(TAG, "BluetoothIO: mSocket == null");
            return;
        }

        try {
            if (mSocket.getOutputStream() == null) {
                Log.d(TAG, "BluetoothIO: mSocket == null");
                return;
            }
            oos = new ObjectOutputStream(mSocket.getOutputStream());
            if (oos == null) {
                Log.d(TAG, "BluetoothIO: oos == null");
            }
            ois = new ObjectInputStream(new BufferedInputStream(mSocket.getInputStream()));
            if (ois == null) {
                Log.d(TAG, "BluetoothIO: ois == null");
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        receive();
    }

    //循环接收数据
    private void receive() {
        while (true) {
            if (mIsClose) break;
            try {
                if (ois != null) {
                    Object obj = ois.readObject();
                    Message.obtain(mHandler, SysDefine.MSG_WHAT_BLUETOOTH_RECEIVE_DATA, obj).sendToTarget();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                closeSocket();
                Message.obtain(mHandler, SysDefine.MSG_WHAT_BLUETOOTH_RECONNECT).sendToTarget();
                break;
            }
        }
    }

    /**
     * 发送数据
     * 返回值判断是否发送成功
     */
    public boolean send(Object data) {
        if (oos == null || data == null || mIsClose) return false;
        try {
            oos.flush();
            Log.d("Bluetooth", "发送数据：" + data);
            oos.writeObject(data);
            oos.flush();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    //关闭当前连接
    public void closeSocket() {
        Log.d(TAG, "closeSocket: 关闭socket");
        mIsClose = true;
        try {
            if (oos != null) oos.close();
            if (ois != null) ois.close();
            if (mSocket != null) mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            oos = null;
            ois = null;
            mSocket = null;
        }
    }
}
