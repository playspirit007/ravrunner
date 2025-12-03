package com.ravrunner.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB-Dokument für eine Fahrradroute.
 * Wird in der Collection "routes" gespeichert.
 */
@Document(collection = "routes")
public class Route {

    @Id
    private String id;  // MongoDB generiert automatisch eine ObjectId

    private String name;        // Name der Route
    private String description; // Beschreibung der Route

    // Liste der Wegpunkte, aus denen die Route besteht
    private List<Waypoint> waypoints = new ArrayList<>();

    // Zeitpunkt, wann die Route erstellt wurde
    private LocalDateTime createdAt;

    /**
     * Standard-Konstruktor.
     * Setzt automatisch das Erstellungsdatum.
     */
    public Route() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Konstruktor für Name + Beschreibung.
     */
    public Route(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }

    /**
     * Konstruktor für Name, Beschreibung und Waypoints.
     */
    public Route(String name, String description, List<Waypoint> waypoints) {
        this();
        this.name = name;
        this.description = description;
        this.waypoints = waypoints != null ? waypoints : new ArrayList<>();
    }

    // ----------------------------
    // Getter & Setter
    // ----------------------------
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Waypoint> getWaypoints() { return waypoints; }
    public void setWaypoints(List<Waypoint> waypoints) {
        this.waypoints = waypoints != null ? waypoints : new ArrayList<>();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /**
     * Fügt der Route einen einzelnen Wegpunkt hinzu.
     */
    public void addWaypoint(Waypoint waypoint) {
        if (waypoints == null) {
            waypoints = new ArrayList<>();
        }
        this.waypoints.add(waypoint);
    }
}
