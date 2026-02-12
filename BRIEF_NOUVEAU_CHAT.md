# 📋 BRIEF POUR NOUVEAU CHAT - Projet Location Voiture

## 🎯 Contexte du projet

**Architecture:** Système client-serveur avec 2 applications séparées

### **BACKOFFICE** (Port 8080 - Tomcat)
- **Technologie:** Spring Boot 3.2.2 déployé en WAR sur Tomcat 10.1
- **Context path:** `/reservation`
- **Rôle:** API REST + Interface d'administration
- **Base de données:** PostgreSQL `bdd_voiture`

**APIs REST disponibles:**
- `GET http://localhost:8080/reservation/api/hostels` → Liste des hôtels
- `GET http://localhost:8080/reservation/api/reservations` → Liste des réservations
- `GET http://localhost:8080/reservation/api/reservations/date/{yyyy-MM-dd}` → Réservations par date
- `POST http://localhost:8080/reservation/api/hostels` → Créer hôtel
- `POST http://localhost:8080/reservation/api/reservations` → Créer réservation

**Format JSON réservation du backoffice:**
```json
{
  "id": 1,
  "hotel": {
    "id": 1,
    "nom": "Hotel Panorama"
  },
  "idClient": "C001",
  "nbPassager": 3,
  "dateHeure": "2026-02-10"
}
```
⚠️ **IMPORTANT:** `dateHeure` est un `LocalDate` (pas `LocalDateTime`), et `hotel` est un objet imbriqué (pas `idHotel`).

### **FRONTOFFICE** (Port 8082 - Spring Boot embedded)
- **Technologie:** Spring Boot 4.0.2
- **Rôle:** Interface client - consultation uniquement (liste réservations + filtre date, liste hôtels)
- **Fonctionnement:** Appelle les APIs du backoffice via `RestTemplate`

---

## ❌ PROBLÈMES ACTUELS IDENTIFIÉS

### 1. **URL incorrecte dans application.properties**
```properties
# ❌ FAUX
reservation.api.base-url=http://localhost:8080

# ✅ CORRECT (avec context path /reservation)
reservation.api.base-url=http://localhost:8080/reservation
```

### 2. **Endpoints incorrects dans `ReservationController`**
Le controller appelle `/reservations` et `/hotels` mais le backoffice expose `/api/reservations` et `/api/hostels`

```java
// ❌ FAUX
apiBaseUrl + "/reservations"  
apiBaseUrl + "/hotels"

// ✅ CORRECT
apiBaseUrl + "/api/reservations"
apiBaseUrl + "/api/hostels"
```

### 3. **DTO incompatible avec JSON du backoffice**
Le `ReservationDto` actuel a:
- `Integer idHotel` → devrait être `HotelDto hotel` (objet imbriqué)
- `LocalDateTime dateHeure` → devrait être `LocalDate dateHeure`

### 4. **Incohérence dans les `@Value`**
- `ReservationController` utilise `${reservation.api.base-url}`
- `HotelController` utilise `${BACKOFFICE_API_URL}` (variable env qui n'existe pas)

### 5. **Fichier `.env` manquant**
Le fichier `.env` n'est pas dans le repo (probablement ignoré par `.gitignore`). Il devrait contenir:
```
BACKOFFICE_API_URL=http://localhost:8080/reservation
```

### 6. **Spring Boot 4.x : `RestTemplateBuilder` supprimé**
`org.springframework.boot.web.client.RestTemplateBuilder` n'existe plus dans Spring Boot 4.x.
Utiliser `new RestTemplate()` directement dans la config :
```java
@Bean
public RestTemplate restTemplate() {
    return new RestTemplate();
}
```

---

## 🔧 ACTIONS À FAIRE

### **Action 1: Créer le fichier `.env`** (à la racine du projet frontoffice)
```
BACKOFFICE_API_URL=http://localhost:8080/reservation
```

### **Action 2: Corriger application.properties**
```properties
spring.application.name=frontOffice
server.port=8082

# URL du backoffice avec context path
backoffice.api.base-url=${BACKOFFICE_API_URL:http://localhost:8080/reservation}
```

### **Action 3: Corriger `RestTemplateConfig.java`**
```java
package com.location.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

### **Action 4: Corriger `ReservationDto.java`**
```java
package com.location.app.web.dto;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;

public class ReservationDto {
    private Long id;
    private HotelDto hotel;        // ⚠️ Objet imbriqué, PAS Integer
    private String idClient;
    private Integer nbPassager;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateHeure;   // ⚠️ LocalDate, PAS LocalDateTime

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public HotelDto getHotel() { return hotel; }
    public void setHotel(HotelDto hotel) { this.hotel = hotel; }

    public String getIdClient() { return idClient; }
    public void setIdClient(String idClient) { this.idClient = idClient; }

    public Integer getNbPassager() { return nbPassager; }
    public void setNbPassager(Integer nbPassager) { this.nbPassager = nbPassager; }

    public LocalDate getDateHeure() { return dateHeure; }
    public void setDateHeure(LocalDate dateHeure) { this.dateHeure = dateHeure; }
}
```

### **Action 5: Corriger `ReservationController.java`**
Le controller doit :
- Utiliser `${backoffice.api.base-url}` (pas `reservation.api.base-url`)
- Appeler `/api/reservations` (pas `/reservations`)
- Supporter le filtre date via `/api/reservations/date/{date}`
- NE PAS avoir de création (consultation uniquement)

```java
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
```

### **Action 6: Corriger `HotelController.java`**
Utiliser la même propriété `${backoffice.api.base-url}` et appeler `/api/hostels`.

### **Action 7: Mettre à jour le template `reservations.html`**
Utiliser `res.hotel.nom` au lieu de la map :
```html
<td th:text="${res.hotel != null ? res.hotel.nom : 'N/A'}"></td>
<td th:text="${res.dateHeure}"></td>
```

---

## 🚀 COMMANDES DE TEST

```powershell
# 1. Démarrer le backoffice
cd projetAvecFrameworkBc\location-voiture-backoffice
.\deploy.ps1

# 2. Tester que l'API répond
curl http://localhost:8080/reservation/api/hostels
curl http://localhost:8080/reservation/api/reservations

# 3. Démarrer le frontoffice
cd projetAvecFramework\location-voiture-frontOffice
mvn clean spring-boot:run

# 4. Tester dans le navigateur
# http://localhost:8082/reservations
# http://localhost:8082/reservations?date=2026-02-12
# http://localhost:8082/hotels
```

---

## 📝 NOTES IMPORTANTES

1. **Deux repos séparés** → Les modifications dans un repo n'affectent pas l'autre
2. **Le backoffice DOIT tourner** avant de lancer le frontoffice
3. **CORS est activé** sur le backoffice (`@CrossOrigin(origins = "*")`)
4. **Ne PAS modifier le backoffice** - il fonctionne correctement
5. **Tous les problèmes sont dans le frontoffice**
6. **Spring Boot 4.x** a supprimé `RestTemplateBuilder` → utiliser `new RestTemplate()`
7. **Ne PAS ajouter `spring-boot-starter-data-jpa`** ni `postgresql` dans le frontoffice (pas de base de données ici)
8. **Pour le déploiement en ligne**, il suffit de changer la valeur dans `.env` :
   ```
   BACKOFFICE_API_URL=https://mon-backoffice-en-ligne.com/reservation
   ```
