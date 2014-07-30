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

package com.android.settings.mkstats;

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.math.BigInteger;
import java.net.NetworkInterface;
import java.security.MessageDigest;

public class Utilities {
    public static String getUniqueID(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx
                .getSystemService(Context.TELEPHONY_SERVICE);

        String device_id = digest(tm.getDeviceId() + Build.SERIAL);
        if (TextUtils.isEmpty(device_id)) {
            String wifiInterface = SystemProperties.get("wifi.interface");
            try {
                String wifiMac = new String(NetworkInterface.getByName(
                        wifiInterface).getHardwareAddress());
                device_id = digest(wifiMac);
            } catch (Exception e) {
                device_id = "Unknown";
            }
        }
        return device_id;
    }

    public static String getIMEI(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx
                .getSystemService(Context.TELEPHONY_SERVICE);
        return !TextUtils.isEmpty(tm.getDeviceId()) ? tm.getDeviceId() : "Unknown";
    }

    public static String getCarrier(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx
                .getSystemService(Context.TELEPHONY_SERVICE);
        String carrier = tm.getNetworkOperatorName();
        if (TextUtils.isEmpty(carrier)) {
            carrier = "Unknown";
        }
        return carrier;
    }

    public static String getCarrierId(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx
                .getSystemService(Context.TELEPHONY_SERVICE);
        String carrierId = tm.getNetworkOperator();
        if (TextUtils.isEmpty(carrierId)) {
            carrierId = "0";
        }
        return carrierId;
    }

    public static String getCountryCode(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx
                .getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = tm.getNetworkCountryIso();
        if (TextUtils.isEmpty(countryCode)) {
            countryCode = "Unknown";
        }
        return countryCode;
    }

    public static String getDevice() {
        String device = SystemProperties.get("ro.mk.device");
        if (TextUtils.isEmpty(device)) {
            device = SystemProperties.get("ro.product.device");
            if (TextUtils.isEmpty(device)) {
                device = "Unknown";
            }
        }
        return device;
    }

    public static String getModVersion() {
        String modVersion = SystemProperties.get("ro.mk.version");
        if (TextUtils.isEmpty(modVersion)) {
            modVersion = SystemProperties.get("ro.modversion");
            if (TextUtils.isEmpty(modVersion)) {
                modVersion = "Unknown";
            }
        }
        return modVersion;
    }

    public static String getMoKeeVersion() {
        String modVersion = getModVersion();
        int index = modVersion.indexOf("-");
        if (!modVersion.startsWith("MK")) index = -1;
        return index == -1 ? "Unknown" : modVersion.substring(0, index);
    }

    public static String getBuildHost() {
        String hostName = SystemProperties.get("ro.build.host");
        if (TextUtils.isEmpty(hostName)) {
            hostName = "Unknown";
        }
        return hostName;
    }

    public static String getBuildUser() {
        String user = SystemProperties.get("ro.build.user");
        if (TextUtils.isEmpty(user)) {
            user = "Unknown";
        }
        return user;
    }

    public static String digest(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return new BigInteger(1, md.digest(input.getBytes())).toString(16).toUpperCase();
        } catch (Exception e) {
            return null;
        }
    }
}
