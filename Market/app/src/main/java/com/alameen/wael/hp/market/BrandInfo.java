package com.alameen.wael.hp.market;


import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class BrandInfo extends AppCompatActivity implements View.OnClickListener {

    EditText brandName;
    CircleImageView brandImage;
    Button confirm;
    private String image, name;
    private ProgressDialog progress;
    DatabaseBrands databaseBrands;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.brand_info);

        brandName = (EditText) findViewById(R.id.brand_name);
        brandImage = (CircleImageView) findViewById(R.id.brand_image);
        confirm = (Button) findViewById(R.id.insert);
        databaseBrands = new DatabaseBrands(this);
        brandImage.setOnClickListener(this);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = brandName.getText().toString();
                databaseBrands.insert(name);
                Task task = new Task();

                if (image != null) {
                    task.execute(name, image);
                } else {
                    Toast.makeText(getApplicationContext(), "الرجاء اختيار صورة", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createProgress() {
        progress = ProgressDialog.show(this, "", "جاري اضافة الماركة", true, true);
    }

    private void dismissProgress() {
        progress.dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String[] filePath = {MediaStore.Images.Media.DATA};
            Uri imageSelected = data.getData();
            Cursor cursor = getContentResolver().query(imageSelected, filePath, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePath[0]);
            String decodedImage = cursor.getString(columnIndex);
            Bitmap imageBitmap = decode(encode(decodedImage));
            brandImage.setImageBitmap(imageBitmap);
            image = BitMapToString(imageBitmap);
            cursor.close();
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
        Bitmap imageUnderEdit =  BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return getResizedBitmap(imageUnderEdit, 260, 280);
    }

    private Bitmap getResizedBitmap(Bitmap image, int newWidth, int newHeight) {
        int width = image.getWidth();
        int height = image.getHeight();
        float scaledWidth = ((float) newWidth) / width;
        float scaledHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaledWidth, scaledHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(image, 0, 0, width, height, matrix, false);
        image.recycle();
        return resizedBitmap;
    }

    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos = new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte [] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.brand_image) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
        }
    }

    class Task extends AsyncTask<String, Void, Void> {
        final String ADD_URL = MainActivity.HOST+"/insert_brands.php";

        @Override
        protected void onPreExecute() {
            createProgress();
        }

        @Override
        protected Void doInBackground(String... params) {
            String name = params[0];
            String image = params[1];

            try {
                URL url = new URL(ADD_URL);
                HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection.setRequestMethod("POST");
                httpConnection.setDoOutput(true);
                OutputStream outStream = httpConnection.getOutputStream();
                BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(outStream));

                String data = URLEncoder.encode("brand_name", "UTF-8")+"="+URLEncoder.encode(name, "UTF-8")+"&"
                        +URLEncoder.encode("brand_image", "UTF-8")+"="+URLEncoder.encode(image, "UTF-8");

                buffer.write(data);
                buffer.flush();
                buffer.close();
                outStream.close();

                InputStream inputStream = httpConnection.getInputStream();
                inputStream.close();

                httpConnection.disconnect();

            } catch (IOException e) {
                Log.d("IOException", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Intent intent = new Intent(BrandInfo.this, FirstFormActivity.class);
            startActivity(intent);
            dismissProgress();
        }
    }
}
