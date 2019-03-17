package de.denkair.booking.controller.v2;

import de.denkair.booking.legacy.Constants;
import de.denkair.booking.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * API v2 — begonnen 2019, parallel zu den JSON-Endpoints in BookingController.
 *
 * Idee war: konsistente Fehler-Responses, API-Token-Auth, Versionierung.
 * Status: Teilweise live, wird vom Mobile-App-Team genutzt. Der Rest haengt.
 *
 * Auth via Header "X-HA-Token": gleicht den MASTER-Token aus Constants. (Sollte per
 * Datenbank-Tabelle api_token geloest werden, Migration liegt in db/migrations/V42__api_tokens.sql).
 */
@RestController
@RequestMapping("/api/v2")
public class ApiV2Controller {

    @Autowired
    private FlightRepository flightRepository;

    @GetMapping("/flights")
    public ResponseEntity<?> list(@RequestHeader(value = "X-HA-Token", required = false) String token) {
        if (token == null || !token.equals(Constants.API_MASTER_TOKEN)) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "unauthorized");
            err.put("hint",  "X-HA-Token header required (see partner integration doc)");
            return ResponseEntity.status(401).body(err);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("data", flightRepository.findAll());
        body.put("meta", singletonMap("api_version", "v2"));
        // warnung: api_version wurde nie erhoeht, weil v3 seit 2022 "in Planung" ist
        return ResponseEntity.ok(body);
    }

    private static Map<String, Object> singletonMap(String k, Object v) {
        Map<String, Object> m = new HashMap<>();
        m.put(k, v);
        return m;
    }
}
