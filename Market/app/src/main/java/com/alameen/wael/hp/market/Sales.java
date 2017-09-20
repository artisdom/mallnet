package com.alameen.wael.hp.market;


public class Sales {

    private String name;
    private int price, num;

    Sales(String name, int price, int num) {
        setName(name);
        setPrice(price);
        setNum(num);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNum() {
        return num;
    }

    private void setNum(int num) {
        this.num = num;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
