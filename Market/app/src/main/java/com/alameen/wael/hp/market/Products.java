package com.alameen.wael.hp.market;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

class Products {

    private String productName;
    private int productImage, productPrice, likesCounter = 0;
    private Bitmap proImage;
    private String currency;

    Products(String productName, int productImage, int productPrice, int likesCounter) {
        setProductName(productName);
        setProductPrice(productPrice);
        setProductImage(productImage);
        setLikesCounter(likesCounter);
    }

    Products(String productName, Bitmap proImage, String productPrice, String currency, String likesCounter) {
        setProductName(productName);
        setProductPrice(Integer.parseInt(productPrice));
        setProImage(proImage);
        //setLikesCounter(Integer.parseInt(likesCounter));
        setCurrency(currency);
    }

    Products(Bitmap proImage) {
        setProImage(proImage);
    }

    int getLikesCounter() {
        return likesCounter;
    }

    private void setLikesCounter(int likesCounter) {
        this.likesCounter = likesCounter;
    }

    int getProductImage() {
        return productImage;
    }

    private void setProductImage(int productImage) {
        this.productImage = productImage;
    }

    int getProductPrice() {
        return productPrice;
    }

    private void setProductPrice(int productPrice) {
        this.productPrice = productPrice;
    }

    String getProductName() {
        return productName;
    }

    private void setProductName(String productName) {
        this.productName = productName;
    }

    Bitmap getProImage() {
        return proImage;
    }

    private void setProImage(Bitmap proImage) {
        this.proImage = proImage;
    }

    public String getCurrency() {
        return currency;
    }

    private void setCurrency(String currency) {
        this.currency = currency;
    }
}
