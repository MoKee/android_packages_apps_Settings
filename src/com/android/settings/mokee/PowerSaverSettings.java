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

package com.android.settings.mokee;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.cyanogenmod.Processor;

public class PowerSaverSettings extends SettingsPreferenceFragment implements
        CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "PowerSaverSettings";
    private static final String KEY_PERFORMANCE_CATEGORY = "power_saver_category_performance";
    private static final String KEY_TOGGLES_CPU_PROFILE = "power_saver_toggles_cpu_profile";
    private static final String KEY_TOGGLES_CPU_GOVERNOR = "power_saver_toggles_cpu_governor";
    private static final String KEY_TOGGLES_MOBILE_DATA = "power_saver_toggles_mobile_data";
    private static final String KEY_TOGGLES_GPS = "power_saver_toggles_gps";
    private static final String KEY_TOGGLES_NOTIFICATION = "power_saver_toggles_notification";
    private ContentResolver resolver;
    private Switch mEnabledSwitch;
    private CheckBoxPreference mTogglesCPUProfile;
    private CheckBoxPreference mTogglesCPUGovernor;
    private CheckBoxPreference mTogglesMobileData;
    private CheckBoxPreference mTogglesGPS;
    private CheckBoxPreference mTogglesNotification;
    private PreferenceCategory mPerformanceCategory;
    private PreferenceScreen prefSet;
    private PowerManager mPowerManager;
    private Activity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.power_saver_settings);
        resolver = getContentResolver();
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mActivity = getActivity();

        mEnabledSwitch = new Switch(mActivity);
        boolean powerSaverEnabled = Settings.System.getInt(resolver,
                Settings.System.POWER_SAVER_ENABLED, 1) != 0;
        final int padding = mActivity.getResources().getDimensionPixelSize(
                R.dimen.action_bar_switch_padding);
        mEnabledSwitch.setPaddingRelative(0, 0, padding, 0);
        mEnabledSwitch.setChecked(powerSaverEnabled);
        mEnabledSwitch.setOnCheckedChangeListener(this);

        prefSet = getPreferenceScreen();
        mPerformanceCategory = (PreferenceCategory) prefSet.findPreference(KEY_PERFORMANCE_CATEGORY);

        mTogglesCPUProfile = (CheckBoxPreference) prefSet.findPreference(KEY_TOGGLES_CPU_PROFILE);
        if (mTogglesCPUProfile != null && !mPowerManager.hasPowerProfiles()) {
            mPerformanceCategory.removePreference(mTogglesCPUProfile);
            mTogglesCPUProfile = null;
        } else {
            mTogglesCPUProfile.setChecked(Settings.System.getInt(resolver, Settings.System.POWER_SAVER_CPU_PROFILE, 0) != 0);
        }

        mTogglesCPUGovernor = (CheckBoxPreference) prefSet.findPreference(KEY_TOGGLES_CPU_GOVERNOR);
        /*
         * Governor Some systems might not use governors
         */
        if (!Utils.fileExists(Processor.GOV_LIST_FILE) || !Utils.fileExists(Processor.GOV_FILE)
                || Utils.fileReadOneLine(Processor.GOV_FILE) == null
                || Utils.fileReadOneLine(Processor.GOV_LIST_FILE) == null) {
            mPerformanceCategory.removePreference(mTogglesCPUGovernor);
            mTogglesCPUGovernor = null;
        } else {
            mTogglesCPUGovernor.setChecked(Settings.System.getInt(resolver,
                    Settings.System.POWER_SAVER_CPU_GOVERNOR, 1) != 0);
        }
        if (mTogglesCPUGovernor == null && mTogglesCPUProfile == null) {
            prefSet.removePreference(mPerformanceCategory);
        }

        mTogglesMobileData = (CheckBoxPreference) prefSet.findPreference(KEY_TOGGLES_MOBILE_DATA);
        mTogglesMobileData.setChecked(Settings.System.getInt(resolver,
                Settings.System.POWER_SAVER_MOBILE_DATA, 0) != 0);
        mTogglesGPS = (CheckBoxPreference) prefSet.findPreference(KEY_TOGGLES_GPS);
        mTogglesGPS.setChecked(Settings.System.getInt(resolver,
                Settings.System.POWER_SAVER_GPS, 0) != 0);
        mTogglesNotification = (CheckBoxPreference) prefSet.findPreference(KEY_TOGGLES_NOTIFICATION);
        mTogglesNotification.setChecked(Settings.System.getInt(resolver,
                Settings.System.POWER_SAVER_NOTIFICATION, 1) != 0);
        setPrefsEnabledState(powerSaverEnabled);
    }

    @Override
    public void onStart() {
        super.onStart();
        final Activity activity = getActivity();
        activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM);
        activity.getActionBar().setCustomView(mEnabledSwitch, new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL | Gravity.END));
    }

    private void setPrefsEnabledState(boolean enabled) {
        prefSet.setEnabled(enabled);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mTogglesCPUProfile) {
            String [] pwrsvValue = getResources().getStringArray(com.android.internal.R.array.perf_profile_values);
            mPowerManager.setPowerProfile(mTogglesCPUProfile.isChecked() ? pwrsvValue[0] : pwrsvValue[1]);
            Settings.System.putInt(resolver, Settings.System.POWER_SAVER_CPU_PROFILE,
                    mTogglesCPUProfile.isChecked() ? 1 : 0);
        } else if (preference == mTogglesCPUGovernor) {
            Settings.System.putInt(resolver, Settings.System.POWER_SAVER_CPU_GOVERNOR,
                    mTogglesCPUGovernor.isChecked() ? 1 : 0);
        } else if (preference == mTogglesMobileData) {
            Settings.System.putInt(resolver, Settings.System.POWER_SAVER_MOBILE_DATA,
                    mTogglesMobileData.isChecked() ? 1 : 0);
        } else if (preference == mTogglesGPS) {
            Settings.System.putInt(resolver, Settings.System.POWER_SAVER_GPS,
                    mTogglesGPS.isChecked() ? 1 : 0);
        }

        if (preference == mTogglesNotification) {
            Settings.System.putInt(resolver, Settings.System.POWER_SAVER_NOTIFICATION,
                    mTogglesNotification.isChecked() ? 1 : 0);
            Intent intent = new Intent("android.intent.action.POWER_SAVER_NOTIFICATION");
            mActivity.sendBroadcast(intent);
        } else {
            Intent intent = new Intent("android.intent.action.POWER_SAVER_SERVICE_UPDATE");
            mActivity.sendBroadcast(intent);
        }

        // Log.i(TAG, "android.intent.action.POWER_SAVER_SERVICE_UPDATE");
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Settings.System.putInt(resolver, Settings.System.POWER_SAVER_ENABLED, isChecked ? 1 : 0);
        setPrefsEnabledState(isChecked);
        Intent service = new Intent().setClassName("org.mokee.services", "org.mokee.services.powersaver.PowerSaverService");
        if (isChecked) {
            mActivity.stopService(service);
            mActivity.startService(service);
        } else {
            mActivity.stopService(service);
        }
        // Log.i(TAG, String.valueOf("PowerSaverService Status: " + String.valueOf(isChecked)));
    }

}
