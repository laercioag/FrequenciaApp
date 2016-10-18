package com.example.sinf.simplescanner.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.sinf.simplescanner.R;
import com.example.sinf.simplescanner.adapters.EventsRecyclerViewAdapter;
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
import java.util.ArrayList;
import java.util.HashMap;

public class ListEventsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {


    SharedPreferences preferences;
    ActionBarDrawerToggle actionBarDrawerToggle;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    RecyclerView recyclerView;
    EventsRecyclerViewAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    SwipeRefreshLayout swipeRefreshLayout;
    SearchView searchView;

    ArrayList<HashMap<String, String>> eventsList;

    boolean isRefreshing = false;

    public static final String TAG_ID = "id";
    public static final String TAG_NAME = "nome";

    SharedPreferences.OnSharedPreferenceChangeListener spChanged = new
            SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                                      String key) {
                    if (key.equals(SettingsActivity.serverDomain)) {
                        new LoadAllEvents().execute();
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_events);
        setupToolbar();
        setupDrawer();
        setupNavigationView();

        preferences = getSharedPreferences(SettingsActivity.settings, MODE_PRIVATE);
        eventsList = new ArrayList<HashMap<String, String>>();

        setupRecyclerView();
        setupSwipeRefreshLayout();
        new LoadAllEvents().execute();
//        new LoadAllEventsCached().execute();
        preferences.registerOnSharedPreferenceChangeListener(spChanged);
    }

    private void setupDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getActionBar().setTitle(mTitle);
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getActionBar().setTitle(mDrawerTitle);
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        actionBarDrawerToggle.syncState();
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
    }

    private void setupNavigationView() {
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_item_1:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        Intent intent = new Intent(ListEventsActivity.this, SettingsActivity.class);
                        startActivity(intent);
                        return true;
                }
                return true;
            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Show menu icon
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.app_name);
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(divider);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new EventsRecyclerViewAdapter(eventsList);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(
                new RecyclerViewItemClickListener(ListEventsActivity.this, new RecyclerViewItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent intent = new Intent(ListEventsActivity.this, ListPeriodsActivity.class);
                        intent.putExtra("eveID", eventsList.get(+position).get(TAG_ID));
                        intent.putExtra("eveName", eventsList.get(+position).get(TAG_NAME));
                        startActivity(intent);
                    }
                })
        );
    }

    private void setupSwipeRefreshLayout() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isRefreshing) {
                    isRefreshing = true;
                    new LoadAllEvents().execute();
//                    new LoadAllEventsCached().execute();
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    private View.OnClickListener snackbarAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new LoadAllEvents().execute();
        }
    };

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

//        For future use
//        if (!searchView.isIconified()) {
//            searchView.setIconified(true);
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list_events, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        final ArrayList<HashMap<String, String>> filteredEventsList = filter(eventsList, query);
        Log.i("Unfiltered List Count", Integer.toString(eventsList.size()));
        Log.i("Filtered List Count", Integer.toString(filteredEventsList.size()));
        adapter.animateTo(filteredEventsList);
        recyclerView.scrollToPosition(0);
        return true;
    }

    private ArrayList<HashMap<String, String>> filter(ArrayList<HashMap<String, String>> list, String query) {
        query = query.toLowerCase();

        final ArrayList<HashMap<String, String>> filteredEventslList = new ArrayList<>();
        for (HashMap<String, String> item : list) {
            final String text = item.get(ListEventsActivity.TAG_NAME).toLowerCase();
            if (text.contains(query)) {
                filteredEventslList.add(item);
            }
        }
        return filteredEventslList;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class LoadAllEvents extends AsyncTask<String, String, Boolean> {

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
                URL url = new URL(domain + SettingsActivity.getEventsUrl());
                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } catch (Exception e) {
                Log.e("ListEventsA.AsyncTask1", e.toString());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            try {
                json = new JSONArray(result.toString());
                Utils.writeToFile(result.toString(), "events", ListEventsActivity.this);
            } catch (JSONException e) {
                Log.e("ListEventsA.AsyncTask2", e.toString());
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            swipeRefreshLayout.setRefreshing(false);
            isRefreshing = false;
            findViewById(R.id.loading_spinner).setVisibility(View.INVISIBLE);
            eventsList.clear();

            if (result) {
                try {
                    json = new JSONArray(Utils.readFromFile("events", ListEventsActivity.this));
                    for (int i = 0; i < json.length(); i++) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        JSONObject j = json.getJSONObject(i);

                        map.put(TAG_ID, j.getString(TAG_ID));
                        map.put(TAG_NAME, Utils.capitalizeWords(j.getString(TAG_NAME)));
                        eventsList.add(map);
                    }
                } catch (JSONException e) {
                    eventsList.clear();
                    adapter = new EventsRecyclerViewAdapter(eventsList);
                    recyclerView.setAdapter(adapter);
                    Log.e("ListEventsA.AsyncTask3", e.toString());
                    Snackbar.make(findViewById(R.id.root_layout), getString(R.string.snackbar_server_response_fail),
                            Snackbar.LENGTH_SHORT)
                            .setAction(getString(R.string.snackbar_no_connection_refresh), snackbarAction)
                            .show();
                }
                adapter = new EventsRecyclerViewAdapter(eventsList);
                recyclerView.setAdapter(adapter);
            } else {
                adapter = new EventsRecyclerViewAdapter(eventsList);
                recyclerView.setAdapter(adapter);
                Snackbar.make(findViewById(R.id.root_layout), getString(R.string.snackbar_no_connection),
                        Snackbar.LENGTH_SHORT)
                        .setAction(getString(R.string.snackbar_no_connection_refresh), snackbarAction)
                        .show();

            }
        }
    }

}
