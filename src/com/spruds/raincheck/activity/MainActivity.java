package com.spruds.raincheck.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.spruds.raincheck.R;
import com.spruds.raincheck.service.RadarImageService;
import com.spruds.raincheck.service.storage.RadarImageAdapter;
import java.util.Date;

/**
 *
 * @author andris
 */
public class MainActivity extends Activity {

    private RadarImageAdapter dataAdapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        dataAdapter = new RadarImageAdapter(this);
        dataAdapter.open();
        
        System.setProperty("http.keepAlive", "false");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.main);

        new DownloadImageTask().execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(dataAdapter != null) {
            dataAdapter.close();
        }
    }
    
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        // Eliminates color banding
        window.setFormat(PixelFormat.RGBA_8888);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case R.id.main_menu_refresh: {
                showProgress();
                new DownloadImageTask().execute();
                break;
            }
            case R.id.main_menu_about: {
                Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
                startActivityForResult(intent, 0);
                break;
            }
            case R.id.main_menu_preferences: {
                Intent intent = new Intent(getApplicationContext(), PreferencesActivity.class);
                startActivityForResult(intent, 0);
                break;
            }
            case R.id.main_menu_history: {
                displayError(getText(R.string.no_history_available));
                break;
            }
        }
        return false;
    }
    
    private class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {        
        @Override
        protected Bitmap doInBackground(String... params) {
            return RadarImageService.getImageForDisplay();
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if(result == null) {
                displayError(getText(R.string.error_radar_image));
                hideProgress();
            }
            else {
                ImageView radarImageView = (ImageView) findViewById(R.id.radarImageView);
                radarImageView.setImageBitmap(result);
                hideProgress();

                if(shouldSaveImage()) {
                    dataAdapter.insertImage(result);
                }
            }
        }
    }

    private void hideProgress() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.radarProgressBar);
        progressBar.setVisibility(View.GONE);

        ImageView radarImageView = (ImageView) findViewById(R.id.radarImageView);
        radarImageView.setVisibility(View.VISIBLE);
    }

    private void showProgress() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.radarProgressBar);
        progressBar.setVisibility(View.VISIBLE);

        ImageView radarImageView = (ImageView) findViewById(R.id.radarImageView);
        radarImageView.setVisibility(View.GONE);
    }
    
    private void displayError(CharSequence text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }
    
    private boolean shouldSaveImage() {
        Date lastTimestamp = dataAdapter.getLastTimestamp();
        if(lastTimestamp == null) {
            return true;
        }
        else {
            long interval = new Date().getTime()-lastTimestamp.getTime();
            int minutes = (int)((interval/60000) % 60);
            int frequency = dataAdapter.getCacheFrequency();

            if(frequency > 0 && minutes >= frequency) {
                return true;
            }
            else {
                return false;
            }
        }
    }
}
