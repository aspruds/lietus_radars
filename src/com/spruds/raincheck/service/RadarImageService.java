package com.spruds.raincheck.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.util.Log;
import com.spruds.raincheck.service.storage.RadarImageAdapter;
import com.spruds.raincheck.utils.ImageUtils;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class RadarImageService extends Service {
    private static final String TAG = RadarImageService.class.getSimpleName();

    private static final int CONNECT_TIMEOUT = 1000 * 3;
    private static final int READ_TIMEOUT = 1000 * 20;
    private static final int RADAR_WIDTH = 500;
    private static final String RADAR_URL = "http://www.meteo.lv/OPSIS/radars/radars.png";
    
    private Timer timer;
    private RadarImageAdapter dataAdapter;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dataAdapter = new RadarImageAdapter(this);

        int frequency = dataAdapter.getCacheFrequency() * 60 * 1000;
        Log.d(TAG, "scheduling timer with frequency " + frequency);
        
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                fetchImage();
            }
        }, 0, frequency);
    }

    private void fetchImage() {
        try {
            Bitmap latestImage = getLatestRadarImage();
            dataAdapter.insertImage(latestImage);
        }
        catch(IOException ex) {
            String message = "could not fetch image";
            Log.e(TAG, message, ex);
        }
    }

    public static Bitmap getImageForDisplay() {
        try {
            return getLatestRadarImage();
        }
        catch(IOException ex) {
            String message = "could not load image";
            Log.e(TAG, message, ex);

            return null;
        }
    }
    
    private static Bitmap getLatestRadarImage() throws IOException {
        Log.d(TAG, "fetching radar image");
        
        Bitmap latestImage = null;
        latestImage = ImageUtils.getImage(RADAR_URL,
                CONNECT_TIMEOUT, READ_TIMEOUT);

        if(latestImage != null) {
            latestImage = Bitmap.createBitmap(latestImage, 0, 0,
                    RADAR_WIDTH, latestImage.getHeight());
        }
        return latestImage;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            try {
                timer.cancel();
            } catch (Exception ignored) {
            }
        }
        if (dataAdapter != null) {
            dataAdapter.close();
        }
    }
}
