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

package com.google.android.c2dm.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskHandle;
import com.google.appengine.api.labs.taskqueue.TaskOptions;

/**
 * Auth Token for app (Google) account:
 * http://code.google.com/apis/accounts/docs/AuthForInstalledApps.html
 * Save {@code authToken} in C2DMConfig table.
 * https://www.google.com/accounts/ClientLogin
 */
public class C2DMessaging {
    private static final String UPDATE_CLIENT_AUTH = "Update-Client-Auth";

    private static final Logger log = Logger.getLogger(C2DMessaging.class.getName());

    public static final String PARAM_REGISTRATION_ID = "registration_id";

    public static final String PARAM_DELAY_WHILE_IDLE = "delay_while_idle";

    public static final String PARAM_COLLAPSE_KEY = "collapse_key";

    private static final String UTF8 = "UTF-8";

    /**
     * Jitter - random interval to wait before retry.
     */
    public static final int DATAMESSAGING_MAX_JITTER_MSEC = 3000;

    static C2DMessaging singleton;

    final C2DMConfigLoader serverConfig;

    // Testing
    protected C2DMessaging() {
        serverConfig = null;
    }

    private C2DMessaging(C2DMConfigLoader serverConfig) {
        this.serverConfig = serverConfig;
    }

    public synchronized static C2DMessaging get(ServletContext servletContext) {
        if (singleton == null) {
            C2DMConfigLoader serverConfig = new C2DMConfigLoader(getPMF(servletContext));
            singleton = new C2DMessaging(serverConfig);
        }
        return singleton;
    }

    public synchronized static C2DMessaging get(PersistenceManagerFactory pmf) {
        if (singleton == null) {
            C2DMConfigLoader serverConfig = new C2DMConfigLoader(pmf);
            singleton = new C2DMessaging(serverConfig);
        }
        return singleton;
    }

    C2DMConfigLoader getServerConfig() {
        return serverConfig;
    }

    /**
     * Initialize PMF - we use a context attribute, so other servlets can
     * be share the same instance. This is similar with a shared static
     * field, but avoids dependencies.
     */
    public static PersistenceManagerFactory getPMF(ServletContext ctx) {
        PersistenceManagerFactory pmfFactory =
            (PersistenceManagerFactory) ctx.getAttribute(
                    PersistenceManagerFactory.class.getName());
        if (pmfFactory == null) {
            pmfFactory = JDOHelper
                .getPersistenceManagerFactory("transactions-optional");
            ctx.setAttribute(
                    PersistenceManagerFactory.class.getName(),
                    pmfFactory);
        }
        return pmfFactory;
}


    public boolean sendNoRetry(String registrationId,
            String collapse,
            Map<String, String[]> params,
            boolean delayWhileIdle)
        throws IOException {

        // Send a sync message to this Android device.
        StringBuilder postDataBuilder = new StringBuilder();
        postDataBuilder.append(PARAM_REGISTRATION_ID).
            append("=").append(registrationId);

        if (delayWhileIdle) {
            postDataBuilder.append("&")
                .append(PARAM_DELAY_WHILE_IDLE).append("=1");
        }
        postDataBuilder.append("&").append(PARAM_COLLAPSE_KEY).append("=").
            append(collapse);

        for (Object keyObj: params.keySet()) {
            String key = (String) keyObj;
            if (key.startsWith("data.")) {
                String[] values = params.get(key);
                postDataBuilder.append("&").append(key).append("=").
                    append(URLEncoder.encode(values[0], UTF8));
            }
        }

        byte[] postData = postDataBuilder.toString().getBytes(UTF8);

        // Hit the dm URL.
        URL url = new URL(serverConfig.getC2DMUrl());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
        String authToken = serverConfig.getToken();
        conn.setRequestProperty("Authorization", "GoogleLogin auth=" + authToken);

        log.info("Post to: " + conn.getURL());
        StringBuilder heads = new StringBuilder();
        Map<String, List<String>> headsMap = conn.getHeaderFields();
        Set<String> headKeys = headsMap.keySet();
        for (String key : headKeys) {
            heads.append(key).append('=').append(headsMap.get(key).toString()).append(' ');
        }
        log.info("Post headers: " + heads.toString());
        StringBuilder parms = new StringBuilder();
        parms.append("Authorization: ").append("GoogleLogin auth=").append(authToken);
        log.info("Post parameters: " + parms.toString());

        OutputStream out = conn.getOutputStream();
        out.write(postData);
        out.close();

        int responseCode = conn.getResponseCode();

        if (responseCode == HttpServletResponse.SC_UNAUTHORIZED ||
                responseCode == HttpServletResponse.SC_FORBIDDEN) {
            // The token is too old - return false to retry later, will fetch the token
            // from DB. This happens if the password is changed or token expires. Either admin
            // is updating the token, or Update-Client-Auth was received by another server,
            // and next retry will get the good one from database.
            log.warning("Unauthorized - need token");
            serverConfig.invalidateCachedToken();
            return false;
        }
        log.info("Got " + responseCode + " response from Google AC2DM endpoint.");

        // Check for updated token header
        String updatedAuthToken = conn.getHeaderField(UPDATE_CLIENT_AUTH);
        if (updatedAuthToken != null && !authToken.equals(updatedAuthToken)) {
            log.info("Got updated auth token from datamessaging servers: " +
                    updatedAuthToken);
            serverConfig.updateToken(updatedAuthToken);
        }

        String responseLine = new BufferedReader(new InputStreamReader(conn.getInputStream()))
            .readLine();

        // NOTE: You *MUST* use exponential backoff if you receive a 503 response code.
        // Since App Engine's task queue mechanism automatically does this for tasks that
        // return non-success error codes, this is not explicitly implemented here.
        // If we weren't using App Engine, we'd need to manually implement this.
        if (responseLine == null || responseLine.equals("")) {
            log.info("Got " + responseCode +
                    " response from Google AC2DM endpoint.");
            throw new IOException("Got empty response from Google AC2DM endpoint.");
        }

        String[] responseParts = responseLine.split("=", 2);
        if (responseParts.length != 2) {
            log.warning("Invalid message from google: " +
                    responseCode + " " + responseLine);
            throw new IOException("Invalid response from Google " +
                    responseCode + " " + responseLine);
        }

        if (responseParts[0].equals("id")) {
            log.info("Successfully sent data message to device: " + responseLine);
            return true;
        }

        if (responseParts[0].equals("Error")) {
            String err = responseParts[1];
            log.warning("Got error response from Google datamessaging endpoint: " + err);
            // No retry.
            // TODO(costin): show a nicer error to the user.
            throw new IOException(err);
        } else {
            // 500 or unparseable response - server error, needs to retry
            log.warning("Invalid response from google " + responseLine + " " + responseCode);
            return false;
        }
    }

