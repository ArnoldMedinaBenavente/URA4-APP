package com.middlewareUran.Objects;

public class ItemReadingLog {
    String epc;

    String estado;

    public ItemReadingLog(String epc, String estado) {
        this.epc = epc;

        this.estado = estado;
    }

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }



    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
