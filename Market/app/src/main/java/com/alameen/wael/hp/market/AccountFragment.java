package com.alameen.wael.hp.market;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

import de.hdodenhof.circleimageview.CircleImageView;


public class AccountFragment extends Fragment implements OnMapReadyCallback {

    private CircleImageView tImage;
    private TextView tName, tEmail, tPhone;
    private static  String name, mail, phone, lati, longi;
    private Bitmap image;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BackgroundTask backgroundTask = new BackgroundTask(getContext());
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("e_mail", 0);
        String email = sharedPreferences.getString("email", "");
        backgroundTask.execute(email);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tImage = (CircleImageView) view.findViewById(R.id.t_image);
        tName = (TextView) view.findViewById(R.id.t_name);
        tEmail = (TextView) view.findViewById(R.id.t_email);
        tPhone = (TextView) view.findViewById(R.id.t_phone);
        Button goUser = (Button) view.findViewById(R.id.go_to_user);
        goUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), MainActivity.class));
            }
        });

        SupportMapFragment fragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        fragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        double latitude = Double.parseDouble(lati);
                        double longitude = Double.parseDouble(longi);
                        LatLng latLng = new LatLng(latitude, longitude);
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                        googleMap.addMarker(new MarkerOptions().position(latLng));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 7000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class BackgroundTask extends AsyncTask<String, Void, String> {

        private static final String INFO = MainActivity.HOST+"/trader_info.php";
        Context context;

        BackgroundTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... params) {
            String email = params[0];

            try {
                URL url = new URL(INFO);
                HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection.setRequestMethod("POST");
                httpConnection.setDoOutput(true);
                OutputStream outStream = httpConnection.getOutputStream();
                BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(outStream));

                String data = URLEncoder.encode("email", "UTF-8")+"="+URLEncoder.encode(email, "UTF-8");
                buffer.write(data);
                buffer.flush();
                buffer.close();
                outStream.close();

                InputStream inputStream = httpConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }

                String json_string = stringBuilder.toString().trim();
                JSONObject object = new JSONObject(json_string);
                JSONArray jsonArray = object.getJSONArray("server_response");
                int i = 0;

                while (i < jsonArray.length()) {
                    JSONObject jsonObj = jsonArray.getJSONObject(i);
                    i++;
                    name = jsonObj.getString("name");
                    mail = jsonObj.getString("email");
                    image = stringToBitMap(jsonObj.getString("image"));
                    lati = jsonObj.getString("lati");
                    longi = jsonObj.getString("longi");
                    phone = jsonObj.getString("phone");
                }

                httpConnection.disconnect();
                inputStream.close();
                buffer.close();


                inputStream.close();

                return "تم اضافة المنتج";

            } catch (IOException e) {
                Log.d("IOException", e.getMessage());
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String msg) {
            super.onPostExecute(msg);
            try {
                tName.setText(name);
                tEmail.setText(mail);
                tImage.setImageBitmap(image);
                tPhone.setText(phone);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
