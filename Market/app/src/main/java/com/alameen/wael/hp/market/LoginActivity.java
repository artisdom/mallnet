package com.alameen.wael.hp.market;

import android.app.ProgressDialog;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

public class LoginActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    private List<String> emailsList = new ArrayList<>();
    private String email, password, phone;
    private static boolean isFound = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final AutoCompleteTextView mPhoneView = (AutoCompleteTextView) findViewById(R.id.phone);
        final AutoCompleteTextView mEmail = (AutoCompleteTextView) findViewById(R.id.email);
        final AutoCompleteTextView mPassword = (AutoCompleteTextView) findViewById(R.id.password);


        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                email = mEmail.getText().toString().trim();
                password = mPassword.getText().toString().trim();
                phone = mPhoneView.getText().toString().trim();
                Log.d("input1", email);
                Log.d("input2", password);
                Log.d("input3", phone);

                if(email.isEmpty() || password.isEmpty() || phone.isEmpty() || (email.isEmpty() && password.isEmpty() && phone.isEmpty())) {
                    Snackbar.make(view, "ادخل المعلومات رجاءا", Snackbar.LENGTH_SHORT).show();
                } else if (!email.contains("@gmail.com") && !email.contains("@yahoo.com")) {
                    Snackbar.make(view, "الايميل غير صالح", Snackbar.LENGTH_SHORT).show();
                } else {
                    new AsyncTask<String, Void, String>() {
                        final String EMAIL_URL = MainActivity.HOST+"/check_email_exist.php";

                        @Override
                        protected String doInBackground(String... params) {
                            String input_email = params[0];
                            try {
                                URL url = new URL(EMAIL_URL);
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
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String email = jsonObject.getString("email");
                                    emailsList.add(email);
                                    i++;
                                }

                                httpConnection.disconnect();
                                inputStream.close();
                                buffer.close();

                                return input_email;

                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(String input_email) {
                            super.onPostExecute(input_email);
                            for (String item : emailsList) {
                                if (item.equalsIgnoreCase(input_email)) {
                                    isFound = true;
                                    break;
                                } else {
                                    isFound = false;
                                }
                            }

                            if (isFound) {
                                Toast.makeText(getApplicationContext(), "الايميل موجود اصلا", Toast.LENGTH_SHORT).show();
                            } else {
                                BackgroundTask task = new BackgroundTask();
                                task.execute(input_email, password, phone);
                                addToTemp();
                            }
                        }

                    }.execute(email);
                }
            }
        });
    }

    private void addToTemp() {
        SharedPreferences sharedPreferences1 = getSharedPreferences("e_mail", MODE_PRIVATE);
        SharedPreferences.Editor editor1 = sharedPreferences1.edit();
        editor1.putString("email", email);
        editor1.commit();

        new AsyncTask<String, Void, Void>() {
            final String TEMP_URL = MainActivity.HOST+"/temp.php";

            @Override
            protected Void doInBackground(String... params) {
                String email = params[0];

                try {
                    URL url = new URL(TEMP_URL);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

                    String data = URLEncoder.encode("email", "UTF-8")+"="+URLEncoder.encode(email, "UTF-8");
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
        }.execute(email);
    }

    private void createProgress() {
        progressDialog = ProgressDialog.show(this, "", "جار التاكيد", true, true);
    }

    private void dismissProgress() {
        progressDialog.dismiss();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == android.R.id.home) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    class BackgroundTask extends AsyncTask<String, Integer, Void> {

        private static final String phone_URL = MainActivity.HOST+"/send_phone.php";
        List<Integer> phones = new ArrayList<>();
        String email, password, phone;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            createProgress();
        }

        @Override
        protected Void doInBackground(String... params) {
            email = params[0];
            password = params[1];
            phone = params[2];

            try {
                URL url = new URL(phone_URL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

                String data = URLEncoder.encode("phone", "UTF-8")+"="+URLEncoder.encode(phone, "UTF-8");
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                inputStream.close();

                publishProgress(50);
                httpURLConnection.disconnect();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

            phones.add(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dismissProgress();

            Intent intent = new Intent(LoginActivity.this, ConfirmCodeActivity.class);
            intent.putExtra("email", email);
            intent.putExtra("password", password);
            intent.putExtra("phone", phone);
            startActivity(intent);
        }
    }
}

