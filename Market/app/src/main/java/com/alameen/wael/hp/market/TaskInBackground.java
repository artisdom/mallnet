package com.alameen.wael.hp.market;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;

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


class TaskInBackground extends AsyncTask<Void, Products, Void> implements AdapterView.OnItemClickListener {

    Context context;
    private Activity activity;
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    List<Products> productsList;
    private static final String READ_URL = MainActivity.HOST+"/read_all_products.php";
    private String type;
    ProgressBar progressBar;

    TaskInBackground(Context context, String type) {
        this.context = context;
        activity = (Activity) context;
        this.type = type;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        productsList = new ArrayList<>();
        adapter = new ProductsAdapter(context, productsList, this);
        recyclerView = (RecyclerView) activity.findViewById(R.id.watches_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        progressBar = (ProgressBar) activity.findViewById(R.id.load_progress_14);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(context, DetailsActivity.class);
        intent.putExtra("product_name", productsList.get(i).getProductName());
        intent.putExtra("type", type);
        context.startActivity(intent);
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
    protected Void doInBackground(Void... voids) {
        try {
            URL url = new URL(READ_URL);
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoOutput(true);
            OutputStream outputStream = httpConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

            String data = URLEncoder.encode("type", "UTF-8")+"="+URLEncoder.encode(type, "UTF-8");
            bufferedWriter.write(data);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();

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

                Products products = new Products(jsonObj.getString("name"), stringToBitMap(jsonObj.getString("image")), jsonObj.getString("price"),
                        jsonObj.getString("currency"), "0");
                publishProgress(products);
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
