package com.middlewareUran.Objects;

public class Spots {
    int idRegistry;
    String id;
    String code;
    String name;
    String status;
    String selected;

    public Spots(int idRegistry, String id, String code, String name, String status, String selected) {
        this.idRegistry = idRegistry;
        this.id = id;
        this.code = code;
        this.name = name;
        this.status = status;
        this.selected = selected;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }
}
