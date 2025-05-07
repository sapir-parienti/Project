package com.example.project;

public class message {
    private String text;
    private String name;
    private int minute;
    private int hour;

    public message (String text, String name, int minute, int hour){
        this.text=text;
        this.name=name;
        this.minute=minute;
        this.hour=hour;
    }

    public String getText() {
        return text;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public String getName() {
        return name;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setText(String text) {
        this.text = text;
    }
}
