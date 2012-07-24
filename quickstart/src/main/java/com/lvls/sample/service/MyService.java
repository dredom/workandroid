/**
 *
 */
package com.lvls.sample.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.lvls.sample.HelloAndroidActivity;
import com.lvls.sample.R;

/**
 * Starts new thread to run simulated long running process.
 * From background thread displays message on main UI thread.
 * Ends.
 * (No IPC, no binding.)
 * http://developer.android.com/reference/android/app/Service.html
 *
 * <p>
 * Better to extend IntentService which takes care of the worker thread.
 * <p>
 * AsyncTask would be better pattern for service that dies after single use.
 *
 * @author auntiedt
 *
 */
public class MyService extends Service {

    private static final String TAG = MyService.class.getSimpleName();

    private MyBinder myBinder;

    private int NOTIFICATION = R.string.my_service_started;
    private NotificationManager nm;

    /**
     * Used only for IPC?
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, ">> onBind:");
//        if (myBinder == null) {
//            myBinder = new MyBinder();
//            myBinder.setMyService(this);
//        }
//        return myBinder;
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, ">> onCreate:");
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

//        showNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, ">> onStartCommand: startid " + startId + ":" + intent);

        showNotification();

        // == WORK DONE ==
        final String param = intent.getStringExtra("param1");

        // Grab the UI thread (this thread) for the UI update later
        final Handler uiThreadCallback = new Handler();
        final Runnable runInUIThread = new Runnable() {
            @Override
            public void run() {
                final String msg = param + ": RESULT";
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        };

        // start new thread for potentially long running process
        new Thread() {
            @Override
            public void run() {
                // Do long running stuff
                try {
                    Thread.sleep(3000L);
                } catch (InterruptedException e) {
                }

                // Publish results to UI
                uiThreadCallback.post(runInUIThread);

                // Die
                MyService.this.stopSelf();
            }
        }.start();

//        // We want this service to continue running until it is explicitly stopped, so sticky
//        return START_STICKY;
        // We want this service to end once its work is done.
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, ">> onDestroy");
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        Log.i(TAG, ">> onLowMemory");
        super.onLowMemory();
    }

    /**
     * Show a notification while this service is running
     */
    private void showNotification() {
//        CharSequence text = getText(R.string.my_service_started);
        CharSequence text = "Button was pushed";

        Notification notification = new Notification(R.drawable.icon, text, System.currentTimeMillis());
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
//        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, HelloAndroidActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.app_name), text, contentIntent);

        // Send the notification
        nm.notify(NOTIFICATION, notification);
    }


}
