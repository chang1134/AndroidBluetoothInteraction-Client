package com.biggerchang.bluetoothclientv2.bluetooth.interactive;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import com.biggerchang.bluetoothclientv2.SysDefine;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BluetoothClient extends Thread {
    private Handler mHandler;
    private ExecutorService mCatchThreadPool;
    private BluetoothSocket mSocket;
    private BluetoothIO mBluetoothIO;
    private boolean isConnected = false;
    private BluetoothDevice mDevice;

    public BluetoothClient(BluetoothDevice device, Handler handler, ExecutorService catchThreadPool) {
        mDevice = device;
        mCatchThreadPool = catchThreadPool;
        mHandler = handler;
    }

    @Override
    public void run() {
        while (true) {
            if (!isConnected) {
                connect();
            } else {
                //连接成功了，就直接退出
                break;
            }
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void connect() {
        try {
            mSocket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString(SysDefine.SOCKET_UUID));
            mSocket.connect();
            mBluetoothIO = new BluetoothIO(mSocket, mHandler);
            mCatchThreadPool.execute(mBluetoothIO);
            isConnected = true;
            Message.obtain(mHandler, SysDefine.MSG_WHAT_BLUETOOTH_CONNECT_SUCCESS).sendToTarget();
        } catch (IOException e) {
            e.printStackTrace();
            isConnected = false;
            //连接失败
            Message.obtain(mHandler, SysDefine.MSG_WHAT_BLUETOOTH_CONNECT_FAILED).sendToTarget();
        }
    }

    public void send(Object data) {
        if (mBluetoothIO != null) mBluetoothIO.send(data);
    }

    public void close() {
        isConnected = false;
        if (mBluetoothIO != null) {
            mBluetoothIO.closeSocket();
        }
        mBluetoothIO = null;
    }
}
