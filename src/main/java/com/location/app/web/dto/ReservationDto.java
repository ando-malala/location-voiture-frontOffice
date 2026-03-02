package com.location.app.web.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public class ReservationDto {

    private Long id;

    /** L'hôtel renvoyé en objet imbriqué par le back-office. */
    private HotelDto hotel;

    private String idClient;
    private Integer nbPassager;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateHeure;

    public ReservationDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public HotelDto getHotel() { return hotel; }
    public void setHotel(HotelDto hotel) { this.hotel = hotel; }

    public String getIdClient() { return idClient; }
    public void setIdClient(String idClient) { this.idClient = idClient; }

    public Integer getNbPassager() { return nbPassager; }
    public void setNbPassager(Integer nbPassager) { this.nbPassager = nbPassager; }

    public LocalDateTime getDateHeure() { return dateHeure; }
    public void setDateHeure(LocalDateTime dateHeure) { this.dateHeure = dateHeure; }
}
