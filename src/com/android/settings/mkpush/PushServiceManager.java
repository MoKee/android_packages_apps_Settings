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

import com.android.settings.mkstats.Utilities;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PushServiceManager extends BroadcastReceiver {

    private static final String PUSH_FIRST_BOOT = "pref_push_first_boot";
    private static final String PUSH_CHECK_LOCK = "pref_push_check_lock";
    private static final String TAG = PushServiceManager.class.getSimpleName();

    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (intent.getAction().equals(PushConstants.ACTION_RECEIVE)) {
            final int errorCode = intent.getIntExtra(PushConstants.EXTRA_ERROR_CODE,
                    PushConstants.ERROR_SUCCESS);
            final String content = new String(intent.getByteArrayExtra(PushConstants.EXTRA_CONTENT));
            final String method = intent.getStringExtra(PushConstants.EXTRA_METHOD);

            if (PushConstants.METHOD_BIND.equals(method)) {
                if (errorCode == 0) {
                    String appid = "";
                    String channelid = "";
                    String userid = "";
                    SharedPreferences prefs = ctx.getSharedPreferences("MKPush", 0);
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
                        Log.d("Bind Fail", "update channel token-----!");
                    }
                }
            }
        } else {
            initPushService(ctx);
        }
    }

    public static void initPushService(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            SharedPreferences prefs = ctx.getSharedPreferences("MKPush", 0);
            boolean checklock = prefs.getBoolean(PUSH_CHECK_LOCK, false);
            boolean firstBoot = prefs.getBoolean(PUSH_FIRST_BOOT, true);
            if (firstBoot && !checklock) {
                prefs.edit().putBoolean(PUSH_CHECK_LOCK, true).apply();
                PushManager.startWork(ctx.getApplicationContext(),
                        PushConstants.LOGIN_TYPE_API_KEY, Utils.getMetaValue(ctx, "api_key"));                
            }
        }
    }
}
