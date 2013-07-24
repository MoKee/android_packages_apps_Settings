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

package com.android.settings.mkpush;

import com.android.settings.R;

import com.android.settings.mkstats.Utilities;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PushServiceManager extends BroadcastReceiver {

    protected static final String TAG = PushServiceManager.class.getSimpleName();

    protected static final String PUSH_PREF = "mokee_push";

    protected static final String PUSH_FIRST_BOOT = "pref_push_first_boot";

    protected static final String PUSH_CHECK_LOCK = "pref_push_check_lock";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        String action = intent.getAction();
        if (action.equals(PushConstants.ACTION_RECEIVE)) {
            final int errorCode = intent.getIntExtra(PushConstants.EXTRA_ERROR_CODE,
                    PushConstants.ERROR_SUCCESS);
            final String content = new String(intent.getByteArrayExtra(PushConstants.EXTRA_CONTENT));
            final String method = intent.getStringExtra(PushConstants.EXTRA_METHOD);

            if (PushConstants.METHOD_BIND.equals(method)) {
                if (errorCode == 0) {
                    String appid = "";
                    String channelid = "";
                    String userid = "";
                    SharedPreferences prefs = ctx.getSharedPreferences(PUSH_PREF, 0);
                    try {
                        JSONObject jsonContent = new JSONObject(content);
                        JSONObject params = jsonContent.getJSONObject("response_params");
                        appid = params.getString("appid");
                        channelid = params.getString("channel_id");
                        userid = params.getString("user_id");
                    } catch (JSONException e) {
                        Log.e(TAG, "Parse bind json infos error: " + e);
                        prefs.edit().putBoolean(PUSH_CHECK_LOCK, false).apply();
                    }

                    prefs.edit().putString("appid", appid).putString("channel_id", channelid)
                            .putString("user_id", userid).putBoolean(PUSH_FIRST_BOOT, false)
                            .apply();
                    List<String> tags = new ArrayList<String>();
                    tags.add(Utilities.getDevice());
                    PushManager.setTags(ctx.getApplicationContext(), tags);
                } else {
                    if (errorCode == 30607) {
                        Log.d(TAG, "Bind Fail: update channel token!");
                    }
                }
            }
        } else if (action.equals(PushConstants.ACTION_MESSAGE)) {
            Log.d(TAG, "Receive message");
            // 获取消息内容
            Bundle bundle = intent.getExtras();
            String message = bundle.getString(PushConstants.EXTRA_PUSH_MESSAGE_STRING);
            String device = PushUtils.getString(bundle, "device");
            String modType = PushUtils.getString(bundle, "type");
            String url = PushUtils.getString(bundle, "url");
            String title = PushUtils.getString(bundle, "title");
            String newVersion = PushUtils.getString(bundle, "version");
            String HASHID = PushUtils.getString(bundle, "hashid");
            String IMEI = PushUtils.getString(bundle, "imei");
            int msg_id = Integer.valueOf(PushUtils.getString(bundle, "id"));
            String mod_device = Utilities.getDevice().toLowerCase();
            String mod_version = Utilities.getModVersion().toLowerCase();

            switch (msg_id) {
                case 0:
                case 1:
                    if (PushUtils.allowPush(device, mod_device, 1)
                            && PushUtils.allowPush(modType, mod_version, 0)
                            || device.equals("all") && modType.equals("all")
                            || device.equals("all") && PushUtils.allowPush(modType, mod_version, 0)
                            || PushUtils.allowPush(device, mod_device, 1) && modType.equals("all")) {
                        switch (msg_id) {
                            case 0:
                                if (!mod_version.contains(newVersion))
                                    promptUser(ctx, url,
                                            ctx.getString(R.string.mokee_push_newversion_title),
                                            ctx.getString(R.string.mokee_push_newversion_msg), msg_id);

                                break;
                            case 1:
                                String currentCountry = ctx.getResources().getConfiguration().locale
                                        .getCountry();
                                if (currentCountry.equals("CN") || currentCountry.equals("TW")) {
                                    promptUser(ctx, url, title, message, msg_id);
                                }
                                break;

                        }
                    }
                    break;
                case 2:
                    if (HASHID.equals(Utilities.getUniqueID(ctx))) {
                        promptUser(ctx, url, title, message, msg_id);
                    }
                    break;
                case 3:
                    if (IMEI.equals(Utilities.getIMEI(ctx))) {
                        promptUser(ctx, url, title, message, msg_id);
                    }
                    break;
            }

        } else {
            Log.d(TAG, "Start service");
            initPushService(ctx);
        }
    }

    private void promptUser(Context context, String url, String title, String message, int id) {
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        PendingIntent pendintIntent = PendingIntent.getActivity(context, 0, intent, 0);

        BigTextStyle noti = new Notification.BigTextStyle(new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_mokee_push).setAutoCancel(true).setTicker(title)
                .setContentIntent(pendintIntent).setWhen(0).setContentTitle(title)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
                .setOngoing(true).setContentText(message)).bigText(message);

        nm.notify(id, noti.build());
    }

    public static void initPushService(final Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            final SharedPreferences prefs = ctx.getSharedPreferences(PUSH_PREF, 0);
            boolean checklock = prefs.getBoolean(PUSH_CHECK_LOCK, false);
            boolean firstBoot = prefs.getBoolean(PUSH_FIRST_BOOT, true);
            if (firstBoot && !checklock) {
                prefs.edit().putBoolean(PUSH_CHECK_LOCK, true).apply();
                PushManager.startWork(ctx.getApplicationContext(),
                        PushConstants.LOGIN_TYPE_API_KEY, PushUtils.getMetaValue(ctx, "api_key"));
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        prefs.edit().putBoolean(PUSH_CHECK_LOCK, false).apply();
                    }
                }, 1000 * 30);
            } else {
                if (!PushManager.isPushEnabled(ctx))
                {
                    PushManager.resumeWork(ctx);
                }
            }
        } else {
            PushManager.stopWork(ctx);
        }
    }
}
