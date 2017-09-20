package com.alameen.wael.hp.market;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.kosalgeek.android.photoutil.GalleryPhoto;
import com.kosalgeek.android.photoutil.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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


public class FirstFormActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    RecyclerView imagesRecycler;
    RecyclerView.Adapter imagesAdapter;
    private ArrayList<Products> list;
    String pName, pPrice, pDesc, pColor, pSizes, pTypes, pLogo, pCurrency, image1, image2, image3, image4, logoName;
    Bitmap imageBitmap;
    Button pick;
    EditText name, price, description, colors, sizes;
    ProgressDialog progress;
    Spinner types, brands, currency;
    ArrayAdapter<CharSequence> spAdapter;
    List<String> stringList =  new ArrayList<>();
    ArrayAdapter<String>  arrayAdapter;
    ArrayAdapter<CharSequence> currencyAdapter;
    private static final String ADD_URL = MainActivity.HOST+"/add.php";
    private GalleryPhoto galleryPhoto;
    private static final int GALLERY_REQUEST = 2200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_form);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.products_insert);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        name = (EditText) findViewById(R.id.p_name);
        price = (EditText) findViewById(R.id.p_price);
        description = (EditText) findViewById(R.id.p_desc);
        colors = (EditText) findViewById(R.id.p_colors);
        sizes = (EditText) findViewById(R.id.p_sizes);
        galleryPhoto = new GalleryPhoto(this);

        createProgress();
        dismissProgress();

        types = (Spinner) findViewById(R.id.type_spinner);
        spAdapter = ArrayAdapter.createFromResource(this, R.array.spinner_items, android.R.layout.simple_spinner_item);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        types.setAdapter(spAdapter);
        types.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                pTypes = adapterView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        DatabaseBrands databaseBrands = new DatabaseBrands(this);
        Cursor result = databaseBrands.showALL();

        if (result.getCount() > 0) {
            while (result.moveToNext()) {
                String name = result.getString(0);
                stringList.add(0, name);
            }
        }

        new AsyncTask<Void, Void, Void>() {
            final String LOGO = MainActivity.HOST+"/read_logos.php";

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    URL url = new URL(LOGO);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
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
                        logoName = json.getString("logo");
                    }

                    in.close();
                    reader.close();
                    httpURLConnection.disconnect();

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                stringList.add(0, logoName);
                arrayAdapter.notifyDataSetChanged();
            }
        }.execute();

        brands = (Spinner) findViewById(R.id.brand_spinner);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stringList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        brands.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();

        brands.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                pLogo = adapterView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ImageButton addBrand = (ImageButton) findViewById(R.id.add_logo);
        addBrand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddDialog();
            }
        });

        list = new ArrayList<>();

        imagesAdapter = new imagesMenuAdapter(this, list, this);
        imagesRecycler = (RecyclerView) findViewById(R.id.pro_images_recycler);
        imagesRecycler.setHasFixedSize(true);
        GridLayoutManager manager = new GridLayoutManager(getApplicationContext(), 4, GridLayoutManager.HORIZONTAL, false);
        manager.setOrientation(GridLayout.VERTICAL);
        imagesRecycler.setLayoutManager(manager);
        imagesRecycler.setAdapter(imagesAdapter);
        imagesAdapter.notifyDataSetChanged();

        pick = (Button) findViewById(R.id.pick);
        pick.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View view) {
                uploadFromGallery();
            }
        });

        currency = (Spinner) findViewById(R.id.currency_spinner);
        currencyAdapter = ArrayAdapter.createFromResource(this, R.array.currency_items, android.R.layout.simple_spinner_item);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currency.setAdapter(currencyAdapter);

        currency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                pCurrency = adapterView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void openAddDialog() {
        startActivity(new Intent(this, BrandInfo.class));
    }

    private void createProgress() {
        progress = ProgressDialog.show(this, "", "جاري اضافة المنتج", true, false);
    }

    private void dismissProgress() {
        progress.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.form_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home) {
            finish();
            onBackPressed();
        }

        if(id == R.id.action_publish) {
            addProducts();
        }

        return super.onOptionsItemSelected(item);
    }

    private void addProducts() {
        SharedPreferences preferences = getSharedPreferences("e_mail", MODE_PRIVATE);
        String traderName = preferences.getString("email", "");
        Log.d("traderName", traderName);
        pName = name.getText().toString();
        pPrice = price.getText().toString();
        pDesc = description.getText().toString();
        pColor = colors.getText().toString();
        pSizes = sizes.getText().toString();

        if (pLogo == null) {
            pLogo = "بدون ماركة";
        }

        if (TextUtils.isEmpty(pName) || TextUtils.isEmpty(pPrice) || TextUtils.isEmpty(pDesc) || TextUtils.isEmpty(pColor) || TextUtils.isEmpty(pSizes)
                || (TextUtils.isEmpty(pName) && TextUtils.isEmpty(pPrice) && TextUtils.isEmpty(pDesc) && TextUtils.isEmpty(pColor) && TextUtils.isEmpty(pSizes))) {
            Toast.makeText(getApplicationContext(), "يجب ملىء كل الحقول", Toast.LENGTH_SHORT).show();
        } else {
            BackgroundTask task = new BackgroundTask(this);

            try {
                if (list.size() == 1) {
                    image1 = BitMapToString(list.get(0).getProImage());
                    image2 = "";
                    image3 = "";
                    image4 = "";
                } else if (list.size() == 2) {
                    image1 = BitMapToString(list.get(0).getProImage());
                    image2 = BitMapToString(list.get(1).getProImage());
                    image3 = "";
                    image4 = "";
                } else if (list.size() == 3) {
                    image1 = BitMapToString(list.get(0).getProImage());
                    image2 = BitMapToString(list.get(1).getProImage());
                    image3 = BitMapToString(list.get(2).getProImage());
                    image4 = "";
                } else {
                    image1 = BitMapToString(list.get(0).getProImage());
                    image2 = BitMapToString(list.get(1).getProImage());
                    image3 = BitMapToString(list.get(2).getProImage());
                    image4 = BitMapToString(list.get(3).getProImage());
                }

                task.execute(pName, pPrice, pDesc, pTypes, pLogo, traderName, pColor, pSizes, image1, image2, image3, image4, pCurrency);

            } catch (Exception e) {
                Log.d("Exception", e.getMessage());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos = new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte [] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void uploadFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            //String[] filePath = {MediaStore.Images.Media.DATA};
            if (list.size() < 4) {
                galleryPhoto.setPhotoUri(data.getData());
                String imagePath = galleryPhoto.getPath();
                try {
                    imageBitmap = ImageLoader.init().from(imagePath).requestSize(512, 512).getBitmap();
                    //imageBitmap = getResizedBitmap(temp, 300, 200);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                Products products = new Products(imageBitmap);
                list.add(products);
                imagesAdapter.notifyDataSetChanged();
//                Uri imageSelected = data.getData();
//                Cursor cursor = getContentResolver().query(imageSelected, filePath, null, null, null);
//                cursor.moveToFirst();
//                int columnIndex = cursor.getColumnIndex(filePath[0]);
//                String decodedImage = cursor.getString(columnIndex);
//                imageBitmap = decode(encode(decodedImage));
//                Products p = new Products(imageBitmap);
//                list.add(p);
//                imagesAdapter.notifyDataSetChanged();
//                cursor.close();
            } else {
                Toast.makeText(FirstFormActivity.this, "اقصى حد هو 4 صور", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String encode(String imagePath) {
        File file = new File(imagePath);
        FileInputStream fileStream = null;

        try {
            fileStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Bitmap decodeFile = BitmapFactory.decodeStream(fileStream);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        decodeFile.compress(Bitmap.CompressFormat.JPEG, 100, out);
        byte[] bytes = out.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private Bitmap decode(String encoded) {
        byte[] bytes = Base64.decode(encoded, Base64.DEFAULT);
        //return getResizedBitmap(imageUnderEdit, 100, 100);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private Bitmap getResizedBitmap(Bitmap image, int newWidth, int newHeight) {
        float scaledWidth, scaledHeight;
        int width = image.getWidth();
        int height = image.getHeight();

        scaledWidth = ((float) newWidth / width);
        scaledHeight = ((float) newHeight / height);

        Matrix matrix = new Matrix();
        matrix.postScale(scaledWidth, scaledHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(image, 0, 0, width, height, matrix, false);
        image.recycle();
        return resizedBitmap;
    }

    class imagesMenuAdapter extends RecyclerView.Adapter<imagesMenuAdapter.ViewHolder> {

        ArrayList<Products> list;
        AdapterView.OnItemClickListener itemClick;

        imagesMenuAdapter(Context context, ArrayList<Products> list, AdapterView.OnItemClickListener itemClick) {
            LayoutInflater.from(context);
            this.list = list;
            this.itemClick = itemClick;
        }

        @Override
        public imagesMenuAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.images_to_upload, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(imagesMenuAdapter.ViewHolder holder, int position) {
            holder.imageContainer.setImageBitmap(list.get(position).getProImage());
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ImageView imageContainer;

            ViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                imageContainer = (ImageView) itemView.findViewById(R.id.image_container);
                imageContainer.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                itemClick.onItemClick(null, view, getLayoutPosition(), getItemId());
            }
        }
    }

    class BackgroundTask extends AsyncTask<String, Void, String> {

        Context context;

        BackgroundTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            createProgress();
        }

        @Override
        protected String doInBackground(String... params) {
            String name = params[0];
            String price = params[1];
            String description = params[2];
            String type = params[3];
            String logo = params[4];
            String trader = params[5];
            String colors = params[6];
            String sizes = params[7];
            String image1 = params[8];
            String image2 = params[9];
            String image3 = params[10];
            String image4 = params[11];
            String currency = params[12];
            Log.d("traderNameAfter", trader);

            try {
                URL url = new URL(ADD_URL);
                HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection.setRequestMethod("POST");
                httpConnection.setDoOutput(true);
                OutputStream outStream = httpConnection.getOutputStream();
                BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(outStream));

                String data = URLEncoder.encode("name", "UTF-8")+"="+URLEncoder.encode(name, "UTF-8")+"&"
                        +URLEncoder.encode("description", "UTF-8")+"="+URLEncoder.encode(description, "UTF-8")+"&"
                        +URLEncoder.encode("price", "UTF-8")+"="+URLEncoder.encode(price, "UTF-8")+"&"
                        +URLEncoder.encode("trader", "UTF-8")+"="+URLEncoder.encode(trader, "UTF-8")+"&"
                        +URLEncoder.encode("type", "UTF-8")+"="+URLEncoder.encode(type, "UTF-8")+"&"
                        +URLEncoder.encode("logo", "UTF-8")+"="+URLEncoder.encode(logo, "UTF-8")+"&"
                        +URLEncoder.encode("currency", "UTF-8")+"="+URLEncoder.encode(currency, "UTF-8")+"&"
                        +URLEncoder.encode("colors", "UTF-8")+"="+URLEncoder.encode(colors, "UTF-8")+"&"
                        +URLEncoder.encode("sizes", "UTF-8")+"="+URLEncoder.encode(sizes, "UTF-8")+"&"
                        +URLEncoder.encode("image1", "UTF-8")+"="+URLEncoder.encode(image1, "UTF-8")+"&"
                        +URLEncoder.encode("image2", "UTF-8")+"="+URLEncoder.encode(image2, "UTF-8")+"&"
                        +URLEncoder.encode("image3", "UTF-8")+"="+URLEncoder.encode(image3, "UTF-8")+"&"
                        +URLEncoder.encode("image4", "UTF-8")+"="+URLEncoder.encode(image4, "UTF-8");

                buffer.write(data);
                buffer.flush();
                buffer.close();
                outStream.close();

                InputStream inputStream = httpConnection.getInputStream();
                inputStream.close();

                return "تم اضافة المنتج";

            } catch (IOException e) {
                Log.d("IOException", e.getMessage());
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String msg) {
            super.onPostExecute(msg);
            try {
                dismissProgress();
                name.getText().clear();
                description.getText().clear();
                price.getText().clear();
                colors.getText().clear();
                sizes.getText().clear();
                list.clear();
                imagesAdapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), msg + " ", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(FirstFormActivity.this, TraderUIActivity.class));

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "فشل عملية الرفع حاول مجددا", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
