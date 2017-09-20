package com.alameen.wael.hp.market;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class ElectronicsActivity extends AppCompatActivity {

    ViewPager eleViewPager;
    TabLayout eleTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_electronics);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(getString(R.string.electronics));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        eleViewPager = (ViewPager) findViewById(R.id.electronics_viewpager);
        eleTabLayout = (TabLayout) findViewById(R.id.electronics_tab);

        Pager pager = new Pager(getSupportFragmentManager());
        pager.add(new TVFragment(), getString(R.string.tvs));
        pager.add(new SmartPhonesFragment(), getString(R.string.smart_phones));
        pager.add(new TabletsFragment(), getString(R.string.tablets));
        pager.add(new AccessoriesMobileFragment(), getString(R.string.sound));
        eleViewPager.setAdapter(pager);
        eleViewPager.setOffscreenPageLimit(4);
        eleTabLayout.setupWithViewPager(eleViewPager);
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
