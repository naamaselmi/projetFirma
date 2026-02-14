package marketplace.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Location entity - Maps to locations table
 * Represents rentals for vehicles or land
 */
public class Location {
    private int id;
    private int utilisateurId;
    private String typeLocation; // "vehicule" or "terrain"
    private int elementId;
    private String numeroLocation;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private int dureeJours;
    private BigDecimal prixTotal;
    private BigDecimal caution;
    private RentalStatus statut;
    private LocalDateTime dateReservation;
    private String notes;

    // Constructors
    public Location() {
    }

    public Location(int utilisateurId, String typeLocation, int elementId,
            LocalDate dateDebut, LocalDate dateFin, BigDecimal prixTotal) {
        this.utilisateurId = utilisateurId;
        this.typeLocation = typeLocation;
        this.elementId = elementId;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.dureeJours = calculerDureeJours(dateDebut, dateFin);
        this.prixTotal = prixTotal;
        this.caution = BigDecimal.ZERO;
        this.statut = RentalStatus.EN_ATTENTE;
    }

    public Location(int id, int utilisateurId, String typeLocation, int elementId,
            String numeroLocation, LocalDate dateDebut, LocalDate dateFin,
            int dureeJours, BigDecimal prixTotal, BigDecimal caution,
            RentalStatus statut, LocalDateTime dateReservation, String notes) {
        this.id = id;
        this.utilisateurId = utilisateurId;
        this.typeLocation = typeLocation;
        this.elementId = elementId;
        this.numeroLocation = numeroLocation;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.dureeJours = dureeJours;
        this.prixTotal = prixTotal;
        this.caution = caution;
        this.statut = statut;
        this.dateReservation = dateReservation;
        this.notes = notes;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public String getTypeLocation() {
        return typeLocation;
    }

    public void setTypeLocation(String typeLocation) {
        this.typeLocation = typeLocation;
    }

    public int getElementId() {
        return elementId;
    }

    public void setElementId(int elementId) {
        this.elementId = elementId;
    }

    public String getNumeroLocation() {
        return numeroLocation;
    }

    public void setNumeroLocation(String numeroLocation) {
        this.numeroLocation = numeroLocation;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
        this.dureeJours = calculerDureeJours(dateDebut, this.dateFin);
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
        this.dureeJours = calculerDureeJours(this.dateDebut, dateFin);
    }

    public int getDureeJours() {
        return dureeJours;
    }

    public void setDureeJours(int dureeJours) {
        this.dureeJours = dureeJours;
    }

    public BigDecimal getPrixTotal() {
        return prixTotal;
    }

    public void setPrixTotal(BigDecimal prixTotal) {
        this.prixTotal = prixTotal;
    }

    public BigDecimal getCaution() {
        return caution;
    }

    public void setCaution(BigDecimal caution) {
        this.caution = caution;
    }

    public RentalStatus getStatut() {
        return statut;
    }

    public void setStatut(RentalStatus statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(LocalDateTime dateReservation) {
        this.dateReservation = dateReservation;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Business methods
    private int calculerDureeJours(LocalDate debut, LocalDate fin) {
        if (debut == null || fin == null) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(debut, fin) + 1;
    }

    public boolean isVehicule() {
        return "vehicule".equalsIgnoreCase(typeLocation);
    }

    public boolean isTerrain() {
        return "terrain".equalsIgnoreCase(typeLocation);
    }

    public boolean isActive() {
        return statut == RentalStatus.CONFIRMEE || statut == RentalStatus.EN_COURS;
    }

    @Override
    public String toString() {
        return "Location{" +
                "id=" + id +
                ", numeroLocation='" + numeroLocation + '\'' +
                ", typeLocation='" + typeLocation + '\'' +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", dureeJours=" + dureeJours +
                ", prixTotal=" + prixTotal +
                ", statut=" + statut +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Location location = (Location) o;
        return id == location.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
