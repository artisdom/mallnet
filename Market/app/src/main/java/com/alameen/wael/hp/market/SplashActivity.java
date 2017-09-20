package com.alameen.wael.hp.market;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class SplashActivity extends AppCompatActivity {

    ImageView car1, car2, tire1, tire2, logo1, logo2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        car1 = (ImageView) findViewById(R.id.car1);
//        car2 = (ImageView) findViewById(R.id.car2);
//        tire1 = (ImageView) findViewById(R.id.tire1);
//        tire2 = (ImageView) findViewById(R.id.tire2);
//        logo1 = (ImageView) findViewById(R.id.logo1);
//        logo2 = (ImageView) findViewById(R.id.logo2);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                Animation anim1 = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.translate);
//                Animation anim2 = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.alpha);
//                anim2.reset();
//
//                RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.splash_layout);
//                relativeLayout.clearAnimation();
//                relativeLayout.startAnimation(anim2);
//
//                anim1.reset();
//                car1.clearAnimation();
//                car1.startAnimation(anim1);
//
//                anim1.reset();
//                car2.clearAnimation();
//                car2.startAnimation(anim1);
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
        }, 4000);
    }
}
