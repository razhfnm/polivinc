package com.example.polivinc;

public class Task {
    private String name;
    private int progress;

    public Task() {
        // Constructor vacío para Firebase
    }

    public Task(String name, int progress) {
        this.name = name;
        this.progress = progress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
