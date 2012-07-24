/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lvlstudios.sample.c2dm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.c2dm.C2DMBaseReceiver;

/**
 * Apparently receives messages from the Google Service Framework comprising
 * <ul>
 *  <li>Cloud to Device Messaging </li>
 *  <li>Google Messaging Service </li>
 * </ul>
 * (This requires the "Market" app and a Gmail account.)
 *
 * @author auntiedt, 2011-11 (Plagiarized from Google's "Chrome to Phone" app)
 *
 */
public class C2DMReceiver extends C2DMBaseReceiver {
    // Logging tag
    private static final String TAG = "C2DM";
    /**
     * SENDER_ID is the Google account used by the application server
     * to call C2DM with a notification message for this device.
     * The key point for the sender is getting an authentication token for this account
     * with which to call C2DM.
     */
    static final String SENDER_ID = "drepics@netzero.net";

    public C2DMReceiver() {
        super(SENDER_ID);
    }

    /**
     * Intent Device Registration sent from C2DM.
     * "{@code com.google.android.c2dm.intent.REGISTRATION}"
     *
     */
    @Override
    public void onRegistered(Context context, String registration) {
        Log.i(TAG, "C2DMReceiver.onRegistered: " + registration);
//        DeviceRegistrar.registerWithServer(context, registration);

        Intent displayIntent = new Intent(this, SampleC2dmActivity.class);
        displayIntent.putExtra("c2dmMessage", "Regn ID: " + registration);
        displayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(displayIntent);
    }

    @Override
    public void onUnregistered(Context context) {
        Log.i(TAG, "onUnregistered: " );
//        SharedPreferences prefs = Prefs.get(context);
//        String deviceRegistrationID = prefs.getString("deviceRegistrationID", null);
//        DeviceRegistrar.unregisterWithServer(context, deviceRegistrationID);
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.e(TAG, "C2DMReceiver.onError: " + errorId);
        context.sendBroadcast(new Intent("com.google.ctp.UPDATE_UI"));
    }

    /**
     * Intent message sent from C2DM.
     * "{@code com.google.android.c2dm.intent.RECEIVE}"
     * <pre>
     * curl -X POST -H "Authorization: GoogleLogin auth=[auth token]" \
     *   -d registration_id=[device registration id] \
     *   -d collapse_key=msgcollapse -d data.title=TST1 -d data.sel=xyZaaaaZyx -o sendmsg.txt \
     *   https://android.apis.google.com/c2dm/send
     * </pre>
     */
    @Override
    public void onMessage(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String url = (String) extras.get("url");
            String title = (String) extras.get("title");
            String sel = (String) extras.get("sel");
            Log.d(TAG, String.format("C2DMReceiver.onMessage: url='%s', title='%s', sel='%s'", url, title, sel));

            // Build a message string
            StringBuilder buf = new StringBuilder();
            buf.append("C2DM Message: ");
            if (title != null) {
                buf.append(title).append(' ');
            }
            if (url != null) {
                buf.append(url).append(' ');
            }
            if (sel != null) {
                buf.append(sel).append(' ');
            }

            Intent displayIntent = new Intent(this, SampleC2dmActivity.class);
            displayIntent.putExtra("c2dmMessage", buf.toString());
            displayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(displayIntent);

        }
    }
}
