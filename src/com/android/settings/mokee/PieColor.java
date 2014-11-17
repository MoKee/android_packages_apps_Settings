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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.Resources;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class PieColor extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String PA_PIE_ENABLE_COLOR = "pa_pie_enable_color";
    private static final String PA_PIE_JUICE = "pa_pie_juice";
    private static final String PA_PIE_BACKGROUND = "pa_pie_background";
    private static final String PA_PIE_SELECT = "pa_pie_select";
    private static final String PA_PIE_OUTLINES = "pa_pie_outlines";
    private static final String PA_PIE_STATUS_CLOCK = "pa_pie_status_clock";
    private static final String PA_PIE_STATUS = "pa_pie_status";
    private static final String PA_PIE_CHEVRON_LEFT = "pa_pie_chevron_left";
    private static final String PA_PIE_CHEVRON_RIGHT = "pa_pie_chevron_right";
    private static final String PA_PIE_BUTTON_COLOR = "pa_pie_button_color";
    private static final int MENU_RESET = Menu.FIRST;

    private static final int COLOR_PIE_BACKGROUND = 0xaa333333;
    private static final int COLOR_PIE_BUTTON = 0xb2ffffff;
    private static final int COLOR_PIE_SELECT = 0xaaffffff;
    private static final int COLOR_PIE_OUTLINES = 0xffffffff;
    private static final int COLOR_CHEVRON_LEFT = 0xdfa9a9a9;
    private static final int COLOR_CHEVRON_RIGHT = 0xdfe3e3e3;
    private static final int COLOR_BATTERY_JUICE = 0xffa5a5a5;
    private static final int COLOR_STATUS = 0xffffffff;

    CheckBoxPreference mEnableColor;
    ColorPickerPreference mPieBg;
    ColorPickerPreference mJuice;
    ColorPickerPreference mSelect;
    ColorPickerPreference mOutlines;
    ColorPickerPreference mStatusClock;
    ColorPickerPreference mStatus;
    ColorPickerPreference mChevronLeft;
    ColorPickerPreference mChevronRight;
    ColorPickerPreference mBtnColor;
    MenuItem mReset;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pa_pie_color);

        mEnableColor = (CheckBoxPreference) findPreference(PA_PIE_ENABLE_COLOR);
        mEnableColor.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.PA_PIE_ENABLE_COLOR, 0) == 1);

        mPieBg = (ColorPickerPreference) findPreference(PA_PIE_BACKGROUND);
        mPieBg.setOnPreferenceChangeListener(this);

        mJuice = (ColorPickerPreference) findPreference(PA_PIE_JUICE);
        mJuice.setOnPreferenceChangeListener(this);

        mSelect = (ColorPickerPreference) findPreference(PA_PIE_SELECT);
        mSelect.setOnPreferenceChangeListener(this);

        mOutlines = (ColorPickerPreference) findPreference(PA_PIE_OUTLINES);
        mOutlines.setOnPreferenceChangeListener(this);

        mStatusClock = (ColorPickerPreference) findPreference(PA_PIE_STATUS_CLOCK);
        mStatusClock.setOnPreferenceChangeListener(this);

        mStatus = (ColorPickerPreference) findPreference(PA_PIE_STATUS);
        mStatus.setOnPreferenceChangeListener(this);

        mChevronLeft = (ColorPickerPreference) findPreference(PA_PIE_CHEVRON_LEFT);
        mChevronLeft.setOnPreferenceChangeListener(this);

        mChevronRight = (ColorPickerPreference) findPreference(PA_PIE_CHEVRON_RIGHT);
        mChevronRight.setOnPreferenceChangeListener(this);

        mBtnColor = (ColorPickerPreference) findPreference(PA_PIE_BUTTON_COLOR);
        mBtnColor.setOnPreferenceChangeListener(this);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(R.drawable.ic_settings_backup)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        mReset = menu.findItem(MENU_RESET);
        mReset.setVisible(mEnableColor.isChecked());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetToDefault();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void resetToDefault() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.reset);
        alertDialog.setMessage(R.string.pa_color_pie_reset_message);
        alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                resetValues();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.create().show();
    }

    private void resetValues() {
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.PA_PIE_BACKGROUND, COLOR_PIE_BACKGROUND);
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.PA_PIE_SELECT, COLOR_PIE_SELECT);
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.PA_PIE_OUTLINES, COLOR_PIE_OUTLINES);
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.PA_PIE_STATUS_CLOCK, COLOR_STATUS);
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.PA_PIE_STATUS, COLOR_STATUS);
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.PA_PIE_CHEVRON_LEFT, COLOR_CHEVRON_LEFT);
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.PA_PIE_CHEVRON_RIGHT, COLOR_CHEVRON_RIGHT);
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.PA_PIE_BUTTON_COLOR, COLOR_PIE_BUTTON);
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.PA_PIE_JUICE, COLOR_BATTERY_JUICE);

        mPieBg.setNewPreviewColor(COLOR_PIE_BACKGROUND);
        mJuice.setNewPreviewColor(COLOR_BATTERY_JUICE);
        mSelect.setNewPreviewColor(COLOR_PIE_SELECT);
        mOutlines.setNewPreviewColor(COLOR_PIE_OUTLINES);
        mStatusClock.setNewPreviewColor(COLOR_STATUS);
        mStatus.setNewPreviewColor(COLOR_STATUS);
        mChevronLeft.setNewPreviewColor(COLOR_CHEVRON_LEFT);
        mChevronRight.setNewPreviewColor(COLOR_CHEVRON_RIGHT);
        mBtnColor.setNewPreviewColor(COLOR_PIE_BUTTON);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mEnableColor) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PA_PIE_ENABLE_COLOR,
                    mEnableColor.isChecked() ? 1 : 0);
            mReset.setVisible(mEnableColor.isChecked());
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mPieBg) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PA_PIE_BACKGROUND, intHex);
            return true;
        } else if (preference == mSelect) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PA_PIE_SELECT, intHex);
            return true;
        } else if (preference == mOutlines) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PA_PIE_OUTLINES, intHex);
            return true;
        } else if (preference == mStatusClock) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PA_PIE_STATUS_CLOCK, intHex);
            return true;
        } else if (preference == mStatus) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PA_PIE_STATUS, intHex);
            return true;
        } else if (preference == mChevronLeft) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PA_PIE_CHEVRON_LEFT, intHex);
            return true;
        } else if (preference == mChevronRight) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PA_PIE_CHEVRON_RIGHT, intHex);
            return true;
        } else if (preference == mBtnColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PA_PIE_BUTTON_COLOR, intHex);
            return true;
        } else if (preference == mJuice) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PA_PIE_JUICE, intHex);
            return true;
        }
        return false;
    }
}
