package com.alameen.wael.hp.market;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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

import static android.content.Context.MODE_PRIVATE;


public class SaleRequestsFragment extends Fragment {

    private ProgressBar progressBar;
    private RecyclerView.Adapter adapter;
    private List<Requests> requestsList;
    private static String currency;
    private String trader;
    private String check;

    public SaleRequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("e_mail", 0);
        String email = sharedPreferences.getString("email", "");
        BackgroundTask backgroundTask = new BackgroundTask(getContext());
        backgroundTask.execute(email);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sale_requests, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar = (ProgressBar) view.findViewById(R.id.load_progress_requests);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.requests);
        recyclerView.setHasFixedSize(true);
        requestsList = new ArrayList<>();
        adapter = new CustomAdapter(getContext(), requestsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
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

        private List<Requests> requestsList;

        CustomAdapter(Context context, List<Requests> requestsList) {
            LayoutInflater.from(context);
            this.requestsList = requestsList;
        }

        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.requests_layout, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final CustomAdapter.ViewHolder holder, int position) {
            final Requests requests = requestsList.get(position);

            new AsyncTask<String, Void, Void>() {

                @Override
                protected Void doInBackground(String... params) {
                    final String READ_URL = MainActivity.HOST +"/get_currency.php";
                    String name = params[0];

                    try {
                        URL url = new URL(READ_URL);
                        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                        httpConnection.setRequestMethod("POST");
                        httpConnection.setDoOutput(true);
                        OutputStream outputStream = httpConnection.getOutputStream();
                        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

                        String data = URLEncoder.encode("name", "UTF-8")+"="+URLEncoder.encode(name, "UTF-8");
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
                            currency = jsonObj.getString("currency");
                            Log.d("currency", currency);
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
                protected void onPostExecute(Void aVoid) {
                    holder.request_name.setText("الاسم : " + requests.getName());
                    try {
                        if (currency.equals("الدولار الامريكي")) {
                            holder.request_price.setText("السعر : " + requests.getPrice() + " " + "دولار امريكي");
                        } else {
                            holder.request_price.setText("السعر : " + requests.getPrice() + " " + "دينار عراقي");
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                    holder.request_num.setText("العدد : " + requests.getNum());
                    holder.request_colors.setText("الالوان : " + requests.getColor());
                    holder.request_sizes.setText("القياسات : " + requests.getSize());

                    switch (requests.getCost()) {
                        case "خلال يومين : 4000 دينار":
                            holder.request_cost.setText("مدة التجهيز : " + "خلال يومين");
                            break;
                        case "خلال ثلاثة ايام : 3000 دينار":
                            holder.request_cost.setText("مدة التجهيز : " + "خلال 3 ايام");
                            break;
                        case "خلال اسبوع : 1000 دينار":
                            holder.request_cost.setText("مدة التجهيز : " + "خلال اسبوع");
                            break;
                    }

                    holder.request_date.setText("تاريخ الطلب : " + requests.getDate());
                }
            }.execute(requests.getName());
        }

        @Override
        public int getItemCount() {
            return requestsList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView request_name, request_price, request_num, request_colors, request_sizes, request_cost, request_date;
            Button yes, no;

            public ViewHolder(View itemView) {
                super(itemView);
                request_name = (TextView) itemView.findViewById(R.id.name);
                request_price = (TextView) itemView.findViewById(R.id.price);
                request_num = (TextView) itemView.findViewById(R.id.num);
                request_colors = (TextView) itemView.findViewById(R.id.color);
                request_sizes = (TextView) itemView.findViewById(R.id.size);
                request_cost = (TextView) itemView.findViewById(R.id.cost);
                request_date = (TextView) itemView.findViewById(R.id.date);
                yes = (Button) itemView.findViewById(R.id.yes);
                no = (Button) itemView.findViewById(R.id.no);

                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AsyncTask<String, Void, Void>() {
                            final String REQ_URL = MainActivity.HOST+"/check_product.php";

                            @Override
                            protected Void doInBackground(String... params) {
                                String check = "1";

                                try {
                                    URL url = new URL(REQ_URL);
                                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                                    httpURLConnection.setRequestMethod("POST");
                                    httpURLConnection.setDoOutput(true);
                                    OutputStream outputStream = httpURLConnection.getOutputStream();
                                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

                                    String data = URLEncoder.encode("check", "UTF-8")+"="+URLEncoder.encode(check, "UTF-8")+"&"
                                            +URLEncoder.encode("name", "UTF-8")+"="+URLEncoder.encode(requestsList.get(getLayoutPosition()).getName(), "UTF-8");
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
                            protected void onPostExecute(Void aVoid) {
                                Toast.makeText(getContext(), "تم تعيين البضاعة على انها متوفرة", Toast.LENGTH_SHORT).show();
                            }
                        }.execute();
                    }
                });

                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AsyncTask<String, Void, Void>() {
                            final String REQ_URL = MainActivity.HOST+"/check_product.php";

                            @Override
                            protected Void doInBackground(String... params) {
                                String check = "2";

                                try {
                                    URL url = new URL(REQ_URL);
                                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                                    httpURLConnection.setRequestMethod("POST");
                                    httpURLConnection.setDoOutput(true);
                                    OutputStream outputStream = httpURLConnection.getOutputStream();
                                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

                                    String data = URLEncoder.encode("check", "UTF-8")+"="+URLEncoder.encode(check, "UTF-8")+"&"
                                            +URLEncoder.encode("name", "UTF-8")+"="+URLEncoder.encode(requestsList.get(getLayoutPosition()).getName(), "UTF-8");
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
                            protected void onPostExecute(Void aVoid) {
                                Toast.makeText(getContext(), "تم تعيين البضاعة على انها غير متوفرة", Toast.LENGTH_SHORT).show();
                            }
                        }.execute();
                    }
                });
            }
        }
    }

    class Requests {
        private String name;
        private String price;
        private String num;
        private String color;
        private String size;
        private String date;
        private String currency;
        private String cost;

        Requests(String name, String price, String num, String color, String size, String cost, String date) {
            setName(name);
            setPrice(price);
            setNum(num);
            setColor(color);
            setSize(size);
            setDate(date);
            setCost(cost);
        }

        public String getColor() {
            return color;
        }

        private void setColor(String color) {
            this.color = color;
        }

        public String getSize() {
            return size;
        }

        private void setSize(String size) {
            this.size = size;
        }

        public String getPrice() {
            return price;
        }

        private void setPrice(String price) {
            this.price = price;
        }

        String getNum() {
            return num;
        }

        private void setNum(String num) {
            this.num = num;
        }

        public String getName() {
            return name;
        }

        private void setName(String name) {
            this.name = name;
        }

        String getDate() {
            return date;
        }

        private void setDate(String date) {
            this.date = date;
        }

        public String getCurrency() {
            return currency;
        }

        private void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getCost() {
            return cost;
        }

        private void setCost(String cost) {
            this.cost = cost;
        }
    }

    class BackgroundTask extends AsyncTask<String, Requests, Void> {

        private static final String READ_URL = MainActivity.HOST+"/read_requests.php";

        BackgroundTask(Context context) {
            LayoutInflater.from(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setProgressBar();
        }

        @Override
        protected Void doInBackground(String... params) {
            String email = params[0];
            Log.d("ttttt", email);

            try {
                URL url = new URL(READ_URL);
                HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection.setRequestMethod("POST");
                httpConnection.setDoOutput(true);
                OutputStream outputStream = httpConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

                String data = URLEncoder.encode("email", "UTF-8")+"="+URLEncoder.encode(email, "UTF-8");
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
                    String name = jsonObj.getString("name");
                    trader = jsonObj.getString("trader");
                    String price = jsonObj.getString("price");
                    String num = jsonObj.getString("num");
                    String color = jsonObj.getString("color");
                    String size = jsonObj.getString("size");
                    String cost = jsonObj.getString("cost");
                    String date = jsonObj.getString("date");
                    Requests requests = new Requests(name, price, num, color, size, cost, date);
                    publishProgress(requests);
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
        protected void onProgressUpdate(Requests... values) {
            super.onProgressUpdate(values);
            setProgressBar();
            requestsList.add(0, values[0]);
            int counter = requestsList.size();
            adapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            endProgress();
            if (trader != null) {
                readCounter(trader);
                deleteCounter(trader);
            }
        }
    }

    private void deleteCounter(String trader) {
        new AsyncTask<String, Void, Void>() {
            final String COUNTER_URL = MainActivity.HOST+"/delete_counter.php";

            @Override
            protected Void doInBackground(String ... params) {
                String trader = params[0];
                try {
                    URL url = new URL(COUNTER_URL);
                    HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                    httpConnection.setRequestMethod("POST");
                    httpConnection.setDoOutput(true);
                    OutputStream outputStream = httpConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

                    String data = URLEncoder.encode("trader", "UTF-8")+"="+URLEncoder.encode(trader, "UTF-8");
                    bufferedWriter.write(data);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    outputStream.close();

                    InputStream inputStream = httpConnection.getInputStream();
                    inputStream.close();
                    httpConnection.disconnect();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

        }.execute(trader);
    }

    private void readCounter(String trader) {
        new AsyncTask<String, Void, Void>() {
            final String COUNTER_URL = MainActivity.HOST+"/counter.php";

            @Override
            protected Void doInBackground(String ... params) {
                String trader = params[0];
                try {
                    URL url = new URL(COUNTER_URL);
                    HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                    httpConnection.setRequestMethod("POST");
                    httpConnection.setDoOutput(true);
                    OutputStream outputStream = httpConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

                    String data = URLEncoder.encode("trader", "UTF-8")+"="+URLEncoder.encode(trader, "UTF-8");
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
                        check = jsonObj.getString("check");
                    }
                    inputStream.close();
                    httpConnection.disconnect();

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Log.d("check", check);
                if (check.equals("true")) {
                    new Broadcasts().onReceive(getContext(), new Intent(getActivity(), SaleRequestsFragment.class));
                }
            }
        }.execute(trader);
    }
}
