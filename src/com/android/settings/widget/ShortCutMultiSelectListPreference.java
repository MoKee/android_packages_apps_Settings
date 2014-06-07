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

package com.android.settings.widget;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.mokee.util.MoKeeUtils;

import com.android.settings.R;

/**
 * A preference that lists shortcut applications, with icons, as a multi choice
 * list.
 * 
 * @author Clark Scheff
 */
public class ShortCutMultiSelectListPreference extends DialogPreference {

    private final List<MyApplicationInfo> mPackageInfoList = new ArrayList<MyApplicationInfo>();
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private Set<String> mValues = new HashSet<String>();
    private Set<String> mNewValues = new HashSet<String>();
    private boolean mPreferenceChanged;
    private PackageManager pm;
    private Context mContext;

    public ShortCutMultiSelectListPreference(Context context) {
        this(context, null);
    }

    public ShortCutMultiSelectListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        String shortcutItemString = Settings.System.getString(context.getContentResolver(), Settings.System.SHORTCUT_ITEMS);
        String [] mShortcutListItems = null;
        if (TextUtils.isEmpty(shortcutItemString)) {
            mShortcutListItems = mContext.getResources().getStringArray(com.android.internal.R.array.shortcut_list_items);
        } else {
            mShortcutListItems = shortcutItemString.split(",");
        }
        for (String packageName : mShortcutListItems) {
            if (!packageName.equals("clear")
                    && MoKeeUtils.isApkInstalledAndEnabled(packageName, context)) {
                pm = context.getPackageManager();
                ApplicationInfo ai = null;
                try {
                    ai = pm.getApplicationInfo(packageName, 0);
                } catch (NameNotFoundException e) {
                }
                MyApplicationInfo info = new MyApplicationInfo();
                info.info = ai;
                info.label = info.info.loadLabel(getContext().getPackageManager()).toString();
                mPackageInfoList.add(info);
            }
        }
        List<CharSequence> entries = new ArrayList<CharSequence>();
        List<CharSequence> entryValues = new ArrayList<CharSequence>();
        for (MyApplicationInfo info : mPackageInfoList) {
            entries.add(info.label);
            entryValues.add(info.info.packageName);
        }
        mEntries = new CharSequence[entries.size()];
        mEntryValues = new CharSequence[entries.size()];
        entries.toArray(mEntries);
        entryValues.toArray(mEntryValues);
    }

    /**
     * Sets the value of the key. This should contain entries in
     * {@link #getEntryValues()}.
     * 
     * @param values The values to set for the key.
     */
    public void setValues(Set<String> values) {
        mValues.clear();
        mValues.addAll(values);

        persistStringSet(values);
    }

    public void setClearValues() {
        mValues.clear();
    }

    /**
     * Retrieves the current value of the key.
     */
    public Set<String> getValues() {
        return mValues;
    }

    /**
     * Returns the index of the given value (in the entry values array).
     * 
     * @param value The value whose index should be returned.
     * @return The index of the value, or -1 if not found.
     */
    public int findIndexOfValue(String value) {
        if (value != null && mEntryValues != null) {
            for (int i = mEntryValues.length - 1; i >= 0; i--) {
                if (mEntryValues[i].equals(value)) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setAdapter(new AppListAdapter(getContext()), null);
        mNewValues.clear();
        mNewValues.addAll(mValues);
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        final AlertDialog dialog = (AlertDialog) getDialog();
        final ListView listView = dialog.getListView();
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final AppViewHolder holder = (AppViewHolder) view.getTag();
                final boolean isChecked = !holder.checkBox.isChecked();
                holder.checkBox.setChecked(isChecked);
                if (isChecked) {
                    mPreferenceChanged |= mNewValues.add(mEntryValues[position].toString());
                } else {
                    mPreferenceChanged |= mNewValues.remove(mEntryValues[position].toString());
                }
            }
        });
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult && mPreferenceChanged) {
            final Set<String> values = mNewValues;
            if (callChangeListener(values)) {
                setValues(values);
            }
        }
        mPreferenceChanged = false;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        final CharSequence[] defaultValues = a.getTextArray(index);
        if (defaultValues != null) {
            final int valueCount = defaultValues.length;
            final Set<String> result = new HashSet<String>();

            for (int i = 0; i < valueCount; i++) {
                result.add(defaultValues[i].toString());
            }

            return result;
        }
        return null;
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValues(restoreValue ? getPersistedStringSet(mValues) : (Set<String>) defaultValue);
    }

    class MyApplicationInfo {
        ApplicationInfo info;
        CharSequence label;
    }

    public class AppListAdapter extends ArrayAdapter<MyApplicationInfo> {
        private final LayoutInflater mInflater;

        public AppListAdapter(Context context) {
            super(context, 0);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            addAll(mPackageInfoList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // A ViewHolder keeps references to children views to avoid
            // unnecessary calls
            // to findViewById() on each row.
            AppViewHolder holder = AppViewHolder.createOrRecycle(mInflater, convertView);
            convertView = holder.rootView;
            MyApplicationInfo info = getItem(position);
            holder.appName.setText(info.label);
            Resources mSystemUiResources = null;
            if (pm != null) {
                try {
                    mSystemUiResources = pm.getResourcesForApplication("com.android.systemui");
                } catch (NameNotFoundException e) {
                }
            }
            if (mSystemUiResources != null) {
                String[] resPathArray = mContext.getResources().getStringArray(
                        com.android.internal.R.array.shortcut_list_drawables_in_systemui);
                String resPath = "";
                for (String resPathStr : resPathArray) {
                    String[] resItem = resPathStr.split("\\|");
                    if (resItem[0].equals(info.info.packageName)) {
                        resPath = resItem[1] + "_normal";
                    }
                }
                int resId = mSystemUiResources.getIdentifier(resPath, null, null);
                Drawable d = mSystemUiResources.getDrawable(resId);
                holder.appIcon.setImageDrawable(d);
            }
            holder.checkBox.setChecked(mNewValues.contains(mEntryValues[position].toString()));
            return convertView;
        }

        @Override
        public MyApplicationInfo getItem(int position) {
            return mPackageInfoList.get(position);
        }
    }

    public static class AppViewHolder {
        public View rootView;
        public TextView appName;
        public ImageView appIcon;
        public CheckBox checkBox;

        public static AppViewHolder createOrRecycle(LayoutInflater inflater, View convertView) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.excluded_app_item, null);

                // Creates a ViewHolder and store references to the two children
                // views
                // we want to bind data to.
                AppViewHolder holder = new AppViewHolder();
                holder.rootView = convertView;
                holder.appName = (TextView) convertView.findViewById(R.id.app_name);
                holder.appIcon = (ImageView) convertView.findViewById(R.id.app_icon);
                holder.checkBox = (CheckBox) convertView.findViewById(android.R.id.checkbox);
                convertView.setTag(holder);
                return holder;
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                return (AppViewHolder) convertView.getTag();
            }
        }
    }
}
