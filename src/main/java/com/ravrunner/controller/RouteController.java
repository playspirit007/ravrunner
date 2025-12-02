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

  @GetMapping("/routes")
    public String listRoutes(Model model) {
    List<Route> routes = routeRepository.findAll();
    model.addAttribute("routes", routes);
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

    private String performDelete(String id, RedirectAttributes ra) {
        try {
            if (!routeRepository.existsById(id)) {
                ra.addFlashAttribute("error", "Route nicht gefunden: " + id);
                return "redirect:/routes";
            }
            routeRepository.deleteById(id);
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


@GetMapping("/route/create")
public String showCreateForm(Model model) {
    model.addAttribute("route", new Route());
    return "create-route"; // Name deines Templates
}

@PostMapping("/route/save")
public String saveRoute(@RequestParam Map<String, String> params, RedirectAttributes ra) {
    try {
        String name = params.get("name");
        String description = params.getOrDefault("description", "");

        if (name == null || name.trim().isEmpty()) {
            ra.addFlashAttribute("error", "Routenname darf nicht leer sein.");
            return "redirect:/route/new";
        }

        Route route = new Route(name, description);

        ObjectMapper mapper = new ObjectMapper();
        List<Waypoint> waypoints = new ArrayList<>();

        // Waypoints in richtiger Reihenfolge anhand des Index (waypoints[0], waypoints[1], ...)
        for (int i = 0; ; i++) {
            String key = "waypoints[" + i + "]";
            if (!params.containsKey(key)) {
                break;
            }
            String json = params.get(key);
            Waypoint wp = mapper.readValue(json, Waypoint.class);
            waypoints.add(wp);
        }

        if (waypoints.isEmpty()) {
            ra.addFlashAttribute("error", "Bitte füge mindestens einen Wegpunkt hinzu.");
            return "redirect:/route/new";
        }

        route.setWaypoints(waypoints);

        Route saved = routeRepository.save(route);

        ra.addFlashAttribute("msg", "Route erfolgreich gespeichert.");
        // Browser per JS-Fetch folgt dem Redirect → URL landet bei /route/{id}
        return "redirect:/route/" + saved.getId();

    } catch (Exception e) {
        ra.addFlashAttribute("error", "Fehler beim Speichern: " + e.getMessage());
        return "redirect:/route/new";
    }
}

private static class WaypointFormData {
    private String name;
    private double latitude;
    private double longitude;

    public WaypointFormData() {
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}

}
