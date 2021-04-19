package com.swatch.smartwatch.sensors;

import java.util.Date;

public class Max30102 {
    private int counter=0;
    private String type = "";
    private StringBuilder spo2 = new StringBuilder();
    private StringBuilder hr = new StringBuilder();
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

    public StringBuilder getSpo2() {
        return spo2;
    }

    public void setSpo2(StringBuilder spo2) {
        this.spo2 = spo2;
    }

    public StringBuilder getHr() {
        return hr;
    }

    public void setHr(StringBuilder hr) {
        this.hr = hr;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
