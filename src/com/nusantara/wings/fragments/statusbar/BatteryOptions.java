/*
 * Copyright (C) 2017-2019 The Dirty Unicorns Project
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

package com.nusantara.wings.fragments.statusbar;

import android.content.Context;
import android.content.ContentResolver;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;
import com.android.settings.search.BaseSearchIndexProvider;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class BatteryOptions extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";
    private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";

    private ListPreference mBatteryPercent;
    private ListPreference mBatteryStyle;

    private int mBatteryPercentValue;
    private int mBatteryPercentValuePrev;

    private static final int BATTERY_STYLE_PORTRAIT = 0;
    private static final int BATTERY_STYLE_TEXT = 4;
    private static final int BATTERY_STYLE_HIDDEN = 5;
    private static final int BATTERY_PERCENT_HIDDEN = 0;
    private static final int BATTERY_PERCENT_SHOW = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.nad_battery_options);
        final PreferenceScreen prefSet = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();

        int batterystyle = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_BATTERY_STYLE, BATTERY_STYLE_PORTRAIT, UserHandle.USER_CURRENT);
        mBatteryStyle = (ListPreference) findPreference("status_bar_battery_style");
        mBatteryStyle.setValue(String.valueOf(batterystyle));
        mBatteryStyle.setSummary(mBatteryStyle.getEntry());
        mBatteryStyle.setOnPreferenceChangeListener(this);

        mBatteryPercentValue = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, 0, UserHandle.USER_CURRENT);
        mBatteryPercentValuePrev = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT + "_prev", -1, UserHandle.USER_CURRENT);
        mBatteryPercent = (ListPreference) findPreference("status_bar_show_battery_percent");
        mBatteryPercent.setValue(String.valueOf(mBatteryPercentValue));
        mBatteryPercent.setSummary(mBatteryPercent.getEntry());
        mBatteryPercent.setOnPreferenceChangeListener(this);

        updateBatteryOptions(batterystyle, mBatteryPercentValue);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mBatteryStyle) {
            int batterystyle = Integer.parseInt((String) newValue);
            updateBatteryOptions(batterystyle, mBatteryPercentValue);
            int index = mBatteryStyle.findIndexOfValue((String) newValue);
            mBatteryStyle.setSummary(mBatteryStyle.getEntries()[index]);
            return true;
        } else if (preference == mBatteryPercent) {
            mBatteryPercentValue = Integer.parseInt((String) newValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, mBatteryPercentValue,
                    UserHandle.USER_CURRENT);
            int index = mBatteryPercent.findIndexOfValue((String) newValue);
            mBatteryPercent.setSummary(mBatteryPercent.getEntries()[index]);
          return true;
        }
        return false;
    }

     private void updateBatteryOptions(int batterystyle, int batterypercent) {
        final ContentResolver resolver = getActivity().getContentResolver();
        switch (batterystyle) {
            case BATTERY_STYLE_TEXT:
            handleTextPercentage(BATTERY_PERCENT_SHOW);
            break;
            case BATTERY_STYLE_HIDDEN:
            handleTextPercentage(BATTERY_PERCENT_HIDDEN);
            break;
            default:
            mBatteryPercent.setEnabled(true);
            if (mBatteryPercentValuePrev != -1) {
                Settings.System.putIntForUser(resolver,
                    Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT,
                    mBatteryPercentValuePrev, UserHandle.USER_CURRENT);
                Settings.System.putIntForUser(resolver,
                    Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT + "_prev",
                    -1, UserHandle.USER_CURRENT);
                mBatteryPercentValue = mBatteryPercentValuePrev;
                mBatteryPercentValuePrev = -1;
                int index = mBatteryPercent.findIndexOfValue(String.valueOf(mBatteryPercentValue));
                mBatteryPercent.setSummary(mBatteryPercent.getEntries()[index]);
            }

            Settings.System.putIntForUser(resolver,
                Settings.System.STATUS_BAR_BATTERY_STYLE, batterystyle,
                UserHandle.USER_CURRENT);
            break;
        }
    }

    private void handleTextPercentage(int batterypercent) {
        final ContentResolver resolver = getActivity().getContentResolver();
        if (mBatteryPercentValuePrev == -1) {
            mBatteryPercentValuePrev = mBatteryPercentValue;
            Settings.System.putIntForUser(resolver,
                Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT + "_prev",
                mBatteryPercentValue, UserHandle.USER_CURRENT);
        }

        Settings.System.putIntForUser(resolver,
            Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT,
            batterypercent, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
            Settings.System.STATUS_BAR_BATTERY_STYLE, BATTERY_STYLE_TEXT,
            UserHandle.USER_CURRENT);
        int index = mBatteryPercent.findIndexOfValue(String.valueOf(batterypercent));
        mBatteryPercent.setSummary(mBatteryPercent.getEntries()[index]);
        mBatteryPercent.setEnabled(false);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.NUSANTARA_PRJ;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.nad_battery_options);
}