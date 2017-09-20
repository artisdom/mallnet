package com.alameen.wael.hp.market;


import android.graphics.Bitmap;

public class Traders {

    private String traderName;
    private Bitmap traderImage;

    Traders(String name, Bitmap image) {
        setTraderName(name);
        setTraderImage(image);
    }

    public String getTraderName() {
        return traderName;
    }

    private void setTraderName(String traderName) {
        this.traderName = traderName;
    }

    public Bitmap getTraderImage() {
        return traderImage;
    }

    private void setTraderImage(Bitmap traderImage) {
        this.traderImage = traderImage;
    }
}
