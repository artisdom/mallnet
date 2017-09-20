package com.alameen.wael.hp.market;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class ClothesActivity extends AppCompatActivity {

    ViewPager clothesViewPager;
    TabLayout clothesTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clothes);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.clothes));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        clothesViewPager = (ViewPager)findViewById(R.id.clothes_viewpager);
        clothesTab = (TabLayout)findViewById(R.id.clothes_tab);

        Pager pager = new Pager(getSupportFragmentManager());
        pager.add(new MenClothesFragment(), getString(R.string.men_clothes));
        pager.add(new WomenClothesFragment(), getString(R.string.women_clothes));
        pager.add(new BoysClothesFragment(), getString(R.string.boys_clothes));
        pager.add(new GirlsClothesFragment(), getString(R.string.girls_clothes));
        clothesViewPager.setAdapter(pager);
        clothesViewPager.setOffscreenPageLimit(4);
        clothesTab.setupWithViewPager(clothesViewPager);
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
