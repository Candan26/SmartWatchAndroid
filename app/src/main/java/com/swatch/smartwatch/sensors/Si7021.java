package com.swatch.smartwatch.sensors;

import java.util.Date;

public class Si7021 {
    private int counter=0;
    private String type = "";
    private StringBuilder temperatureByte = new StringBuilder();
    private StringBuilder humidityByte = new StringBuilder();
    private Date date;
    private Float temperature = new Float(0);
    private Float Humidity = new Float(0);

    public Float getTemperature() { return temperature;  }

    public void setTemperature(Float temperature) { this.temperature = temperature; }

    public Float getHumidity() { return Humidity; }

    public void setHumidity(Float humidity) {  Humidity = humidity; }

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

    public StringBuilder getTemperatureByte() {
        return temperatureByte;
    }

    public void setTemperatureByte(StringBuilder temperatureByte) {
        this.temperatureByte = temperatureByte;
    }

    public StringBuilder getHumidityByte() {
        return humidityByte;
    }

    public void setHumidityByte(StringBuilder humidityByte) {
        this.humidityByte = humidityByte;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Si7021{" +
                "counter=" + counter +
                ", type='" + type + '\'' +
                ", temperatureByte=" + temperatureByte +
                ", humidityByte=" + humidityByte +
                ", date=" + date +
                '}';
    }
}
