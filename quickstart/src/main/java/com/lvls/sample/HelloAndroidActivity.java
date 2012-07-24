package com.lvls.sample;

import javax.naming.Binding;

import com.lvls.sample.service.MyBinder;
import com.lvls.sample.service.MyService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class HelloAndroidActivity extends Activity {

    private static String TAG = "quickstart";

    private MyService myBoundService;
    private boolean bound;

    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.i(TAG, "> onCreate");
        setContentView(R.layout.main);
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "> onStart .. after onCreate or onRestart");
        super.onStart();
    }

    @Override
    protected void onRestart() {
        Log.i(TAG, "> onRestart .. coming to foreground");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "> onResume .. back after something else took priority");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "> onPause .. something else has taken priority; save state quickly");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "> onStop .. deep sleep");
        super.onStop();
    }

    public void clickMyButton(View myButton) {
        Log.d(TAG, "starting service...");
        final Intent intent = new Intent(this, MyService.class);
//        intent.putExtra("uiThread", this);
        intent.putExtra("param1", "My Button click");
        startService(intent);
        doBindService();
        Toast.makeText(getApplicationContext(), "Buttoned...", Toast.LENGTH_LONG).show();
    }


    private ServiceConnection connection = new ServiceConnection() {

        /**
         * Called when the connection with the service has been unexpectedly disconnected -
         * ie, its process crashed.
         * Because it is running in our same process we should never see this happen.
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            myBoundService = null;
            Toast.makeText(/*Binding.this*/ HelloAndroidActivity.this, R.string.my_service_disconnected, Toast.LENGTH_SHORT).show();
        }

        /**
         * Called when the connection with the service has been established,
         * giving us the service object we can use to interact with the service.
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
          // Because we have bound to an explicit service we know is running in our process,
          // we can cast its IBinder to a concrete class and directly access it.
            myBoundService = ((MyBinder) service).getMyService();

            // Tell the user about this for our demo
            Toast.makeText(HelloAndroidActivity.this, R.string.my_service_connected, Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * Establish a connection with the service.
     * We use an explicit class name because we want a specific service implementation
     * that we know will be running in our own process (and thus won't be supporting
     * component replacement by other applications).
     */
    void doBindService() {
        bindService(new Intent(HelloAndroidActivity.this, MyService.class),
                connection, Context.BIND_AUTO_CREATE);
        bound = true;
    }

    void doUnbindService() {
        if (bound) {
            // Detach our existing connection.
            unbindService(connection);
            bound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
        stopService(new Intent(this, MyService.class));
    }

}

