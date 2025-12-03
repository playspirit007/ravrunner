package com.ravrunner.model;

public class Waypoint {
    private String name;
    private double latitude;
    private double longitude;


/**
 * Repräsentiert einen einzelnen Wegpunkt einer Route.
 * Wird als Bestandteil der Route in MongoDB gespeichert.
 *
 * Besteht aus:
 * - name        → Anzeigename (z.B. "Start", "Punkt 3")
 * - latitude    → Breitengrad
 * - longitude   → Längengrad
 */

    public Waypoint() {}

    public Waypoint(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}