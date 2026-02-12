package com.location.app.web;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.location.app.web.dto.HotelDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/hotels")
public class HotelController {

    private final RestTemplate restTemplate;

    @Value("${BACKOFFICE_API_URL:http://localhost:8080/reservation}")
    private String apiBaseUrl;

    public HotelController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * GET /hotels — Liste tous les hôtels depuis le backoffice (GET /api/hostels).
     */
    @GetMapping
    public String listHotels(Model model) {
        List<HotelDto> hotels = Collections.emptyList();
        try {
            ResponseEntity<List<HotelDto>> resp = restTemplate.exchange(
                    apiBaseUrl + "/api/hostels", HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<HotelDto>>() {}
            );
            hotels = resp.getBody() != null ? resp.getBody() : Collections.emptyList();
        } catch (Exception ex) {
            model.addAttribute("error",
                    "Impossible de récupérer les hôtels depuis le backoffice : " + ex.getMessage());
        }
        model.addAttribute("hotels", hotels);
        return "hotels";
    }

    /**
     * GET /hotels/new — Formulaire de création d'un hôtel.
     */
    @GetMapping("/new")
    public String showCreateForm() {
        return "hotel-form";
    }

    /**
     * POST /hotels/save — Envoie la création au backoffice via POST /api/hostels.
     * Le backoffice attend : { "nom": "..." }
     */
    @PostMapping("/save")
    public String createHotel(@RequestParam("nom") String nom, Model model) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("nom", nom);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(
                    apiBaseUrl + "/api/hostels",
                    request,
                    String.class
            );
            return "redirect:/hotels";
        } catch (Exception ex) {
            model.addAttribute("error",
                    "Erreur lors de la création de l'hôtel : " + ex.getMessage());
            return "hotel-form";
        }
    }
}
