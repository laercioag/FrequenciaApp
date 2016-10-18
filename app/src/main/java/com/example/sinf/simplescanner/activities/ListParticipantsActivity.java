package com.example.sinf.simplescanner.activities;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.sinf.simplescanner.R;
import com.example.sinf.simplescanner.adapters.ParticipantsRecyclerViewAdapter;
import com.example.sinf.simplescanner.util.DividerItemDecoration;
import com.example.sinf.simplescanner.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ListParticipantsActivity extends AppCompatActivity {

    int notifyId = 3;
    String eveID, periodID, periodName;

    AppBarLayout appBarLayout;
    //    SwipeRefreshLayout swipeRefreshLayout;
//    TabLayout tabLayout;
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    FloatingActionButton floatingActionButton;
    SharedPreferences preferences;
    TextView title;

    ArrayList<HashMap<String, String>> participantsList;

    boolean isRefreshing = false;
    public static final String TAG_NAME = "nome";
    public static final String TAG_ATTENDANCE = "presente";
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 0;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_participants);
        setupToolbar();
//        setupTablayout();

        preferences = getSharedPreferences(SettingsActivity.settings, MODE_PRIVATE);
        participantsList = new ArrayList<HashMap<String, String>>();
        eveID = getIntent().getStringExtra("eveID");
        periodID = getIntent().getStringExtra("periodID");
        periodName = getIntent().getStringExtra("periodName");
        title = (TextView) findViewById(R.id.text_view);
        title.setText(periodName);
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);

        setupRecyclerView();
        setupFloatActionButton();
//        setupSwipeRefreshLayout();
        new LoadAllParticipants().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        appBarLayout.addOnOffsetChangedListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        appBarLayout.removeOnOffsetChangedListener(this);
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Show menu icon
        final ActionBar ab = getSupportActionBar();
        //ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
            ab.setDisplayShowTitleEnabled(false);
        }
    }

//    private void setupTablayout(){
//        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
//        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
//        tabLayout.addTab(tabLayout.newTab().setText("Todos"));
//        tabLayout.addTab(tabLayout.newTab().setText("Presentes"));
//        tabLayout.addTab(tabLayout.newTab().setText("Ausentes"));
//    }

    private void setupRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(divider);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ParticipantsRecyclerViewAdapter(participantsList);
        recyclerView.setAdapter(adapter);
    }

    private void setupFloatActionButton() {
        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startContinuousScan(v);
            }
        });
    }

