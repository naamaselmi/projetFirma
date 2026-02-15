package edu.connection3a7.test;

import edu.connection3a7.entities.*;
import edu.connection3a7.services.EvenementService;
import edu.connection3a7.services.ParticipationService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static EvenementService evenementService = new EvenementService();
    private static ParticipationService participationService = new ParticipationService();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== TEST DE L'APPLICATION GESTION D'ÉVÉNEMENTS ===\n");

        boolean continuer = true;
        while (continuer) {
            afficherMenu();
            int choix = scanner.nextInt();
            scanner.nextLine(); // Consommer la ligne

            try {
                switch (choix) {
                    case 1:
                        testerAjoutEvenement();
                        break;
                    case 2:
                        testerAffichageEvenements();
                        break;
                    case 3:
                        testerModificationEvenement();
                        break;
                    case 4:
                        testerSuppressionEvenement();
                        break;
                    case 5:
                        testerAjoutParticipation();
                        break;
                    case 6:
                        testerAffichageParticipations();
                        break;
                    case 7:
                        testerModificationParticipation();
                        break;
                    case 8:
                        testerSuppressionParticipation();
                        break;
                    case 9:
                        testerReservationPlaces();
                        break;
                    case 0:
                        continuer = false;
                        System.out.println("Au revoir !");
                        break;
                    default:
                        System.out.println("Choix invalide !");
                }
            } catch (Exception e) {
                System.err.println("Erreur : " + e.getMessage());
                e.printStackTrace();
            }

            if (continuer) {
                System.out.println("\nAppuyez sur Entrée pour continuer...");
                scanner.nextLine();
            }
        }

        scanner.close();
    }

    private static void afficherMenu() {
        System.out.println("\n========== MENU PRINCIPAL ==========");
        System.out.println("--- Gestion des Événements ---");
        System.out.println("1. Ajouter un événement");
        System.out.println("2. Afficher tous les événements");
        System.out.println("3. Modifier un événement");
        System.out.println("4. Supprimer un événement");
        System.out.println("\n--- Gestion des Participations ---");
        System.out.println("5. Ajouter une participation");
        System.out.println("6. Afficher toutes les participations");
        System.out.println("7. Modifier une participation");
        System.out.println("8. Supprimer une participation");
        System.out.println("9. Tester la réservation de places");
        System.out.println("\n0. Quitter");
        System.out.print("\nVotre choix : ");
    }

    // ========== TESTS ÉVÉNEMENTS ==========

    private static void testerAjoutEvenement() throws SQLException {
        System.out.println("\n=== TEST : Ajout d'un événement ===");

        Evenement evenement = new Evenement();

        System.out.print("Titre : ");
        evenement.setTitre(scanner.nextLine());

        System.out.print("Description : ");
        evenement.setDescription(scanner.nextLine());

        System.out.print("URL de l'image : ");
        evenement.setImageUrl(scanner.nextLine());

        System.out.println("Type (CONFERENCE, ATELIER, CONCERT, SPORT, AUTRE) : ");
        evenement.setTypeEvenement(Type.valueOf(scanner.nextLine().toUpperCase()));

        System.out.println("Date de début (format: 2025-03-15T10:00:00) : ");
        //evenement.setDateDebut(LocalDateTime.parse(scanner.nextLine()));

        System.out.println("Date de fin (format: 2025-03-15T18:00:00) : ");
        //evenement.setDateFin(LocalDateTime.parse(scanner.nextLine()));

        System.out.print("Lieu : ");
        evenement.setLieu(scanner.nextLine());

        System.out.print("Adresse : ");
        evenement.setAdresse(scanner.nextLine());

        /*System.out.print("Latitude (ex: 36.8065) : ");
        evenement.setLatitude(new BigDecimal(scanner.nextLine()));

        System.out.print("Longitude (ex: 10.1815) : ");
        evenement.setLongitude(new BigDecimal(scanner.nextLine()));*/

        System.out.print("Capacité maximale : ");
        evenement.setCapaciteMax(scanner.nextInt());

        System.out.print("Places disponibles : ");
        evenement.setPlacesDisponibles(scanner.nextInt());
        scanner.nextLine();

        System.out.print("Organisateur : ");
        evenement.setOrganisateur(scanner.nextLine());

        System.out.print("Email de contact : ");
        evenement.setContactEmail(scanner.nextLine());

        System.out.print("Téléphone de contact : ");
        evenement.setContactTel(scanner.nextLine());

        evenement.setStatut(Statutevent.ACTIF);

        evenementService.addEntity(evenement);
        System.out.println("✓ Événement ajouté avec succès !");
    }

    private static void testerAffichageEvenements() throws Exception {
        System.out.println("\n=== TEST : Affichage de tous les événements ===");

        List<Evenement> evenements = evenementService.getData();

        if (evenements.isEmpty()) {
            System.out.println("Aucun événement trouvé.");
        } else {
            System.out.println("Nombre d'événements : " + evenements.size() + "\n");
            for (Evenement e : evenements) {
                afficherEvenement(e);
                System.out.println("-------------------");
            }
        }
    }

    private static void testerModificationEvenement() throws SQLException {
        System.out.println("\n=== TEST : Modification d'un événement ===");

        System.out.print("ID de l'événement à modifier : ");
        int id = scanner.nextInt();
        scanner.nextLine();

        Evenement evenement = new Evenement();

        System.out.print("Nouveau titre : ");
        evenement.setTitre(scanner.nextLine());

        System.out.print("Nouvelle description : ");
        evenement.setDescription(scanner.nextLine());

        System.out.print("Nouvelle URL d'image : ");
        evenement.setImageUrl(scanner.nextLine());

        System.out.println("Type (CONFERENCE, ATELIER, CONCERT, SPORT, AUTRE) : ");
        evenement.setTypeEvenement(Type.valueOf(scanner.nextLine().toUpperCase()));

        System.out.println("Date de début (format: 2025-03-15T10:00:00) : ");
        //evenement.setDateDebut(LocalDateTime.parse(scanner.nextLine()));

        System.out.println("Date de fin (format: 2025-03-15T18:00:00) : ");
        //evenement.setDateFin(LocalDateTime.parse(scanner.nextLine()));

        System.out.print("Lieu : ");
        evenement.setLieu(scanner.nextLine());

        System.out.print("Adresse : ");
        evenement.setAdresse(scanner.nextLine());

        /*System.out.print("Latitude : ");
        evenement.setLatitude(new BigDecimal(scanner.nextLine()));

        System.out.print("Longitude : ");
        evenement.setLongitude(new BigDecimal(scanner.nextLine()));*/

        System.out.print("Capacité maximale : ");
        evenement.setCapaciteMax(scanner.nextInt());

        System.out.print("Places disponibles : ");
        evenement.setPlacesDisponibles(scanner.nextInt());
        scanner.nextLine();

        System.out.print("Organisateur : ");
        evenement.setOrganisateur(scanner.nextLine());

        System.out.print("Email : ");
        evenement.setContactEmail(scanner.nextLine());

        System.out.print("Téléphone : ");
        evenement.setContactTel(scanner.nextLine());

        System.out.println("Statut (ACTIF, ANNULE, TERMINE, COMPLET) : ");
        evenement.setStatut(Statutevent.valueOf(scanner.nextLine().toUpperCase()));

        evenementService.updateEntity(id, evenement);
        System.out.println("✓ Événement modifié avec succès !");
    }

    private static void testerSuppressionEvenement() throws SQLException {
        System.out.println("\n=== TEST : Suppression d'un événement ===");

        System.out.print("ID de l'événement à supprimer : ");
        int id = scanner.nextInt();

        Evenement evenement = new Evenement();
        evenement.setIdEvenement(id);

        evenementService.deleteEntity(evenement);
        System.out.println("✓ Événement supprimé avec succès !");
    }

    // ========== TESTS PARTICIPATIONS ==========

    private static void testerAjoutParticipation() throws SQLException {
        System.out.println("\n=== TEST : Ajout d'une participation ===");

        Participation participation = new Participation();

        System.out.print("ID de l'événement : ");
        participation.setIdEvenement(scanner.nextInt());

        System.out.print("ID de l'utilisateur : ");
        participation.setIdUtilisateur(scanner.nextInt());

        System.out.print("Nombre d'accompagnants : ");
        participation.setNombreAccompagnants(scanner.nextInt());
        scanner.nextLine();

        System.out.print("Commentaire (optionnel) : ");
        participation.setCommentaire(scanner.nextLine());

        participation.setStatut(Statut.CONFIRME);
        participation.setDateInscription(LocalDateTime.now());

        participationService.addEntity(participation);
        System.out.println("✓ Participation ajoutée avec succès !");
    }

    private static void testerAffichageParticipations() throws Exception {
        System.out.println("\n=== TEST : Affichage de toutes les participations ===");

        List<Participation> participations = participationService.getData();

        if (participations.isEmpty()) {
            System.out.println("Aucune participation trouvée.");
        } else {
            System.out.println("Nombre de participations : " + participations.size() + "\n");
            for (Participation p : participations) {
                afficherParticipation(p);
                System.out.println("-------------------");
            }
        }
    }

    private static void testerModificationParticipation() throws SQLException {
        System.out.println("\n=== TEST : Modification d'une participation ===");

        System.out.print("ID de la participation à modifier : ");
        int id = scanner.nextInt();

        Participation participation = new Participation();

        System.out.print("ID de l'événement : ");
        participation.setIdEvenement(scanner.nextInt());

        System.out.print("Nouveau nombre d'accompagnants : ");
        participation.setNombreAccompagnants(scanner.nextInt());
        scanner.nextLine();

        System.out.println("Statut (CONFIRME, EN_ATTENTE, ANNULE) : ");
        participation.setStatut(Statut.valueOf(scanner.nextLine().toUpperCase()));

        System.out.print("Commentaire : ");
        participation.setCommentaire(scanner.nextLine());

        participationService.updateEntity(id, participation);
        System.out.println("✓ Participation modifiée avec succès !");
    }

    private static void testerSuppressionParticipation() throws SQLException {
        System.out.println("\n=== TEST : Suppression d'une participation ===");

        System.out.print("ID de la participation à supprimer : ");
        int id = scanner.nextInt();

        // Récupérer la participation pour avoir les infos
        Participation participation = participationService.getById(id);

        if (participation != null) {
            participationService.deleteEntity(participation);
            System.out.println("✓ Participation supprimée avec succès !");
        } else {
            System.out.println("✗ Participation non trouvée !");
        }
    }

    // ========== TESTS SPÉCIFIQUES ==========

    private static void testerReservationPlaces() throws SQLException {
        System.out.println("\n=== TEST : Réservation/Libération de places ===");

        System.out.print("ID de l'événement : ");
        int idEvenement = scanner.nextInt();

        System.out.print("Nombre de places à réserver : ");
        int nombrePlaces = scanner.nextInt();

        try {
            EvenementService.reserverPlaces(idEvenement, nombrePlaces);
            System.out.println("✓ Places réservées avec succès !");

            System.out.print("\nVoulez-vous libérer ces places ? (o/n) : ");
            scanner.nextLine();
            String reponse = scanner.nextLine();

            if (reponse.equalsIgnoreCase("o")) {
                EvenementService.libererPlaces(idEvenement, nombrePlaces);
                System.out.println("✓ Places libérées avec succès !");
            }
        } catch (SQLException e) {
            System.err.println("✗ Erreur : " + e.getMessage());
        }
    }



    // ========== MÉTHODES UTILITAIRES ==========

    private static Evenement creerEvenementTest() {
        Evenement e = new Evenement();
        e.setTitre("Événement Test - " + LocalDateTime.now().toString());
        e.setDescription("Ceci est un événement de test créé automatiquement");
        e.setImageUrl("https://example.com/image.jpg");
        e.setTypeEvenement(Type.CONFERENCE);
        //e.setDateDebut(LocalDateTime.now().plusDays(7));
        //e.setDateFin(LocalDateTime.now().plusDays(7).plusHours(3));
        e.setLieu("Salle de Test");
        e.setAdresse("123 Rue de Test, Tunis");

        e.setCapaciteMax(100);
        e.setPlacesDisponibles(100);
        e.setOrganisateur("Test Organisateur");
        e.setContactEmail("test@example.com");
        e.setContactTel("+216 12 345 678");
        e.setStatut(Statutevent.ACTIF);
        return e;
    }

    private static Participation creerParticipationTest(int idEvenement) {
        Participation p = new Participation();
        p.setIdEvenement(idEvenement);
        p.setIdUtilisateur(1); // Supposons qu'un utilisateur avec ID 1 existe
        p.setStatut(Statut.EN_ATTENTE);
        p.setDateInscription(LocalDateTime.now());
        p.setNombreAccompagnants(2);
        p.setCommentaire("Participation de test");
        return p;
    }

    private static void afficherEvenement(Evenement e) {
        System.out.println("ID: " + e.getIdEvenement());
        System.out.println("Titre: " + e.getTitre());
        System.out.println("Description: " + e.getDescription());
        System.out.println("Type: " + e.getTypeEvenement());
        System.out.println("Date début: " + e.getDateDebut());
        System.out.println("Date fin: " + e.getDateFin());
        System.out.println("Lieu: " + e.getLieu());
        System.out.println("Places: " + e.getPlacesDisponibles() + "/" + e.getCapaciteMax());
        System.out.println("Organisateur: " + e.getOrganisateur());
        System.out.println("Statut: " + e.getStatut());
    }

    private static void afficherParticipation(Participation p) {
        System.out.println("ID: " + p.getIdParticipation());
        System.out.println("ID Événement: " + p.getIdEvenement());
        System.out.println("ID Utilisateur: " + p.getIdUtilisateur());
        System.out.println("Statut: " + p.getStatut());
        System.out.println("Date inscription: " + p.getDateInscription());
        System.out.println("Accompagnants: " + p.getNombreAccompagnants());
        System.out.println("Commentaire: " + p.getCommentaire());
    }
}