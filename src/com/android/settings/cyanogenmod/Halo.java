package com.android.settings.cyanogenmod;

import android.app.INotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class Halo extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "MoKee Halo";
    private static final String KEY_HALO_STATE = "halo_state";
    private static final String KEY_HALO_HIDE = "halo_hide";
    private static final String KEY_HALO_REVERSED = "halo_reversed";

    private ListPreference mHaloState;
    private CheckBoxPreference mHaloHide;
    private CheckBoxPreference mHaloReversed;

    private Context mContext;
    private INotificationManager mNotificationManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();

        addPreferencesFromResource(R.xml.halo_settings);
        PreferenceScreen prefSet = getPreferenceScreen();

        mNotificationManager = INotificationManager.Stub.asInterface(
                ServiceManager.getService(Context.NOTIFICATION_SERVICE));

        mHaloState = (ListPreference) prefSet.findPreference(KEY_HALO_STATE);
        mHaloState.setValue(String.valueOf((isHaloPolicyBlack() ? "1" : "0")));
        mHaloState.setOnPreferenceChangeListener(this);

        mHaloHide = (CheckBoxPreference) prefSet.findPreference(KEY_HALO_HIDE);
        mHaloHide.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.HALO_HIDE, 0) == 1);

        mHaloReversed = (CheckBoxPreference) prefSet.findPreference(KEY_HALO_REVERSED);
        mHaloReversed.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.HALO_REVERSED, 1) == 1);

    }

    private boolean isHaloPolicyBlack() {
        try {
            return mNotificationManager.isHaloPolicyBlack();
        } catch (android.os.RemoteException ex) {
                // System dead
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mHaloHide) {	
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.HALO_HIDE, mHaloHide.isChecked()
                    ? 1 : 0);	
        } else if (preference == mHaloReversed) {	
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.HALO_REVERSED, mHaloReversed.isChecked()
                    ? 1 : 0);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mHaloState) {
            boolean state = Integer.valueOf((String) newValue) == 1;
            try {
                mNotificationManager.setHaloPolicyBlack(state);
            } catch (android.os.RemoteException ex) {
                // System dead
            }          
            return true;
        }
        return false;
    }
}
