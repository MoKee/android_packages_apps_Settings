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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.TextUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.widget.ShortCutMultiSelectListPreference;

public class RecentShortCutSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String KEY_GRAVITY = "recent_shortcut_gravity";
    private static final String KEY_EXCLUDED_APPS = "recent_shortcut_excluded_apps";

    private ContentResolver mResolver;
    private Context mContext;

    private ListPreference mGravityPref;
    private ShortCutMultiSelectListPreference mExcludedAppsPref;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.recent_shortcut_settings);

        mContext = getActivity().getApplicationContext();
        mResolver = mContext.getContentResolver();
        PreferenceScreen prefSet = getPreferenceScreen();

        mGravityPref = (ListPreference) prefSet.findPreference(KEY_GRAVITY);
        int layoutGravity = Settings.System.getInt(mResolver, Settings.System.SHORTCUT_ITEMS_GRAVITY, 0);
        mGravityPref.setValue(String.valueOf(layoutGravity));
        mGravityPref.setSummary(mGravityPref.getEntry());
        mGravityPref.setOnPreferenceChangeListener(this);

        mExcludedAppsPref = (ShortCutMultiSelectListPreference) prefSet
                .findPreference(KEY_EXCLUDED_APPS);
        Set<String> excludedApps = getExcludedApps();
        if (excludedApps != null) {
            mExcludedAppsPref.setValues(excludedApps);
        }
        mExcludedAppsPref.setOnPreferenceChangeListener(this);
    }

    private Set<String> getExcludedApps() {
        String excluded = Settings.System.getString(mResolver,
                Settings.System.SHORTCUT_ITEMS_EXCLUDED_APPS);
        if (TextUtils.isEmpty(excluded)) {
            return null;
        }
        return new HashSet<String>(Arrays.asList(excluded.split("\\|")));
    }

    private void storeExcludedApps(Set<String> values) {
        StringBuilder builder = new StringBuilder();
        String delimiter = "";
        for (String value : values) {
            builder.append(delimiter);
            builder.append(value);
            delimiter = "|";
        }
        Settings.System.putString(mResolver,
                Settings.System.SHORTCUT_ITEMS_EXCLUDED_APPS, builder.toString());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mGravityPref) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(mResolver,
                    Settings.System.SHORTCUT_ITEMS_GRAVITY, val);
            mGravityPref.setSummary(mGravityPref.getEntries()[val]);
            return true;
        } else if (preference == mExcludedAppsPref) {
            storeExcludedApps((Set<String>) newValue);
            return true;
        }
        return false;
    }
}
