package edu.connection3a7.entities;

public class Coordonnees {
    private double latitude;
    private double longitude;
    private String adresse;

    public Coordonnees() {}

    public Coordonnees(double latitude, double longitude, String adresse) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.adresse = adresse;
    }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
}