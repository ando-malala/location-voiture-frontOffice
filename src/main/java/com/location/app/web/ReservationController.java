package com.location.app.web;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.location.app.web.dto.HotelDto;
import com.location.app.web.dto.ReservationDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.format.annotation.DateTimeFormat;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

    private final RestTemplate restTemplate;

    @Value("${reservation.api.base-url:http://localhost:8081}")
    private String apiBaseUrl;

    public ReservationController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping
    public String listReservations(@RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                   Model model) {
        List<ReservationDto> reservations = Collections.emptyList();
        Map<Integer, String> hotels = Collections.emptyMap();

        try {
            ResponseEntity<List<ReservationDto>> resp = restTemplate.exchange(
                    apiBaseUrl + "/reservations",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ReservationDto>>() {
                    }
            );
            reservations = resp.getBody();
        } catch (Exception ex) {
            model.addAttribute("error", "Impossible de récupérer la liste des réservations: " + ex.getMessage());
            reservations = Collections.emptyList();
        }

        try {
            ResponseEntity<List<HotelDto>> resp = restTemplate.exchange(
                    apiBaseUrl + "/hotels",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<HotelDto>>() {
                    }
            );
            List<HotelDto> hotelList = resp.getBody();
            if (hotelList != null) {
                hotels = hotelList.stream().collect(Collectors.toMap(HotelDto::getId, HotelDto::getNom));
            }
        } catch (Exception ex) {
            // keep empty map and show a note
            model.addAttribute("hotelFetchError", "Impossible de récupérer la liste des hôtels: " + ex.getMessage());
        }

        if (date != null && reservations != null) {
            reservations = reservations.stream()
                    .filter(r -> r.getDateHeure() != null && r.getDateHeure().toLocalDate().equals(date))
                    .collect(Collectors.toList());
        }

        model.addAttribute("reservations", reservations);
        model.addAttribute("hotels", hotels);
        model.addAttribute("selectedDate", date != null ? date.toString() : "");

        return "reservations";
    }
}
