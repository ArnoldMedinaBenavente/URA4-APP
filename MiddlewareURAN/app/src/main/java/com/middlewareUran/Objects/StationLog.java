package com.middlewareUran.Objects;

public class StationLog {

    int idRegistry;
    String id;
    String idSpot;
    String codeSpot;
    String nameSpot;
    String idStation;
    String codeStation;
    String idStaff;
    String codeStaff;
    String nameStaff;
    String start_at;
    String status;

    public StationLog(int idRegistry, String id, String idSpot, String codeSpot, String nameSpot, String idStation,
                      String codeStation, String idStaff, String codeStaff, String nameStaff, String start_at, String status) {
        this.idRegistry = idRegistry;
        this.id = id;
        this.idSpot = idSpot;
        this.codeSpot = codeSpot;
        this.nameSpot = nameSpot;
        this.idStation = idStation;
        this.codeStation = codeStation;
        this.idStaff = idStaff;
        this.codeStaff = codeStaff;
        this.nameStaff = nameStaff;
        this.start_at = start_at;
        this.status = status;
    }

    public int getIdRegistry() {
        return idRegistry;
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

    public String getCodeSpot() {
        return codeSpot;
    }


    public void setCodeSpot(String codeSpot) {
        this.codeSpot = codeSpot;
    }

    public String getNameSpot() {
        return nameSpot;
    }

    public void setNameSpot(String nameSpot) {
        this.nameSpot = nameSpot;
    }

    public String getIdStation() {
        return idStation;
    }

    public void setIdStation(String idStation) {
        this.idStation = idStation;
    }

    public String getCodeStation() {
        return codeStation;
    }

    public void setCodeStation(String codeStation) {
        this.codeStation = codeStation;
    }

    public String getIdStaff() {
        return idStaff;
    }

    public void setIdStaff(String idStaff) {
        this.idStaff = idStaff;
    }

    public String getCodeStaff() {
        return codeStaff;
    }

    public void setCodeStaff(String codeStaff) {
        this.codeStaff = codeStaff;
    }

    public String getNameStaff() {
        return nameStaff;
    }

    public void setNameStaff(String nameStaff) {
        this.nameStaff = nameStaff;
    }

    public String getStart_at() {
        return start_at;
    }

    public void setStart_at(String start_at) {
        this.start_at = start_at;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
