package com.spruds.raincheck.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageUtils {
    private static final String TAG = ImageUtils.class.getSimpleName();

    public static Bitmap getImage(String url, int connectTimeout, int readTimeout)
            throws IOException {

        URL fileUrl = null;
        try {
            fileUrl = new URL(url);
        } catch (MalformedURLException ex) {
            String message = "invalid url";
            throw new RuntimeException(message, ex);
        }

        Bitmap image = null;
        
        HttpURLConnection conn = (HttpURLConnection) fileUrl.openConnection();
        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);
        conn.connect();

        InputStream is = conn.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(is, 8012);
        image = BitmapFactory.decodeStream(bis);

        is.close();
        bis.close();
        
        return image;
    }
}
