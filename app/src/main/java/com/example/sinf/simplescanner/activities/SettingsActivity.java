package com.example.sinf.simplescanner.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.sinf.simplescanner.R;
import com.example.sinf.simplescanner.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class SettingsActivity extends AppCompatActivity {

    public static final String settings = "appSettings";
    public static final String serverDomain = "serverDomain";

    public static final String defaultServerDomain = "http://demo5867062.mockable.io";
    public static final String eventsUrl = "/eventos";
    public static final String periodsUrl = "/periodos/";
    public static final String participantsUrl = "/participantes/";
    public static final String registerUrl = "/";
    static final int notifyId = 1;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    TextView textView;
    RelativeLayout relativeLayout;
    LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupToolbar();

        preferences = getSharedPreferences(settings, MODE_PRIVATE);
        inflater = this.getLayoutInflater();

        setupItemOne();
        setupItemTwo();
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
        }
    }

    public void setupItemOne() {
        String domain = preferences.getString(SettingsActivity.serverDomain, SettingsActivity.defaultServerDomain);
        editor = preferences.edit();

        textView = (TextView) findViewById(R.id.item_one_subtitle);

        if (domain.equals(SettingsActivity.defaultServerDomain)) {
            textView.setText(R.string.dialog_default);
        } else {
            textView.setText(domain);
        }

        relativeLayout = (RelativeLayout) findViewById(R.id.item_one_layout);
        relativeLayout.setOnClickListener(new RelativeLayout.OnClickListener() {
            @Override
            public void onClick(View view) {

                final View viewDialog = inflater.inflate(R.layout.set_domain_dialog, null);

                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle(R.string.domain)
                        .setMessage(R.string.domain_dialog_message)
                        .setView(viewDialog)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                final EditText editText = (EditText) viewDialog.findViewById(R.id.edit_text);
                                String input = editText.getEditableText().toString();
                                editor.putString(SettingsActivity.serverDomain, input);
                                editor.commit();
                                textView.setText(preferences.getString(SettingsActivity.serverDomain, SettingsActivity.defaultServerDomain));
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setNeutralButton(R.string.dialog_default, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                editor.putString(SettingsActivity.serverDomain, SettingsActivity.defaultServerDomain);
                                editor.commit();
                                textView.setText(R.string.dialog_default);
                            }
                        })
                        .show();
            }
        });
    }

    public void setupItemTwo() {
        relativeLayout = (RelativeLayout) findViewById(R.id.item_two_layout);
        relativeLayout.setOnClickListener(new RelativeLayout.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LoadAllEventsCached().execute();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
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

    public static String getEventsUrl() {
        return eventsUrl;
    }

    public static String getPeriodsUrl(String eveID) {
        return periodsUrl + eveID;
    }

    public static String getParticipantsUrl(String eveID, String periodID) {
        return participantsUrl + eveID + "/" + periodID;
    }

    public static String getRegisterUrl(String periodID, String scanResult) {
        return registerUrl + periodID + "/" + scanResult;
    }

    public class LoadAllEventsCached extends AsyncTask<String, String, Boolean> {

        JSONArray json, json2;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(String... args) {

            String res;
            String domain = preferences.getString(SettingsActivity.serverDomain, SettingsActivity.defaultServerDomain);
            NotificationManager notificationManager = (NotificationManager) getSystemService(SettingsActivity.NOTIFICATION_SERVICE);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(SettingsActivity.this)
                    .setSmallIcon(R.drawable.ic_file_download_white_48dp)
                    .setContentTitle(getResources().getString(R.string.settings_save_to_offline_notification_title))
                    .setContentText(getResources().getString(R.string.settings_save_to_offline_notification_text))
                    .setTicker(getResources().getString(R.string.settings_save_to_offline_notification_ticker_text))
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setCategory(Notification.CATEGORY_PROGRESS);

            notificationManager.notify(notifyId, notificationBuilder.build());

            try {
                res = Utils.readFromHttp(domain + getEventsUrl());
                json = new JSONArray(res);
                Log.e("Write to File:", "events");
                Utils.writeToFile(res, "events", SettingsActivity.this);

                notificationBuilder.setProgress(100, 0, false);
                notificationManager.notify(notifyId, notificationBuilder.build());
                for (int i = 0; i < json.length(); i++) {
                    JSONObject j = json.getJSONObject(i);

                    notificationBuilder.setProgress(100, 100 * i / json.length(), false);
                    notificationManager.notify(notifyId, notificationBuilder.build());

                    Log.e("Write to File:", domain + getPeriodsUrl(j.getString(ListPeriodsActivity.TAG_ID)));
                    res = Utils.readFromHttp(domain + getPeriodsUrl(j.getString(ListPeriodsActivity.TAG_ID)));
                    json2 = new JSONArray(res);
                    Log.e("Write to File:", "event"+j.getString(ListEventsActivity.TAG_ID));
                    Utils.writeToFile(res, "event" + j.getString(ListEventsActivity.TAG_ID), SettingsActivity.this);

                    for (int i2 = 0; i2 < json2.length(); i2++) {
                        JSONObject j2 = json2.getJSONObject(i2);

                        Log.e("Write to File:", domain + getParticipantsUrl(j2.getString(ListPeriodsActivity.TAG_EVEID), j2.getString(ListPeriodsActivity.TAG_ID)));
                        res = Utils.readFromHttp(domain + getParticipantsUrl(j2.getString(ListPeriodsActivity.TAG_EVEID), j2.getString(ListPeriodsActivity.TAG_ID)));
                        Log.e("Write to File:", "period"+j2.getString(ListPeriodsActivity.TAG_ID));
                        Utils.writeToFile(res, "period" + j2.getString(ListPeriodsActivity.TAG_ID), SettingsActivity.this);
                    }
                }
                notificationBuilder.setProgress(100, 100, false);
                notificationManager.notify(notifyId, notificationBuilder.build());

                Intent resultIntent = new Intent(SettingsActivity.this, ListEventsActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(SettingsActivity.this, 0, resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                notificationBuilder.setProgress(0, 0, false);
                notificationBuilder.setContentText(getResources().getString(R.string.settings_save_to_offline_notification_final_text))
                    .setTicker(getResources().getString(R.string.settings_save_to_offline_notification_ticker_final_text))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
                notificationManager.notify(notifyId, notificationBuilder.build());
            } catch (Exception e) {
                notificationBuilder.setProgress(0, 0, false);
                notificationBuilder.setContentText(getResources().getString(R.string.settings_save_to_offline_notification_fail));
                notificationBuilder.setTicker(getResources().getString(R.string.settings_save_to_offline_notification_ticker_fail));
                notificationManager.notify(notifyId, notificationBuilder.build());
                Log.e("SettingsA.AsyncTask1", e.toString());
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
        }
    }
}
