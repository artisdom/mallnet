package com.alameen.wael.hp.market;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
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

public class ConfirmCodeActivity extends AppCompatActivity implements View.OnClickListener {

    Button confirmCode;
    AutoCompleteTextView enterCode;
    TextView retry;
    String email, password, phone, code;
    ProgressDialog progressDialog;
    protected static int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_code);

        confirmCode = (Button) findViewById(R.id.confirm_code);
        enterCode = (AutoCompleteTextView) findViewById(R.id.enter_code);
        retry = (TextView) findViewById(R.id.retry);

        confirmCode.setOnClickListener(this);
        final String emailAgain = getIntent().getExtras().getString("email");
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter++;

                if (counter != 3) {
                    new AsyncTask<String, Void, Void>() {
                        final String TEMP_URL = MainActivity.HOST + "/temp.php";

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

                                String data = URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8");
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
                    }.execute(emailAgain);
                } else {
                    Toast.makeText(getApplicationContext(), "طلب رمز تاكيد 3 مرات فقط", Toast.LENGTH_SHORT).show();
                    retry.setClickable(false);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        moveTaskToBack(true);
    }

    private void createProgress() {
        progressDialog = ProgressDialog.show(this, "", "جار التاكيد", true, true);
    }

    private void dismissProgress() {
        progressDialog.dismiss();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.confirm_code:
                code = enterCode.getText().toString();
                if(TextUtils.isEmpty(code)) {
                    Snackbar.make(view, "الرجاء ادخال رمز التاكيد", Snackbar.LENGTH_SHORT).show();

                } else {
                    email = getIntent().getExtras().getString("email");
                    password = getIntent().getExtras().getString("password");
                    phone = getIntent().getExtras().getString("phone");
                    BackgroundTask backgroundTask = new BackgroundTask();
                    backgroundTask.execute(email, password, phone, code);
                }
                break;
        }
    }

    class BackgroundTask extends AsyncTask<String, Void, String> {

        private static final String reg_URL = MainActivity.HOST+"/match_codes.php";
        private String head, message;
        String email, pass, phoneNumber, code;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            createProgress();
        }

        @Override
        protected String doInBackground(String... params) {
            email = params[0];
            pass = params[1];
            phoneNumber = params[2];
            code = params[3];
            Log.d("code", code);
            Log.d("phone", phone);

            try {
                URL url = new URL(reg_URL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

                String data = URLEncoder.encode("entered_code", "UTF-8")+"="+URLEncoder.encode(code, "UTF-8");
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                InputStream inputStream = httpURLConnection.getInputStream();
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
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    i++;
                    head = jsonObject.getString("head");
                    message = jsonObject.getString("message");
                }

                inputStream.close();
                httpURLConnection.disconnect();

                return message;

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String message) {
            dismissProgress();
            try {
                if (message.equals("الكود غير صحيح")) {
                    Toast.makeText(getApplicationContext(), "" + message, Toast.LENGTH_SHORT).show();

                } else {
                    new AsyncTask<String, Void, Void>() {
                        final String CODE_URL = MainActivity.HOST+"/delete_code.php";

                        @Override
                        protected Void doInBackground(String... params) {
                            String check = "true";

                            try {
                                URL url = new URL(CODE_URL);
                                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                                httpURLConnection.setRequestMethod("POST");
                                httpURLConnection.setDoOutput(true);
                                OutputStream outputStream = httpURLConnection.getOutputStream();
                                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

                                String data = URLEncoder.encode("check", "UTF-8")+"="+URLEncoder.encode(check, "UTF-8");
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
                    }.execute();

                    Intent intent = new Intent(ConfirmCodeActivity.this, TraderInsertInfoActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("password", pass);
                    intent.putExtra("phone_no", phoneNumber);
                    startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "خطا في الاتصال اعد المحاولة", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
