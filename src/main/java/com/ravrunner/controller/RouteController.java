package com.ravrunner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ravrunner.model.Route;
import com.ravrunner.model.Waypoint;
import com.ravrunner.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map;

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
            model.addAttribute("routes", routes);
            System.out.println("Lade " + routes.size() + " Routen");
            return "routes";
        } catch (Exception e) {
            System.err.println("Fehler beim Laden der Routen: " + e.getMessage());
            model.addAttribute("error", "Fehler beim Laden der Routen: " + e.getMessage());
            return "routes";
        }
    }

    @GetMapping("/route/new")
    public String createRouteForm(Model model) {
        model.addAttribute("route", new Route());
        return "create-route";
    }

    // NEUE IMPLEMENTIERUNG: Einfache Parameter-Übergabe
@PostMapping("/route/save")
public String saveRoute(@RequestParam String name, 
                       @RequestParam String description,
                       @RequestParam(required = false) List<String> waypoints) {
    
    System.out.println("=== SPEICHERE ROUTE ===");
    System.out.println("Name: " + name);
    System.out.println("Beschreibung: " + description);
    
    try {
        // Route erstellen
        Route route = new Route(name, description);
        
        // Waypoints verarbeiten
        if (waypoints != null) {
            List<Waypoint> waypointList = new ArrayList<>();
            for (String waypointJson : waypoints) {
                try {
                    // JSON zu Waypoint-Objekt parsen
                    ObjectMapper mapper = new ObjectMapper();
                    Waypoint waypoint = mapper.readValue(waypointJson, Waypoint.class);
                    waypointList.add(waypoint);
                } catch (Exception e) {
                    System.err.println("Fehler beim Parsen des Waypoints: " + e.getMessage());
                }
            }
            route.setWaypoints(waypointList);
        }
        
        // In MongoDB speichern
        Route saved = routeRepository.save(route);
        System.out.println("✅ Route gespeichert! ID: " + saved.getId());
        
        return "redirect:/routes";
        
    } catch (Exception e) {
        System.err.println("❌ Fehler beim Speichern: " + e.getMessage());
        return "redirect:/route/new?error=true";
    }
}

    @GetMapping("/route/{id}")
    public String routeDetails(@PathVariable String id, Model model) {
        try {
            Optional<Route> route = routeRepository.findById(id);
            if (route.isPresent()) {
                model.addAttribute("route", route.get());
                return "route-details";
            }
            return "redirect:/routes";
        } catch (Exception e) {
            System.err.println("Fehler beim Laden der Route: " + e.getMessage());
            return "redirect:/routes";
        }
    }

    @PostMapping("/route/{id}/delete")
    public String deleteRoute(@PathVariable String id) {
        try {
            routeRepository.deleteById(id);
            System.out.println("Route gelöscht: " + id);
        } catch (Exception e) {
            System.err.println("Fehler beim Löschen: " + e.getMessage());
        }
        return "redirect:/routes";
    }



    //NEU

    @PostMapping("/route/debug-save")
@ResponseBody
public String debugSaveRoute(@RequestParam Map<String, String> allParams) {
    System.out.println("=== 🐛 DEBUG FORMULAR DATEN ===");
    
    // Zeige alle Parameter
    allParams.forEach((key, value) -> {
        System.out.println("🔍 " + key + " = " + value);
    });
    
    // Spezielle Waypoint-Analyse
    System.out.println("=== 🎯 WAYPOINT ANALYSIS ===");
    int waypointCount = 0;
    for (String key : allParams.keySet()) {
        if (key.startsWith("waypoints")) {
            waypointCount++;
            System.out.println("📍 Waypoint " + waypointCount + ": " + allParams.get(key));
        }
    }
    System.out.println("Gesamte Waypoints: " + waypointCount);
    
    // Versuche Route zu speichern
    try {
        String name = allParams.get("name");
        String description = allParams.get("description");
        
        if (name == null || name.trim().isEmpty()) {
            return "❌ FEHLER: Routenname ist leer!";
        }
        
        Route route = new Route(name, description != null ? description : "");
        
        // Waypoints verarbeiten
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
}