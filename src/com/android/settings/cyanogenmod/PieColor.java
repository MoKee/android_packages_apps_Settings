
package com.android.settings.cyanogenmod;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.android.settings.widget.ColorPickerPreference;

public class PieColor extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String PIE_ENABLE_COLOR = "pie_enable_color";
    private static final String PIE_JUICE = "pie_juice";
    private static final String PIE_BACKGROUND = "pie_background";
    private static final String PIE_SELECT = "pie_select";
    private static final String PIE_OUTLINES = "pie_outlines";
    private static final String PIE_STATUS_CLOCK = "pie_status_clock";
    private static final String PIE_STATUS = "pie_status";
    private static final String PIE_CHEVRON_LEFT = "pie_chevron_left";
    private static final String PIE_CHEVRON_RIGHT = "pie_chevron_right";
    private static final String PIE_BUTTON_COLOR = "pie_button_color";

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pie_color);

        mEnableColor = (CheckBoxPreference) findPreference(PIE_ENABLE_COLOR);
        mEnableColor.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.PIE_ENABLE_COLOR, 0) == 1);

        mPieBg = (ColorPickerPreference) findPreference(PIE_BACKGROUND);
        mPieBg.setOnPreferenceChangeListener(this);

        mJuice = (ColorPickerPreference) findPreference(PIE_JUICE);
        mJuice.setOnPreferenceChangeListener(this);

        mSelect = (ColorPickerPreference) findPreference(PIE_SELECT);
        mSelect.setOnPreferenceChangeListener(this);

        mOutlines = (ColorPickerPreference) findPreference(PIE_OUTLINES);
        mOutlines.setOnPreferenceChangeListener(this);

        mStatusClock = (ColorPickerPreference) findPreference(PIE_STATUS_CLOCK);
        mStatusClock.setOnPreferenceChangeListener(this);

        mStatus = (ColorPickerPreference) findPreference(PIE_STATUS);
        mStatus.setOnPreferenceChangeListener(this);

        mChevronLeft = (ColorPickerPreference) findPreference(PIE_CHEVRON_LEFT);
        mChevronLeft.setOnPreferenceChangeListener(this);

        mChevronRight = (ColorPickerPreference) findPreference(PIE_CHEVRON_RIGHT);
        mChevronRight.setOnPreferenceChangeListener(this);

        mBtnColor = (ColorPickerPreference) findPreference(PIE_BUTTON_COLOR);
        mBtnColor.setOnPreferenceChangeListener(this);

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mEnableColor) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_ENABLE_COLOR,
                    mEnableColor.isChecked() ? 1 : 0);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mPieBg) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_BACKGROUND, intHex);
            return true;
        } else if (preference == mSelect) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_SELECT, intHex);
            return true;
        } else if (preference == mOutlines) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_OUTLINES, intHex);
            return true;
        } else if (preference == mStatusClock) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_STATUS_CLOCK, intHex);
            return true;
        } else if (preference == mStatus) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_STATUS, intHex);
            return true;
        } else if (preference == mChevronLeft) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_CHEVRON_LEFT, intHex);
            return true;
        } else if (preference == mChevronRight) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_CHEVRON_RIGHT, intHex);
            return true;
        } else if (preference == mBtnColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_BUTTON_COLOR, intHex);
            return true;
        } else if (preference == mJuice) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_JUICE, intHex);
            return true;
        }
        return false;
    }
}
