package com.android.settings.profiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import mokee.app.Profile;
import mokee.app.ProfileManager;
import mokee.providers.MKSettings;

import java.util.UUID;

public class SetupDefaultProfileReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (MKSettings.System.getInt(context.getContentResolver(),
                MKSettings.System.SYSTEM_PROFILES_ENABLED, 1) == 1) {
            ProfileManager profileManager = ProfileManager.getInstance(context);
            Profile defaultProfile = profileManager.getProfile(
                    UUID.fromString("0230226d-0d05-494a-a9bd-d222a1117655"));
            if (defaultProfile != null) {
                SetupActionsFragment.fillProfileWithCurrentSettings(context, defaultProfile);
                profileManager.updateProfile(defaultProfile);
            }
        }


    }
}
