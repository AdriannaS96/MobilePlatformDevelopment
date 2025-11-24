package org.me.gcu.cw1;

public class CurrencyItem {

    private String currencyName;
    private String currencyCode;
    private double rate;
    private String date;

    public CurrencyItem(String currencyName, String currencyCode, double rate, String date) {
        this.currencyName = currencyName;
        this.currencyCode = currencyCode;
        this.rate = rate;
        this.date = date;
    }

    public String getCurrencyName() { return currencyName; }
    public String getCurrencyCode() { return currencyCode; }
    public double getRate() { return rate; }
    public String getDate() { return date; }

    public void setCurrencyName(String name) { this.currencyName = name; }
    public void setCurrencyCode(String code) { this.currencyCode = code; }
    public void setRate(double rate) { this.rate = rate; }
    public void setDate(String date) { this.date = date; }

    @Override
    public String toString() {
        return currencyCode + " - " + currencyName + ": " + rate + "\n" + date;
    }
}