    /**
     * Helper method to send a message, with 2 parameters.
     *
     * Permanent errors will result in IOException.
     * Retryable errors will cause the message to be scheduled for retry.
     */
    public void sendWithRetry(String token, String collapseKey,
            String name1, String value1, String name2, String value2,
            String name3, String value3)
                throws IOException {

        Map<String, String[]> params = new HashMap<String, String[]>();
        if (value1 != null) params.put("data." + name1, new String[] {value1});
        if (value2 != null) params.put("data." + name2, new String[] {value2});
        if (value3 != null) params.put("data." + name3, new String[] {value3});

        boolean sentOk = sendNoRetry(token, collapseKey, params, true);
        if (!sentOk) {
            retry(token, collapseKey, params, true);
        }
    }

    public boolean sendNoRetry(String token, String collapseKey,
            String name1, String value1, String name2, String value2,
            String name3, String value3)
                throws IOException {

        Map<String, String[]> params = new HashMap<String, String[]>();
        if (value1 != null) params.put("data." + name1, new String[] {value1});
        if (value2 != null) params.put("data." + name2, new String[] {value2});
        if (value3 != null) params.put("data." + name3, new String[] {value3});

        try {
            return sendNoRetry(token, collapseKey, params, true);
        } catch (IOException ex) {
            return false;
        }
    }

    public boolean sendNoRetry(String token, String collapseKey,
            String... nameValues)
                throws IOException {

        log.info("sendNoRetry token=" + token + " collapseKey=" + collapseKey
                + " params:" + Arrays.deepToString(nameValues));

        Map<String, String[]> params = new HashMap<String, String[]>();
        int len = nameValues.length;
        if (len % 2 == 1) {
            len--; // ignore last
        }
        for (int i = 0; i < len; i+=2) {
            String name = nameValues[i];
            String value = nameValues[i + 1];
            if (name != null && value != null) {
                params.put("data." + name, new String[] {value});
            }
        }

        try {
            return sendNoRetry(token, collapseKey, params, true);
        } catch (IOException ex) {
            return false;
        }
    }

    private void retry(String token, String collapseKey,
            Map<String, String[]> params, boolean delayWhileIdle) {
        Queue dmQueue = QueueFactory.getQueue("c2dm");
        try {
            TaskOptions url =
                TaskOptions.Builder.url(C2DMRetryServlet.URI)
                .param(C2DMessaging.PARAM_REGISTRATION_ID, token)
                .param(C2DMessaging.PARAM_COLLAPSE_KEY, collapseKey);
            if (delayWhileIdle) {
                url.param(PARAM_DELAY_WHILE_IDLE, "1");
            }
            for (String key: params.keySet()) {
                String[] values = params.get(key);
                url.param(key, URLEncoder.encode(values[0], UTF8));
            }

            // Task queue implements the exponential backoff
            long jitter = (int) Math.random() * DATAMESSAGING_MAX_JITTER_MSEC;
            url.countdownMillis(jitter);

            TaskHandle add = dmQueue.add(url);
        } catch (UnsupportedEncodingException e) {
            // Ignore - UTF8 should be supported
            log.log(Level.SEVERE, "Unexpected error", e);
        }

    }

}
