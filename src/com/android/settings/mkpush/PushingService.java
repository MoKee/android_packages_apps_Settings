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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;

public class PushingService extends Service {

    protected static final String TAG = PushingService.class.getSimpleName();

    private PushInitTask mTask;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        Log.d(TAG, "PushingService init start");

        if (mTask == null || mTask.getStatus() == AsyncTask.Status.FINISHED) {
            mTask = new PushInitTask();
            mTask.execute();
        }

        return Service.START_REDELIVER_INTENT;
    }

    private class PushInitTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            final Context ctx = PushingService.this;
            boolean success = false;
            PushManager.startWork(ctx.getApplicationContext(),
                    PushConstants.LOGIN_TYPE_API_KEY, PushingUtils.getMetaValue(ctx, "api_key"));
            if (PushingUtils.hasBind(ctx)) {
                success = true;
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            stopSelf();
        }
    }
}
