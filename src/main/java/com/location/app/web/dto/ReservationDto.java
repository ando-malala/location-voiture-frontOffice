package com.location.app.web.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

public class ReservationDto {

    private Long id;
    private HotelDto hotel;
    private String idClient;
    private Integer nbPassager;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateHeure;

    public ReservationDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public HotelDto getHotel() {
        return hotel;
    }

    public void setHotel(HotelDto hotel) {
        this.hotel = hotel;
    }

    public String getIdClient() {
        return idClient;
    }

    public void setIdClient(String idClient) {
        this.idClient = idClient;
    }

    public Integer getNbPassager() {
        return nbPassager;
    }

    public void setNbPassager(Integer nbPassager) {
        this.nbPassager = nbPassager;
    }

    public LocalDate getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(LocalDate dateHeure) {
        this.dateHeure = dateHeure;
    }
}
