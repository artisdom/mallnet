package com.alameen.wael.hp.market;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class TraderUIActivity extends AppCompatActivity {

    ViewPager uiViewPager;
    TabLayout uiTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trader_ui);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.t_section));

        uiViewPager = (ViewPager)findViewById(R.id.ui_viewpager);
        uiTabLayout = (TabLayout)findViewById(R.id.ui_tab);
        Pager pager = new Pager(getSupportFragmentManager());
        pager.add(new TraderUIFragment(), getString(R.string.add_sales));
        pager.add(new SaleRequestsFragment(), getString(R.string.sale_request));
        pager.add(new AccountFragment(), getString(R.string.account));

        uiViewPager.setAdapter(pager);
        uiViewPager.setOffscreenPageLimit(3);
        uiTabLayout.setupWithViewPager(uiViewPager);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_items_from_trader, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_add) {
            addProducts();
        } else if(id == R.id.action_logout) {
            logoutDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    private void logoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.log_out_msg);
        builder.setCancelable(true);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences sharedPreferences = getSharedPreferences("login_check", MODE_PRIVATE);
                boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);

                if(isLoggedIn) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("is_logged_in", false);
                    editor.commit();
                }

                startActivity(new Intent(TraderUIActivity.this, MainActivity.class));
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

    private void addProducts() {
        Intent in = new Intent(this, FirstFormActivity.class);
        startActivity(in);
    }
}
