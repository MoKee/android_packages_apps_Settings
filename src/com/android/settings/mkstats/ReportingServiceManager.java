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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

public class ReportingServiceManager extends BroadcastReceiver {

    protected static final String TAG = ReportingServiceManager.class.getSimpleName();
    
    public static final long dMill = 24 * 60 * 60 * 1000;
    public static final long tFrame = 7 * dMill;

    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            setAlarm(ctx);
        } else {
            launchService(ctx);
        }
    }

    protected static void setAlarm (Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ReportingService.ANONYMOUS_PREF, 0);
        boolean firstBoot = prefs.getBoolean(ReportingService.ANONYMOUS_FIRST_BOOT, true);
        if (firstBoot) {
            return;
        }
        long lastSynced = prefs.getLong(ReportingService.ANONYMOUS_LAST_CHECKED, 0);
        if (lastSynced == 0) {
            return;
        }
        long timeLeft = (lastSynced + tFrame) - System.currentTimeMillis();
        Intent sIntent = new Intent(ConnectivityManager.CONNECTIVITY_ACTION);
        sIntent.setComponent(new ComponentName(ctx.getPackageName(), ReportingServiceManager.class.getName()));
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeLeft, PendingIntent.getBroadcast(ctx, 0, sIntent, 0));
        Log.d(TAG, "Next sync attempt in : " + timeLeft / dMill + " days");
    }

    public static void launchService (Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            final SharedPreferences prefs = ctx.getSharedPreferences(ReportingService.ANONYMOUS_PREF, 0);
            long lastSynced = prefs.getLong(ReportingService.ANONYMOUS_LAST_CHECKED, 0);
            boolean firstBoot = prefs.getBoolean(ReportingService.ANONYMOUS_FIRST_BOOT, true);
            boolean checklock = prefs.getBoolean(ReportingService.ANONYMOUS_CHECK_LOCK, false);

            boolean shouldSync = false;
            if (lastSynced == 0) {
                shouldSync = true;
            } else if (System.currentTimeMillis() - lastSynced >= tFrame) {
                shouldSync = true;
            }
            if (shouldSync || firstBoot) {
				if(prefs.getLong(ReportingService.ANONYMOUS_FLASH_TIME, 0) == 0 && !checklock) {
	                		Intent sIntent = new Intent();
	                		sIntent.setComponent(new ComponentName(ctx.getPackageName(), ReportingService.class.getName()));
	                		ctx.startService(sIntent);
					new Handler().postDelayed(new Runnable(){

					    @Override
					    public void run() {
						prefs.edit().putBoolean(ReportingService.ANONYMOUS_CHECK_LOCK, false).apply();
					    }}, 1000 * 30);
				}
				else if(prefs.getLong(ReportingService.ANONYMOUS_FLASH_TIME, 0) != 0) {
	                		Intent sIntent = new Intent();
	                		sIntent.setComponent(new ComponentName(ctx.getPackageName(), UpdatingService.class.getName()));
	                		ctx.startService(sIntent);
				}
                
            } else {
                setAlarm(ctx);
            }
        }
    }
}
