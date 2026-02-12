package com.location.app.web.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * DTO correspondant au JSON renvoyé par le backoffice :
 * {
 *   "id": 1,
 *   "hotel": { "id": 1, "nom": "..." },
 *   "idClient": "C001",
 *   "nbPassager": 3,
 *   "dateHeure": "2026-02-10"
 * }
 */
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
