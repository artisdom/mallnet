package com.alameen.wael.hp.market;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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

public class CheckLoginActivity extends AppCompatActivity {

    private AutoCompleteTextView email, password;
    private ProgressDialog progress;
    private String message, mEmail, mPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_login);

        email = (AutoCompleteTextView) findViewById(R.id.email);
        password = (AutoCompleteTextView) findViewById(R.id.password);
        final Button check = (Button) findViewById(R.id.sign_button);
        TextView createNew = (TextView) findViewById(R.id.sign_up_now);

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEmail = email.getText().toString().trim();
                mPass = password.getText().toString().trim();

                if(TextUtils.isEmpty(mEmail) || TextUtils.isEmpty(mPass) || (TextUtils.isEmpty(mEmail) && TextUtils.isEmpty(mPass))) {
                    Toast.makeText(getApplicationContext(), "الرجاء ادخال جميع المعلومات", Toast.LENGTH_SHORT).show();
                } else {
                    String mEmail, mPass;
                    mEmail = email.getText().toString().trim();
                    mPass = password.getText().toString().trim();
                    new BackgroundTask(CheckLoginActivity.this).execute(mEmail, mPass);
                }
            }
        });

        createNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CheckLoginActivity.this, LoginActivity.class));
            }
        });
    }

    private void createProgress() {
        progress = ProgressDialog.show(this, "", "جاري التاكيد", true, true);
    }

    private void dismissProgress() {
        progress.dismiss();
    }

    class BackgroundTask extends AsyncTask<String, Void, String> {

        Context context;
        String email;
        private static final String LOG_URL = MainActivity.HOST+"/check_account.php";

        BackgroundTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            createProgress();
        }

        @Override
        protected String doInBackground(String... params) {
            email = params[0];
            String password = params[1];

            try {
                URL url = new URL(LOG_URL);
                HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection.setRequestMethod("POST");
                httpConnection.setDoOutput(true);
                OutputStream outStream = httpConnection.getOutputStream();
                BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(outStream));

                String data = URLEncoder.encode("email", "UTF-8")+"="+URLEncoder.encode(email, "UTF-8")+"&"
                        +URLEncoder.encode("password", "UTF-8")+"="+URLEncoder.encode(password, "UTF-8");

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
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    i++;
                    String head = jsonObject.getString("head");
                    message = jsonObject.getString("message");
                }

                inputStream.close();
                httpConnection.disconnect();

                return message;

            } catch (IOException e) {
                Log.d("IOException", e.getMessage());
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String message) {
            dismissProgress();
            try {
                if (message.equals("الحساب غير موجود")) {
                    Toast.makeText(getApplicationContext(), "" + message, Toast.LENGTH_SHORT).show();

                } else {
                    Intent intent = new Intent(CheckLoginActivity.this, TraderUIActivity.class);
                    startActivity(intent);
                    SharedPreferences sharedPreferences = getSharedPreferences("login_check", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("is_logged_in", true);
                    editor.commit();

                    SharedPreferences sharedPreferences1 = getSharedPreferences("e_mail", MODE_PRIVATE);
                    SharedPreferences.Editor editor1 = sharedPreferences1.edit();
                    editor1.putString("email", email);
                    editor1.commit();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "خطا في الاتصال اعد المحاولة", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
