/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.me.raincheck;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 *
 * @author andris
 */
public class PreferencesActivity extends PreferenceActivity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.preferences);
    }
}
