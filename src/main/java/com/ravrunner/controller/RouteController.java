package com.ravrunner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ravrunner.model.Route;
import com.ravrunner.model.Waypoint;
import com.ravrunner.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class RouteController {

    @Autowired
    private RouteRepository routeRepository;

    /**
     * Zeigt eine Liste aller gespeicherten Routen an.
     * Rendert die Template-Datei: templates/routes.html
     */
    @GetMapping("/routes")
    public String listRoutes(Model model) {
        List<Route> routes = routeRepository.findAll();
        model.addAttribute("routes", routes);
        return "routes";
    }

    /**
     * Zeigt das Formular zum Erstellen einer neuen Route.
     * Template: create-route.html
     */
    @GetMapping("/route/new")
    public String createRouteForm(Model model) {
        model.addAttribute("route", new Route());
        return "create-route";
    }

    /**
     * Zeigt die Detailseite einer bestimmten Route anhand ihrer MongoDB-ID.
     * Falls nicht vorhanden → zurück zur Routenseite mit Fehlermeldung.
     */
    @GetMapping("/route/{id}")
    public String routeDetails(@PathVariable("id") String id,
                               Model model,
                               RedirectAttributes ra) {

        try {
            Optional<Route> route = routeRepository.findById(id);

            if (route.isPresent()) {
                model.addAttribute("route", route.get());
                return "route-details"; // Template anzeigen
            } else {
                ra.addFlashAttribute("error", "Route nicht gefunden: " + id);
                return "redirect:/routes";
            }

        } catch (Exception e) {
            System.err.println("Fehler beim Laden der Route: " + e.getMessage());
            ra.addFlashAttribute("error", "Fehler beim Laden der Route.");
            return "redirect:/routes";
        }
    }

    /**
     * Löscht eine Route über POST (CSRF-sicher).
     * Leitet immer zurück auf /routes.
     */
    @PostMapping("/route/{id}/delete")
    public String deleteRoutePost(@PathVariable("id") String id,
                                  RedirectAttributes ra) {
        return performDelete(id, ra);
    }

    /**
     * Führt das Löschen der Route inkl. Fehlertests aus.
     */
    private String performDelete(String id, RedirectAttributes ra) {
        try {
            if (!routeRepository.existsById(id)) {
                ra.addFlashAttribute("error", "Route nicht gefunden: " + id);
                return "redirect:/routes";
            }

            routeRepository.deleteById(id);
            ra.addFlashAttribute("msg", "Route gelöscht.");

        } catch (IllegalArgumentException ex) {
            System.err.println("Ungültige ID: " + id);
            ra.addFlashAttribute("error", "Ungültige ID: " + id);

        } catch (Exception e) {
            System.err.println("Fehler beim Löschen: " + e.getMessage());
            ra.addFlashAttribute("error", "Fehler beim Löschen.");
        }

        return "redirect:/routes";
    }

    /**
     * Speichert eine neue Route:
     * - Name + Beschreibung
     * - Wegpunkte aus create-routes.js (als JSON in Hidden-Inputs)
     *
     * Nach dem Speichern → Redirect zur Detailseite /route/{id}
     */
    @PostMapping("/route/save")
    public String saveRoute(@RequestParam Map<String, String> params,
                            RedirectAttributes ra) {

        try {
            // Grunddaten aus dem Formular
            String name = params.get("name");
            String description = params.getOrDefault("description", "");

            if (name == null || name.trim().isEmpty()) {
                ra.addFlashAttribute("error", "Routenname darf nicht leer sein.");
                return "redirect:/route/new";
            }

            Route route = new Route(name, description);

            // JSON-Waypoints (von create-routes.js erzeugt)
            ObjectMapper mapper = new ObjectMapper();
            List<Waypoint> waypoints = new ArrayList<>();

            // Liest waypoints[0], waypoints[1], waypoints[2] ...
            for (int i = 0; ; i++) {
                String key = "waypoints[" + i + "]";
                if (!params.containsKey(key)) break;

                String json = params.get(key);
                Waypoint wp = mapper.readValue(json, Waypoint.class);
                waypoints.add(wp);
            }

            if (waypoints.isEmpty()) {
                ra.addFlashAttribute("error", "Bitte füge mindestens einen Wegpunkt hinzu.");
                return "redirect:/route/new";
            }

            route.setWaypoints(waypoints);

            // In MongoDB speichern
            Route saved = routeRepository.save(route);

            ra.addFlashAttribute("msg", "Route erfolgreich gespeichert.");
            return "redirect:/route/" + saved.getId();

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Fehler beim Speichern: " + e.getMessage());
            return "redirect:/route/new";
        }
    }
}