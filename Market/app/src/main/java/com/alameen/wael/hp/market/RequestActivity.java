package com.alameen.wael.hp.market;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class RequestActivity extends AppCompatActivity {

    private AutoCompleteTextView mColor, mSize, mNumber, mAddress, mPhone;
    private String name;
    private String trader;
    private String price, address, cost;
    private ProgressDialog progress;
    LocationManager locationManager;
    LocationListener listener;
    private InnerDataBase innerDataBase;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.buying);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        TextView mName = (TextView) findViewById(R.id.name);
        TextView mTrader = (TextView) findViewById(R.id.trader_name);
        final TextView mPrice = (TextView) findViewById(R.id.price);
        TextView txtColor = (TextView) findViewById(R.id.color);
        TextView txtSize = (TextView) findViewById(R.id.size);
        mColor = (AutoCompleteTextView) findViewById(R.id.in_color);
        mSize = (AutoCompleteTextView) findViewById(R.id.in_size);
        mNumber = (AutoCompleteTextView) findViewById(R.id.in_number);
        mAddress = (AutoCompleteTextView) findViewById(R.id.in_address);
        mPhone = (AutoCompleteTextView) findViewById(R.id.in_phone);
        Spinner select = (Spinner) findViewById(R.id.select_cost);
        innerDataBase = new InnerDataBase(this);

        try {
            name = getIntent().getExtras().getString("name");
            mName.append(name);
            trader = getIntent().getExtras().getString("trader");
            Log.d("t after", trader);
            mTrader.append(trader);
            String currency = getIntent().getExtras().getString("currency");
            price = getIntent().getExtras().getString("price");

            if (currency.equals("الدولار الامريكي")) {
                mPrice.append(price + " دولار ");

            } else {
                mPrice.append(price + " دينار ");
            }

            String colors = getIntent().getExtras().getString("colors");
            txtColor.append("\n" + colors);
            String sizes = getIntent().getExtras().getString("sizes");
            txtSize.append("\n" + sizes);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "الانترنت عندك ضعيف", Toast.LENGTH_SHORT).show();
        }

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.costs, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        select.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();

        select.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                cost = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Button sendRequest = (Button) findViewById(R.id.request);
        sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String color, size, number, lat, longi, phone, currentDate;
                number = mNumber.getText().toString().trim();
                color = mColor.getText().toString().trim();
                size = mSize.getText().toString().trim();
                lat = String.valueOf(getLatitude());
                longi = String.valueOf(getLongitude());
                address = mAddress.getText().toString().trim();
                phone = mPhone.getText().toString().trim();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM//yy");
                Date date = new Date();
                currentDate = simpleDateFormat.format(date);
                SharedPreferences preferences = getSharedPreferences("this_phone", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit().putString("this_phone", phone);
                editor.commit();

                if (TextUtils.isEmpty(number) || TextUtils.isEmpty(color) || TextUtils.isEmpty(size) || TextUtils.isEmpty(phone) || (
                        TextUtils.isEmpty(number) && TextUtils.isEmpty(color) && TextUtils.isEmpty(size) && TextUtils.isEmpty(phone))) {
                    Toast.makeText(getApplicationContext(), "يجب ملىء كل الحقول", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        innerDataBase.insert(name, trader, price, number, color, size, lat, longi, address, phone, cost, currentDate);
                        progress = ProgressDialog.show(RequestActivity.this, "", "جاري اضافة الطلب الى سلة المشتريات", true, true);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progress.dismiss();
                                finish();
                                onBackPressed();
                            }
                        }, 3000);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "الانترنت عندك ضعيف", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home) {
            finish();
            onBackPressed();
        }
        return true;
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
        });

        AlertDialog alertDialog = builder.create();
        try {
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
