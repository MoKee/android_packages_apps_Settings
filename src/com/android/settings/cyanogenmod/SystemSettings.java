/*
 * Copyright (C) 2012 The CyanogenMod project
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

package com.android.settings.cyanogenmod;

import android.content.Context;
import android.content.ContentResolver;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.IWindowManager;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SystemSettings extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "SystemSettings";

    private static final String KEY_NOTIFICATION_PULSE = "notification_pulse";
    private static final String KEY_BATTERY_LIGHT = "battery_light";
    private static final String KEY_HARDWARE_KEYS = "hardware_keys";
    private static final String KEY_NAVIGATION_BAR = "navigation_bar";
    private static final String KEY_NAVIGATION_BAR_HEIGHT = "navigation_bar_height";
    private static final String KEY_NAVIGATION_RING = "navigation_ring";
    private static final String KEY_NAVIGATION_BAR_CATEGORY = "navigation_bar_category";
    private static final String KEY_LOCK_CLOCK = "lock_clock";
    private static final String KEY_STATUS_BAR = "status_bar";
    private static final String KEY_QUICK_SETTINGS = "quick_settings_panel";
    private static final String KEY_NOTIFICATION_DRAWER = "notification_drawer";
    private static final String KEY_POWER_MENU = "power_menu";
    private static final String KEY_PIE_CONTROL = "pie_control";
    private static final String KEY_FULLSCREEN_KEYBOARD = "fullscreen_keyboard";
    private static final String KEY_MMS_BREATH = "mms_breath";
    private static final String KEY_MISSED_CALL_BREATH = "missed_call_breath";

    private PreferenceScreen mNotificationPulse;
    private PreferenceScreen mBatteryPulse;
    private PreferenceScreen mPieControl;
    private ListPreference mNavButtonsHeight;
    private CheckBoxPreference mFullscreenKeyboard;
    private CheckBoxPreference mMMSBreath;
    private CheckBoxPreference mMissedCallBreath;
    private boolean mIsPrimary;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.system_settings);

        mNavButtonsHeight = (ListPreference) findPreference(KEY_NAVIGATION_BAR_HEIGHT);
        mNavButtonsHeight.setOnPreferenceChangeListener(this);

        int statusNavButtonsHeight = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.NAVIGATION_BAR_HEIGHT, 48);
        mNavButtonsHeight.setValue(String.valueOf(statusNavButtonsHeight));
        mNavButtonsHeight.setSummary(mNavButtonsHeight.getEntry());

        mFullscreenKeyboard = (CheckBoxPreference) findPreference(KEY_FULLSCREEN_KEYBOARD);
        mFullscreenKeyboard.setOnPreferenceChangeListener(this);
        mFullscreenKeyboard.setChecked(Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.FULLSCREEN_KEYBOARD, 0) == 1);
        mMMSBreath = (CheckBoxPreference) findPreference(KEY_MMS_BREATH);
        mMMSBreath.setOnPreferenceChangeListener(this);
        mMMSBreath.setChecked(Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.MMS_BREATH, 0) == 1);
        mMissedCallBreath = (CheckBoxPreference) findPreference(KEY_MISSED_CALL_BREATH);
        mMissedCallBreath.setOnPreferenceChangeListener(this);
        mMissedCallBreath.setChecked(Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.MISSED_CALL_BREATH, 0) == 1);

        PreferenceScreen prefScreen = getPreferenceScreen();

        // Only show the hardware keys config on a device that does not have a navbar
        // and the navigation bar config on phones that has a navigation bar
        boolean removeKeys = false;
        boolean removeNavbar = false;

        IWindowManager windowManager = IWindowManager.Stub.asInterface(
                ServiceManager.getService(Context.WINDOW_SERVICE));
        try {
            if (windowManager.hasNavigationBar()) {
                removeKeys = true;
            } else {
                removeNavbar = true;
            }
        } catch (RemoteException e) {
            // Do nothing
        }

        // Determine which user is logged in
        mIsPrimary = UserHandle.myUserId() == UserHandle.USER_OWNER;
        if (mIsPrimary) {
            // Primary user only preferences
            // Battery lights
            mBatteryPulse = (PreferenceScreen) findPreference(KEY_BATTERY_LIGHT);
            if (mBatteryPulse != null) {
                if (getResources().getBoolean(
                        com.android.internal.R.bool.config_intrusiveBatteryLed) == false) {
                    prefScreen.removePreference(mBatteryPulse);
                    mBatteryPulse = null;
                }
            }

            // Act on the above
            if (removeKeys) {
                prefScreen.removePreference(findPreference(KEY_HARDWARE_KEYS));
            }
            if (removeNavbar) {
                prefScreen.removePreference(findPreference(KEY_NAVIGATION_BAR));
                prefScreen.removePreference(findPreference(KEY_NAVIGATION_BAR_HEIGHT));
                prefScreen.removePreference(findPreference(KEY_NAVIGATION_RING));
                prefScreen.removePreference(findPreference(KEY_NAVIGATION_BAR_CATEGORY));
            }
        } else {
            // Secondary user is logged in, remove all primary user specific preferences
            prefScreen.removePreference(findPreference(KEY_BATTERY_LIGHT));
            prefScreen.removePreference(findPreference(KEY_HARDWARE_KEYS));
            prefScreen.removePreference(findPreference(KEY_NAVIGATION_BAR));
            prefScreen.removePreference(findPreference(KEY_NAVIGATION_BAR_HEIGHT));
            prefScreen.removePreference(findPreference(KEY_NAVIGATION_RING));
            prefScreen.removePreference(findPreference(KEY_NAVIGATION_BAR_CATEGORY));
            prefScreen.removePreference(findPreference(KEY_STATUS_BAR));
            prefScreen.removePreference(findPreference(KEY_QUICK_SETTINGS));
            prefScreen.removePreference(findPreference(KEY_POWER_MENU));
            prefScreen.removePreference(findPreference(KEY_NOTIFICATION_DRAWER));
        }

        // Preferences that applies to all users
        // Notification lights
        mNotificationPulse = (PreferenceScreen) findPreference(KEY_NOTIFICATION_PULSE);
        if (mNotificationPulse != null) {
            if (!getResources().getBoolean(com.android.internal.R.bool.config_intrusiveNotificationLed)) {
                prefScreen.removePreference(mNotificationPulse);
                mNotificationPulse = null;
            }
        }

        // Pie controls
        mPieControl = (PreferenceScreen) findPreference(KEY_PIE_CONTROL);
        if (mPieControl != null && removeNavbar) {
            // Remove on devices without a navbar to start with
            prefScreen.removePreference(mPieControl);
            mPieControl = null;
        }

        // Don't display the lock clock preference if its not installed
        removePreferenceIfPackageNotInstalled(findPreference(KEY_LOCK_CLOCK));
    }

    @Override
    public void onResume() {
        super.onResume();

        // All users
        if (mNotificationPulse != null) {
            updateLightPulseDescription();
        }
        if (mPieControl != null) {
            updatePieControlDescription();
        }

        // Primary user only
        if (mIsPrimary && mBatteryPulse != null) {
            updateBatteryPulseDescription();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void updateLightPulseDescription() {
        if (Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.NOTIFICATION_LIGHT_PULSE, 0) == 1) {
            mNotificationPulse.setSummary(getString(R.string.notification_light_enabled));
        } else {
            mNotificationPulse.setSummary(getString(R.string.notification_light_disabled));
        }
    }

    private void updateBatteryPulseDescription() {
        if (Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.BATTERY_LIGHT_ENABLED, 1) == 1) {
            mBatteryPulse.setSummary(getString(R.string.notification_light_enabled));
        } else {
            mBatteryPulse.setSummary(getString(R.string.notification_light_disabled));
        }
     }

    private void updatePieControlDescription() {
        if (Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.PIE_CONTROLS, 0) == 1) {
            mPieControl.setSummary(getString(R.string.pie_control_enabled));
        } else {
            mPieControl.setSummary(getString(R.string.pie_control_disabled));
        }
    }

    private boolean removePreferenceIfPackageNotInstalled(Preference preference) {
        String intentUri = ((PreferenceScreen) preference).getIntent().toUri(1);
        Pattern pattern = Pattern.compile("component=([^/]+)/");
        Matcher matcher = pattern.matcher(intentUri);

        String packageName = matcher.find() ? matcher.group(1) : null;
        if (packageName != null) {
            try {
                getPackageManager().getPackageInfo(packageName, 0);
            } catch (NameNotFoundException e) {
                Log.e(TAG, "package " + packageName + " not installed, hiding preference.");
                getPreferenceScreen().removePreference(preference);
                return true;
            }
        }
        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
    	ContentResolver resolver = getActivity().getApplicationContext().getContentResolver();
        if (preference == mNavButtonsHeight) {
            int index = mNavButtonsHeight.findIndexOfValue((String) objValue);
            Settings.System.putInt(resolver, Settings.System.NAVIGATION_BAR_HEIGHT, 
            		Integer.valueOf((String) objValue));
            mNavButtonsHeight.setSummary(mNavButtonsHeight.getEntries()[index]);
            return true;
        } else if (preference == mFullscreenKeyboard) {
            Settings.System.putInt(resolver, Settings.System.FULLSCREEN_KEYBOARD,
            		((Boolean) objValue).booleanValue() ? 1 : 0);
            return true;
        } else if (preference == mMMSBreath) {
            Settings.System.putInt(resolver, Settings.System.MMS_BREATH, 
            		((Boolean) objValue).booleanValue() ? 1 : 0);
            return true;
        } else if (preference == mMissedCallBreath) {
            Settings.System.putInt(resolver, Settings.System.MISSED_CALL_BREATH, 
            		((Boolean) objValue).booleanValue() ? 1 : 0);
            return true;
        }
        return false;
    }
}
