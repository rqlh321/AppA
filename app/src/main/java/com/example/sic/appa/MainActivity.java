package com.example.sic.appa;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.Date;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    public final static String TAG = "ProgForce";
    public static final String APP_RECIPIENT = "com.example.sic.appb";
    public static final String PID = "0";
    public static final String WIFI_STATE = "1";
    public static final String SCREEN_STATE = "2";

    private static TextView status;

    private Intent appBServiceIntent;

    private int pid;
    private WifiManager wifiManager;

    private boolean binded = false;
    private ServiceConnection serviceConnection;
    private Messenger messenger;


    @OnClick(R.id.start_service)
    public void onStartServiceClick() {
        bindService(appBServiceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    @OnClick(R.id.send_info)
    public void onSendInfoClick() {
        if (binded) {
            int wifiState = wifiManager.isWifiEnabled() ? 1 : 0;

            Message msg = Message.obtain();

            Bundle bundle = new Bundle();
            bundle.putInt(PID, pid);
            bundle.putInt(WIFI_STATE, wifiState);
            msg.setData(bundle);

            try {
                messenger.send(msg);
                Log.d(TAG, "FirstApp:: Sent info at " + (new Date()) + ", WiFiState= " + wifiState);
                status.setText("FirstApp:: Sent info at " + (new Date()) + ", WiFiState= " + wifiState);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        status = (TextView) findViewById(R.id.status);

        appBServiceIntent = new Intent("appb.service.intent");
        appBServiceIntent.setPackage(APP_RECIPIENT);

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                messenger = new Messenger(iBinder);
                binded = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                binded = false;
            }
        };

        pid = android.os.Process.myPid();
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    }


    public static class AppADataReceive extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();

            status.setText("FirstApp:: Received info at " + new Date() +
                    " from process " + bundle.getInt(PID) +
                    ", ScreenState = " + bundle.getInt(SCREEN_STATE));

            Log.d(TAG, "FirstApp:: Received info at " + new Date() +
                    " from process " + bundle.getInt(PID) +
                    ", ScreenState = " + bundle.getInt(SCREEN_STATE));
        }
    }
}
