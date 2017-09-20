package com.alameen.wael.hp.market;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ProductsFromTraderActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    String traderName;
    private ProgressBar progressBar;
    private RecyclerView.Adapter adapter;
    private List<Products> productsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products_from_trader);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        traderName = getIntent().getExtras().getString("trader_name");
        getSupportActionBar().setTitle(traderName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        productsList = new ArrayList<>();
        progressBar = (ProgressBar) findViewById(R.id.load_progress_14);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.watches_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new MenuAdapter(this, productsList, this);
        recyclerView.setAdapter(adapter);

        BackgroundTask task = new BackgroundTask(this);
        task.execute(traderName);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(this, TradersDetailsActivity.class);
        intent.putExtra("trader", traderName);
        intent.putExtra("product_name", productsList.get(i).getProductName());
        startActivity(intent);
    }

    public void setProgressBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progressBar.setProgress(50, true);
        }
    }

    public void endProgress() {
        progressBar.setVisibility(View.GONE);
    }

    private class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {

        private List<Products> productsList;
        private AdapterView.OnItemClickListener onItemClick;
        private Context context;

        MenuAdapter(Context context, List<Products> productsList, AdapterView.OnItemClickListener onItemClick) {
            LayoutInflater.from(context);
            this.productsList = productsList;
            this.onItemClick = onItemClick;
            this.context = context;
        }

        @Override
        public MenuAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_products_menu, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MenuAdapter.ViewHolder holder, int position) {
            Products products = productsList.get(position);
            holder.proImage.setImageBitmap(products.getProImage());
            holder.proName.setText(products.getProductName());
            holder.proPrice.setText(Integer.toString(products.getProductPrice()) + " $");
        }

        @Override
        public int getItemCount() {
            return productsList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private static final String DETAILS_URL = MainActivity.HOST+"/read_details.php";
            private String color, size;
            ImageView proImage;
            TextView proName, proPrice;
            Button buy;

            ViewHolder(View itemView) {
                super(itemView);

                proImage = (ImageView) itemView.findViewById(R.id.product_image);
                proName = (TextView) itemView.findViewById(R.id.product_name);
                proPrice = (TextView) itemView.findViewById(R.id.product_price);
                buy = (Button) itemView.findViewById(R.id.buy);
                itemView.setOnClickListener(this);
                buy.setOnClickListener(this);
            }

            @Override
            public void onClick(final View view) {
                onItemClick.onItemClick(null, view, getLayoutPosition(), getItemId());

                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        try {
                            URL url = new URL(DETAILS_URL);
                            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                            httpURLConnection.setRequestMethod("POST");
                            httpURLConnection.setDoOutput(true);
                            OutputStream outputStream = httpURLConnection.getOutputStream();
                            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

                            String data = URLEncoder.encode("product_name", "UTF-8")+"="+URLEncoder.encode(productsList.get(getLayoutPosition()).
                                    getProductName(), "UTF-8")+"&"
                                    + URLEncoder.encode("likes_counter", "UTF-8")+"="+URLEncoder.encode("2", "UTF-8");
                            bufferedWriter.write(data);
                            bufferedWriter.flush();
                            bufferedWriter.close();
                            outputStream.close();

                            InputStream inputStream = httpURLConnection.getInputStream();
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
                                color = jsonObj.getString("colors");
                                size = jsonObj.getString("sizes");
                            }

                            httpURLConnection.disconnect();
                            inputStream.close();
                            buffer.close();

                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        if(view.getId() == R.id.buy) {
                            Intent intent = new Intent(context, RequestActivity.class);
                            intent.putExtra("name", productsList.get(getLayoutPosition()).getProductName());
                            intent.putExtra("trader", traderName);
                            System.out.println("t in intent "+ traderName);
                            int price = productsList.get(getLayoutPosition()).getProductPrice();
                            intent.putExtra("price", Integer.toString(price));
                            intent.putExtra("currency", productsList.get(getLayoutPosition()).getCurrency());
                            intent.putExtra("colors", color);
                            intent.putExtra("sizes", size);
                            context.startActivity(intent);
                        }
                    }
                }.execute();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home) {
            finish();
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    private class BackgroundTask extends AsyncTask<String, Products, Void> {

        private static final String READ_URL = MainActivity.HOST+"/read_trader2.php";

        BackgroundTask (Context context) {
            LayoutInflater.from(context);
        }

        @Override
        protected Void doInBackground(String... params) {
            String trader_name = params[0];
            //String count = params[1];

            try {
                URL url = new URL(READ_URL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

                String data = URLEncoder.encode("trader_name", "UTF-8")+"="+URLEncoder.encode(trader_name, "UTF-8");
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                InputStream inputStream = httpURLConnection.getInputStream();
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
                    try {
                        if (jsonObj.getString("price").equals("")) {
                            Products products = new Products(jsonObj.getString("name"), stringToBitMap(jsonObj.getString("image")),
                                    "0", jsonObj.getString("currency"), "0");
                            publishProgress(products);
                        } else {
                            Products products = new Products(jsonObj.getString("name"), stringToBitMap(jsonObj.getString("image")),
                                    jsonObj.getString("price"), jsonObj.getString("currency"), "0");
                            publishProgress(products);
                        }

                    } catch (Exception e) {
                        e.getCause();
                        e.printStackTrace();
                    }
                }

                httpURLConnection.disconnect();
                inputStream.close();
                buffer.close();

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Products... values) {
            super.onProgressUpdate(values);

            setProgressBar();
            productsList.add(values[0]);
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
