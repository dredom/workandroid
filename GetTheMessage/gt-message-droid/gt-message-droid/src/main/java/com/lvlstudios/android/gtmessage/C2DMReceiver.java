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
package com.lvlstudios.android.gtmessage;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.c2dm.C2DMBaseReceiver;


public class C2DMReceiver extends C2DMBaseReceiver {
    // Logging tag
    private static final String TAG = "GTM";

    public C2DMReceiver() {
        super(DeviceRegistrar.SENDER_ID);
    }

    @Override
    public void onRegistered(Context context, String registration) {
        Log.i(TAG, "C2DMReceiver.onRegistered: " + registration);
        DeviceRegistrar.registerWithServer(context, registration);
    }

    @Override
    public void onUnregistered(Context context) {
        SharedPreferences prefs = Prefs.get(context);
        String deviceRegistrationID = prefs.getString("deviceRegistrationID", null);
        DeviceRegistrar.unregisterWithServer(context, deviceRegistrationID);
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.e(TAG, "C2DMReceiver.onError: " + errorId);
        context.sendBroadcast(new Intent("com.google.ctp.UPDATE_UI"));
    }

    @Override
    public void onMessage(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String url = (String) extras.get("url");
            String title = (String) extras.get("title");
            String sel = (String) extras.get("sel");
            String debug = (String) extras.get("debug");
            Log.d(TAG, String.format("C2DMReceiver.onMessage: url='%s', title='%s', sel='%s'", url, title, sel));

            if (debug != null) {
                // server-controlled debug - the server wants to know we received the message, and when.
                // This is not user-controllable,
                // we don't want extra traffic on the server or phone. Server may
                // turn this on for a small percentage of requests or for users
                // who report issues.
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(AppEngineClient.BASE_URL + "/debug?id="
                        + extras.get("collapse_key"));
                // No auth - the purpose is only to generate a log/confirm delivery
                // (to avoid overhead of getting the token)
                try {
                    client.execute(get);
                } catch (ClientProtocolException e) {
                    // ignore
                } catch (IOException e) {
                    // ignore
                }
            }

            if (title != null && url != null && url.startsWith("http")) {
                SharedPreferences settings = Prefs.get(context);
                Intent launchIntent = LauncherUtils.getLaunchIntent(context, title, url, sel);

                // Notify and optionally start activity
                if (settings.getBoolean("launchBrowserOrMaps", true) && launchIntent != null) {
                    try {
                        context.startActivity(launchIntent);
                        LauncherUtils.playNotificationSound(context);
                    } catch (ActivityNotFoundException e) {
                        return;
                    }
                } else {
                    if (sel != null && sel.length() > 0) {  // have selection
                        LauncherUtils.generateNotification(context, sel,
                                context.getString(R.string.copied_desktop_clipboard), launchIntent);
                    } else {
                        LauncherUtils.generateNotification(context, url, title, launchIntent);
                    }
                }

                // Record history (for link/maps only)
                if (launchIntent != null && launchIntent.getAction().equals(Intent.ACTION_VIEW)) {
                    HistoryDatabase.get(context).insertHistory(title, url);
                }
            }
        }
    }
}
