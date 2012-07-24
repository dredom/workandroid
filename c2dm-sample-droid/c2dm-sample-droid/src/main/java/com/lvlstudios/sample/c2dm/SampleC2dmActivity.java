package com.lvlstudios.sample.c2dm;

import java.util.Calendar;

import com.lvlstudios.sample.c2dm.R;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import de.akquinet.android.androlog.Log;

/**
 * Sample of push notifications using Cloud to Device Messaging (C2DM) infrastructure.
 * This sample has no server piece, uses manual {@code curl} commands to send notifications.
 * <p>
 * Steps for use:
 * <ul>
 *  <li>Sign up for C2DM services for {@code drepics@netzero.net} (done).
 *  <li>Obtain authentication token from Google ClientLogin services for {@code drepics@netzero.net}.</li>
 *   <ul><li>curl -X POST -H "Content-type: application/x-www-form-urlencoded" -o authtkn.txt
 *          -b accountType=HOSTED_OR_GOOGLE
 *          -data-urlencode Email=drepics@netzero.net -b Passwd=G3$$h3M3ssag3
 *          -b service=ac2dm -b source=LEVEL-c2dm-sample-1
 *          https://www.google.com/accounts/ClientLogin</li></ul>
 *  </li>
 *  <li>Get device with Android version 2.2 or greater and "Market" app installed and active
 *      (requires a Gmail account).</li>
 *  <li>Deploy this app to device (not emulator) via USB.
 *   <ul><li>{@code mvn package android:deploy}</li></ul>
 *  </li>
 *  <li>Start this app on device. </li>
 *  <li>Monitor device log with {@code adb} command.
 *   <ul><li>{@code adb logcat C2DM:D *:W }</li></ul>
 *  </li>
 *  <li>On device request C2DM registration.</li>
 *  <li>Capture device registration id from device log.</li>
 *  <li>Send notification to device using {@code curl} with the authentication token
 *      and device registration id.
 *   <ul><li> curl -X POST -H "Authorization: GoogleLogin auth=[auth token]"  -o sendmsg.txt
 *      -d registration_id=[device registration id]
 *      -d collapse_key=msgcollapse -d data.title=TST1 -d data.sel=xyZaaaaZyx
 *      https://android.apis.google.com/c2dm/send</li></ul>
 *  </li>
 *  <li>Message should appear on phone.</li>
 * </ul>
 *
 * <i>Miscellaneous</i>
 * <ul>
 *  <li>Sign up for C2DM with account</li>
 *  You provide a package name when you sign up but this appears to be ignored (11/11)
 *  so the account id can be used for different test projects (different package names).
 *  <li>Device registration id</li>
 *  C2DM servers can notify device of new registration id at any time.
 *  <li>Send notification to device</li>
 *  The C2DM server can optionally return a new authentication token at "send" time.
 * </ul>
 *
 * <i>How Things Work</i>
 * <p>The "Market" app makes use of the Google Service Framework (GSF) comprising
 * <ul>
 *  <li>Cloud to Device Messaging </li>
 *  <li>Google Messaging Service </li>
 * </ul>
 * This app does not call C2DM directly, but starts {@code Intent}s that invoke GSF services.
 * Similarly it registers a {@code receiver} (see AndroidManifest.xml) to respond to
 * {@code Intent}s started by GSF when C2DM messages are received.
 *
 * @author auntiedt, 2011-11
 *
 * @see <a href="http://code.google.com/android/c2dm/index.html">http://code.google.com/android/c2dm/index.html</a>
 * @see <a href="http://code.google.com/apis/accounts/docs/AuthForInstalledApps.html">http://code.google.com/apis/accounts/docs/AuthForInstalledApps.html</a>
 */
public class SampleC2dmActivity extends Activity {

    private static final String TAG = "C2DM";

    private TextView mDateDisplay;
    private Button mPickDate;
    private int mYear;
    private int mMonth;
    private int mDay;

    private Button registerC2dm;
    private TextView c2dmDisplay;
    private String c2dmMessage;

    /**
     * Email account of the application that will send the notification to this device.
     */
    private static final String emailOfAppserver = "drepics@netzero.net";

    static final int DATE_DIALOG_ID = 0;


    private DatePickerDialog.OnDateSetListener mDateSetListener =
        new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mYear = year;
                mMonth = monthOfYear;
                mDay = dayOfMonth;
                updateDisplay();
            }
        };

    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "SampleC2dmActivity.onCreate");
        setContentView(R.layout.main);

        // Capture our View element
        mDateDisplay = (TextView) findViewById(R.id.dateDisplay);
        mPickDate = (Button) findViewById(R.id.datePick);
        registerC2dm = (Button) findViewById(R.id.registerC2dm);
        c2dmDisplay = (TextView) findViewById(R.id.c2dmDisplay);

        // Add a click listener to the button
        mPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });

        // Get the current date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        // Result of registration after button click - perhaps
        // Result of notification message sent to this device - perhaps
        Intent intent = getIntent();
        c2dmMessage = intent.getStringExtra("c2dmMessage");

        // Display date and message
        updateDisplay();
    }

    private void updateDisplay() {
        mDateDisplay.setText(new StringBuilder()
            .append(mMonth + 1).append('-')
            .append(mDay).append('-')
            .append(mYear).append(" "));
        c2dmDisplay.setText(c2dmMessage);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DATE_DIALOG_ID:
            return new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);
        default:
            break;
        }
        return null;
    }

    public void clickC2dmButton(View button) {
        Log.d(TAG, "SampleC2dmActivity.clickC2dmButton");
        c2dmMessage = "button clicked";
        registerC2dm.setClickable(false);
        registerC2dm();
        updateDisplay();
    }

    /**
     * See project android-c2dm C2DMessaging#register
     */
    private void registerC2dm() {
     // Use the Intent API to get a registration ID
     // Registration ID is compartmentalized per app/device
     Intent regIntent = new Intent( "com.google.android.c2dm.intent.REGISTER");
     Log.d(TAG, "SampleC2dmActivity regIntent.setPackage(\"com.google.android.gsf\")");
     regIntent.setPackage("com.google.android.gsf");
     // Identify your app
     regIntent.putExtra("app",
             PendingIntent.getBroadcast(this /* your activity */,
                 0, new Intent(), 0));
     // Identify role account server will use to send
     regIntent.putExtra("sender", emailOfAppserver);

     // Start the registration process
     startService(regIntent);
    }

}

