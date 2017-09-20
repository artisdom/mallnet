package com.alameen.wael.hp.market;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import de.hdodenhof.circleimageview.CircleImageView;


public class TraderInsertInfoActivity extends AppCompatActivity {

    CircleImageView traderImage;
    EditText enterTraderName;
    Button complete;
    private double latitude, longitude;
    private ProgressDialog progressDialog;
    String theImage, email, password, phone;
    LocationManager locationManager;
    LocationListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trader_insert_info);

        traderImage = (CircleImageView) findViewById(R.id.enter_trader_image);
        enterTraderName = (EditText) findViewById(R.id.enter_trader_name);
        complete = (Button) findViewById(R.id.complete);

        email = getIntent().getExtras().getString("email");
        password = getIntent().getExtras().getString("password");
        phone = getIntent().getExtras().getString("phone_no");
        Log.d("phone", phone);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Log.d("la, lo", Double.toString(latitude)+" "+Double.toString(longitude));
                setLatitude(latitude);
                setLongitude(longitude);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                showAlertDialog();
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if(location != null) {
            setLatitude(location.getLatitude());
            setLongitude(location.getLongitude());
        }

        onClickEvent();
    }

    public double getLatitude() {
        Log.d("lat_here_get", Double.toString(latitude));
        return latitude;
    }

    public double getLongitude() {
        Log.d("long_here_get", Double.toString(longitude));
        return longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
        Log.d("lat_here_set", Double.toString(latitude));
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
        Log.d("long_here_set", Double.toString(longitude));
    }

    public void createProgress() {
        progressDialog = ProgressDialog.show(this, "", "جار التاكيد", true, true);
    }

    public void dismissProgress() {
        progressDialog.dismiss();
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.alert_message);
        builder.setCancelable(false);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton(R.string.dialog_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void onClickEvent() {

        complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = enterTraderName.getText().toString().trim();
                if(name.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "الرجاء ملىء الحقل", Toast.LENGTH_SHORT).show();

                } else {
                    Log.d("name", name);
                    String lati = String.valueOf(getLatitude());
                    String longi = String.valueOf(getLongitude());
                    Log.d("lat", lati);
                    Log.d("long", longi);
                    SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
                    String token = preferences.getString("registration token", "");
                    BackgroundTask task = new BackgroundTask();

                    if (theImage != null || TextUtils.isEmpty(name) || (theImage != null && TextUtils.isEmpty(name))) {
                        task.execute(name, theImage, email, password, phone, lati, longi, token);

                    } else {
                        Toast.makeText(getApplicationContext(), "الرجاء وضع صورة التاجر و اسم التاجر", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        traderImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFromGallery();
            }
        });
    }

    private void uploadFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    public String bitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos = new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte [] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String[] filePath = {MediaStore.Images.Media.DATA};
            Uri imageSelected = data.getData();
            Cursor cursor = getContentResolver().query(imageSelected, filePath, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePath[0]);
            String decodedImage = cursor.getString(columnIndex);
            Bitmap image = decode(encode(decodedImage));
            traderImage.setImageBitmap(image);
            theImage = bitMapToString(image);
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
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    class BackgroundTask extends AsyncTask<String, Void, Void> {

        private static final String SUBSCRIBER_REGISTER_URL = MainActivity.HOST+"/login_subscriber.php";
        String name;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            createProgress();
        }

        @Override
        protected Void doInBackground(String... params) {
            name = params[0];
            String image = params[1];
            String email = params[2];
            String password = params[3];
            String phone = params[4];
            String latitude = params[5];
            String longitude = params[6];

            try {
                URL url = new URL(SUBSCRIBER_REGISTER_URL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

                String data = URLEncoder.encode("name", "UTF-8")+"="+URLEncoder.encode(name, "UTF-8")+"&"
                        +URLEncoder.encode("image", "UTF-8")+"="+URLEncoder.encode(image, "UTF-8")+"&"
                        +URLEncoder.encode("email", "UTF-8")+"="+URLEncoder.encode(email, "UTF-8")+"&"
                        +URLEncoder.encode("password", "UTF-8")+"="+URLEncoder.encode(password, "UTF-8")+"&"
                        +URLEncoder.encode("phone", "UTF-8")+"="+URLEncoder.encode(phone, "UTF-8")+"&"
                        +URLEncoder.encode("lat", "UTF-8")+"="+URLEncoder.encode(latitude, "UTF-8")+"&"
                        +URLEncoder.encode("long", "UTF-8")+"="+URLEncoder.encode(longitude, "UTF-8");

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
            dismissProgress();
            SharedPreferences sharedPreferences = getSharedPreferences("login_check", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("is_logged_in", true);
            editor.commit();
            Intent intent = new Intent(TraderInsertInfoActivity.this, TraderUIActivity.class);
            SharedPreferences preferences = getSharedPreferences("preferences", MODE_PRIVATE);
            SharedPreferences.Editor editor2 = preferences.edit().putString("trader", name);
            Log.d("ttr", name);
            editor.commit();
            startActivity(intent);
        }
    }
}
