package com.middlewareUran.Objects;

public class Cages {
    int idRegistry;
    String id,id_customer,code,manager,status,selected,name_customer;

    public Cages(int idRegistry, String id, String id_customer, String code, String manager, String status, String selected, String name_customer) {
        this.idRegistry = idRegistry;
        this.id = id;
        this.id_customer = id_customer;
        this.code = code;
        this.manager = manager;
        this.status = status;
        this.selected = selected;
        this.name_customer = name_customer;
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

    public String getId_customer() {
        return id_customer;
    }

    public void setId_customer(String id_customer) {
        this.id_customer = id_customer;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
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

    public String getName_customer() {
        return name_customer;
    }

    public void setName_customer(String name_customer) {
        this.name_customer = name_customer;
    }

}
