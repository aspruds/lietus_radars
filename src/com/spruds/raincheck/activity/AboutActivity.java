package com.spruds.raincheck.activity;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Toast;
import com.spruds.raincheck.R;
import java.io.IOException;
import java.io.InputStream;

public class AboutActivity extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.about);

        try {
            InputStream fin = getResources().openRawResource(R.raw.about);
                byte[] buffer = new byte[fin.available()];
                fin.read(buffer);
                fin.close();

                WebView vw = (WebView)findViewById(R.id.aboutWebView);
                vw.loadData(new String(buffer), "text/html", "UTF-8");
        } catch (IOException ignored) {
            Toast.makeText(this, getText(R.string.error_about_dialog),
                Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        // Eliminates color banding
        window.setFormat(PixelFormat.RGBA_8888);
    }
}
