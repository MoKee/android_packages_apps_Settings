/*
 * Copyright (C) 2012 ParanoidAndroid Project
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

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class Pie extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {
    private static final String PA_PIE_CONTROLS = "pa_pie_controls";
    private static final String PA_PIE_GRAVITY = "pa_pie_gravity";
    private static final String PA_PIE_MODE = "pa_pie_mode";
    private static final String PA_PIE_SIZE = "pa_pie_size";
    private static final String PA_PIE_TRIGGER = "pa_pie_trigger";
    private static final String PA_PIE_ANGLE = "pa_pie_angle";
    private static final String PA_PIE_GAP = "pa_pie_gap";

    private CheckBoxPreference mPie;
    private ListPreference mPieMode;
    private ListPreference mPieSize;
    private ListPreference mPieGravity;
    private ListPreference mPieTrigger;
    private ListPreference mPieAngle;
    private ListPreference mPieGap;

    private ContentResolver mResolver;

    protected Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pa_pie_control);
        PreferenceScreen prefSet = getPreferenceScreen();

        Context context = getActivity();
        mResolver = context.getContentResolver();

        mPie = (CheckBoxPreference) prefSet.findPreference(PA_PIE_CONTROLS);
        int pie = Settings.System.getInt(mResolver,
                Settings.System.PA_PIE_CONTROLS, 0);
        mPie.setChecked(pie != 0);

        mPieGravity = (ListPreference) prefSet.findPreference(PA_PIE_GRAVITY);
        int pieGravity = Settings.System.getInt(mResolver,
                Settings.System.PA_PIE_GRAVITY, 3);
        mPieGravity.setValue(String.valueOf(pieGravity));
        mPieGravity.setSummary(mPieGravity.getEntry());
        mPieGravity.setOnPreferenceChangeListener(this);

        mPieMode = (ListPreference) prefSet.findPreference(PA_PIE_MODE);
        int pieMode = Settings.System.getInt(mResolver,
                Settings.System.PA_PIE_MODE, 1);
        mPieMode.setValue(String.valueOf(pieMode));
        mPieMode.setSummary(mPieMode.getEntry());
        mPieMode.setOnPreferenceChangeListener(this);

        mPieGap = (ListPreference) prefSet.findPreference(PA_PIE_GAP);
        int pieGap = Settings.System.getInt(mResolver,
                Settings.System.PA_PIE_GAP, 2);
        mPieGap.setValue(String.valueOf(pieGap));
        mPieGap.setSummary(mPieGap.getEntry());
        mPieGap.setOnPreferenceChangeListener(this);

        mPieAngle = (ListPreference) prefSet.findPreference(PA_PIE_ANGLE);
        int pieAngle = Settings.System.getInt(mResolver,
                Settings.System.PA_PIE_ANGLE, 12);
        mPieAngle.setValue(String.valueOf(pieAngle));
        mPieAngle.setSummary(mPieAngle.getEntry());
        mPieAngle.setOnPreferenceChangeListener(this);

        mPieSize = (ListPreference) prefSet.findPreference(PA_PIE_SIZE);
        float pieSize = Settings.System.getFloat(mResolver,
                Settings.System.PA_PIE_SIZE, 1.0f);
        mPieSize.setValue(String.valueOf(pieSize));
        mPieSize.setSummary(mPieSize.getEntry());
        mPieSize.setOnPreferenceChangeListener(this);

        mPieTrigger = (ListPreference) prefSet.findPreference(PA_PIE_TRIGGER);
        float pieTrigger = Settings.System.getFloat(mResolver,
                Settings.System.PA_PIE_TRIGGER, 2.0f);
        mPieTrigger.setValue(String.valueOf(pieTrigger));
        mPieTrigger.setSummary(mPieTrigger.getEntry());
        mPieTrigger.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mPie) {
            updatePieStatus(mPie.isChecked());
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void updatePieStatus(boolean status) {
         Settings.System.putInt(getActivity().getContentResolver(),
                 Settings.System.PA_PIE_CONTROLS, status ? 1 : 0);
         int state = Settings.System.getInt(getActivity().getContentResolver(),
                 Settings.System.EXPANDED_DESKTOP_STYLE, 0);
         if (state == 1 && status) {
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.EXPANDED_DESKTOP_STATE, 0);
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.EXPANDED_DESKTOP_STYLE, 2);
         }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mPieMode) {
            int pieMode = Integer.valueOf((String) newValue);
            int index = mPieMode.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PA_PIE_MODE, pieMode);
            mPieMode.setSummary(mPieMode.getEntries()[index]);
            return true;
        } else if (preference == mPieSize) {
            float pieSize = Float.valueOf((String) newValue);
            int index = mPieSize.findIndexOfValue((String) newValue);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.PA_PIE_SIZE, pieSize);
            mPieSize.setSummary(mPieSize.getEntries()[index]);
            return true;
        } else if (preference == mPieGravity) {
            int pieGravity = Integer.valueOf((String) newValue);
            int index = mPieGravity.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PA_PIE_GRAVITY, pieGravity);
            mPieGravity.setSummary(mPieGravity.getEntries()[index]);
            return true;
        } else if (preference == mPieTrigger) {
            float pieTrigger = Float.valueOf((String) newValue);
            int index = mPieTrigger.findIndexOfValue((String) newValue);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.PA_PIE_TRIGGER, pieTrigger);
            mPieTrigger.setSummary(mPieTrigger.getEntries()[index]);
            return true;
        } else if (preference == mPieMode) {
            int pieMode = Integer.valueOf((String) newValue);
            int index = mPieMode.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PA_PIE_MODE, pieMode);
            mPieMode.setSummary(mPieMode.getEntries()[index]);
            return true;
        } else if (preference == mPieAngle) {
            int pieAngle = Integer.valueOf((String) newValue);
            int index = mPieAngle.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PA_PIE_ANGLE, pieAngle);
            mPieAngle.setSummary(mPieAngle.getEntries()[index]);
            return true;
        } else if (preference == mPieGap) {
            int pieGap = Integer.valueOf((String) newValue);
            int index = mPieGap.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PA_PIE_GAP, pieGap);
            mPieGap.setSummary(mPieGap.getEntries()[index]);
            return true;
        }
        return false;
    }
}
