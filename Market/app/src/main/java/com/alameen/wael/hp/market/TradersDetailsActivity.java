package com.alameen.wael.hp.market;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

public class TradersDetailsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private Slider adapter;
    private ProgressBar progressBar;
    protected List<Details> detailsList = new ArrayList<>();
    private List<Bitmap> bitmapList = new ArrayList<>();
    public String productName, productDetails, productPrice, currency, productColors, productSizes;
    public static String counter;
    private List<Products> productsList;
    public Bitmap productImage1, productImage2, productImage3, productImage4;
    TextView name, details, price, like, color, size;
    ViewPager viewPager;
    ImageView imageInSlide, pressLike;
    public int likesCounter = 0;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private RecyclerView.Adapter recyclerAdapter;
    private static int offset = -4;
    GridLayoutManager manager;
    ImageButton right;
    private static final String RELATED_ITEMS_URL = MainActivity.HOST+"/trader_details.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traders_details);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        progressBar = (ProgressBar) findViewById(R.id.load_details);
        Button buy = (Button) findViewById(R.id.buy);
        pressLike = (ImageView) findViewById(R.id.press_like);
        like = (TextView) findViewById(R.id.like);
        name = (TextView) findViewById(R.id.name);
        details = (TextView) findViewById(R.id.details);
        price = (TextView) findViewById(R.id.price);
        color = (TextView) findViewById(R.id.colors);
        size = (TextView) findViewById(R.id.sizes);
        viewPager = (ViewPager) findViewById(R.id.images_slider);
        final ImageButton left = (ImageButton) findViewById(R.id.left);
        right = (ImageButton) findViewById(R.id.right);

        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int tab = viewPager.getCurrentItem();

                if (tab > 0) {
                    tab--;
                    viewPager.setCurrentItem(tab);
                } else if (tab == 0) {
                    left.setVisibility(View.GONE);
                    viewPager.setCurrentItem(tab);
                }
            }
        });

        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int tab = viewPager.getCurrentItem();
                tab++;
                viewPager.setCurrentItem(tab);
                left.setVisibility(View.VISIBLE);
            }
        });

        try {
            adapter = new Slider(this);
            viewPager.setAdapter(adapter);
            viewPager.setOffscreenPageLimit(4);
            viewPager.setCurrentItem(1);
            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "خطا في التحميل", Toast.LENGTH_SHORT).show();
        }

        productsList = new ArrayList<>();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.related_recycler);
        recyclerView.setHasFixedSize(true);
        manager = new GridLayoutManager(this, 4, GridLayoutManager.HORIZONTAL, false);
        manager.setOrientation(GridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerAdapter = new CustomAdapter(this, productsList, this);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.notifyDataSetChanged();

        String product_name = getIntent().getExtras().getString("product_name");
        getSupportActionBar().setTitle(product_name);
        sharedPreferences = getSharedPreferences("likes", MODE_PRIVATE);
        counter = sharedPreferences.getString("counter", "");
        like.setText(counter);
        BackgroundTask backgroundTask = new BackgroundTask(this);
        backgroundTask.execute(product_name, counter);

        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String trader_name = getIntent().getExtras().getString("trader");
                Intent intent = new Intent(TradersDetailsActivity.this, RequestActivity.class);
                intent.putExtra("name", productName);
                intent.putExtra("trader", trader_name);
                intent.putExtra("price", productPrice);
                intent.putExtra("currency", currency);
                intent.putExtra("colors", productColors);
                intent.putExtra("sizes", productSizes);
                startActivity(intent);
            }
        });

        pressLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                likesCounter++;
                counter = Integer.toString(likesCounter);
                sharedPreferences = getSharedPreferences("likes", MODE_PRIVATE);
                editor = sharedPreferences.edit();
                editor.putString("counter", counter);
                editor.commit();
                like.setText(Integer.toString(likesCounter));
                pressLike.setClickable(false);
            }
        });

        AsyncTask<Void, Products, Void> task = new AsyncTask<Void, Products, Void>() {

            String trader_name = getIntent().getExtras().getString("trader");
            String product_name = getIntent().getExtras().getString("product_name");

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    URL url = new URL(RELATED_ITEMS_URL);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    OutputStream out = httpURLConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

                    String data = URLEncoder.encode("trader", "UTF-8")+"="
                            +URLEncoder.encode(trader_name, "UTF-8")+"&"
                            +URLEncoder.encode("product_name", "UTF-8")+"="
                            +URLEncoder.encode(product_name, "UTF-8")+"&"
                            +URLEncoder.encode("offset", "UTF-8")+"="+URLEncoder.encode(Integer.toString(offset), "UTF-8");

                    writer.write(data);
                    writer.flush();
                    writer.close();
                    out.close();

                    InputStream in = httpURLConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }

                    String json_string = stringBuilder.toString().trim();
                    JSONObject object = new JSONObject(json_string);
                    JSONArray jsonArray = object.getJSONArray("server_response");
                    int i = 0;

                    while (i < jsonArray.length()) {
                        JSONObject json = jsonArray.getJSONObject(i);
                        i++;
                        String name = json.getString("name");
                        Bitmap image = stringToBitMap(json.getString("image"));
                        //String currency = json.getString("currency");
                        Products p = new Products(name, image, "0", "$", "0");
                        publishProgress(p);
                    }

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(Products... values) {
                super.onProgressUpdate(values);
                productsList.add(values[0]);
                recyclerAdapter.notifyDataSetChanged();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }

            private Bitmap stringToBitMap(String encodedString){
                byte [] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
                ByteArrayInputStream imageStream = new ByteArrayInputStream(encodeByte);
                Bitmap theImage = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                imageStream.reset();
                return theImage;
            }
        };

        try {
            task.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(offset > productsList.size()) {
            offset = 0;
        } else {
            offset += 4;
        }
        Log.d("offset", Integer.toString(offset));
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home) {
            finish();
            onBackPressed();
        }

        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String trader_name = getIntent().getExtras().getString("trader");
        Intent intent = new Intent(TradersDetailsActivity.this, TradersDetailsActivity.class);
        intent.putExtra("product_name", productsList.get(i).getProductName());
        intent.putExtra("trader", trader_name);
        startActivity(intent);
    }

    class Slider extends PagerAdapter {
        Context context;
        LayoutInflater layoutInflater;

        Slider(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            try {
                return bitmapList.size();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "خطا في الاتصال", Toast.LENGTH_SHORT).show();
            }
            return 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            try {
                layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                View itemView = layoutInflater.inflate(R.layout.manual_slide, container, false);
                imageInSlide = (ImageView) itemView.findViewById(R.id.image_in_slide);
                imageInSlide.setImageBitmap(bitmapList.get(position));
                imageInSlide.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmapList.get(position).compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                        byte[] bytes = byteArrayOutputStream.toByteArray();
                        Intent intent = new Intent(TradersDetailsActivity.this, FullScreenImage.class);
                        intent.putExtra("image", bytes);
                        intent.putExtra("name", productName);
                        intent.putExtra("price", productPrice);
                        intent.putExtra("currency", currency);
                        startActivity(intent);
                    }
                });

                container.addView(itemView);
                return itemView;

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "خطا في التحميل", Toast.LENGTH_SHORT).show();
            }

            return null;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            try {
                container.removeView((LinearLayout) object);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "خطا في التحميل", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class BackgroundTask extends AsyncTask<String, Details, Void> {

        private static final String DETAILS_URL = MainActivity.HOST+"/read_details.php";

        BackgroundTask (Context context) {
            LayoutInflater.from(context);
        }

        @Override
        protected Void doInBackground(String... params) {
            String product_name = params[0];
            String count = params[1];

            try {
                URL url = new URL(DETAILS_URL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

                String data = URLEncoder.encode("product_name", "UTF-8")+"="
                        +URLEncoder.encode(product_name, "UTF-8");

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
                    productName = jsonObj.getString("name");
                    productDetails = jsonObj.getString("description");
                    productPrice = jsonObj.getString("price");
                    productColors = jsonObj.getString("colors");
                    productSizes = jsonObj.getString("sizes");
                    currency = jsonObj.getString("currency");
                    productImage1 = stringToBitMap(jsonObj.getString("image1"));
                    productImage2 = stringToBitMap(jsonObj.getString("image2"));
                    productImage3 = stringToBitMap(jsonObj.getString("image3"));
                    productImage4 = stringToBitMap(jsonObj.getString("image4"));

                    if (productImage1 != null && productImage2 == null && productImage3 == null && productImage4 == null) {
                        bitmapList.add(0, productImage1);

                    } else if (productImage1 != null && productImage2 != null && productImage3 == null && productImage4 == null) {
                        bitmapList.add(0, productImage1);
                        bitmapList.add(1, productImage2);

                    } else if (productImage1 != null && productImage2 != null && productImage3 != null && productImage4 == null) {
                        bitmapList.add(0, productImage1);
                        bitmapList.add(1, productImage2);
                        bitmapList.add(2, productImage3);

                    } else {
                        bitmapList.add(0, productImage1);
                        bitmapList.add(1, productImage2);
                        bitmapList.add(2, productImage3);
                        bitmapList.add(3, productImage4);
                    }

                    if (productPrice.equals("")) {
                        Details details = new Details(productName, productDetails, 0, currency, likesCounter);
                        publishProgress(details);
                    } else {
                        Details details = new Details(productName, productDetails, Integer.parseInt(productPrice), currency, likesCounter);
                        publishProgress(details);
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
        protected void onProgressUpdate(Details... values) {
            super.onProgressUpdate(values);
            setProgressBar();
            detailsList.add(values[0]);
            adapter.notifyDataSetChanged();

            if (productImage1 != null && productImage2 == null && productImage3 == null && productImage4 == null) {
                right.setVisibility(View.INVISIBLE);

            } else if (productImage1 != null && productImage2 != null && productImage3 == null && productImage4 == null) {
                if (viewPager.getCurrentItem() == 1) {
                    right.setVisibility(View.INVISIBLE);
                }

            } else if (productImage1 != null && productImage2 != null && productImage3 != null && productImage4 == null) {
                if (viewPager.getCurrentItem() == 2) {
                    right.setVisibility(View.INVISIBLE);
                }
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            endProgress();
            name.setText(productName);
            details.setText(productDetails);
            Log.d("curr", currency);
            if (currency.equals("الدولار الامريكي")) {
                price.setText(productPrice + " $");
            } else if (currency.equals("الدينار العراقي")) {
                price.setText(productPrice + " IQD");
            }
            color.append("يتوفر بالالوان : " + productColors);
            size.append("يتوفر بالقياسات : " + productSizes);
            //like.setText(Integer.toString(likesCounter));
        }

        private Bitmap stringToBitMap(String encodedString){
            byte [] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            ByteArrayInputStream imageStream = new ByteArrayInputStream(encodeByte);
            Bitmap theImage = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            imageStream.reset();
            return theImage;
        }
    }

    private class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

        private AdapterView.OnItemClickListener itemClick;
        private List<Products> productsList;

        CustomAdapter(Context context, List<Products> productsList, AdapterView.OnItemClickListener itemClick) {
            LayoutInflater.from(context);
            this.itemClick = itemClick;
            this.productsList = productsList;
        }

        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.related_products_menu, parent, false);
            return new CustomAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(CustomAdapter.ViewHolder holder, int position) {
            Products products = productsList.get(position);
            holder.imageProduct.setImageBitmap(products.getProImage());
            holder.nameProduct.setText(products.getProductName());
        }

        @Override
        public int getItemCount() {
            return productsList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ImageView imageProduct;
            TextView nameProduct;

            ViewHolder(View itemView) {
                super(itemView);

                imageProduct = (ImageView) itemView.findViewById(R.id.product_image);
                nameProduct = (TextView) itemView.findViewById(R.id.product_name);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                itemClick.onItemClick(null, view, getLayoutPosition(), getItemId());
            }
        }
    }
}
