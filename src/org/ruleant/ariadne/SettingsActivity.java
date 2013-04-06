package org.ruleant.ariadne;

import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
    /**
     * Key of preference Location Update Distance.
     */
    public static final String KEY_PREF_LOC_UPDATE_DIST = "loc_update_dist";

    /**
     * Default value of preference Location Update Distance.
     */
    public static final String DEFAULT_PREF_LOC_UPDATE_DIST = "10";

    /**
     * Key of preference Location Update Time.
     */
    public static final String KEY_PREF_LOC_UPDATE_TIME = "loc_update_time";

    /**
     * Default value of preference Location Update Time.
     */
    public static final String DEFAULT_PREF_LOC_UPDATE_TIME = "10000";

    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);

        // Add 'debug' preferences if DEBUG is enabled.
        if (BuildConfig.DEBUG) {
            PreferenceCategory headerDebug = new PreferenceCategory(this);
            headerDebug.setTitle(R.string.pref_header_debug);
            getPreferenceScreen().addPreference(headerDebug);
            addPreferencesFromResource(R.xml.pref_debug);
        }

        // Bind the summaries of the preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(
            findPreference(KEY_PREF_LOC_UPDATE_DIST));
        bindPreferenceSummaryToValue(
            findPreference(KEY_PREF_LOC_UPDATE_TIME));
        if (BuildConfig.DEBUG) {
            bindPreferenceSummaryToValue(
                    findPreference(DebugLevel.PREF_DEBUG_LEVEL));
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     *
     * @param context Context of the App
     * @return true if device has an extra-large screen
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     *
     * @param context Context of the App
     * @return true if simplified settings UI should be shown
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    /** {@inheritDoc} */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
            // Add 'debug' preference header if DEBUG is enabled.
            if (BuildConfig.DEBUG) {
                loadHeadersFromResource(R.xml.pref_header_debug, target);
            }
        }
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener
        sBindPreferenceSummaryToValueListener
        = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                if (index >= 0) {
                    preference.setSummary(listPreference.getEntries()[index]);
                } else {
                    preference.setSummary(null);
                }
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @param preference Preference to bind to
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference
        .setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(
                preference,
                PreferenceManager.getDefaultSharedPreferences(
                        preference.getContext()).getString(preference.getKey(),
                                ""));
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Bind the summaries of the preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(
                findPreference("loc_update_dist"));
            bindPreferenceSummaryToValue(
                findPreference("loc_update_time"));
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DebugPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Add 'debug' preferences if DEBUG is enabled.
            if (!BuildConfig.DEBUG) {
                return;
            }

            addPreferencesFromResource(R.xml.pref_debug);

            // Bind the summaries of the preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(
                findPreference(DebugLevel.PREF_DEBUG_LEVEL));
        }
    }
}
