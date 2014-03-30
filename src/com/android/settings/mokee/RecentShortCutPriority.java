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

import java.util.ArrayList;
import java.util.List;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.mokee.util.MoKeeUtils;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.cyanogenmod.TouchInterceptor;

public class RecentShortCutPriority extends ListFragment {

    private final TouchInterceptor.DropListener mDropListener =
            new TouchInterceptor.DropListener() {

                public void drop(int from, int to) {
                    if (from == to) return;
                    List<String> mItems = mAdapter.getItems();
                    String item = mItems.remove(from);
                    mItems.add(to, item);
                    String shortcutItemString = "";
                    for (String itemString : mItems) {
                        shortcutItemString = shortcutItemString + itemString + ",";
                    }
                    shortcutItemString = shortcutItemString.substring(0, shortcutItemString.length() - 1) + "," + mAdapter.getDeleteItems();
                    Settings.System.putStringForUser(mContext.getContentResolver(),
                            Settings.System.SHORTCUT_ITEMS, shortcutItemString,
                            UserHandle.USER_CURRENT);
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SHORTCUT_ITEMS_CHANGED);
                    mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                    mAdapter.notifyDataSetChanged();
                }
            };

    private TouchInterceptor mShortCutsListView;
    private RecentShortCutPriorityAdapter mAdapter;
    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recent_shortcut_priority, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();
        mShortCutsListView = (TouchInterceptor) getListView();
        mShortCutsListView.setDropListener(mDropListener);
        mAdapter = new RecentShortCutPriorityAdapter(mContext);
        setListAdapter(mAdapter);
    }

    @Override
    public void onDestroy() {
        mShortCutsListView.setDropListener(null);
        setListAdapter(null);
        super.onDestroy();
    }

    private class RecentShortCutPriorityAdapter extends BaseAdapter {

        private final String[] mShortcutItems;
        private final LayoutInflater mInflater;
        private List<String> mItems;
        private String mDeleteItems;

        public RecentShortCutPriorityAdapter(Context ctx) {
            mShortcutItems = Settings.System.getString(mContext.getContentResolver(),
                    Settings.System.SHORTCUT_ITEMS).split(",");
            mInflater = LayoutInflater.from(ctx);
            reloadShortCutItems();
        }

        private void reloadShortCutItems() {
            if (mItems == null) {
                mItems = new ArrayList<String>();
            }
            String excluded = Settings.System.getString(mContext.getContentResolver(),
                    Settings.System.SHORTCUT_ITEMS_EXCLUDED_APPS);
            mDeleteItems = "";
            for (String packageName : mShortcutItems) {
                if (packageName.equals("clear")
                        || MoKeeUtils.isApkInstalledAndEnabled(packageName, mContext) && !excluded.contains(packageName)) {
                    mItems.add(packageName);
                } else {
                    mDeleteItems = mDeleteItems + packageName + ",";
                }
            }
            mDeleteItems = mDeleteItems.substring(0, mDeleteItems.length() - 1);
        }

        List<String> getItems() {
            return mItems;
        }

        String getDeleteItems() {
            return mDeleteItems;
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View v;
            if (convertView == null) {
                v = mInflater.inflate(R.layout.recent_shortcut_priority_list_item, null);
            } else {
                v = convertView;
            }
            String packageName = ((String) getItem(position));
            final ImageView icon = (ImageView) v.findViewById(R.id.icon);
            final TextView name = (TextView) v.findViewById(R.id.name);
            PackageManager pm = mContext.getPackageManager();
            ApplicationInfo mApplicationInfo = null;
            Resources mSystemUiResources = null;
            if (!packageName.equals("clear")) {
                try {
                    mApplicationInfo = pm.getApplicationInfo(packageName, 0);
                } catch (NameNotFoundException e) {
                }
                name.setText(pm.getApplicationLabel(mApplicationInfo).toString());
            } else {
                name.setText(R.string.recent_shortcut_clear_title);
            }
            if (pm != null) {
                try {
                    mSystemUiResources = pm.getResourcesForApplication("com.android.systemui");
                } catch (NameNotFoundException e) {
                }
            }
            if (mSystemUiResources != null) {
                String[] resPathArray = mContext.getResources().getStringArray(
                        com.mokee.internal.R.array.shortcut_list_drawables_in_systemui);
                String resPath = "";
                for (String resPathStr : resPathArray) {
                    String[] resItem = resPathStr.split("\\|");
                    if (resItem[0].equals(packageName)) {
                        resPath = resItem[1];
                    }
                }
                int resId = mSystemUiResources.getIdentifier(resPath, null, null);
                Drawable d = mSystemUiResources.getDrawable(resId);
                icon.setImageDrawable(d);
            }

            return v;
        }
    }
}
