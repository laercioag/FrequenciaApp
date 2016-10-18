package com.example.sinf.simplescanner.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.sinf.simplescanner.R;
import com.example.sinf.simplescanner.adapters.PeriodsRecyclerViewAdapter;
import com.example.sinf.simplescanner.util.DividerItemDecoration;
import com.example.sinf.simplescanner.util.RecyclerViewItemClickListener;
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
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class ListPeriodsActivity extends AppCompatActivity {

    String eveID, eveName;

    AppBarLayout appBarLayout;
    //    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    TextView title;

    ArrayList<HashMap<String, String>> periodsList;

    boolean isRefreshing = false;

    public static final String TAG_ID = "id";
    public static final String TAG_NAME = "nome";
    public static final String TAG_EVEID = "eveId";
    public static final String TAG_DATA = "data";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_periods);
        setupToolbar();

        periodsList = new ArrayList<HashMap<String, String>>();
        eveID = getIntent().getStringExtra("eveID");
        eveName = getIntent().getStringExtra("eveName");
        title = (TextView) findViewById(R.id.text_view);
        title.setText(eveName);
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);

        setupRecyclerView();
//        setupSwipeRefreshLayout();
        new LoadAllPeriods().execute();
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

    private void setupRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(divider);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PeriodsRecyclerViewAdapter(periodsList);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(
                new RecyclerViewItemClickListener(ListPeriodsActivity.this, new RecyclerViewItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent intent = new Intent(ListPeriodsActivity.this, ListParticipantsActivity.class);
                        intent.putExtra("eveID", eveID);
                        intent.putExtra("periodID", periodsList.get(+position).get(TAG_ID));
                        intent.putExtra("periodName", periodsList.get(+position).get(TAG_NAME));
                        startActivity(intent);
                    }
                })
        );
    }

//    private void setupSwipeRefreshLayout() {
//        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
//        swipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);
//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                if(!isRefreshing) {
//                    isRefreshing = true;
//                    new LoadAllPeriods().execute();
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
            new LoadAllPeriods().execute();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_periods, menu);
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

    private class LoadAllPeriods extends AsyncTask<String, String, Boolean> {

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
            SharedPreferences preferences = getSharedPreferences(SettingsActivity.settings, MODE_PRIVATE);
            String domain = preferences.getString(SettingsActivity.serverDomain, SettingsActivity.defaultServerDomain);

            try {
                URL url = new URL(domain + SettingsActivity.getPeriodsUrl(eveID));
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            } catch (Exception e) {
                Log.e("ListPeriodsA.AsyncTask", e.toString());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            try {
                json = new JSONArray(result.toString());
                Utils.writeToFile(result.toString(), "event" + eveID, ListPeriodsActivity.this);
            } catch (JSONException e) {
                Log.e("ListPeriodsA.AsyncTask", e.toString());
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {

//            swipeRefreshLayout.setRefreshing(false);
            isRefreshing = false;
            findViewById(R.id.loading_spinner).setVisibility(View.INVISIBLE);
            periodsList.clear();

            if (result) {
                try {
                    json = new JSONArray(Utils.readFromFile("event" + eveID, ListPeriodsActivity.this));
                    for (int i = 0; i < json.length(); i++) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        JSONObject j = json.getJSONObject(i);

                        map.put(TAG_ID, j.getString(TAG_ID));
                        map.put(TAG_NAME, Utils.capitalizeWords(j.getString(TAG_NAME)));
                        map.put(TAG_EVEID, j.getString(TAG_EVEID));
                        map.put(TAG_DATA, new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(Long.parseLong(j.getString(TAG_DATA)))));
                        periodsList.add(map);
                    }
                } catch (JSONException e) {
                    periodsList.clear();
                    adapter = new PeriodsRecyclerViewAdapter(periodsList);
                    recyclerView.setAdapter(adapter);
                    Log.e("ListPeriodsA.AsyncTask", e.toString());
                    Snackbar.make(findViewById(R.id.root_layout), getString(R.string.snackbar_server_response_fail),
                            Snackbar.LENGTH_SHORT)
                            .setAction(getString(R.string.snackbar_no_connection_refresh), snackbarAction)
                            .show();
                }
                adapter = new PeriodsRecyclerViewAdapter(periodsList);
                recyclerView.setAdapter(adapter);


            } else {
                adapter = new PeriodsRecyclerViewAdapter(periodsList);
                recyclerView.setAdapter(adapter);
                Snackbar.make(findViewById(R.id.root_layout), getString(R.string.snackbar_no_connection),
                        Snackbar.LENGTH_SHORT)
                        .setAction(getString(R.string.snackbar_no_connection_refresh), snackbarAction)
                        .show();

            }
        }
    }
}
