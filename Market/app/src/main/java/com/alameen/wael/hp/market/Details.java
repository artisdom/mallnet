package com.alameen.wael.hp.market;


class Details {

    private String names, descriptions, currency;
    private int prices, like;

    Details(String names, String descriptions, int prices, String currency, int like) {
        setName(names);
        setDescriptions(descriptions);
        setPrices(prices);
        setLike(like);
        setCurrency(currency);
    }

    public int getPrices() {
        return prices;
    }

    private void setPrices(int prices) {
        this.prices = prices;
    }

    public String getNames() {
        return names;
    }

    public void setName(String names) {
        this.names = names;
    }

    public int getLike() {
        return like;
    }

    private void setLike(int like) {
        this.like = like;
    }

    public String getDescriptions() {
        return descriptions;
    }

    private void setDescriptions(String descriptions) {
        this.descriptions = descriptions;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
