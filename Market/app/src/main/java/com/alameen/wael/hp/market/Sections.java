package com.alameen.wael.hp.market;


import android.graphics.Bitmap;

class Sections {

    private String secTitle;
    private int secImage;

    Sections(String title, int image) {
        setSecTitle(title);
        setSecImage(image);
    }


    int getSecImage() {
        return secImage;
    }

    private void setSecImage(int secImage) {
        this.secImage = secImage;
    }

    String getSecTitle() {
        return secTitle;
    }

    private void setSecTitle(String secTitle) {
        this.secTitle = secTitle;
    }
}
