package com.biggerchang.bluetoothclientv2;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.biggerchang.bluetoothclientv2.bluetooth.connect.BluetoothConnector;

public class MainActivity extends Activity implements View.OnClickListener {

    public static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    //判断是否拥有权限
    private boolean mIsOwnBluetoothPermission = false;
    private static final String TAG = "MainActivity";
    private BluetoothConnector mConnector;
    private EditText mEtSend;
    private TextView mTvSend;
    private TextView mTvReceive;
    private static final int MSG_WHAT_SEND_MSG_ON_TIME = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkBluetoothPermission();
        initView();
        mConnector = new BluetoothConnector(this, mHandler);
    }

    private void initView() {
        findViewById(R.id.btn_enable).setOnClickListener(this);
        findViewById(R.id.btn_connect_paired_device).setOnClickListener(this);
        findViewById(R.id.btn_search).setOnClickListener(this);
        findViewById(R.id.btn_pair).setOnClickListener(this);
        findViewById(R.id.btn_connect_found_device).setOnClickListener(this);
        findViewById(R.id.btn_send).setOnClickListener(this);
        mEtSend = (EditText) findViewById(R.id.et_send);
        mTvSend = (TextView) findViewById(R.id.tv_send);
        mTvReceive = (TextView) findViewById(R.id.tv_receive);
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SysDefine.MSG_WHAT_BLUETOOTH_CONNECT_FAILED:
                    Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                    break;
                case SysDefine.MSG_WHAT_BLUETOOTH_CONNECT_SUCCESS:
                    Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                    mConnector.send(System.currentTimeMillis());
                    mTvSend.setText(System.currentTimeMillis() + "");
                    mStartTime = System.currentTimeMillis();
                    connectCount++;
                    everyCounts += currentCount + " ";
                    currentCount = 0;
                    break;
                case SysDefine.MSG_WHAT_BLUETOOTH_RECONNECT:
                    Toast.makeText(MainActivity.this, "重新连接", Toast.LENGTH_SHORT).show();
                    mConnector.connectPairedDevice();
                    break;
                case SysDefine.MSG_WHAT_BLUETOOTH_RECEIVE_DATA:
                    currentCount++;
                    long offsetTime = System.currentTimeMillis() - Long.parseLong(msg.obj.toString());
                    totalTime += offsetTime;
                    long allTime = System.currentTimeMillis() - mStartTime;
                    Log.d(TAG, "总循环次数：" + ++index + "  本轮循环次数：" + connectCount + " 本轮循环的时间：" + allTime + "  总时间：" + totalTime + " 每轮的次数：" + everyCounts + "  平均每次时长：" + (totalTime * 1.0f / index));
                    mTvReceive.setText(msg.obj.toString());
                    mHandler.sendEmptyMessageDelayed(MSG_WHAT_SEND_MSG_ON_TIME, 1000);
                    break;
                case MSG_WHAT_SEND_MSG_ON_TIME:
                    mTvSend.setText(System.currentTimeMillis() + "");
                    mConnector.send(System.currentTimeMillis());
                    break;
                default:
                    Log.d(TAG, "handleMessage: ERROR MSG:" + msg.obj);
                    break;
            }
        }
    };
    private int index = 0;
    private long mStartTime = 0;
    private long totalTime = 0;
    private int connectCount = 0;
    private int currentCount = 0;
    private String everyCounts = "";

    /*
       校验蓝牙权限
      */
    private void checkBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            //校验是否已具有模糊定位权限
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            } else {
                //具有权限
                mIsOwnBluetoothPermission = true;
            }
        } else {
            //系统不高于6.0直接执行
            mIsOwnBluetoothPermission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //同意权限
                mIsOwnBluetoothPermission = false;
            } else {
                // 权限拒绝
                // 下面的方法最好写一个跳转，可以直接跳转到权限设置页面，方便用户
                Toast.makeText(this, "获取权限失败，无法进行蓝牙连接", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onClick(View v) {
        if (!mIsOwnBluetoothPermission) {
            Toast.makeText(this, "蓝牙权限不足", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (v.getId()) {
            case R.id.btn_enable:
                mConnector.setAdapterEnable();
                break;
            case R.id.btn_connect_paired_device:
                mConnector.connectPairedDevice();
                break;
            case R.id.btn_search:
                mConnector.startDiscovery();
                break;
            case R.id.btn_pair:
                mConnector.pairDevice();
                break;
            case R.id.btn_connect_found_device:
                mConnector.connectFoundDevice();
                break;
            case R.id.btn_send:
                mConnector.send(mEtSend.getText().toString());
                break;
            default:
                Log.d(TAG, "onClick: ");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        mConnector.close();
        super.onDestroy();
    }
}
