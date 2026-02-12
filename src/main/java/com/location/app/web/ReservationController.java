package com.location.app.web;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

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

    @Value("${backoffice.api.base-url:http://localhost:8080/reservation}")
    private String apiBaseUrl;

    public ReservationController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * GET /reservations — Liste les réservations (avec filtre optionnel par date).
     * Appelle GET /api/reservations (ou /api/reservations/date/{date}) sur le backoffice.
     */
    @GetMapping
    public String listReservations(
            @RequestParam(name = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        List<ReservationDto> reservations = Collections.emptyList();

        try {
            String url;
            if (date != null) {
                url = apiBaseUrl + "/api/reservations/date/" + date.toString();
            } else {
                url = apiBaseUrl + "/api/reservations";
            }
            ResponseEntity<List<ReservationDto>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<ReservationDto>>() {}
            );
            reservations = resp.getBody() != null ? resp.getBody() : Collections.emptyList();
        } catch (Exception ex) {
            model.addAttribute("error",
                    "Impossible de récupérer les réservations : " + ex.getMessage());
        }

        model.addAttribute("reservations", reservations);
        model.addAttribute("selectedDate", date != null ? date.toString() : "");
        return "reservations";
    }
}