//    private void setupSwipeRefreshLayout() {
//        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
//        swipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);
//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                if(!isRefreshing) {
//                    isRefreshing = true;
//                    new LoadAllParticipants().execute();
//                } else{
//                    swipeRefreshLayout.setRefreshing(false);
//                }
//            }
//        });
//    }
//
//    @Override
//    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
//        if (i == 0) {
//            swipeRefreshLayout.setEnabled(true);
//        } else {
//            swipeRefreshLayout.setEnabled(false);
//        }
//    }

    private View.OnClickListener snackbarAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new LoadAllParticipants().execute();
        }
    };

    public void startContinuousScan(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            Intent intent = new Intent(this, ContinuousCaptureActivity.class);
            intent.putExtra("periodID", periodID);
            startActivityForResult(intent, 1);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                isRefreshing = true;
                new LoadAllParticipants().execute();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_participants, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    saveAttendanceToExternalStorage();
                } else {
                    // permission not granted, nay!
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    Intent intent = new Intent(this, ContinuousCaptureActivity.class);
                    intent.putExtra("periodID", periodID);
                    startActivityForResult(intent, 1);
                } else {
                    // permission not granted, nay!
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void saveAttendanceToExternalStorage() {
        Utils.writeToExternalFile(Utils.readFromFile(ContinuousCaptureActivity.filename + periodID, ListParticipantsActivity.this), ContinuousCaptureActivity.filename + periodID + ".txt");

        NotificationManager notificationManager = (NotificationManager) getSystemService(SettingsActivity.NOTIFICATION_SERVICE);
        int notifyId = 2;

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ListParticipantsActivity.this)
                .setSmallIcon(R.drawable.ic_file_download_white_48dp)
                .setContentTitle(getResources().getString(R.string.downloaded_frequency_notification_title))
                .setContentText(getResources().getString(R.string.downloaded_frequency_notification_text))
                .setTicker(getResources().getString(R.string.downloaded_frequency_notification_ticker))
                .setPriority(Notification.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_PROGRESS)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        notificationManager.notify(notifyId, notificationBuilder.build());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.item_resend:
                new ResendFrequency().execute();
                break;
            case R.id.item_clear:
                new AlertDialog.Builder(ListParticipantsActivity.this)
                        .setTitle(R.string.activity_list_participants_delete_dialog_title)
                        .setMessage(R.string.activity_list_participants_delete_dialog_message)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                deleteFile(ContinuousCaptureActivity.filename + periodID);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .show();
                break;
            case R.id.item_download:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                } else {
                    saveAttendanceToExternalStorage();
                }
                break;
            case R.id.item_share:

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                Uri uri = Utils.writeToExternalFile(Utils.readFromFile(ContinuousCaptureActivity.filename + periodID, ListParticipantsActivity.this), ContinuousCaptureActivity.filename + periodID + ".txt");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.setType("file/txt");
                startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.activity_list_participants_menu_share)));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class LoadAllParticipants extends AsyncTask<String, String, Boolean> {

        HttpURLConnection urlConnection;
        JSONArray json;

        @Override
        protected void onPreExecute() {
            if (!isRefreshing) {
                findViewById(R.id.loading_spinner).setVisibility(View.VISIBLE);
                isRefreshing = true;
            }
        }

        @Override
        protected Boolean doInBackground(String... args) {

            StringBuilder result = new StringBuilder();
            String domain = preferences.getString(SettingsActivity.serverDomain, SettingsActivity.defaultServerDomain);

            try {
                URL url = new URL(domain + SettingsActivity.getParticipantsUrl(eveID, periodID));

                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            } catch (Exception e) {
                Log.e("LParticipantA.AsyncTask", e.toString());
                //return false;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            try {
                json = new JSONArray(result.toString());
                Utils.writeToFile(result.toString(), "period" + periodID, ListParticipantsActivity.this);
                //return true;
            } catch (JSONException e) {
                Log.e("LParticipantA.AsyncTask", e.toString());
                //return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {

//            swipeRefreshLayout.setRefreshing(false);
            isRefreshing = false;
            findViewById(R.id.loading_spinner).setVisibility(View.INVISIBLE);
            participantsList.clear();

            if (result) {
                try {
                    json = new JSONArray(Utils.readFromFile("period" + periodID, ListParticipantsActivity.this));
                    for (int i = 0; i < json.length(); i++) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        JSONObject j = json.getJSONObject(i);

                        map.put(TAG_NAME, Utils.capitalizeWords(j.getString(TAG_NAME)));
                        map.put(TAG_ATTENDANCE, j.getString(TAG_ATTENDANCE));
                        participantsList.add(map);
                    }
                } catch (JSONException e) {
                    participantsList.clear();
                    adapter = new ParticipantsRecyclerViewAdapter(participantsList);
                    recyclerView.setAdapter(adapter);
                    Log.e("LParticipantA.AsyncTask", e.toString());
                    Snackbar.make(findViewById(R.id.root_layout), getString(R.string.snackbar_server_response_fail),
                            Snackbar.LENGTH_SHORT)
                            .setAction(getString(R.string.snackbar_no_connection_refresh), snackbarAction)
                            .show();
                }

                adapter = new ParticipantsRecyclerViewAdapter(participantsList);
                recyclerView.setAdapter(adapter);


            } else {
                adapter = new ParticipantsRecyclerViewAdapter(participantsList);
                recyclerView.setAdapter(adapter);
                Snackbar.make(findViewById(R.id.root_layout), getString(R.string.snackbar_no_connection),
                        Snackbar.LENGTH_SHORT)
                        .setAction(getString(R.string.snackbar_no_connection_refresh), snackbarAction)
                        .show();

            }
        }

    }

    public class ResendFrequency extends AsyncTask<String, String, Boolean> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(String... args) {

            String domain = preferences.getString(SettingsActivity.serverDomain, SettingsActivity.defaultServerDomain);
            NotificationManager notificationManager = (NotificationManager) getSystemService(SettingsActivity.NOTIFICATION_SERVICE);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ListParticipantsActivity.this)
                    .setSmallIcon(R.drawable.ic_file_download_white_48dp)
                    .setContentTitle(getResources().getString(R.string.resend_frequency_notification_title))
                    .setContentText(getResources().getString(R.string.resend_frequency_notification_text))
                    .setTicker(getResources().getString(R.string.resend_frequency_notification_ticker_text))
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setCategory(Notification.CATEGORY_PROGRESS);

            notificationManager.notify(notifyId, notificationBuilder.build());

            try {
                List<String> scanned = new ArrayList<String>(Arrays.asList(Utils.readFromFile(ContinuousCaptureActivity.filename + periodID, ListParticipantsActivity.this).split("\\s*,\\s*")));
                notificationBuilder.setProgress(100, 0, false);
                notificationManager.notify(notifyId, notificationBuilder.build());
                for (int i = 0; i < scanned.size(); i++) {
                    Utils.readFromHttp(domain + SettingsActivity.getRegisterUrl(periodID, scanned.get(i)));
                    notificationBuilder.setProgress(100, 100 * i / scanned.size(), false);
                    notificationManager.notify(notifyId, notificationBuilder.build());
                }
                notificationBuilder.setProgress(100, 100, false);
                notificationManager.notify(notifyId, notificationBuilder.build());
                notificationBuilder.setProgress(0, 0, false);
                notificationBuilder.setContentText(getResources().getString(R.string.resend_frequency_notification_final_text));
                notificationBuilder.setTicker(getResources().getString(R.string.resend_frequency_notification_ticker_final_text));
                notificationManager.notify(notifyId, notificationBuilder.build());
            } catch (Exception e) {
                notificationBuilder.setProgress(0, 0, false);
                notificationBuilder.setContentText(getResources().getString(R.string.resend_frequency_notification_fail));
                notificationBuilder.setTicker(getResources().getString(R.string.resend_frequency_notification_ticker_fail));
                notificationManager.notify(notifyId, notificationBuilder.build());
                Log.e("LParticipantA.AsyncTask", e.toString());
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
        }
    }

}
