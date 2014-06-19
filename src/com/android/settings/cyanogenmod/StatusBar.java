/*
 * Copyright (C) 2012 The CyanogenMod Project
 * Copyright (C) 2012 - 2014 The MoKee OpenSource Project
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

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.MSimTelephonyManager;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.widget.EditText;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class StatusBar extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "StatusBar";

    private static final String STATUS_BAR_AM_PM = "status_bar_am_pm";
    private static final String STATUS_BAR_CARRIER = "status_bar_carrier";
    private static final String CUSTOM_CARRIER_LABEL = "custom_carrier_label";
    private static final String STATUS_BAR_TRAFFIC_STYLE = "status_bar_traffic_style";
    private static final String STATUS_BAR_BATTERY = "status_bar_battery";
    private static final String STATUS_BAR_SIGNAL = "status_bar_signal";

    private static final String STATUS_BAR_BATTERY_SHOW_PERCENT = "status_bar_battery_show_percent";

    private static final String STATUS_BAR_STYLE_HIDDEN = "4";
    private static final String STATUS_BAR_STYLE_TEXT = "6";

    private ListPreference mStatusBarBattery;
    private SystemSettingCheckBoxPreference mStatusBarBatteryShowPercent;
    private ListPreference mStatusBarCmSignal;

    private ListPreference mStatusBarAmPm;
    private ListPreference mStatusBarTraffic;
    private CheckBoxPreference mStatusBarCarrier;
    private PreferenceScreen mCustomStatusBarCarrierLabel;

    private String mCustomStatusBarCarrierLabelText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.status_bar);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mStatusBarAmPm = (ListPreference) prefSet.findPreference(STATUS_BAR_AM_PM);
        if (DateFormat.is24HourFormat(getActivity())) {
            prefSet.removePreference(prefSet.findPreference(STATUS_BAR_AM_PM));
        } else {
            mStatusBarAmPm = (ListPreference) prefSet.findPreference(STATUS_BAR_AM_PM);
            int statusBarAmPm = Settings.System.getInt(resolver, Settings.System.STATUS_BAR_AM_PM, 0);
            mStatusBarAmPm.setValue(String.valueOf(statusBarAmPm));
            mStatusBarAmPm.setSummary(mStatusBarAmPm.getEntry());
            mStatusBarAmPm.setOnPreferenceChangeListener(this);
        }

        mStatusBarTraffic = (ListPreference) prefSet.findPreference(STATUS_BAR_TRAFFIC_STYLE);
        int trafficStyle = Settings.System.getInt(resolver, Settings.System.STATUS_BAR_TRAFFIC_STYLE, 0);
        mStatusBarTraffic.setValue(String.valueOf(trafficStyle));
        mStatusBarTraffic.setSummary(mStatusBarTraffic.getEntry());
        mStatusBarTraffic.setOnPreferenceChangeListener(this);

        mStatusBarCarrier = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_CARRIER);
        mStatusBarCarrier.setChecked((Settings.System.getInt(resolver, Settings.System.STATUS_BAR_CARRIER, 0) == 1));
        mStatusBarCarrier.setOnPreferenceChangeListener(this);
        mCustomStatusBarCarrierLabel = (PreferenceScreen) prefSet.findPreference(CUSTOM_CARRIER_LABEL);
        if (MSimTelephonyManager.getDefault().isMultiSimEnabled()) {
            prefSet.removePreference(mStatusBarCarrier);
            prefSet.removePreference(mCustomStatusBarCarrierLabel);
        } else {
            updateCustomLabelTextSummary();
        }

        mStatusBarBattery = (ListPreference) findPreference(STATUS_BAR_BATTERY);
        mStatusBarBatteryShowPercent =
                (SystemSettingCheckBoxPreference) findPreference(STATUS_BAR_BATTERY_SHOW_PERCENT);
        mStatusBarCmSignal = (ListPreference) prefSet.findPreference(STATUS_BAR_SIGNAL);

        CheckBoxPreference statusBarBrightnessControl = (CheckBoxPreference)
                prefSet.findPreference(Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL);

        try {
            if (Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE)
                    == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                statusBarBrightnessControl.setEnabled(false);
                statusBarBrightnessControl.setSummary(R.string.status_bar_toggle_info);
            }
        } catch (SettingNotFoundException e) {
            // Do nothing
        }

        int batteryStyle = Settings.System.getInt(resolver, Settings.System.STATUS_BAR_BATTERY, 2);
        mStatusBarBattery.setValue(String.valueOf(batteryStyle));
        mStatusBarBattery.setSummary(mStatusBarBattery.getEntry());
        mStatusBarBattery.setOnPreferenceChangeListener(this);

        int signalStyle = Settings.System.getInt(resolver, Settings.System.STATUS_BAR_SIGNAL_TEXT, 0);
        mStatusBarCmSignal.setValue(String.valueOf(signalStyle));
        mStatusBarCmSignal.setSummary(mStatusBarCmSignal.getEntry());
        mStatusBarCmSignal.setOnPreferenceChangeListener(this);

        if (Utils.isWifiOnly(getActivity())
                || (MSimTelephonyManager.getDefault().isMultiSimEnabled())) {
            prefSet.removePreference(mStatusBarCmSignal);
        }

        enableStatusBarBatteryDependents(mStatusBarBattery.getValue());
    }

    private void updateCustomLabelTextSummary() {
        mCustomStatusBarCarrierLabelText = Settings.System.getString(getActivity().getContentResolver(),
            Settings.System.CUSTOM_CARRIER_LABEL);

        if (TextUtils.isEmpty(mCustomStatusBarCarrierLabelText)) {
            mCustomStatusBarCarrierLabel.setSummary(R.string.custom_carrier_label_notset);
        } else {
            mCustomStatusBarCarrierLabel.setSummary(mCustomStatusBarCarrierLabelText);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mStatusBarBattery) {
            int batteryStyle = Integer.valueOf((String) newValue);
            int index = mStatusBarBattery.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_BATTERY, batteryStyle);
            mStatusBarBattery.setSummary(mStatusBarBattery.getEntries()[index]);

            enableStatusBarBatteryDependents((String) newValue);
            return true;
        } else if (preference == mStatusBarTraffic) {
            int trafficStyle = Integer.valueOf((String) newValue);
            int index = mStatusBarTraffic.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_TRAFFIC_STYLE, trafficStyle);
            mStatusBarTraffic.setSummary(mStatusBarTraffic.getEntries()[index]);
            return true;
        } else if (mStatusBarAmPm != null && preference == mStatusBarAmPm) {
            int statusBarAmPm = Integer.valueOf((String) newValue);
            int index = mStatusBarAmPm.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_AM_PM, statusBarAmPm);
            mStatusBarAmPm.setSummary(mStatusBarAmPm.getEntries()[index]);
            return true;
        } else if (preference == mStatusBarCarrier) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_CARRIER, value ? 1 : 0);
            return true;
        } else if (preference == mStatusBarCmSignal) {
            int signalStyle = Integer.valueOf((String) newValue);
            int index = mStatusBarCmSignal.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_SIGNAL_TEXT, signalStyle);
            mStatusBarCmSignal.setSummary(mStatusBarCmSignal.getEntries()[index]);
            return true;
        }

        return false;
    }

    private void enableStatusBarBatteryDependents(String value) {
        boolean enabled = !(value.equals(STATUS_BAR_STYLE_TEXT)
                || value.equals(STATUS_BAR_STYLE_HIDDEN));
        mStatusBarBatteryShowPercent.setEnabled(enabled);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            final Preference preference) {
        final ContentResolver resolver = getActivity().getContentResolver();
        if (preference.getKey().equals(CUSTOM_CARRIER_LABEL)) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle(R.string.custom_carrier_label_title);
            alert.setMessage(R.string.custom_carrier_label_explain);

            // Set an EditText view to get user input
            final EditText input = new EditText(getActivity());
            input.setText(TextUtils.isEmpty(mCustomStatusBarCarrierLabelText) ? "" : mCustomStatusBarCarrierLabelText);
            input.setSelection(input.getText().length());
            alert.setView(input);
            alert.setPositiveButton(getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = ((Spannable) input.getText()).toString().trim();
                    Settings.System.putString(resolver, Settings.System.CUSTOM_CARRIER_LABEL, value);
                    updateCustomLabelTextSummary();
                    Intent i = new Intent();
                    i.setAction(Intent.ACTION_CUSTOM_CARRIER_LABEL_CHANGED);
                    getActivity().sendBroadcast(i);
                }
            });
            alert.setNegativeButton(getResources().getString(R.string.cancel), null);
            alert.show();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
