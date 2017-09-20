package com.alameen.wael.hp.market;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

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


public class ThirdFragment extends Fragment implements AdapterView.OnItemClickListener {

    private RecyclerView.Adapter adapter;
    private List<Traders> tradersList;
    private ProgressBar progressBar;

    public ThirdFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Background task = new Background(getContext());
        task.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_third, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar = (ProgressBar) view.findViewById(R.id.load_progress_1);
        tradersList = new ArrayList<>();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.third_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false));
        adapter = new CustomAdapter(getContext(), tradersList, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(getContext(), ProductsFromTraderActivity.class);
        intent.putExtra("trader_name", tradersList.get(i).getTraderName());
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

    private class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

        private List<Traders> tradersList;
        private AdapterView.OnItemClickListener onItemClick;

        CustomAdapter(Context context, List<Traders> tradersList, AdapterView.OnItemClickListener onItemClick) {
            this.tradersList = tradersList;
            LayoutInflater.from(context);
            this.onItemClick = onItemClick;
        }

        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_layout, parent, false);
            return new CustomAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(CustomAdapter.ViewHolder holder, int position) {
            holder.trader_image.setImageBitmap(tradersList.get(position).getTraderImage());
            holder.trader_name.setText(tradersList.get(position).getTraderName());
        }

        @Override
        public int getItemCount() {
            return tradersList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            CircleImageView trader_image;
            TextView trader_name;

            public ViewHolder(View itemView) {
                super(itemView);
                trader_image = (CircleImageView) itemView.findViewById(R.id.trader_image);
                trader_name = (TextView) itemView.findViewById(R.id.trader_name);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                onItemClick.onItemClick(null, view, getLayoutPosition(), getItemId());
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
