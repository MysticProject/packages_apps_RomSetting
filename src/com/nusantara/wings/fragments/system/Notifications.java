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

package com.nusantara.wings.fragments.system;

import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

import com.nusantara.support.colorpicker.ColorPickerPreference;
import com.nusantara.support.preferences.SystemSettingSwitchPreference;
import com.nusantara.support.preferences.SystemSettingListPreference;
import com.nusantara.support.preferences.CustomSeekBarPreference;
import com.nusantara.support.preferences.GlobalSettingMasterSwitchPreference;

import com.nusantara.wings.UtilsNad;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class Notifications extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String NOTIFICATION_HEADER = "notification_headers";
    private static final String CENTER_NOTIFICATION_HEADER = "center_notification_headers";
    private static final String HEADS_UP_NOTIFICATIONS_ENABLED = "heads_up_notifications_enabled";
    // EdgeLight
    private static final String NOTIFICATION_PULSE_COLOR_MODE = "ambient_notification_light_color_mode";
    private static final String NOTIFICATION_PULSE_COLOR = "ambient_notification_light_color";
    private static final String NOTIFICATION_PULSE_DURATION = "ambient_notification_light_duration";
    private static final String NOTIFICATION_PULSE_REPEATS = "ambient_notification_light_repeats";
    private static final String NOTIFICATION_PULSE_BLEND_COLOR = "ambient_light_blend_color";
    private static final String KEY_AMBIENT = "ambient_notification_light_enabled";

    private GlobalSettingMasterSwitchPreference mHeadsUpEnabled;
    private SystemSettingSwitchPreference mNotificationHeader;
    private SystemSettingSwitchPreference mCenterNotificationHeader;

    // EdgeLight
    private ColorPickerPreference mEdgeLightColorPreference;
    private ColorPickerPreference mEdgeLightColorBlendPreference;
    private CustomSeekBarPreference mEdgeLightDurationPreference;
    private CustomSeekBarPreference mEdgeLightRepeatCountPreference;
    private SystemSettingSwitchPreference mAmbientPref;
    private SystemSettingListPreference mEdgeLightColorMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.nad_notifications);
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();

        mNotificationHeader = (SystemSettingSwitchPreference) findPreference(NOTIFICATION_HEADER);
        mNotificationHeader.setChecked((Settings.System.getInt(resolver,
                Settings.System.NOTIFICATION_HEADERS, 1) == 1));
        mNotificationHeader.setOnPreferenceChangeListener(this);

        mCenterNotificationHeader = (SystemSettingSwitchPreference) findPreference(CENTER_NOTIFICATION_HEADER);
        mCenterNotificationHeader.setChecked((Settings.System.getInt(resolver,
                Settings.System.CENTER_NOTIFICATION_HEADERS, 0) == 1));
        mCenterNotificationHeader.setOnPreferenceChangeListener(this);

        mHeadsUpEnabled = (GlobalSettingMasterSwitchPreference) findPreference(HEADS_UP_NOTIFICATIONS_ENABLED);
        mHeadsUpEnabled.setOnPreferenceChangeListener(this);
        int headsUpEnabled = Settings.Global.getInt(getContentResolver(),
                HEADS_UP_NOTIFICATIONS_ENABLED, 1);
        mHeadsUpEnabled.setChecked(headsUpEnabled != 0);

        mEdgeLightRepeatCountPreference = (CustomSeekBarPreference) findPreference(NOTIFICATION_PULSE_REPEATS);
        mEdgeLightRepeatCountPreference.setOnPreferenceChangeListener(this);
        int repeats = Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIFICATION_PULSE_REPEATS, 0);
        mEdgeLightRepeatCountPreference.setValue(repeats);

        mEdgeLightDurationPreference = (CustomSeekBarPreference) findPreference(NOTIFICATION_PULSE_DURATION);
        mEdgeLightDurationPreference.setOnPreferenceChangeListener(this);
        int duration = Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIFICATION_PULSE_DURATION, 2);
        mEdgeLightDurationPreference.setValue(duration);

        mEdgeLightColorMode = (SystemSettingListPreference) findPreference(NOTIFICATION_PULSE_COLOR_MODE);
        int edgeLightColorMode = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.NOTIFICATION_PULSE_COLOR_MODE, 0, UserHandle.USER_CURRENT);
        mEdgeLightColorMode.setValue(String.valueOf(edgeLightColorMode));
        mEdgeLightColorMode.setSummary(mEdgeLightColorMode.getEntry());
        mEdgeLightColorMode.setOnPreferenceChangeListener(this);

        mColorMode.setValue(Integer.toString(value));
        mColorMode.setSummary(mColorMode.getEntry());
        mColorMode.setOnPreferenceChangeListener(this);

        mEdgeLightColorPreference = (ColorPickerPreference) findPreference(NOTIFICATION_PULSE_COLOR);
        int edgeLightColor = Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIFICATION_PULSE_COLOR, 0xFFFFFFFF);
        mEdgeLightColorPreference.setNewPreviewColor(edgeLightColor);
        mEdgeLightColorPreference.setAlphaSliderEnabled(false);
        String edgeLightColorHex = String.format("#%08x", (0xFFFFFFFF & edgeLightColor));
        if (edgeLightColorHex.equals("#ffffffff")) {
            mEdgeLightColorPreference.setSummary(R.string.color_default);
        } else {
            mEdgeLightColorPreference.setSummary(edgeLightColorHex);
        }
        mEdgeLightColorPreference.setOnPreferenceChangeListener(this);

        mEdgeLightColorBlendPreference = (ColorPickerPreference) findPreference(NOTIFICATION_PULSE_BLEND_COLOR);
        int edgeblendColor = Settings.System.getInt(getContentResolver(),
                Settings.System.AMBIENT_LIGHT_BLEND_COLOR, 0xFF3980FF);
        mEdgeLightColorPreference.setNewPreviewColor(edgeblendColor);
        mEdgeLightColorBlendPreference.setAlphaSliderEnabled(false);
        String edgeBlendColorHex = String.format("#%08x", (0xFF3980FF & edgeblendColor));
        if (edgeLightColorHex.equals("#0xFF3980FF")) {
            mEdgeLightColorBlendPreference.setSummary(R.string.color_default);
        } else {
            mEdgeLightColorBlendPreference.setSummary(edgeBlendColorHex);
        }
        mEdgeLightColorBlendPreference.setOnPreferenceChangeListener(this);

        mAmbientPref = (SystemSettingSwitchPreference) findPreference(KEY_AMBIENT);
        boolean aodEnabled = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.DOZE_ALWAYS_ON, 0, UserHandle.USER_CURRENT) == 1;
        if (!aodEnabled) {
            mAmbientPref.setChecked(false);
            mAmbientPref.setEnabled(false);
            mAmbientPref.setSummary(R.string.aod_disabled);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mNotificationHeader) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.NOTIFICATION_HEADERS, value ? 1 : 0);
            UtilsNad.showSystemUiRestartDialog(getContext());
            return true;
        } else if (preference == mCenterNotificationHeader) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.CENTER_NOTIFICATION_HEADERS, value ? 0 : 1);
            UtilsNad.showSystemUiRestartDialog(getContext());
            return true;
        } else if (preference == mHeadsUpEnabled) {
            boolean value = (Boolean) newValue;
            Settings.Global.putInt(getContentResolver(),
                    HEADS_UP_NOTIFICATIONS_ENABLED, value ? 1 : 0);
            return true;
        } else if (preference == mEdgeLightColorPreference) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#ffffffff")) {
                preference.setSummary(R.string.color_default);
            } else {
                preference.setSummary(hex);
            }
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NOTIFICATION_PULSE_COLOR, intHex);
            return true;
        } else if (preference == mEdgeLightRepeatCountPreference) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.NOTIFICATION_PULSE_REPEATS, value, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mEdgeLightDurationPreference) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.NOTIFICATION_PULSE_DURATION, value, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mEdgeLightColorMode) {
            int edgeLightColorMode = Integer.valueOf((String) newValue);
            int index = mEdgeLightColorMode.findIndexOfValue((String) newValue);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.NOTIFICATION_PULSE_COLOR_MODE, edgeLightColorMode, UserHandle.USER_CURRENT);
            mEdgeLightColorMode.setSummary(mEdgeLightColorMode.getEntries()[index]);
            updateColorPrefs(edgeLightColorMode);
            return true;
        } else if (preference == mEdgeLightColorBlendPreference) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#FF3980FF")) {
                preference.setSummary(R.string.color_default);
            } else {
                preference.setSummary(hex);
            }
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.AMBIENT_LIGHT_BLEND_COLOR, intHex);
            return true;
        }
        return false;
    }

    private void updateColorPrefs(int edgeLightColorMode) {
        if (mEdgeLightColorPreference != null) {
            if (edgeLightColorMode == 3) {
                getPreferenceScreen().addPreference(mEdgeLightColorPreference);
                getPreferenceScreen().removePreference(mEdgeLightColorBlendPreference);
             } else if (edgeLightColorMode == 4) {
                getPreferenceScreen().addPreference(mEdgeLightColorPreference);
                getPreferenceScreen().addPreference(mEdgeLightColorBlendPreference);
             } else  {
                getPreferenceScreen().removePreference(mEdgeLightColorPreference);
                getPreferenceScreen().removePreference(mEdgeLightColorBlendPreference);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.NUSANTARA_PRJ;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.nad_notifications;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
        }
    };
}
