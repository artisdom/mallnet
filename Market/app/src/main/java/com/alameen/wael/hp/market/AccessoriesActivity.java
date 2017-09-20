package com.alameen.wael.hp.market;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class AccessoriesActivity extends AppCompatActivity {

    ViewPager accViewPager;
    TabLayout accTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accessories);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.accessories));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        accViewPager = (ViewPager)findViewById(R.id.acc_viewpager);
        accTabLayout = (TabLayout)findViewById(R.id.acc_tab);
        Pager pager = new Pager(getSupportFragmentManager());
        pager.add(new GlassesFragment(), getString(R.string.glasses));
        pager.add(new WatchesFragment(), getString(R.string.watches));
        pager.add(new JeweleryFragment(), getString(R.string.jewellry));

        accViewPager.setAdapter(pager);
        accViewPager.setOffscreenPageLimit(3);
        accTabLayout.setupWithViewPager(accViewPager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
