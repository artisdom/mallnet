package com.alameen.wael.hp.market;

import android.Manifest;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener {

    NavigationView navigationView;
    ViewPager viewPager;
    TabLayout tabLayout;
    RecyclerView navRecycler;
    RecyclerView.Adapter navAdapter;
    List<Integer> iconsList = new ArrayList<>();
    List<String> titlesList = new ArrayList<>();
    List<Sales> salesList;
    Animation fbOpen, fbClose, rotateClockWise, rotateAntiClockWise;
    private AlertDialog alertDialog;
    private InnerDataBase innerDataBase;
    public static boolean isOpened = false;
    private static final String phone_number = "07505331632";
    public static final String HOST = "http://mallnet.me/mallnet/";
    protected static int inter_counter = 0;
    Cursor result;
    String res, phoneNUM;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final FloatingActionButton search = (FloatingActionButton) findViewById(R.id.fab_search);
        final TextView fabTitle = (TextView) findViewById(R.id.fab_title);
        fbOpen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fb_open);
        fbClose = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fb_close);
        rotateClockWise = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_clockwise);
        rotateAntiClockWise = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_anti_clockwise);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isOpened) {
                    search.startAnimation(fbClose);
                    fabTitle.setVisibility(View.GONE);
                    fab.startAnimation(rotateAntiClockWise);
                    viewPager.setAlpha(1);
                    isOpened = false;

                } else {
                    search.startAnimation(fbOpen);
                    fab.startAnimation(rotateClockWise);
                    search.setClickable(true);
                    fabTitle.setVisibility(View.VISIBLE);
                    fab.setClickable(true);
                    viewPager.setAlpha(0.2f);
                    isOpened = true;
                }
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, TradersActivity.class));
                isOpened = true;
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        String[] titles = getResources().getStringArray(R.array.nav_item_titles);
        Collections.addAll(titlesList, titles);

        iconsList.add(0, R.drawable.ic_logout_user_24dp);
        iconsList.add(1, R.drawable.ic_local_offer_black_24dp);
        iconsList.add(2, R.drawable.ic_local_phone_black_24dp);
        iconsList.add(3, R.drawable.ic_share_black_24dp);
        iconsList.add(4, R.drawable.ic_smartphone_black_24dp);

        navAdapter = new NavRecyclerAdapter(this, titlesList, iconsList, this);
        navRecycler = (RecyclerView) findViewById(R.id.nav_recylerview);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        navRecycler.setHasFixedSize(true);
        navRecycler.setLayoutManager(manager);
        navRecycler.setAdapter(navAdapter);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        tabLayout = (TabLayout) findViewById(R.id.tab);
        Pager adapter = new Pager(getSupportFragmentManager());
        HomePageFragment homePageFragment = new HomePageFragment();
        SectionsFragment sectionsFragment = new SectionsFragment();
        ThirdFragment thirdFragment = new ThirdFragment();

        adapter.add(homePageFragment, getString(R.string.home_page));
        adapter.add(sectionsFragment, getString(R.string.sections));
        adapter.add(thirdFragment, getString(R.string.trader55));
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_home_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_list_24dp);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_person_black_24dp);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        innerDataBase = new InnerDataBase(MainActivity.this);
        result = innerDataBase.showData();
        CounterBadgeDatabase counterBadgeDatabase = new CounterBadgeDatabase(MainActivity.this);

        Cursor result2 = counterBadgeDatabase.showCounterData();

        if (result2.getCount() > 0) {
            while (result2.moveToNext()) {
                inter_counter = result2.getInt(0);
            }
        }

        Log.d("inter_counter", Integer.toString(inter_counter));
        SharedPreferences pref = getSharedPreferences("this_phone", MODE_PRIVATE);
        String thisPhone = pref.getString("this_phone", "");


        new AsyncTask<String, Void, Void>() {
            final String REQ = HOST+"requests_web.php";

            @Override
            protected Void doInBackground(String... params) {
                String phone = params[0];

                try {
                    URL url = new URL(REQ);
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
                        res = json.getString("status");
                        phoneNUM = json.getString("phone");
                    }

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                try {
                    SharedPreferences pref = getSharedPreferences("this_phone", MODE_PRIVATE);
                    String thisPhone = pref.getString("this_phone", "");
                    if (res.equals("2") && phoneNUM.equals(thisPhone)) {
                        new Broadcasting().onReceive(MainActivity.this, new Intent(MainActivity.this, MainActivity.class));
                        deleteRequest(res, thisPhone);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.execute(thisPhone);
    }

    private void deleteRequest(String res, String phone) {

        new AsyncTask<String, Void, Void>() {
            final String NOT_URL = HOST+"delete_not_found.php";

            @Override
            protected Void doInBackground(String... params) {
                String res = params[0];
                String phone = params[1];

                try {
                    URL url = new URL(NOT_URL);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

                    String data = URLEncoder.encode("status", "UTF-8")+"="+URLEncoder.encode(res, "UTF-8")+"&"+
                            URLEncoder.encode("phone", "UTF-8")+"="+URLEncoder.encode(phone, "UTF-8");
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
        }.execute(res, phone);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        if (result.getCount() > 0) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setMessage("عندك مشتريات ممكمل شرائها تريد تخرج من MALL net ؟");
            builder.setPositiveButton(R.string.yes2, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    moveTaskToBack(true);
                }
            }).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });

            Dialog dialog = builder.create();
            dialog.show();
        } else {
            moveTaskToBack(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.action_basket);
        RelativeLayout relativeLayout = (RelativeLayout) item.getActionView();
        Button basket = (Button) relativeLayout.findViewById(R.id.badge_icon_button);
        TextView counter = (TextView) relativeLayout.findViewById(R.id.badge_textView);

        basket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewSales();
            }
        });

        counter.setText(Integer.toString(inter_counter));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_basket) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void viewSales() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.basket_layout, null);
        builder.setView(view);
        alertDialog = builder.create();
        alertDialog.setContentView(view);
        salesList = new ArrayList<>();
        RecyclerView recyclerView = (RecyclerView) alertDialog.findViewById(R.id.sales_recycler);
        recyclerView.setHasFixedSize(true);
        final RecyclerView.Adapter adapter = new CustomAdapter(this, salesList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        TextView sum = (TextView) alertDialog.findViewById(R.id.sum);

        innerDataBase = new InnerDataBase(this);
        Cursor result = innerDataBase.showData();
        String name, num = null, price;
        int priceNum, summ = 0;

        try {
            if (result.getCount() > 0) {

                while (result.moveToNext()) {
                    name = result.getString(0);
                    price = result.getString(1);
                    num = result.getString(2);
                    Sales sales = new Sales(name, Integer.parseInt(price), Integer.parseInt(num));
                    salesList.add(sales);
                    adapter.notifyDataSetChanged();
                }

                for (int i = 0; i < salesList.size(); i++) {
                    if (salesList.get(i).getNum() > 1) {
                        priceNum = salesList.get(i).getPrice() * salesList.get(i).getNum();
                        summ = summ + priceNum;
                    } else {
                        summ = summ + salesList.get(i).getPrice();
                    }
                }

                sum.setText("المجموع الكلي : " + Integer.toString(summ) + " دينار ");

                if (sum == null) {
                    sum.setText("المجموع الكلي : " + Integer.toString(0) + " دينار ");
                }

            } else {
                Toast.makeText(getApplicationContext(), "السلة فارغة", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "جرب مرة ثانية", Toast.LENGTH_SHORT).show();
        }

        final Button send = (Button) alertDialog.findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendItems();
            }
        });
        alertDialog.show();
    }

    private void sendItems() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.final_confirm);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.yes2, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                innerDataBase = new InnerDataBase(MainActivity.this);
                Cursor result = innerDataBase.showAllData();
                String name, trader, price, num, color, size, lat, longi, address, phone, cost, date;

                if (result.getCount() > 0) {
                    while (result.moveToNext()) {
                        name = result.getString(0);
                        trader = result.getString(1);
                        price = result.getString(2);
                        num = result.getString(3);
                        color = result.getString(4);
                        size = result.getString(5);
                        lat = result.getString(6);
                        longi = result.getString(7);
                        address = result.getString(8);
                        phone = result.getString(9);
                        cost = result.getString(10);
                        date = result.getString(11);
                        new Task().execute(name, trader, price, num, color, size, lat, longi, address, phone, cost, date);
                    }
                }

                innerDataBase.deleteRows();
                alertDialog.dismiss();
                result.close();
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putString("fragment", "sectionFragment");
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i) {
            case 0:
                SharedPreferences sharedPreferences = getSharedPreferences("login_check", MODE_PRIVATE);
                boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);

                if (isLoggedIn) {
                    startActivity(new Intent(this, TraderUIActivity.class));
                } else {
                    startActivity(new Intent(this, CheckLoginActivity.class));
                }
                break;
            case 1:
                startActivity(new Intent(this, LogosActivity.class));
                break;
            case 2:
                call();
                break;
            case 3:
                share();
                break;
            case 4:
                about();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    private void call() {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phone_number));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(intent);
    }

    private void share() {
        String message = "ويه تطبيق مول نت اتسوك وين ما جنت";
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(Intent.createChooser(shareIntent, "شارك التطبيق"));
    }

    private void about() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.about);
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

        private List<Sales> salesList;

        CustomAdapter(Context context, List<Sales> salesList) {
            LayoutInflater.from(context);
            this.salesList = salesList;
        }

        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sold_items, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(CustomAdapter.ViewHolder holder, int position) {
            Sales sales = salesList.get(position);
            holder.mName.setText(sales.getName());
            holder.mNumber.setText(Integer.toString(sales.getNum()));
            if (sales.getNum() > 1) {
                int price = sales.getPrice() * sales.getNum();
                holder.mPrice.setText(Integer.toString(price));
            } else {
                holder.mPrice.setText(Integer.toString(sales.getPrice()));
            }
        }

        @Override
        public int getItemCount() {
            return salesList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView mName, mNumber, mPrice;

            public ViewHolder(View itemView) {
                super(itemView);
                mName = (TextView) itemView.findViewById(R.id.name);
                mNumber = (TextView) itemView.findViewById(R.id.number);
                mPrice = (TextView) itemView.findViewById(R.id.price);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        innerDataBase = new InnerDataBase(this);
        Cursor result = innerDataBase.showData();
        CounterBadgeDatabase counterBadgeDatabase = new CounterBadgeDatabase(this);

        if (result.getCount() > 0) {
            Log.d("inter_counter", Integer.toString(result.getCount()));
            counterBadgeDatabase.update(result.getCount());
        } else {
            counterBadgeDatabase.update(0);
        }
    }

    class Task extends AsyncTask<String, Void, String> {

        private static final String REQUEST_URL = HOST+"/send_request.php";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String name = params[0];
            String trader = params[1];
            String price = params[2];
            String number = params[3];
            String color = params[4];
            String size = params[5];
            String lati = params[6];
            String longi = params[7];
            String address = params[8];
            String phone = params[9];
            String cost = params[10];
            String date = params[11];

            try {
                URL url = new URL(REQUEST_URL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

                String data = URLEncoder.encode("name", "UTF-8")+"="+URLEncoder.encode(name, "UTF-8")+"&"
                        +URLEncoder.encode("trader", "UTF-8")+"="+URLEncoder.encode(trader, "UTF-8")+"&"
                        +URLEncoder.encode("price", "UTF-8")+"="+URLEncoder.encode(price, "UTF-8")+"&"
                        +URLEncoder.encode("num", "UTF-8")+"="+URLEncoder.encode(number, "UTF-8")+"&"
                        +URLEncoder.encode("color", "UTF-8")+"="+URLEncoder.encode(color, "UTF-8")+"&"
                        +URLEncoder.encode("size", "UTF-8")+"="+URLEncoder.encode(size, "UTF-8")+"&"
                        +URLEncoder.encode("lati", "UTF-8")+"="+URLEncoder.encode(lati, "UTF-8")+"&"
                        +URLEncoder.encode("longi", "UTF-8")+"="+URLEncoder.encode(longi, "UTF-8")+"&"
                        +URLEncoder.encode("address", "UTF-8")+"="+URLEncoder.encode(address, "UTF-8")+"&"
                        +URLEncoder.encode("phone", "UTF-8")+"="+URLEncoder.encode(phone, "UTF-8")+"&"
                        +URLEncoder.encode("cost", "UTF-8")+"="+URLEncoder.encode(cost, "UTF-8")+"&"
                        +URLEncoder.encode("date", "UTF-8")+"="+URLEncoder.encode(date, "UTF-8");

                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                inputStream.close();
                httpURLConnection.disconnect();

                return "تم ارسال الطلب";

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String msg) {
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    private class MyTimer extends TimerTask {
        @Override
        public void run() {
            innerDataBase = new InnerDataBase(MainActivity.this);
            Cursor result = innerDataBase.showData();
            CounterBadgeDatabase counterBadgeDatabase = new CounterBadgeDatabase(MainActivity.this);

            if (result.getCount() > 0) {
                Log.d("inter_counter", Integer.toString(result.getCount()));
                counterBadgeDatabase.update(result.getCount());
            } else {
                counterBadgeDatabase.update(0);
            }
        }
    }
}
