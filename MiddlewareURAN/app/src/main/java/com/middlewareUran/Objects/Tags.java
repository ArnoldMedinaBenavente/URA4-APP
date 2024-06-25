package com.middlewareUran.Objects;

public class Tags {
    String epc;
    String antena;
    String conut;
    String chofer;
    String placas;

    public Tags(String epc, String antena, String conut, String chofer, String placas) {
        this.epc = epc;
        this.antena = antena;
        this.conut = conut;
        this.chofer = chofer;
        this.placas = placas;
    }

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public String getAntena() {
        return antena;
    }

    public void setAntena(String antena) {
        this.antena = antena;
    }

    public String getConut() {
        return conut;
    }

    public void setConut(String conut) {
        this.conut = conut;
    }

    public String getChofer() {
        return chofer;
    }

    public void setChofer(String chofer) {
        this.chofer = chofer;
    }

    public String getPlacas() {
        return placas;
    }

    public void setPlacas(String placas) {
        this.placas = placas;
    }
}
