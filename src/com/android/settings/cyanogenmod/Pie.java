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

package com.android.settings.cyanogenmod;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.Spannable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.android.settings.widget.ColorPickerPreference;

public class Pie extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "MoKee PIE";

    private static final String PIE_CONTROLS = "pie_controls";
    private static final String PIE_GRAVITY = "pie_gravity";
    private static final String PIE_MODE = "pie_mode";
    private static final String PIE_SIZE = "pie_size";
    private static final String PIE_TRIGGER = "pie_trigger";
    private static final String PIE_ANGLE = "pie_angle";
    private static final String PIE_GAP = "pie_gap";
    private static final String PIE_POWER = "pie_power";
    private static final String PIE_LASTAPP = "pie_lastapp";
    private static final String PIE_MENU = "pie_menu";
    private static final String PIE_SEARCH = "pie_search";
    private static final String PIE_CENTER = "pie_center";
    private static final String PIE_STICK = "pie_stick";

    private ListPreference mPieMode;
    private ListPreference mPieSize;
    private ListPreference mPieGravity;
    private ListPreference mPieTrigger;
    private ListPreference mPieAngle;
    private ListPreference mPieGap;
    private CheckBoxPreference mPieControls;
    private CheckBoxPreference mPieMenu;
    private CheckBoxPreference mPiePower;
    private CheckBoxPreference mPieLastApp;
    private CheckBoxPreference mPieSearch;
    private CheckBoxPreference mPieCenter;
    private CheckBoxPreference mPieStick;

    private ContentResolver resolver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pie_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        resolver = getContentResolver();

        mPieControls = (CheckBoxPreference) findPreference(PIE_CONTROLS);
        mPieControls.setChecked((Settings.System.getInt(resolver,
                Settings.System.PIE_CONTROLS, 0) == 1));

        mPieGravity = (ListPreference) prefSet.findPreference(PIE_GRAVITY);
        int pieGravity = Settings.System.getInt(resolver,
                Settings.System.PIE_GRAVITY, 3);
        mPieGravity.setValue(String.valueOf(pieGravity));
        mPieGravity.setOnPreferenceChangeListener(this);

        mPieMode = (ListPreference) prefSet.findPreference(PIE_MODE);
        int pieMode = Settings.System.getInt(resolver,
                Settings.System.PIE_MODE, 0);
        mPieMode.setValue(String.valueOf(pieMode));
        mPieMode.setOnPreferenceChangeListener(this);

        mPieSize = (ListPreference) prefSet.findPreference(PIE_SIZE);
        mPieTrigger = (ListPreference) prefSet.findPreference(PIE_TRIGGER);
        try {
            float pieSize = Settings.System.getFloat(resolver,
                    Settings.System.PIE_SIZE, 1.0f);
            mPieSize.setValue(String.valueOf(pieSize));
  
            float pieTrigger = Settings.System.getFloat(resolver,
                    Settings.System.PIE_TRIGGER);
            mPieTrigger.setValue(String.valueOf(pieTrigger));
        } catch(Settings.SettingNotFoundException ex) {
            // So what
        }

        mPieSize.setOnPreferenceChangeListener(this);
        mPieTrigger.setOnPreferenceChangeListener(this);

        mPieGap = (ListPreference) prefSet.findPreference(PIE_GAP);
        int pieGap = Settings.System.getInt(resolver,
                Settings.System.PIE_GAP, 2);
        mPieGap.setValue(String.valueOf(pieGap));
        mPieGap.setOnPreferenceChangeListener(this);

        mPieAngle = (ListPreference) prefSet.findPreference(PIE_ANGLE);
        int pieAngle = Settings.System.getInt(resolver,
                Settings.System.PIE_ANGLE, 12);
        mPieAngle.setValue(String.valueOf(pieAngle));
        mPieAngle.setOnPreferenceChangeListener(this);

        mPieMenu = (CheckBoxPreference) prefSet.findPreference(PIE_MENU);
        mPieMenu.setChecked(Settings.System.getInt(resolver,
                Settings.System.PIE_MENU, 1) == 1);

        mPiePower = (CheckBoxPreference) prefSet.findPreference(PIE_POWER);
        mPiePower.setChecked(Settings.System.getInt(resolver,
                Settings.System.PIE_POWER, 0) == 1);

        mPieLastApp = (CheckBoxPreference) prefSet.findPreference(PIE_LASTAPP);
        mPieLastApp.setChecked(Settings.System.getInt(resolver,
                Settings.System.PIE_LAST_APP, 0) == 1);

        mPieSearch = (CheckBoxPreference) prefSet.findPreference(PIE_SEARCH);
        mPieSearch.setChecked(Settings.System.getInt(resolver,
                Settings.System.PIE_SEARCH, 1) == 1);

        mPieCenter = (CheckBoxPreference) prefSet.findPreference(PIE_CENTER);
        mPieCenter.setChecked(Settings.System.getInt(resolver,
                Settings.System.PIE_CENTER, 1) == 1);

        mPieStick = (CheckBoxPreference) prefSet.findPreference(PIE_STICK);
        mPieStick.setChecked(Settings.System.getInt(resolver,
                Settings.System.PIE_STICK, 0) == 1);

    }

    private void updateExpandedDesktop(boolean isChecked) {
        boolean mDisabled = Settings.System.getInt(resolver,
                Settings.System.EXPANDED_DESKTOP_STATE, 0) == 0;
        boolean mStyleOff = Settings.System.getInt(resolver,
                Settings.System.EXPANDED_DESKTOP_STYLE, 0) == 0;
        boolean mPowerMenuOff = Settings.System.getInt(resolver,
                Settings.System.POWER_MENU_EXPANDED_DESKTOP_ENABLED, 0) == 0;
        if (mDisabled) {
            if (mStyleOff) {
                Settings.System.putInt(resolver,
                    Settings.System.EXPANDED_DESKTOP_STYLE, 2);//Expanded Desktop Style default set to 2
            }
        }
	if(isChecked && mPowerMenuOff)
        Settings.System.putInt(resolver,
            Settings.System.POWER_MENU_EXPANDED_DESKTOP_ENABLED, 1);
        Settings.System.putInt(resolver,
            Settings.System.EXPANDED_DESKTOP_STATE, isChecked ? 1 : 0);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mPieControls) {
            boolean mIsChecked = mPieControls.isChecked();
            Settings.System.putInt(resolver,
                    Settings.System.PIE_CONTROLS, mIsChecked ? 1 : 0);
            updateExpandedDesktop(mIsChecked);
            //Helpers.restartSystemUI();
        } else if (preference == mPieMenu) {
            Settings.System.putInt(resolver,
                    Settings.System.PIE_MENU, mPieMenu.isChecked() ? 1 : 0);
        } else if (preference == mPiePower) {
            Settings.System.putInt(resolver,
                    Settings.System.PIE_POWER, mPiePower.isChecked() ? 1 : 0);
        } else if (preference == mPieLastApp) {
            Settings.System.putInt(resolver,
                    Settings.System.PIE_LAST_APP, mPieLastApp.isChecked() ? 1 : 0);
        } else if (preference == mPieSearch) {
            Settings.System.putInt(resolver,
                    Settings.System.PIE_SEARCH, mPieSearch.isChecked() ? 1 : 0);
        } else if (preference == mPieCenter) {
            Settings.System.putInt(resolver,
                    Settings.System.PIE_CENTER, mPieCenter.isChecked() ? 1 : 0);
        } else if (preference == mPieStick) {
            Settings.System.putInt(resolver,
                    Settings.System.PIE_STICK, mPieStick.isChecked() ? 1 : 0);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mPieMode) {
            int pieMode = Integer.valueOf((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.PIE_MODE, pieMode);
            return true;
        } else if (preference == mPieSize) {
            float pieSize = Float.valueOf((String) newValue);
            Settings.System.putFloat(resolver,
                    Settings.System.PIE_SIZE, pieSize);
            return true;
        } else if (preference == mPieGravity) {
            int pieGravity = Integer.valueOf((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.PIE_GRAVITY, pieGravity);
            return true;
        } else if (preference == mPieAngle) {
            int pieAngle = Integer.valueOf((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.PIE_ANGLE, pieAngle);
            return true;
        } else if (preference == mPieGap) {
            int pieGap = Integer.valueOf((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.PIE_GAP, pieGap);
            return true;
        } else if (preference == mPieTrigger) {
            float pieTrigger = Float.valueOf((String) newValue);
            Settings.System.putFloat(resolver,
                    Settings.System.PIE_TRIGGER, pieTrigger);
            return true;
        }
        return false;
    }
}
