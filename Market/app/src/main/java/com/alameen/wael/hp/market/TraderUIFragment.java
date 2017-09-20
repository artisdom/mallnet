package com.alameen.wael.hp.market;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static java.lang.System.in;


public class TraderUIFragment extends Fragment {

    public static final String READ_URL = MainActivity.HOST+"/read_trader.php";
    RecyclerView addedProductsRecycler;
    RecyclerView.Adapter addedAdapter;
    List<Products> productsList;
    private ProgressBar loadProgress;
    private ProgressDialog progressDialog;

    public TraderUIFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getActivity().getSharedPreferences("e_mail", MODE_PRIVATE);
        String traderName = preferences.getString("email", "");
        BackgroundTask task = new BackgroundTask(getContext());
        task.execute(traderName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trader_ui, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        productsList = new ArrayList<>();
        addedAdapter = new TraderUIAdapter(getContext(), productsList, null);
        addedProductsRecycler = (RecyclerView) view.findViewById(R.id.added_products);
        addedProductsRecycler.setHasFixedSize(true);
        addedProductsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        addedProductsRecycler.setAdapter(addedAdapter);
        addedAdapter.notifyDataSetChanged();
        loadProgress = (ProgressBar) view.findViewById(R.id.load_progress);
    }

    public void setProgressBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            loadProgress.setProgress(50, true);
        }
    }

    public void endProgress() {
        loadProgress.setVisibility(View.GONE);
    }

    private void createProgress() {
        progressDialog = ProgressDialog.show(getContext(), "", "جار مسح المنتج", true, true);
    }

    private void dismissProgress() {
        progressDialog.dismiss();
    }

    class TraderUIAdapter extends RecyclerView.Adapter<TraderUIAdapter.ViewHolder> {

        List<Products> productsList;
        AdapterView.OnItemClickListener itemClickListener;

        TraderUIAdapter(Context context, List<Products> productsList, AdapterView.OnItemClickListener itemClickListener) {
            LayoutInflater.from(context);
            this.productsList = productsList;
            this.itemClickListener = itemClickListener;
        }

        @Override
        public TraderUIAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trader_ui_adapter, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(TraderUIAdapter.ViewHolder holder, int position) {
            Products p = productsList.get(position);
            holder.uiProductImage.setImageBitmap(p.getProImage());
            holder.uiProductName.setText(p.getProductName());
            if (productsList.get(position).getCurrency().equals("الدولار الامريكي")) {
                holder.uiProductPrice.setText(String.format("%s $", Integer.toString(p.getProductPrice())));
            } else if(productsList.get(position).getCurrency().equals("الدينار العراقي")) {
                holder.uiProductPrice.setText(String.format("%s IQD", Integer.toString(p.getProductPrice())));
            }
        }

        @Override
        public int getItemCount() {
            return productsList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ImageView uiProductImage;
            TextView uiProductName, uiProductPrice;
            Button delete;

            ViewHolder(View itemView) {
                super(itemView);
                uiProductImage = (ImageView) itemView.findViewById(R.id.ui_product_Image);
                uiProductName = (TextView) itemView.findViewById(R.id.ui_product_name);
                uiProductPrice = (TextView) itemView.findViewById(R.id.ui_product_price);
                delete = (Button) itemView.findViewById(R.id.del);
                delete.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.del) {
                    String name = productsList.get(getLayoutPosition()).getProductName();
                    SharedPreferences preferences = getActivity().getSharedPreferences("e_mail", MODE_PRIVATE);
                    String email = preferences.getString("email", "");
                    Log.d("trader", email);
                    new Task().execute(name, email);
                }
            }

            class Task extends AsyncTask<String, Void, Void> {
                final String DELETE_URL = MainActivity.HOST+"/delete.php";

                @Override
                protected void onPreExecute() {
                    createProgress();
                }

                @Override
                protected Void doInBackground(String... params) {
                    String name = params[0];
                    String trader = params[1];

                    try {
                        URL url = new URL(DELETE_URL);
                        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                        httpURLConnection.setRequestMethod("POST");
                        httpURLConnection.setDoOutput(true);
                        OutputStream outputStream = httpURLConnection.getOutputStream();
                        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                        String data = URLEncoder.encode("name", "UTF-8")+"="+URLEncoder.encode(name, "UTF-8")+"&"
                                +URLEncoder.encode("trader", "UTF-8")+"="+URLEncoder.encode(trader, "UTF-8");
                        bufferedWriter.write(data);
                        bufferedWriter.flush();
                        bufferedWriter.close();
                        outputStream.close();

                        InputStream inputStream = httpURLConnection.getInputStream();
                        inputStream.close();
                        httpURLConnection.disconnect();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return null;
                }

                @Override
                protected void onProgressUpdate(Void... values) {
                    productsList.remove(productsList.get(getLayoutPosition()));
                    addedAdapter.notifyDataSetChanged();
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    dismissProgress();
                    productsList.remove(productsList.get(getLayoutPosition()));
                    addedAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    class BackgroundTask extends AsyncTask<String, Products, Void> {

        Context context;

        BackgroundTask(Context context) {
            LayoutInflater.from(context);
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            setProgressBar();
        }

        @Override
        protected Void doInBackground(String... params) {
            String trader = params[0];
            Log.d("tt", trader);

            try {
                URL url = new URL(READ_URL);
                HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection.setRequestMethod("POST");
                httpConnection.setDoOutput(true);
                OutputStream outputStream = httpConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                String data = URLEncoder.encode("trader_name", "UTF-8")+"="+URLEncoder.encode(trader, "UTF-8");
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                InputStream input = httpConnection.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(input));
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
                            jsonObj.getString("price"), jsonObj.getString("currency"), "0");
                    publishProgress(products);
                }

                httpConnection.disconnect();
                input.close();
                buffer.close();

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onProgressUpdate(Products... values) {
            productsList.add(0, values[0]);
            addedAdapter.notifyDataSetChanged();
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
