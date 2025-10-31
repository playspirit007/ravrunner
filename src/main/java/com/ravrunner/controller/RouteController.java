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

    @GetMapping("/")
    public String home(Model model) {
        try {
            long routeCount = routeRepository.count();
            model.addAttribute("routeCount", routeCount);
            System.out.println("MongoDB Verbindung erfolgreich! Routen: " + routeCount);
        } catch (Exception e) {
            System.err.println("MongoDB Verbindungsfehler: " + e.getMessage());
            model.addAttribute("error", "Datenbank nicht verbunden");
        }
        return "index";
    }

    @GetMapping("/routes")
    public String listRoutes(Model model) {
        try {
            List<Route> routes = routeRepository.findAll();
            if (routes == null) routes = new ArrayList<>();
            model.addAttribute("routes", routes);
            System.out.println("Lade " + routes.size() + " Routen");
        } catch (Exception e) {
            System.err.println("Fehler beim Laden der Routen: " + e.getMessage());
            model.addAttribute("routes", new ArrayList<>());
            model.addAttribute("error", "Fehler beim Laden der Routen: " + e.getMessage());
        }
        return "routes";
    }

    @GetMapping("/route/new")
    public String createRouteForm(Model model) {
        model.addAttribute("route", new Route());
        return "create-route";
    }

    @GetMapping("/route/{id}")
    public String routeDetails(@PathVariable("id") String id, Model model, RedirectAttributes ra) {
        try {
            Optional<Route> route = routeRepository.findById(id);
            if (route.isPresent()) {
                model.addAttribute("route", route.get());
                return "route-details";
            } else {
                ra.addFlashAttribute("error", "Route nicht gefunden: " + id);
                return "redirect:/routes";
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Laden der Route: " + e.getMessage());
            ra.addFlashAttribute("error", "Fehler beim Laden der Route: " + e.getMessage());
            return "redirect:/routes";
        }
    }

    @PostMapping("/route/{id}/delete")
    public String deleteRoutePost(@PathVariable("id") String id, RedirectAttributes ra) {
        return performDelete(id, ra);
    }

    @GetMapping("/route/{id}/delete")
    public String deleteRouteGet(@PathVariable("id") String id, RedirectAttributes ra) {
        return performDelete(id, ra);
    }

    private String performDelete(String id, RedirectAttributes ra) {
        try {
            if (!routeRepository.existsById(id)) {
                ra.addFlashAttribute("error", "Route nicht gefunden: " + id);
                return "redirect:/routes";
            }
            routeRepository.deleteById(id);
            System.out.println("Route gelöscht: " + id);
            ra.addFlashAttribute("msg", "Route gelöscht.");
        } catch (IllegalArgumentException ex) {
            System.err.println("Ungültige ID: " + id + " | " + ex.getMessage());
            ra.addFlashAttribute("error", "Ungültige ID: " + id);
        } catch (Exception e) {
            System.err.println("Fehler beim Löschen: " + e.getMessage());
            ra.addFlashAttribute("error", "Fehler beim Löschen: " + e.getMessage());
        }
        return "redirect:/routes";
    }

    @PostMapping("/route/debug-save")
    @ResponseBody
    public String debugSaveRoute(@RequestParam Map<String, String> allParams) {
        System.out.println("=== 🐛 DEBUG FORMULAR DATEN ===");
        allParams.forEach((key, value) -> System.out.println("🔍 " + key + " = " + value));

        System.out.println("=== 🎯 WAYPOINT ANALYSIS ===");
        int waypointCount = 0;
        for (String key : allParams.keySet()) {
            if (key.startsWith("waypoints")) {
                waypointCount++;
                System.out.println("📍 Waypoint " + waypointCount + ": " + allParams.get(key));
            }
        }
        System.out.println("Gesamte Waypoints: " + waypointCount);

        try {
            String name = allParams.get("name");
            String description = allParams.get("description");

            if (name == null || name.trim().isEmpty()) {
                return "❌ FEHLER: Routenname ist leer!";
            }

            Route route = new Route(name, description != null ? description : "");

            List<Waypoint> waypointList = new ArrayList<>();
            for (String key : allParams.keySet()) {
                if (key.startsWith("waypoints")) {
                    try {
                        String waypointJson = allParams.get(key);
                        ObjectMapper mapper = new ObjectMapper();
                        Waypoint waypoint = mapper.readValue(waypointJson, Waypoint.class);
                        waypointList.add(waypoint);
                        System.out.println("✅ Waypoint gespeichert: " + waypoint.getName());
                    } catch (Exception e) {
                        System.err.println("❌ Waypoint Fehler: " + e.getMessage());
                    }
                }
            }

            route.setWaypoints(waypointList);
            Route saved = routeRepository.save(route);

            return "✅ DEBUG ERFOLG!<br>" +
                    "Route gespeichert mit ID: " + saved.getId() + "<br>" +
                    "Waypoints: " + waypointList.size() + "<br>" +
                    "<a href='/routes'>Zu den Routen</a> | " +
                    "<a href='/route/new'>Neue Route</a>";

        } catch (Exception e) {
            return "❌ FEHLER beim Speichern: " + e.getMessage();
        }
    }

    @ExceptionHandler(Exception.class)
    public String handleAnyException(Exception e, RedirectAttributes ra) {
        System.err.println("Unerwarteter Fehler: " + e.getClass().getName() + " | " + e.getMessage());
        ra.addFlashAttribute("error", "Unerwarteter Fehler: " + e.getMessage());
        return "redirect:/routes";
    }
}
