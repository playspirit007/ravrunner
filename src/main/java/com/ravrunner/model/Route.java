package com.ravrunner.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "routes")
public class Route {
    @Id
    private String id;
    private String name;
    private String description;
    private List<Waypoint> waypoints = new ArrayList<>();
    private LocalDateTime createdAt;

    // Konstruktoren
    public Route() {
        this.createdAt = LocalDateTime.now();
    }

    public Route(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }

    public Route(String name, String description, List<Waypoint> waypoints) {
        this();
        this.name = name;
        this.description = description;
        this.waypoints = waypoints != null ? waypoints : new ArrayList<>();
    }

    // Getter und Setter
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
}