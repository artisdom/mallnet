package com.alameen.wael.hp.market;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEventSource;
import android.widget.AdapterView;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class LogosActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, SearchView.OnQueryTextListener {

    private ProgressBar progressBar;
    private RecyclerView.Adapter adapter;
    private List<Logos> logosList;
    private RecyclerView recyclerView;
    String logoName;
    List<Logos> filtered;
    SearchView searchView;
    private static boolean pointer = false;
    private static int press = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.logos);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);

        logosList = new ArrayList<>();
        progressBar = (ProgressBar) findViewById(R.id.load_progress_logos);

        recyclerView = (RecyclerView) findViewById(R.id.logos_recycler);
        recyclerView.setHasFixedSize(true);
        adapter = new CustomAdapter(this, logosList, this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        Background background = new Background(this);
        background.execute();
    }

    public void setProgressBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progressBar.setProgress(50, true);
        }
    }

    public void endProgress() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.search_trader_menu, menu);
        final MenuItem menuItem = menu.findItem(R.id.action_search);
        searchView = new SearchView(this);
        MenuItemCompat.setActionView(menuItem, searchView);
        int id = android.support.v7.appcompat.R.id.search_button;
        ImageView image = (ImageView) searchView.findViewById(id);
        image.setImageResource(R.drawable.ic_search_24dp);
        searchView.setOnQueryTextListener(this);
        searchView.setQuery(searchView.getQuery(), true);
        searchView.setQueryHint(getString(R.string.search_logos));

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                setItemsVisibility(menu, menuItem, true);
                getSupportActionBar().setTitle(R.string.logos);
                MenuItemCompat.collapseActionView(menuItem);
                return true;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setItemsVisibility(menu, menuItem, false);
                getSupportActionBar().setTitle(null);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void setItemsVisibility(Menu menu, MenuItem search, boolean visible) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);

            if(item != search) {
                item.setVisible(visible);
            }
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        if(TextUtils.isEmpty(query)) {
            adapter.notifyDataSetChanged();
        } else {
            pointer = true;
            filtered = filter(logosList, query);
            Log.d("query", query);
            adapter = new CustomAdapter(this, filtered, this);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(LogosActivity.this, 2, GridLayout.VERTICAL, false);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
        return true;
    }

    private List<Logos> filter(List<Logos> logosList, String query) {
        query = query.toLowerCase();
        List<Logos> filteredList = new ArrayList<>();
        if (logosList != null && logosList.size() > 0) {
            for (Logos logo : logosList) {
                if (logo.getLogoName().toLowerCase().contains(query)) {
                    filteredList.add(logo);
                }
            }
        }
        return filteredList;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home) {
            if (press == 1) {
                press++;
                searchView.onActionViewCollapsed();

            } else if (press == 2) {
                press = 1;
                getSupportActionBar().setTitle(R.string.logos);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new GridLayoutManager(LogosActivity.this, 2));
                adapter = new CustomAdapter(LogosActivity.this, logosList, this);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setHomeButtonEnabled(false);
            }
        }

        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        try {
            Intent intent = new Intent(this, LogosDetailsActivity.class);
            if(pointer) {
                intent.putExtra("logo", filtered.get(i).getLogoName());
                pointer = false;
            } else {
                intent.putExtra("logo", logosList.get(i).getLogoName());
            }
            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "خطأ في الاتصال", Toast.LENGTH_SHORT).show();
        }

    }

    private class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

        private List<Logos> logosList;
        private AdapterView.OnItemClickListener itemClickListener;
        int pos = 0;

        CustomAdapter(Context context, List<Logos> logosList, AdapterView.OnItemClickListener itemClickListener) {
            LayoutInflater.from(context);
            this.logosList = logosList;
            this.itemClickListener = itemClickListener;
        }

        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_layout, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(CustomAdapter.ViewHolder holder, int position) {
            Logos logos = logosList.get(position);
            holder.name.setText(logos.getLogoName());
            holder.image.setImageBitmap(logos.getLogoImage());
        }

        @Override
        public int getItemCount() {
            return logosList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            CircleImageView image;
            TextView name;

            public ViewHolder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.trader_name);
                image = (CircleImageView) itemView.findViewById(R.id.trader_image);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                itemClickListener.onItemClick(null, view, getLayoutPosition(), getItemId());
            }
        }
    }

    class Logos {
        private String logoName;
        private Bitmap logoImage;

        Logos(String name, Bitmap image) {
            setLogoName(name);
            setLogoImage(image);
        }

        Bitmap getLogoImage() {
            return logoImage;
        }

        void setLogoImage(Bitmap logoImage) {
            this.logoImage = logoImage;
        }

        String getLogoName() {
            return logoName;
        }

        void setLogoName(String logoName) {
            this.logoName = logoName;
        }
    }

    class Background extends AsyncTask<Void, Logos, Void> {

        private static final String LOGOS_URL = MainActivity.HOST+"/read_logos.php";

        Background(Context context) {
            LayoutInflater.from(context);
        }

        @Override
        protected void onPreExecute() {
            setProgressBar();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                java.net.URL url = new URL(LOGOS_URL);
                HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection.setChunkedStreamingMode(1024);
                InputStream inputStream = httpConnection.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = buffer.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }

                String json_string = stringBuilder.toString().trim();
                JSONObject object = new JSONObject(json_string);
                JSONArray jsonArray = object.getJSONArray("server_response");
                int i = 0;

                while (i < jsonArray.length()) {
                    JSONObject jsonObj = jsonArray.getJSONObject(i);
                    i++;
                    logoName = jsonObj.getString("logo");
                    Bitmap logoImage = stringToBitMap(jsonObj.getString("image"));
                    Logos logos = new Logos(logoName, logoImage);
                    publishProgress(logos);
                }

                httpConnection.disconnect();
                inputStream.close();
                buffer.close();

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Logos... values) {
            logosList.add(0, values[0]);
            adapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            endProgress();
        }

        private Bitmap stringToBitMap(String encodedString){
            byte [] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            ByteArrayInputStream imageStream = new ByteArrayInputStream(encodeByte);
            Bitmap theImage = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            imageStream.reset();
            return theImage;
        }
    }
}
