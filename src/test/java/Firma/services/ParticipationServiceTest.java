package Firma.services;

import Firma.entities.GestionEvenement.*;
import Firma.services.GestionEvenement.EvenementService;
import Firma.services.GestionEvenement.ParticipationService;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour ParticipationService.
 * Ordre CRUD : Create → Read → Update → Delete.
 * 
 * Pré-requis : un événement et un utilisateur doivent exister en base.
 * L'événement est créé/supprimé automatiquement par les tests.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ParticipationServiceTest {

    private static ParticipationService participationService;
    private static EvenementService evenementService;

    // Données de test partagées entre les tests ordonnés
    private static int idEvenementTest;
    private static int idParticipationTest;
    private static Participation participationTest;

    // ID utilisateur existant dans la base (utilisateur de test)
    // On utilisera l'ID 1 qui devrait exister dans la table utilisateurs
    private static final int ID_UTILISATEUR_TEST = 1;

    /**
     * Initialise les services et crée un événement de test avant tous les tests.
     */
    @BeforeAll
    static void setUp() throws Exception {
        participationService = new ParticipationService();
        evenementService = new EvenementService();

        // Créer un événement de test pour les participations
        Evenement evenementTest = new Evenement();
        evenementTest.setTitre("TEST_PARTICIPATION - Événement temporaire");
        evenementTest.setDescription("Événement créé pour tester les participations");
        evenementTest.setImageUrl("http://test.com/img.jpg");
        evenementTest.setTypeEvenement(Type.conference);
        evenementTest.setDateDebut(LocalDate.of(2026, 12, 15));
        evenementTest.setDateFin(LocalDate.of(2026, 12, 15));
        evenementTest.setHoraireDebut(LocalTime.of(9, 0));
        evenementTest.setHoraireFin(LocalTime.of(17, 0));
        evenementTest.setLieu("Salle Test Participation");
        evenementTest.setAdresse("Adresse Test");
        evenementTest.setCapaciteMax(50);
        evenementTest.setPlacesDisponibles(50);
        evenementTest.setOrganisateur("JUnit Test");
        evenementTest.setContactEmail("test@participation.org");
        evenementTest.setContactTel("+21600000000");
        evenementTest.setStatut(Statutevent.actif);

        evenementService.addEntity(evenementTest);

        // Récupérer l'ID de l'événement créé
        List<Evenement> liste = evenementService.getData();
        idEvenementTest = liste.stream()
                .filter(e -> "TEST_PARTICIPATION - Événement temporaire".equals(e.getTitre()))
                .mapToInt(Evenement::getIdEvenement)
                .max()
                .orElse(-1);

        assertTrue(idEvenementTest > 0, "L'événement de test pour les participations doit être créé");
        System.out.println("🔧 Événement de test créé (ID=" + idEvenementTest + ") pour les tests de participation");
    }

    /**
     * Nettoyage après chaque test : journalisation de l'état.
     */
    @AfterEach
    void afterEach() {
        System.out.println("--- Fin du test. ID participation courant : " + idParticipationTest + " ---");
    }

    // ========================
    // TEST 1 : CREATE (Ajout)
    // ========================
    @Test
    @Order(1)
    @DisplayName("Ajout d'une participation")
    void testAddEntity() throws SQLException {
        participationTest = new Participation();
        participationTest.setIdEvenement(idEvenementTest);
        participationTest.setIdUtilisateur(ID_UTILISATEUR_TEST);
        participationTest.setStatut(Statut.CONFIRME);
        participationTest.setDateInscription(LocalDateTime.now());
        participationTest.setNombreAccompagnants(2);
        participationTest.setCommentaire("Participation de test JUnit");

        // Ajouter la participation
        assertDoesNotThrow(() -> participationService.addEntity(participationTest));

        // Vérifier que l'ID a été généré
        idParticipationTest = participationTest.getIdParticipation();
        assertTrue(idParticipationTest > 0, "L'ID de participation doit être généré automatiquement");

        // Vérifier que le code de participation a été généré
        assertNotNull(participationTest.getCodeParticipation(),
                "Le code de participation doit être généré");
        assertTrue(participationTest.getCodeParticipation().startsWith("PART-"),
                "Le code doit commencer par 'PART-'");

        System.out.println("✅ Participation créée (ID=" + idParticipationTest
                + ", code=" + participationTest.getCodeParticipation() + ")");
    }

    // ========================
    // TEST 2 : READ (Par ID)
    // ========================
    @Test
    @Order(2)
    @DisplayName("Lecture d'une participation par ID")
    void testGetById() throws SQLException {
        assertTrue(idParticipationTest > 0, "Pré-condition : la participation doit exister");

        Participation p = participationService.getById(idParticipationTest);

        assertNotNull(p, "La participation récupérée ne doit pas être null");
        assertEquals(idEvenementTest, p.getIdEvenement());
        assertEquals(ID_UTILISATEUR_TEST, p.getIdUtilisateur());
        // MySQL enum stocke en minuscules → Statut.confirme
        assertEquals(Statut.confirme, p.getStatut());
        assertEquals(2, p.getNombreAccompagnants());
        assertEquals("Participation de test JUnit", p.getCommentaire());

        System.out.println("✅ Participation lue avec succès : " + p);
    }

    // ====================================
    // TEST 3 : READ (Par événement)
    // ====================================
    @Test
    @Order(3)
    @DisplayName("Récupération des participations par événement")
    void testGetParticipationsByEvent() throws SQLException {
        List<Participation> participations = participationService.getParticipationsByEvent(idEvenementTest);

        assertNotNull(participations);
        assertFalse(participations.isEmpty(),
                "Il doit y avoir au moins une participation pour l'événement de test");

        boolean found = participations.stream()
                .anyMatch(p -> p.getIdParticipation() == idParticipationTest);
        assertTrue(found, "Notre participation de test doit être dans la liste");

        System.out.println("✅ " + participations.size() + " participation(s) trouvée(s) pour l'événement");
    }

    // ====================================
    // TEST 4 : READ (Comptage)
    // ====================================
    @Test
    @Order(4)
    @DisplayName("Comptage des participations confirmées")
    void testCountParticipationsByEvent() throws SQLException {
        int count = participationService.countParticipationsByEvent(idEvenementTest);

        assertTrue(count >= 1,
                "Le comptage doit retourner au moins 1 participation confirmée");

        System.out.println("✅ Nombre de participations confirmées : " + count);
    }

    // ====================================
    // TEST 5 : READ (Total participants)
    // ====================================
    @Test
    @Order(5)
    @DisplayName("Comptage total des participants (avec accompagnants)")
    void testCountTotalParticipantsByEvent() throws SQLException {
        int total = participationService.countTotalParticipantsByEvent(idEvenementTest);

        // Au moins 1 participant + 2 accompagnants = 3
        assertTrue(total >= 3,
                "Le total doit inclure le participant + ses 2 accompagnants (>= 3)");

        System.out.println("✅ Nombre total de participants (avec accompagnants) : " + total);
    }

    // ====================================
    // TEST 6 : READ (Vérifier doublon)
    // ====================================
    @Test
    @Order(6)
    @DisplayName("Vérification qu'un utilisateur est déjà inscrit")
    void testIsUserAlreadyParticipating() throws SQLException {
        boolean isParticipating = participationService.isUserAlreadyParticipating(
                ID_UTILISATEUR_TEST, idEvenementTest);

        assertTrue(isParticipating,
                "L'utilisateur de test doit être reconnu comme déjà inscrit");

        System.out.println("✅ Vérification de doublon OK : utilisateur déjà inscrit = " + isParticipating);
    }

    // ============================
    // TEST 7 : UPDATE (Modification)
    // ============================
    @Test
    @Order(7)
    @DisplayName("Modification d'une participation")
    void testUpdateEntity() throws SQLException {
        assertTrue(idParticipationTest > 0, "Pré-condition : la participation doit exister");

        Participation p = participationService.getById(idParticipationTest);
        assertNotNull(p);

        // Modifier les champs
        p.setNombreAccompagnants(3);
        p.setCommentaire("Commentaire modifié par test JUnit");
        p.setStatut(Statut.CONFIRME);

        assertDoesNotThrow(() -> participationService.updateEntity(idParticipationTest, p));

        // Vérifier la mise à jour
        Participation updated = participationService.getById(idParticipationTest);
        assertNotNull(updated);
        assertEquals(3, updated.getNombreAccompagnants());
        assertEquals("Commentaire modifié par test JUnit", updated.getCommentaire());

        System.out.println("✅ Participation mise à jour avec succès");
    }

    // ============================
    // TEST 8 : DELETE (Suppression)
    // ============================
    @Test
    @Order(8)
    @DisplayName("Suppression d'une participation")
    void testDeleteEntity() throws SQLException {
        assertTrue(idParticipationTest > 0, "Pré-condition : la participation doit exister");

        Participation p = participationService.getById(idParticipationTest);
        assertNotNull(p, "La participation à supprimer doit exister");

        assertDoesNotThrow(() -> participationService.deleteEntity(p));

        // Vérifier la suppression
        Participation deleted = participationService.getById(idParticipationTest);
        assertNull(deleted, "La participation doit être null après suppression");

        System.out.println("✅ Participation supprimée (ID=" + idParticipationTest + ")");
        idParticipationTest = 0;
    }

    // ============================================
    // TEST 9 : Nettoyage final - supprimer l'événement
    // ============================================
    @Test
    @Order(9)
    @DisplayName("Nettoyage : suppression de l'événement de test")
    void testNettoyageFinal() throws SQLException {
        if (idEvenementTest > 0) {
            Evenement e = evenementService.getById(idEvenementTest);
            if (e != null) {
                evenementService.deleteEntity(e);
                System.out.println("🧹 Événement de test supprimé (ID=" + idEvenementTest + ")");
            }
        }

        // Vérifier que l'événement est bien supprimé
        Evenement deleted = evenementService.getById(idEvenementTest);
        assertNull(deleted, "L'événement de test doit être supprimé");

        // Vérifier qu'il n'y a plus de participations pour cet événement
        List<Participation> remaining = participationService.getParticipationsByEvent(idEvenementTest);
        assertTrue(remaining.isEmpty(),
                "Aucune participation ne doit rester pour l'événement supprimé");

        System.out.println("✅ Base nettoyée - aucune donnée de test résiduelle");
    }
}
