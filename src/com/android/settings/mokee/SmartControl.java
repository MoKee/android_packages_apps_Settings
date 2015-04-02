/*
 * Copyright (C) 2014-2015 The MoKee OpenSource project
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
import android.os.Bundle;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class SmartControl extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "SmartControl";

    private static final String DIRECT_CALL_FOR_DIALER = "direct_call_for_dialer";
    private static final String DIRECT_CALL_FOR_MMS = "direct_call_for_mms";

    private SwitchPreference mDirectCallForDialer;
    private SwitchPreference mDirectCallForMms;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.smartcontrol_settings);

        ContentResolver resolver = getActivity().getContentResolver();

        mDirectCallForDialer = (SwitchPreference) findPreference(DIRECT_CALL_FOR_DIALER);
        mDirectCallForDialer.setChecked((Settings.System.getInt(resolver,
                Settings.System.DIRECT_CALL_FOR_DIALER, 0) == 1));
        mDirectCallForDialer.setOnPreferenceChangeListener(this);

        mDirectCallForMms = (SwitchPreference) findPreference(DIRECT_CALL_FOR_MMS);
        mDirectCallForMms.setChecked((Settings.System.getInt(resolver,
                Settings.System.DIRECT_CALL_FOR_MMS, 0) == 1));
        mDirectCallForMms.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mDirectCallForDialer) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver, Settings.System.DIRECT_CALL_FOR_DIALER, value ? 1 : 0);
            return true;
        } else if (preference == mDirectCallForMms) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver, Settings.System.DIRECT_CALL_FOR_MMS, value ? 1 : 0);
            return true;
        }
        return false;
    }
}
