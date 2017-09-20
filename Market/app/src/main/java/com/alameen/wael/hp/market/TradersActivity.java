package com.alameen.wael.hp.market;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
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

public class TradersActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, AdapterView.OnItemClickListener {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<Traders> tradersList;
    private ProgressBar progress;
    SearchView searchView;
    private String traderName;
    private static boolean pointer = false;
    private static int press = 1;
    private List<Traders> filtered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traders);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.traders);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);

        progress = (ProgressBar) findViewById(R.id.load_traders);
        tradersList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.search_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2, GridLayout.VERTICAL, false));
        adapter = new SearchAdapter(this, tradersList, this);
        recyclerView.setAdapter(adapter);

        Background background = new Background(this);
        background.execute();
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
        searchView.setQueryHint(getString(R.string.searching));

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                setItemsVisibility(menu, menuItem, true);
                getSupportActionBar().setTitle(R.string.app_name);
                MenuItemCompat.collapseActionView(menuItem);
                recyclerView.setHasFixedSize(true);
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

    public void setProgressBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progress.setProgress(50, true);
        }
    }

    public void endProgress() {
        progress.setVisibility(View.GONE);
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
                recyclerView.setLayoutManager(new GridLayoutManager(TradersActivity.this, 2));
                adapter = new SearchAdapter(TradersActivity.this, tradersList, this);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setHomeButtonEnabled(false);
            }
        }

        return true;
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
            filtered = filter(tradersList, query);
            Log.d("query", query);
            adapter = new SearchAdapter(this, filtered, this);
            recyclerView.setHasFixedSize(true);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(TradersActivity.this, 2, GridLayout.VERTICAL, false);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
        return true;
    }

    private List<Traders> filter(List<Traders> traderList, String query) {
        query = query.toLowerCase();
        List<Traders> filteredList = new ArrayList<>();
        if (traderList != null && traderList.size() > 0) {
            for (Traders trader : traderList) {
                if (trader.getTraderName().toLowerCase().contains(query)) {
                    filteredList.add(trader);
                }
            }
        }
        return filteredList;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        try {
            Intent intent = new Intent(this, ProductsFromTraderActivity.class);
            if(pointer) {
                intent.putExtra("trader_name", filtered.get(i).getTraderName());
                pointer = false;
            } else {
                intent.putExtra("trader_name", tradersList.get(i).getTraderName());
            }
            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "خطأ في الاتصال", Toast.LENGTH_SHORT).show();
        }
    }

    private class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

        List<Traders> tradersList;
        private AdapterView.OnItemClickListener onItemClickListener;

        SearchAdapter(Context context, List<Traders> tradersList, AdapterView.OnItemClickListener onItemClickListener) {
            LayoutInflater.from(context);
            this.tradersList = tradersList;
            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public SearchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_layout, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(SearchAdapter.ViewHolder holder, int position) {
            holder.traderImage.setImageBitmap(tradersList.get(position).getTraderImage());
            holder.traderName.setText(tradersList.get(position).getTraderName());
        }

        @Override
        public int getItemCount() {
            return tradersList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            CircleImageView traderImage;
            TextView traderName;

            ViewHolder(View itemView) {
                super(itemView);

                traderImage = (CircleImageView) itemView.findViewById(R.id.trader_image);
                traderName = (TextView) itemView.findViewById(R.id.trader_name);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                onItemClickListener.onItemClick(null, view, getLayoutPosition(), getItemId());
            }
        }
    }

    class Background extends AsyncTask<Void, Traders, Void> {

        private static final String URL = MainActivity.HOST+"/traders.php";

        Background(Context context) {
            LayoutInflater.from(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                java.net.URL url = new URL(URL);
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

                    Traders traders = new Traders(jsonObj.getString("name"), stringToBitMap(jsonObj.getString("image")));
                    traderName = jsonObj.getString("name");
                    publishProgress(traders);
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
        protected void onProgressUpdate(Traders... values) {
            super.onProgressUpdate(values);

            setProgressBar();
            tradersList.add(values[0]);
            adapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

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
