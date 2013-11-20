/*
 * Copyright (C) 2013 The MoKee OpenSource Project
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

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class PushingUtils {

    public static String getMetaValue(Context context, String metaKey) {
        Bundle metaData = null;
        String apiKey = null;
        if (context == null || metaKey == null) {
            return null;
        }
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (null != ai) {
                metaData = ai.metaData;
            }
            if (null != metaData) {
                apiKey = metaData.getString(metaKey);
            }
        } catch (NameNotFoundException e) {

        }
        return apiKey;
    }

    public static boolean allowPush(String str1, String str2, int mode) {
        String[] strs = str1.split(",");
        for (int i = 0; i < strs.length; i++)
        {
            switch (mode)
            {
                case 1:
                    if (strs[i].equals(str2))
                        return true;
                default:
                    if (str2.contains(strs[i]))
                        return true;
            }
        }
        return false;
    }
    
    public static String getStringFromJson(String key, JSONObject customJson) {
        String value = "";
        if (!customJson.isNull(key)) {
            try {
                value = customJson.getString(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    public static int getIntFromJson(String key, JSONObject customJson) {
        int value = 999;
        if (!customJson.isNull(key)) {
            try {
                value = Integer.valueOf(customJson.getString(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    public static void setBind(Context context, boolean flag) {
        String flagStr = "not";
        if (flag) {
            flagStr = "ok";
        }
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sp.edit();
        editor.putString("bind_flag", flagStr);
        editor.apply();
    }

    public static boolean hasBind(Context context) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        String flag = sp.getString("bind_flag", "");
        if ("ok".equalsIgnoreCase(flag)) {
            return true;
        }
        return false;
    }
}
