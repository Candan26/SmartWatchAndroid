package com.swatch.smartwatch.sensors;

import java.util.Date;

public class SkinResistance {
    private int counter=0;
    private String type = "";
    private StringBuilder skinResistance = new StringBuilder();
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

    public StringBuilder getSkinResistance() {
        return skinResistance;
    }

    public void setSkinResistance(StringBuilder skinResistance) {
        this.skinResistance = skinResistance;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "SkinResistance{" +
                "counter=" + counter +
                ", type='" + type + '\'' +
                ", skinResistance=" + skinResistance +
                ", date=" + date +
                '}';
    }
}
