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

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

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

public class LogosDetailsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ProgressBar progressBar;
    private RecyclerView.Adapter adapter;
    private List<Products> productsList;
    String logoName;
    private static String ids, traderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logos_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        logoName = getIntent().getExtras().getString("logo");
        getSupportActionBar().setTitle(logoName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        FirebaseInstanceId.getInstance().getToken();
        FirebaseMessaging.getInstance().subscribeToTopic("test");

        productsList = new ArrayList<>();
        progressBar = (ProgressBar) findViewById(R.id.progress_logos);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.logos_products_recycler);
        adapter = new CustomAdapter(this, productsList, this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        Background background = new Background();
        background.execute(logoName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home) {
            finish();
            onBackPressed();
        }

        return true;
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
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        logoName = getIntent().getExtras().getString("logo");
        Intent intent = new Intent(this, LogoProductsDetailsActivity.class);
        intent.putExtra("product_name", productsList.get(i).getProductName());
        intent.putExtra("product_logo", logoName);
        startActivity(intent);
    }

    private class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

        private List<Products> productsList;
        private AdapterView.OnItemClickListener onItemClickListener;
        private Context context;

        CustomAdapter(Context context, List<Products> productsList, AdapterView.OnItemClickListener onItemClickListener) {
            LayoutInflater.from(context);
            this.productsList = productsList;
            this.onItemClickListener = onItemClickListener;
            this.context = context;
        }

        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_products_menu, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CustomAdapter.ViewHolder holder, int position) {
            Products products = productsList.get(position);
            holder.productName.setText(products.getProductName());
            if (productsList.get(position).getCurrency().equals("الدولار الامريكي")) {
                holder.productPrice.setText(String.format("%s $", Integer.toString(products.getProductPrice())));
            } else if(productsList.get(position).getCurrency().equals("الدينار العراقي")) {
                holder.productPrice.setText(String.format("%s IQD", Integer.toString(products.getProductPrice())));
            }
            holder.productImage.setImageBitmap(products.getProImage());
        }

        @Override
        public int getItemCount() {
            return productsList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView productName, productPrice;
            ImageView productImage;
            Button buy;
            private static final String DETAILS_URL = MainActivity.HOST+"/read_details.php";
            private String color, size;

            public ViewHolder(View itemView) {
                super(itemView);
                productName = (TextView) itemView.findViewById(R.id.product_name);
                productImage = (ImageView) itemView.findViewById(R.id.product_image);
                productPrice = (TextView) itemView.findViewById(R.id.product_price);
                buy = (Button) itemView.findViewById(R.id.buy);
                itemView.setOnClickListener(this);
                buy.setOnClickListener(this);
            }

            @Override
            public void onClick(final View view) {
                onItemClickListener.onItemClick(null, view, getLayoutPosition(), getItemId());

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
                                ids = jsonObj.getString("id");
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

                }.execute();

                new AsyncTask<String, Void, Void>() {
                    final String EX_URL = MainActivity.HOST+"/extract.php";
                    @Override
                    protected Void doInBackground(String... params) {

                        try {
                            URL url = new URL(EX_URL);
                            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                            httpURLConnection.setRequestMethod("POST");
                            httpURLConnection.setDoOutput(true);
                            OutputStream out = httpURLConnection.getOutputStream();
                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
                            Log.d("id", ids);
                            String data = URLEncoder.encode("id", "UTF-8")+"="+URLEncoder.encode(ids, "UTF-8");

                            writer.write(data);
                            writer.flush();
                            writer.close();
                            out.close();

                            InputStream in = httpURLConnection.getInputStream();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                            StringBuilder stringBuilder = new StringBuilder();
                            String line;

                            while ((line = reader.readLine()) != null) {
                                stringBuilder.append(line);
                            }

                            String json_string = stringBuilder.toString().trim();
                            JSONObject object = new JSONObject(json_string);
                            JSONArray jsonArray = object.getJSONArray("server_response");
                            int i = 0;

                            while (i < jsonArray.length()) {
                                JSONObject json = jsonArray.getJSONObject(i);
                                i++;
                                traderName = json.getString("trader");
                                Log.d("t", traderName);
                            }

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

    class Background extends AsyncTask<String, Products, Void> {

        private static final String READ_URL = MainActivity.HOST+"/logo_products.php";

        @Override
        protected void onPreExecute() {
            setProgressBar();
        }

        @Override
        protected Void doInBackground(String... params) {
            String logo = params[0];
            try {
                URL url = new URL(READ_URL);
                HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection.setRequestMethod("POST");
                httpConnection.setDoOutput(true);
                OutputStream outStream = httpConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outStream));

                String data = URLEncoder.encode("logo", "UTF-8")+"="+URLEncoder.encode(logo, "UTF-8");
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outStream.close();

                InputStream inputStream = httpConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }

                String json_string = stringBuilder.toString().trim();
                JSONObject object = new JSONObject(json_string);
                JSONArray jsonArray = object.getJSONArray("server_response");
                int i = 0;

                while (i < jsonArray.length()) {
                    JSONObject jsonObj = jsonArray.getJSONObject(i);
                    i++;
                    String name = jsonObj.getString("name");
                    Bitmap image = stringToBitMap(jsonObj.getString("image"));
                    String price = jsonObj.getString("price");
                    String currency = jsonObj.getString("currency");
                    Products products = new Products(name, image, price, currency, "0");
                    publishProgress(products);
                }

                httpConnection.disconnect();
                inputStream.close();

            } catch (IOException e) {
                Log.d("IOException", e.getMessage());
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Products... values) {
            productsList.add(0, values[0]);
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
