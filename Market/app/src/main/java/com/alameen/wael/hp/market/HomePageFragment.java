package com.alameen.wael.hp.market;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;


public class HomePageFragment extends Fragment {

    private ImageView mImage1;
    private ImageView mImage2;
    private ImageView mImage3;
    private ImageView mImage4;
    private ProgressBar progressBar;
    private ImageButton next, last;
    ViewPager viewPager;
    private List<Bitmap> bitmapList = new ArrayList<>();
    private Slider adapter;
    Bitmap image1, image2, image3, image4, image5, image6, image7, image8;
    String name1, name2, name3, name4, name5, name6, name7, name8;

    public HomePageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConnectTask connectTask = new ConnectTask();
        connectTask.execute();

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        NetworkInfo.State mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();

        if (wifi == NetworkInfo.State.DISCONNECTED || mobile == NetworkInfo.State.DISCONNECTED) {
            showWarnDialog(wifi, mobile);
        }
    }

    private void showWarnDialog(NetworkInfo.State wifi, NetworkInfo.State mobile) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(false);
        builder.setMessage("الانترنت عندك مكطوع");
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton(getString(R.string.retry), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo.State wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
                NetworkInfo.State mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();

                if (wifi == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTED) {
                    Dialog dialog = builder.create();
                    dialog.dismiss();
                    startActivity(new Intent(getActivity(), MainActivity.class));

                } else {
                    if (wifi == NetworkInfo.State.DISCONNECTED || mobile == NetworkInfo.State.DISCONNECTED) {
                        Dialog dialog = builder.create();
                        dialog.show();

                    } else {
                        dialogInterface.dismiss();
                        startActivity(new Intent(getActivity(), MainActivity.class));
                    }
                }
            }
        });

        if (wifi == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTED) {
            Dialog dialog = builder.create();
            dialog.dismiss();
        } else {
            Dialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_page, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        mImage1 = (ImageView) view.findViewById(R.id.image_static1);
        mImage2 = (ImageView) view.findViewById(R.id.image_static2);
        mImage3 = (ImageView) view.findViewById(R.id.image_static3);
        mImage4 = (ImageView) view.findViewById(R.id.image_static4);
        progressBar = (ProgressBar) view.findViewById(R.id.load_progress_21);

        viewPager = (ViewPager) view.findViewById(R.id.ads_pager);
        viewPager.setOffscreenPageLimit(4);
        adapter = new Slider(getContext());
        viewPager.setAdapter(adapter);
        TabLayout tab = (TabLayout) view.findViewById(R.id.tabDots);
        tab.setupWithViewPager(viewPager, true);
        adapter.notifyDataSetChanged();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new MyTimer(), 5000, 5000);

        mImage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), LogosDetailsActivity.class);
                intent.putExtra("logo", name5);
                startActivity(intent);
            }
        });

        mImage2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), LogosDetailsActivity.class);
                intent.putExtra("logo", name6);
                startActivity(intent);
            }
        });

        mImage3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), LogosDetailsActivity.class);
                intent.putExtra("logo", name7);
                startActivity(intent);
            }
        });

        mImage4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), LogosDetailsActivity.class);
                intent.putExtra("logo", name8);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        NetworkInfo.State mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        if (wifi == NetworkInfo.State.DISCONNECTED || mobile == NetworkInfo.State.DISCONNECTED) {
            showWarnDialog(wifi, mobile);
        }
    }

    class MyTimer extends TimerTask {

        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (viewPager.getCurrentItem() == 0) {
                        viewPager.setCurrentItem(1);
                    } else if (viewPager.getCurrentItem() == 1) {
                        viewPager.setCurrentItem(2);
                    } else if (viewPager.getCurrentItem() == 2) {
                        viewPager.setCurrentItem(3);
                    } else if (viewPager.getCurrentItem() == 3) {
                        viewPager.setCurrentItem(0);
                    }
                }
            });
        }
    }

    class Slider extends PagerAdapter {
        Context context;
        LayoutInflater layoutInflater;

        Slider(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return bitmapList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            try {
                layoutInflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
                View itemView = layoutInflater.inflate(R.layout.manual_slide, container, false);
                ImageView slideImage = (ImageView) itemView.findViewById(R.id.image_in_slide);
                try {
                    slideImage.setImageBitmap(bitmapList.get(position));
                    container.addView(itemView);
                } catch (Exception e) {
                    e.getCause();
                    e.printStackTrace();
                }


                slideImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            if (position == 0) {
                                Intent intent = new Intent(getActivity(), LogosDetailsActivity.class);
                                intent.putExtra("logo", name1);
                                startActivity(intent);
                            } else if (position == 1) {
                                Intent intent = new Intent(getActivity(), LogosDetailsActivity.class);
                                intent.putExtra("logo", name2);
                                startActivity(intent);
                            } else if (position == 2) {
                                Intent intent = new Intent(getActivity(), LogosDetailsActivity.class);
                                intent.putExtra("logo", name3);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(getActivity(), LogosDetailsActivity.class);
                                intent.putExtra("logo", name4);
                                startActivity(intent);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            e.getCause();
                        }
                    }
                });

                return itemView;

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "خطا في التحميل", Toast.LENGTH_SHORT).show();
            }

            return null;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            try {
                container.removeView((LinearLayout) object);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "خطا في التحميل", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setProgressBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progressBar.setProgress(50, true);
        }
    }

    public void endProgress() {
        progressBar.setVisibility(View.GONE);
    }

    class ConnectTask extends AsyncTask<Void, Void, Void> {

        private static final String ADS_URL = MainActivity.HOST+"/ads.php";

        @Override
        protected void onPreExecute() {
            setProgressBar();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                URL url = new URL(ADS_URL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
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
                    image1 = stringToBitMap(jsonObj.getString("image1"));
                    image2 = stringToBitMap(jsonObj.getString("image2"));
                    image3 = stringToBitMap(jsonObj.getString("image3"));
                    image4 = stringToBitMap(jsonObj.getString("image4"));
                    image5 = stringToBitMap(jsonObj.getString("image5"));
                    image6 = stringToBitMap(jsonObj.getString("image6"));
                    image7 = stringToBitMap(jsonObj.getString("image7"));
                    image8 = stringToBitMap(jsonObj.getString("image8"));
                    name1 = jsonObj.getString("name1");
                    name2 = jsonObj.getString("name2");
                    name3 = jsonObj.getString("name3");
                    name4 = jsonObj.getString("name4");
                    name5 = jsonObj.getString("name5");
                    name6 = jsonObj.getString("name6");
                    name7 = jsonObj.getString("name7");
                    name8 = jsonObj.getString("name8");

                    bitmapList.add(0, image1);
                    bitmapList.add(1, image2);
                    bitmapList.add(2, image3);
                    bitmapList.add(3, image4);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            endProgress();
            mImage1.setImageBitmap(image5);
            mImage2.setImageBitmap(image6);
            mImage3.setImageBitmap(image7);
            mImage4.setImageBitmap(image8);
            adapter.notifyDataSetChanged();
        }

        private Bitmap stringToBitMap(String encodedString){
            byte [] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        }
    }
}
