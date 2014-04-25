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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.Notification.BigTextStyle;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.mokee.util.MoKeeUtils;

import com.android.settings.R;
import com.android.settings.mkstats.Utilities;
import com.baidu.android.pushservice.PushManager;
import com.baidu.frontia.api.FrontiaPushMessageReceiver;

public class PushingMessageReceiver extends FrontiaPushMessageReceiver {

    protected static final String TAG = PushingMessageReceiver.class.getSimpleName();

    @Override
    public void onMessage(Context ctx, String message, String customContentString) {
        if (customContentString != null & customContentString != "") {
            JSONObject customJson = null;
            try {
                customJson = new JSONObject(customContentString);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }

            String device = PushingUtils.getStringFromJson("device", customJson);
            String modType = PushingUtils.getStringFromJson("type", customJson);
            String url = PushingUtils.getStringFromJson("url", customJson);
            String title = PushingUtils.getStringFromJson("title", customJson);
            String newVersion = PushingUtils.getStringFromJson("version", customJson);
            String HASHID = PushingUtils.getStringFromJson("hashid", customJson);
            String user = PushingUtils.getStringFromJson("user", customJson);
            String IMEI = PushingUtils.getStringFromJson("imei", customJson);
            int msg_id = PushingUtils.getIntFromJson("id", customJson);
            String mod_device = Utilities.getDevice().toLowerCase();
            String mod_version = Utilities.getModVersion().toLowerCase();

            switch (msg_id) {
                case 0:
                case 1:
                    if (PushingUtils.allowPush(device, mod_device, 1)
                            && PushingUtils.allowPush(modType, mod_version, 0)
                            || device.equals("all") && modType.equals("all")
                            || device.equals("all")
                            && PushingUtils.allowPush(modType, mod_version, 0)
                            || PushingUtils.allowPush(device, mod_device, 1)
                            && modType.equals("all")) {
                        switch (msg_id) {
                            case 0:
                                String mod_version_code = mod_version.split("-")[2];
                                int new_version_code = Integer.parseInt(newVersion);
                                if (mod_version_code.length() > 6) {
                                    mod_version_code = mod_version_code.substring(2, 8);
                                }
                                if (new_version_code > Integer.parseInt(mod_version_code)) {
                                    promptUser(ctx, url, ctx.getString(R.string.mokee_push_newversion_title),
                                            ctx.getString(R.string.mokee_push_newversion_msg), msg_id, R.drawable.ic_mokee_updater);
                                }
                                break;
                            case 1:
                                if (MoKeeUtils.isChineseLanguage()) {
                                    promptUser(ctx, url, title, message, msg_id, R.drawable.ic_mokee_msg);
                                }
                                break;
                        }
                    }
                    break;
                case 2:
                    if (HASHID.equals(Utilities.getUniqueID(ctx))) {
                        promptUser(ctx, url, title, message, msg_id, R.drawable.ic_mokee_msg);
                    }
                    break;
                case 3:
                    if (IMEI.equals(Utilities.getIMEI(ctx))) {
                        promptUser(ctx, url, title, message, msg_id, R.drawable.ic_mokee_msg);
                    }
                    break;
                case 4:
                    if (user.equals(Utilities.getBuildUser())) {
                        promptUser(ctx, url, title, message, msg_id, R.drawable.ic_mokee_msg);
                    }
            }
        }
    }

    private void promptUser(Context context, String url, String title, String message, int id, int icon) {
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        PendingIntent pendintIntent = PendingIntent.getActivity(context, 0, intent, 0);

        BigTextStyle noti = new Notification.BigTextStyle(new Notification.Builder(context)
                .setSmallIcon(icon).setAutoCancel(true).setTicker(title)
                .setContentIntent(pendintIntent).setWhen(0).setContentTitle(title)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
                .setOngoing(true).setContentText(message)).bigText(message);

        nm.notify(id, noti.build());
    }

    @Override
    public void onNotificationClicked(Context context, String title,
            String description, String customContentString) {
    }

    @Override
    public void onBind(Context context, int errorCode, String appid,
            String userId, String channelId, String requestId) {
        String responseString = "onBind errorCode=" + errorCode + " appid="
                + appid + " userId=" + userId + " channelId=" + channelId
                + " requestId=" + requestId;
        Log.d(TAG, responseString);

        if (errorCode == 0) {
            PushingUtils.setBind(context, true);
            List<String> tags = new ArrayList<String>();
            tags.add(Utilities.getDevice());
            tags.add(Utilities.getMoKeeVersion());
            PushManager.setTags(context.getApplicationContext(), tags);
        }
    }

    @Override
    public void onDelTags(Context context, int errorCode,
            List<String> sucessTags, List<String> failTags, String requestId) {
        String responseString = "onDelTags errorCode=" + errorCode + " sucessTags="
                + sucessTags + " failTags=" + failTags + " requestId="
                + requestId;
        Log.d(TAG, responseString);
    }

    @Override
    public void onListTags(Context context, int errorCode,
            List<String> tags, String requestId) {
        String responseString = "onListTags errorCode=" + errorCode + " tags=" + tags;
        Log.d(TAG, responseString);
    }

    @Override
    public void onSetTags(Context context, int errorCode,
            List<String> sucessTags, List<String> failTags, String requestId) {
        String responseString = "onSetTags errorCode=" + errorCode + " sucessTags="
                + sucessTags + " failTags=" + failTags + " requestId="
                + requestId;
        Log.d(TAG, responseString);
    }

    @Override
    public void onUnbind(Context context, int errorCode, String requestId) {
        String responseString = "onUnbind errorCode=" + errorCode
                + " requestId = " + requestId;
        Log.d(TAG, responseString);

        if (errorCode == 0) {
            PushingUtils.setBind(context, false);
        }
    }
}
