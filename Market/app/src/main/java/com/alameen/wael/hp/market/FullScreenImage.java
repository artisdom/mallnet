package com.alameen.wael.hp.market;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FullScreenImage extends AppCompatActivity {

    private static boolean isZoom = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        final ImageView product_image = (ImageView) findViewById(R.id.full_screen);
        Button exit = (Button) findViewById(R.id.exit);
        TextView product_name = (TextView) findViewById(R.id.product_name);
        TextView product_price = (TextView) findViewById(R.id.product_price);

        byte[] bytes = getIntent().getByteArrayExtra("image");
        Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        String name = getIntent().getExtras().getString("name");
        String price = getIntent().getExtras().getString("price");
        String currency = getIntent().getExtras().getString("currency");

        product_image.setImageBitmap(image);
        product_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isZoom) {
                    Animation animation = AnimationUtils.loadAnimation(FullScreenImage.this, R.anim.zoom_in);
                    animation.reset();
                    product_image.startAnimation(animation);
                    isZoom = false;

                } else {
                    Animation animation = AnimationUtils.loadAnimation(FullScreenImage.this, R.anim.zoom_out);
                    animation.reset();
                    product_image.startAnimation(animation);
                    isZoom = true;
                }
            }
        });

        product_name.setText(name);

        if (currency != null) {
            if (currency.equals("الدولار الامريكي")) {
                product_price.setText(price + " $");

            } else {
                product_price.setText(price + " IQD");
            }
        }

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                onBackPressed();
            }
        });
    }
}
