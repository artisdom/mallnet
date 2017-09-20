package com.alameen.wael.hp.market;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ViewHolder> {

    private List<Products> productsList;
    private AdapterView.OnItemClickListener onItemClickListener;
    private Context context;
    private static String ids, traderName;

    ProductsAdapter(Context context, List<Products> productsList, AdapterView.OnItemClickListener onItemClickListener) {
        this.productsList = productsList;
        LayoutInflater.from(context);
        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }

    ProductsAdapter(Context context, List<Products> productsList) {
        this.productsList = productsList;
        LayoutInflater.from(context);
    }

    @Override
    public ProductsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_products_menu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductsAdapter.ViewHolder holder, int position) {
        Products prods = productsList.get(position);
        holder.productName.setText(prods.getProductName());
        holder.productImage.setImageBitmap(prods.getProImage());
        if (productsList.get(position).getCurrency().equals("الدولار الامريكي")) {
            holder.productPrice.setText(Integer.toString(prods.getProductPrice()) + " $");
        } else if (productsList.get(position).getCurrency().equals("الدينار العراقي")) {
            holder.productPrice.setText(Integer.toString(prods.getProductPrice()) + " IQD");
        }
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private static final String DETAILS_URL = MainActivity.HOST+"/read_details.php";
        private String color, size;
        TextView productName, productPrice;
        CircleImageView productImage;
        Button buy;

        ViewHolder(View itemView) {
            super(itemView);

            productName = (TextView) itemView.findViewById(R.id.product_name);
            productPrice = (TextView) itemView.findViewById(R.id.product_price);
            productImage = (CircleImageView) itemView.findViewById(R.id.product_image);
            buy = (Button) itemView.findViewById(R.id.buy);
            buy.setOnClickListener(this);
            itemView.setOnClickListener(this);
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
                        try {
                            Log.d("id", ids);
                            String data = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(ids, "UTF-8");

                            writer.write(data);
                            writer.flush();
                            writer.close();
                            out.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                            //Toast.makeText(ProductsAdapter.this, "خطا في الاتصال", Toast.LENGTH_SHORT).show();
                        }

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
