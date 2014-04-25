/*
 * Copyright (C) 2014 The MoKee OpenSource Project
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.mokee.util.MoKeeUtils;

import com.baidu.android.pushservice.PushManager;

public class PushingServiceManager extends BroadcastReceiver {

    protected static final String TAG = PushingServiceManager.class.getSimpleName();

    @Override
    public void onReceive(Context ctx, Intent intent) {
        Log.d(TAG, "Start or stop service");
        initPushService(ctx);
    }

    public static void initPushService(final Context ctx) {
        if (MoKeeUtils.isOnline(ctx)) {
            if (!PushingUtils.hasBind(ctx)) {
                Intent intent = new Intent();
                intent.setClass(ctx, PushingService.class);
                ctx.startService(intent);
            } else {
                if (!PushManager.isPushEnabled(ctx)) {
                    PushManager.resumeWork(ctx);
                }
            }
        } else {
            PushManager.stopWork(ctx);
        }
    }
}
