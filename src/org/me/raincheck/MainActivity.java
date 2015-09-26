/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.me.raincheck;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author andris
 */
public class MainActivity extends Activity {

    private static final int RADAR_WIDTH = 500;
    private static final int CONNECT_TIMEOUT = 1000 * 3;
    private static final int READ_TIMEOUT = 1000 * 20;
    private static final String RADAR_URL = "http://www.meteo.lv/OPSIS/radars/radars.png";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        System.setProperty("http.keepAlive", "false");

        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.main);

        new DownloadImageTask().execute(RADAR_URL);
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
                new DownloadImageTask().execute(RADAR_URL);
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
        private boolean isFailed = false;
        
        @Override
        protected Bitmap doInBackground(String... url) {
            return loadBitamp(url[0]);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if(isFailed) {
                displayError(getText(R.string.error_radar_image));
            }
            else {
                ImageView radarImageView = (ImageView) findViewById(R.id.radarImageView);
                radarImageView.setImageBitmap(result);
            }
            hideProgress();
        }
        
        Bitmap loadBitamp(String fileUrl) {
            URL myFileUrl = null;
            try {
                myFileUrl = new URL(fileUrl);
            } catch (MalformedURLException ex) {
                String message = "invalid url";
                throw new RuntimeException(message, ex);
            }

            Bitmap radarImage = null;
            try {
                HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);
                conn.connect();

                InputStream is = conn.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is, 8012);

                radarImage = BitmapFactory.decodeStream(bis);
                if (radarImage != null) {
                    radarImage = Bitmap.createBitmap(radarImage, 0, 0, RADAR_WIDTH, radarImage.getHeight());
                } else {
                    isFailed = true;
                }

                is.close();
                bis.close();
            } catch (IOException ex) {
                isFailed = true;
            }
            return radarImage;
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
}
