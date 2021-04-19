package com.swatch.smartwatch.sensors;

import java.util.Date;

public class Max3003 {
    private int counter=0;
    private String type = "";
    private StringBuilder ecg = new StringBuilder();
    private StringBuilder rr = new StringBuilder();
    private Date date;

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public StringBuilder getEcg() {
        return ecg;
    }

    public void setEcg(StringBuilder ecg) {
        this.ecg = ecg;
    }

    public StringBuilder getRr() {
        return rr;
    }

    public void setRr(StringBuilder rr) {
        this.rr = rr;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
