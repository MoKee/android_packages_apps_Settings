/*
 * Copyright (C) 2012 The MoKee OpenSource Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.mkstats;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.Settings;

public class UpdatingService extends Service {
    protected static final String TAG = UpdatingService.class.getSimpleName();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        Log.d(TAG, "User has opted in -- updating.");
        Thread thread = new Thread() {
            @Override
            public void run() {
                update();
            }
        };
        thread.start();
        return Service.START_REDELIVER_INTENT;
    }

    private void update() {
        String deviceId = Utilities.getUniqueID(getApplicationContext());
        String deviceVersion = Utilities.getModVersion();
        String deviceFlashTime = String.valueOf(getSharedPreferences(ReportingService.ANONYMOUS_PREF, 0).getLong(ReportingService.ANONYMOUS_FLASH_TIME, 0));

        Log.d(TAG, "SERVICE: Device ID=" + deviceId);
        Log.d(TAG, "SERVICE: Device Version=" + deviceVersion);
        Log.d(TAG, "SERVICE: Device Flash Time=" + deviceFlashTime);

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://stats.mfunz.com/index.php/Submit/updatev1");
        try {
            List<NameValuePair> kv = new ArrayList<NameValuePair>(1);
            kv.add(new BasicNameValuePair("device_hash", deviceId));
            kv.add(new BasicNameValuePair("device_version", deviceVersion));
            kv.add(new BasicNameValuePair("device_flash_time", deviceFlashTime));
            httppost.setEntity(new UrlEncodedFormEntity(kv));
            httpclient.execute(httppost);
            getSharedPreferences(ReportingService.ANONYMOUS_PREF, 0).edit().putLong(ReportingService.ANONYMOUS_LAST_CHECKED,
                    System.currentTimeMillis()).apply();
        } catch (Exception e) {
            Log.e(TAG, "Got Exception", e);
        }
        ReportingServiceManager.setAlarm(this);
        stopSelf();
    }
}
