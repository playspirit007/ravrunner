package com.ravrunner.controller;

import com.ravrunner.model.Route;
import com.ravrunner.model.Waypoint;
import com.ravrunner.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
            model.addAttribute("routes", routes);
            System.out.println("Lade " + routes.size() + " Routen");
            return "routes";
        } catch (Exception e) {
            System.err.println("Fehler beim Laden der Routen: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Fehler beim Laden der Routen: " + e.getMessage());
            return "routes";
        }
    }

    @GetMapping("/route/new")
    public String createRouteForm(Model model) {
        model.addAttribute("route", new Route());
        return "create-route";
    }

    @PostMapping("/route/save")
    public String saveRoute(@ModelAttribute Route route) {
        try {
            System.out.println("Speichere Route: " + route.getName());
            routeRepository.save(route);
            return "redirect:/routes";
        } catch (Exception e) {
            System.err.println("Fehler beim Speichern: " + e.getMessage());
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
}