package com.example.sinf.simplescanner.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.sinf.simplescanner.R;
import com.example.sinf.simplescanner.util.Utils;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContinuousCaptureActivity extends AppCompatActivity {
    private static final String TAG = ContinuousCaptureActivity.class.getSimpleName();
    private CompoundBarcodeView barcodeView;
    private boolean isPaused = false;

    String periodID;
    public static String filename = "frequencia";
    String scanResult = null;
    List<String> scanned;

    TextView textView;

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                barcodeView.setStatusText(result.getText());
                scanResult = result.getText();
                if (!scanned.contains(scanResult)) {
                    scanned.add(scanResult);
                    Utils.appendToFile(scanResult + ',', filename + periodID, ContinuousCaptureActivity.this);
                    textView = (TextView) findViewById(R.id.text_view);
                    textView.setText(Integer.toString(scanned.size()));
                    //new RegisterUser().execute();
                }
            }
            //Added preview of scanned barcode
            //ImageView imageView = (ImageView) findViewById(R.id.barcodePreview);
            //imageView.setImageBitmap(result.getBitmapWithResultPoints(Color.YELLOW));
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        periodID = getIntent().getStringExtra("periodID");

        setContentView(R.layout.activity_continuous_capture);
        setupToolbar();
        String fileContent = Utils.readFromFile(filename + periodID, ContinuousCaptureActivity.this);
        if (!fileContent.isEmpty()) {
            scanned = new ArrayList<String>(Arrays.asList(Utils.readFromFile(filename + periodID, ContinuousCaptureActivity.this).split("\\s*,\\s*")));
        } else {
            scanned = new ArrayList<String>();
        }

        textView = (TextView) findViewById(R.id.text_view);
        textView.setText(Integer.toString(scanned.size()));

        barcodeView = (CompoundBarcodeView) findViewById(R.id.barcode_scanner);
        barcodeView.setStatusText(getString(R.string.permission_camera_granted));
        barcodeView.decodeContinuous(callback);

        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Show menu icon
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
            //ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
        }
        return super.onOptionsItemSelected(item);
    }

    public void switchStates(View view) {

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        if (isPaused) {
            resume(view);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_white_48dp, getApplicationContext().getTheme()));
            } else {
                fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_white_48dp));
            }
            isPaused = false;
        } else {
            pause(view);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_white_48dp, getApplicationContext().getTheme()));
            } else {
                fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_white_48dp));
            }
            isPaused = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        barcodeView.pause();
    }

    public void pause(View view) {
        barcodeView.pause();
    }

    public void resume(View view) {
        barcodeView.resume();
    }

    public void triggerScan(View view) {
        barcodeView.decodeSingle(callback);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    private class RegisterUser extends AsyncTask<String, String, String> {

        HttpURLConnection urlConnection;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... args) {

            // StringBuilder result = new StringBuilder();
            // SharedPreferences preferences = getSharedPreferences(SettingsActivity.settings, MODE_PRIVATE);
            // String domain = preferences.getString(SettingsActivity.serverDomain, SettingsActivity.defaultServerDomain);

            // try {
            //     URL url = new URL(domain + SettingsActivity.getRegisterUrl(periodID, scanResult));
            //     urlConnection = (HttpURLConnection) url.openConnection();
            //     InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            //     BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            //     String line;
            //     while ((line = reader.readLine()) != null) {
            //         result.append(line);
            //     }

            //     return result.toString();

            // } catch (Exception e) {
            //     e.printStackTrace();
            // } finally {
            //     urlConnection.disconnect();
            // }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            //Toast.makeText(ContinuousCaptureActivity.this, result, Toast.LENGTH_SHORT).show();

        }
    }
}
