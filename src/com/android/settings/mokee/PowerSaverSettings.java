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
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
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
    private static final String KEY_TOGGLES_CPU = "power_saver_toggles_cpu";
    private static final String KEY_TOGGLES_MOBILE_DATA = "power_saver_toggles_mobile_data";
    private static final String KEY_TOGGLES_GPS = "power_saver_toggles_gps";
    private ContentResolver resolver;
    private Switch mEnabledSwitch;
    private CheckBoxPreference mTogglesCPU;
    private CheckBoxPreference mTogglesMobileData;
    private CheckBoxPreference mTogglesGPS;
    private PreferenceScreen prefSet;
    private Activity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.power_saver_settings);
        resolver = getContentResolver();
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
        mTogglesCPU = (CheckBoxPreference) prefSet.findPreference(KEY_TOGGLES_CPU);
        /*
         * Governor Some systems might not use governors
         */
        if (!Utils.fileExists(Processor.GOV_LIST_FILE) || !Utils.fileExists(Processor.GOV_FILE)
                || Utils.fileReadOneLine(Processor.GOV_FILE) == null
                || Utils.fileReadOneLine(Processor.GOV_LIST_FILE) == null) {
            prefSet.removePreference(mTogglesCPU);
        } else {
            mTogglesCPU.setChecked(Settings.System.getInt(resolver,
                    Settings.System.POWER_SAVER_CPU, 1) != 0);
        }
        mTogglesMobileData = (CheckBoxPreference) prefSet.findPreference(KEY_TOGGLES_MOBILE_DATA);
        mTogglesMobileData.setChecked(Settings.System.getInt(resolver,
                Settings.System.POWER_SAVER_MOBILE_DATA, 0) != 0);
        mTogglesGPS = (CheckBoxPreference) prefSet.findPreference(KEY_TOGGLES_GPS);
        mTogglesGPS.setChecked(Settings.System.getInt(resolver,
                Settings.System.POWER_SAVER_GPS, 0) != 0);
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
        if (preference == mTogglesCPU) {
            Settings.System.putInt(resolver, Settings.System.POWER_SAVER_CPU,
                    mTogglesCPU.isChecked() ? 1 : 0);
        } else if (preference == mTogglesMobileData) {
            Settings.System.putInt(resolver, Settings.System.POWER_SAVER_MOBILE_DATA,
                    mTogglesMobileData.isChecked() ? 1 : 0);
        } else if (preference == mTogglesGPS) {
            Settings.System.putInt(resolver, Settings.System.POWER_SAVER_GPS,
                    mTogglesGPS.isChecked() ? 1 : 0);
        }
        Intent intent = new Intent("android.intent.action.POWER_SAVER_SERVICE_UPDATE");
        mActivity.sendBroadcast(intent);
        Log.i(TAG, "android.intent.action.POWER_SAVER_SERVICE_UPDATE");
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Settings.System.putInt(resolver, Settings.System.POWER_SAVER_ENABLED, isChecked ? 1 : 0);
        setPrefsEnabledState(isChecked);
        Intent service = (new Intent()).setClassName("com.android.systemui",
                "com.android.systemui.powersaver.PowerSaverService");
        if (isChecked) {
            mActivity.stopService(service);
            mActivity.startService(service);
        } else {
            mActivity.stopService(service);
        }
        Log.i(TAG, String.valueOf("PowerSaverService Status: " + String.valueOf(isChecked)));
    }

}
