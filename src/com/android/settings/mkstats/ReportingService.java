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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.android.settings.R;
import com.android.settings.Settings;

public class ReportingService extends Service {
    protected static final String TAG = ReportingService.class.getSimpleName();
    
    protected static final String ANONYMOUS_PREF = "mokee_stats";

    protected static final String ANONYMOUS_ALARM_SET = "pref_anonymous_alarm_set";
	
    protected static final String ANONYMOUS_CHECK_LOCK = "pref_anonymous_check_lock";

    protected static final String ANONYMOUS_FIRST_BOOT = "pref_anonymous_first_boot";

    protected static final String ANONYMOUS_FLASH_TIME = "pref_anonymous_flash_time";
	
    protected static final String ANONYMOUS_LAST_CHECKED = "pref_anonymous_checked_in";

    protected static final String DEVICE_MOKEE_VERSION = "pref_device_mokee_version";
	
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        SharedPreferences prefs =  getSharedPreferences(ANONYMOUS_PREF, 0);
        if (!prefs.getBoolean(ANONYMOUS_CHECK_LOCK, false)) {
		prefs.edit().putBoolean(ANONYMOUS_CHECK_LOCK, true).apply();
		Log.d(TAG, "User has opted in -- reporting.");
		Thread thread = new Thread() {
			@Override
		        public void run() {
				report();
		        }
		};
		thread.start();
	}
        return Service.START_REDELIVER_INTENT;
    }

    private void report() {
        final Context context = ReportingService.this;
        String deviceId = Utilities.getUniqueID(context);
        String deviceName = Utilities.getDevice();
        String deviceVersion = Utilities.getModVersion();
        String deviceCountry = Utilities.getCountryCode(context);
        String deviceCarrier = Utilities.getCarrier(context);
        String deviceCarrierId = Utilities.getCarrierId(context);
        String deviceMoKeeVersion = Utilities.getMoKeeVersion();

        Log.d(TAG, "SERVICE: Device ID=" + deviceId);
        Log.d(TAG, "SERVICE: Device Name=" + deviceName);
        Log.d(TAG, "SERVICE: Device Version=" + deviceVersion);
        Log.d(TAG, "SERVICE: Country=" + deviceCountry);
        Log.d(TAG, "SERVICE: Carrier=" + deviceCarrier);
        Log.d(TAG, "SERVICE: Carrier ID=" + deviceCarrierId);

        // report to google analytics
        GoogleAnalytics ga = GoogleAnalytics.getInstance(this);
        Tracker tracker = ga.getTracker(getString(R.string.ga_trackingId));
        tracker.sendEvent(deviceName, deviceVersion, deviceCountry, null);
        // this really should be set at build time...
        // format of version should be:
        // version[-date-type]-device
        String[] parts = deviceVersion.split("-");
        String deviceVersionNoDevice = null;
        if (parts.length == 2) {
            deviceVersionNoDevice = parts[0];
        }
        else if (parts.length == 4) {
            deviceVersionNoDevice = parts[0] + "-" + parts[2];
        }
        if (deviceVersionNoDevice != null)
            tracker.sendEvent("checkin", deviceName, deviceVersionNoDevice, null);
        tracker.close();

        // report to the cmstats service
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://stats.mfunz.com/index.php/Submit/flash");
		SharedPreferences prefs =  getSharedPreferences(ANONYMOUS_PREF, 0);
        try {
            List<NameValuePair> kv = new ArrayList<NameValuePair>(5);
            kv.add(new BasicNameValuePair("device_hash", deviceId));
            kv.add(new BasicNameValuePair("device_name", deviceName));
            kv.add(new BasicNameValuePair("device_version", deviceVersion));
            kv.add(new BasicNameValuePair("device_country", deviceCountry));
            kv.add(new BasicNameValuePair("device_carrier", deviceCarrier));
            kv.add(new BasicNameValuePair("device_carrier_id", deviceCarrierId));
            httppost.setEntity(new UrlEncodedFormEntity(kv));
            InputStream is = httpclient.execute(httppost).getEntity().getContent();
            long device_flash_time = Long.valueOf(convertStreamToJSONObject(is).getString("device_flash_time"));
            prefs.edit().putLong(ANONYMOUS_LAST_CHECKED,
                    System.currentTimeMillis()).putLong(ANONYMOUS_FLASH_TIME,
                                device_flash_time).putBoolean(ANONYMOUS_FIRST_BOOT, false).putBoolean(ANONYMOUS_CHECK_LOCK, false).putString(DEVICE_MOKEE_VERSION, deviceMoKeeVersion).apply();
        } catch (Exception e) {
            Log.e(TAG, "Got Exception", e);
			prefs.edit().putBoolean(ANONYMOUS_CHECK_LOCK, false).apply();
        }
        ReportingServiceManager.setAlarm(this);
        stopSelf();
    }

	private JSONObject convertStreamToJSONObject(InputStream is) throws IOException, JSONException {
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		for(String str = reader.readLine();str != null; str= reader.readLine())
		{
			builder.append(str);
		}
		return new JSONObject(builder.toString()); 
		
	}
}
