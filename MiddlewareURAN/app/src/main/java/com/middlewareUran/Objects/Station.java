package com.middlewareUran.Objects;

import android.widget.Button;

import java.util.ArrayList;

public class Station {
    int idRegistry;
    String id;
    String idSpot;
    String code;
    StationLog stationLog;
    Button button;
    ArrayList<String> arrayTags;

    public Station(int idRegistry, String id, String idSpot, String code, StationLog stationLog, Button button, ArrayList<String> arrayTags) {
        this.idRegistry = idRegistry;
        this.id = id;
        this.idSpot = idSpot;
        this.code = code;
        this.stationLog = stationLog;
        this.button = button;
        this.arrayTags = arrayTags;
    }

    public ArrayList<String> getArrayTags() {
        return arrayTags;
    }

    public void setArrayTags(ArrayList<String> arrayTags) {
        this.arrayTags = arrayTags;
    }

    public Button getButton() {
        return button;
    }

    public void setButton(Button button) {
        this.button = button;
    }

    public int getIdRegistry() {
        return idRegistry;
    }

    public StationLog getStationLog() {
        return stationLog;
    }

    public void setStationLog(StationLog stationLog) {
        this.stationLog = stationLog;
    }

    public void setIdRegistry(int idRegistry) {
        this.idRegistry = idRegistry;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdSpot() {
        return idSpot;
    }

    public void setIdSpot(String idSpot) {
        this.idSpot = idSpot;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
