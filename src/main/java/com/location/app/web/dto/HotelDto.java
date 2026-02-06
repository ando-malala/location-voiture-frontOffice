package com.location.app.web.dto;

public class HotelDto {

    private Integer id;
    private String nom;

    public HotelDto() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
}
