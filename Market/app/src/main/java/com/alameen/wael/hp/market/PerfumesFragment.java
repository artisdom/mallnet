package com.alameen.wael.hp.market;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
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


public class PerfumesFragment extends Fragment implements AdapterView.OnItemClickListener {

    RecyclerView menClothesRecycler;
    RecyclerView.Adapter adapter;
    List<Products> productsList;
    ProgressBar progressBar;

    public PerfumesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BackgroundTask backgroundTask = new BackgroundTask(getContext());
        backgroundTask.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_perfumes, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View v = getLayoutInflater(savedInstanceState).inflate(R.layout.all_products_menu, null);
        final LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.products_layout);
        progressBar = (ProgressBar) view.findViewById(R.id.load_progress_9);

        productsList = new ArrayList<>();
        adapter = new ProductsAdapter(getContext(), productsList, this);
        menClothesRecycler = (RecyclerView) view.findViewById(R.id.je_recycler);
        menClothesRecycler.setHasFixedSize(true);
        menClothesRecycler.setLayoutManager(new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false));
        menClothesRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        menClothesRecycler.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.topMargin = 10;
                linearLayout.setLayoutParams(params);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String TYPE = "الاكسسوارات-العطور";
        Intent intent = new Intent(getActivity(), DetailsActivity.class);
        intent.putExtra("product_name", productsList.get(i).getProductName());
        intent.putExtra("type", TYPE);
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

    class BackgroundTask extends AsyncTask<Void, Products, Void> {

        private static final String READ_URL = MainActivity.HOST+"/read_all_products.php";
        private static final String TYPE = "الاكسسوارات-العطور";

        BackgroundTask(Context context) {
            LayoutInflater.from(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

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

                String data = URLEncoder.encode("type", "UTF-8")+"="+URLEncoder.encode(TYPE, "UTF-8");
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

                    Products products = new Products(jsonObj.getString("name"), stringToBitMap(jsonObj.getString("image")),
                            jsonObj.getString("price"), jsonObj.getString("currency"), jsonObj.getString("likes"));
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

        @RequiresApi(api = Build.VERSION_CODES.N)
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
