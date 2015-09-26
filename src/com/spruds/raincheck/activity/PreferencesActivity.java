package com.spruds.raincheck.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.spruds.raincheck.R;

public class PreferencesActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.preferences);
    }
}
