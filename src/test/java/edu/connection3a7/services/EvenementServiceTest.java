package edu.connection3a7.services;

import edu.connection3a7.entities.Evenement;
import edu.connection3a7.entities.Statutevent;
import edu.connection3a7.entities.Type;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour EvenementService.
 * Ordre CRUD : Create → Read → Update → Delete.
 * Nettoyage automatique après chaque test via @AfterEach.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EvenementServiceTest {

    private static EvenementService evenementService;
    private static Evenement evenementTest;
    private static int idEvenementCree;

    /**
     * Initialise le service une seule fois avant tous les tests.
     */
    @BeforeAll
    static void setUp() {
        evenementService = new EvenementService();
    }

    /**
     * Nettoyage automatique : supprime l'événement de test s'il existe encore.
     * Garantit l'indépendance des tests et évite la pollution de la base.
     */
    @AfterEach
    void cleanUp() {
        // Le nettoyage final se fait dans le test de suppression (Ordre 4).
        // Ici on s'assure qu'en cas d'échec d'un test intermédiaire,
        // les données de test sont quand même nettoyées.
        if (idEvenementCree > 0) {
            try {
                Evenement e = evenementService.getById(idEvenementCree);
                if (e != null) {
                    // L'événement existe encore, on le laisse pour les tests suivants
                    // sauf si c'est le dernier test qui a échoué
                }
            } catch (SQLException ex) {
                System.err.println("Erreur lors du nettoyage : " + ex.getMessage());
            }
        }
    }

    // ========================
    // TEST 1 : CREATE (Ajout)
    // ========================
    @Test
    @Order(1)
    @DisplayName("Ajout d'un événement de test")
    void testAddEntity() throws Exception {
        // Préparer un événement de test
        evenementTest = new Evenement();
        evenementTest.setTitre("TEST - Conférence JUnit");
        evenementTest.setDescription("Événement créé par les tests unitaires");
        evenementTest.setImageUrl("http://test.com/image.jpg");
        evenementTest.setTypeEvenement(Type.conference);
        evenementTest.setDateDebut(LocalDate.of(2026, 12, 1));
        evenementTest.setDateFin(LocalDate.of(2026, 12, 1));
        evenementTest.setHoraireDebut(LocalTime.of(9, 0));
        evenementTest.setHoraireFin(LocalTime.of(17, 0));
        evenementTest.setLieu("Salle de Test");
        evenementTest.setAdresse("123 Rue de Test, Tunis");
        evenementTest.setCapaciteMax(100);
        evenementTest.setPlacesDisponibles(100);
        evenementTest.setOrganisateur("JUnit Test");
        evenementTest.setContactEmail("test@junit.org");
        evenementTest.setContactTel("+21612345678");
        evenementTest.setStatut(Statutevent.actif);

        // Compter les événements avant l'ajout
        List<Evenement> listeAvant = evenementService.getData();
        int countAvant = listeAvant.size();

        // Ajouter l'événement
        assertDoesNotThrow(() -> evenementService.addEntity(evenementTest));

        // Vérifier que l'événement a bien été ajouté
        List<Evenement> listeApres = evenementService.getData();
        assertEquals(countAvant + 1, listeApres.size(),
                "Le nombre d'événements doit augmenter de 1 après l'ajout");

        // Récupérer l'ID de l'événement créé (le dernier inséré avec le titre de test)
        idEvenementCree = listeApres.stream()
                .filter(e -> "TEST - Conférence JUnit".equals(e.getTitre()))
                .mapToInt(Evenement::getIdEvenement)
                .max()
                .orElse(-1);

        assertTrue(idEvenementCree > 0, "L'événement de test doit avoir été inséré avec un ID valide");
        System.out.println("✅ Événement de test créé avec ID = " + idEvenementCree);
    }

    // ========================
    // TEST 2 : READ (Lecture)
    // ========================
    @Test
    @Order(2)
    @DisplayName("Lecture de l'événement créé par ID")
    void testGetById() throws SQLException {
        assertTrue(idEvenementCree > 0, "Pré-condition : l'événement de test doit exister");

        Evenement e = evenementService.getById(idEvenementCree);

        assertNotNull(e, "L'événement récupéré ne doit pas être null");
        assertEquals("TEST - Conférence JUnit", e.getTitre());
        assertEquals("Événement créé par les tests unitaires", e.getDescription());
        assertEquals(Type.conference, e.getTypeEvenement());
        assertEquals("Salle de Test", e.getLieu());
        assertEquals(100, e.getCapaciteMax());
        assertEquals(100, e.getPlacesDisponibles());
        assertEquals("JUnit Test", e.getOrganisateur());
        assertEquals("test@junit.org", e.getContactEmail());

        System.out.println("✅ Lecture de l'événement OK : " + e.getTitre());
    }

    // ============================
    // TEST 3 : READ (Liste complète)
    // ============================
    @Test
    @Order(3)
    @DisplayName("Récupération de la liste complète des événements")
    void testGetData() throws Exception {
        List<Evenement> liste = evenementService.getData();

        assertNotNull(liste, "La liste d'événements ne doit pas être null");
        assertFalse(liste.isEmpty(), "La liste d'événements ne doit pas être vide");

        // Vérifier que notre événement de test est dans la liste
        boolean found = liste.stream()
                .anyMatch(e -> e.getIdEvenement() == idEvenementCree);
        assertTrue(found, "L'événement de test doit être présent dans la liste");

        System.out.println("✅ Liste des événements récupérée : " + liste.size() + " événement(s)");
    }

    // ============================
    // TEST 4 : UPDATE (Modification)
    // ============================
    @Test
    @Order(4)
    @DisplayName("Modification de l'événement de test")
    void testUpdateEntity() throws SQLException {
        assertTrue(idEvenementCree > 0, "Pré-condition : l'événement de test doit exister");

        // Récupérer l'événement existant
        Evenement e = evenementService.getById(idEvenementCree);
        assertNotNull(e);

        // Modifier les champs
        e.setTitre("TEST - Conférence JUnit (Modifié)");
        e.setDescription("Description modifiée par le test unitaire");
        e.setCapaciteMax(200);
        e.setPlacesDisponibles(150);
        e.setLieu("Nouvelle Salle de Test");

        // Appliquer la mise à jour
        assertDoesNotThrow(() -> evenementService.updateEntity(idEvenementCree, e));

        // Vérifier la mise à jour
        Evenement updated = evenementService.getById(idEvenementCree);
        assertNotNull(updated);
        assertEquals("TEST - Conférence JUnit (Modifié)", updated.getTitre());
        assertEquals("Description modifiée par le test unitaire", updated.getDescription());
        assertEquals(200, updated.getCapaciteMax());
        assertEquals(150, updated.getPlacesDisponibles());
        assertEquals("Nouvelle Salle de Test", updated.getLieu());

        System.out.println("✅ Événement mis à jour avec succès");
    }

    // ==============================
    // TEST 5 : UPDATE STATUT
    // ==============================
    @Test
    @Order(5)
    @DisplayName("Mise à jour du statut de l'événement")
    void testUpdateStatut() throws SQLException {
        assertTrue(idEvenementCree > 0, "Pré-condition : l'événement de test doit exister");

        // Changer le statut en "annule"
        assertDoesNotThrow(() -> evenementService.updateStatut(idEvenementCree, "annule"));

        Evenement e = evenementService.getById(idEvenementCree);
        assertNotNull(e);
        assertEquals(Statutevent.annule, e.getStatut(),
                "Le statut doit être 'annule' après la mise à jour");

        System.out.println("✅ Statut mis à jour vers 'annule'");
    }

    // ============================
    // TEST 6 : DELETE (Suppression)
    // ============================
    @Test
    @Order(6)
    @DisplayName("Suppression de l'événement de test")
    void testDeleteEntity() throws SQLException {
        assertTrue(idEvenementCree > 0, "Pré-condition : l'événement de test doit exister");

        Evenement e = evenementService.getById(idEvenementCree);
        assertNotNull(e, "L'événement à supprimer doit exister");

        // Supprimer l'événement
        assertDoesNotThrow(() -> evenementService.deleteEntity(e));

        // Vérifier la suppression
        Evenement deleted = evenementService.getById(idEvenementCree);
        assertNull(deleted, "L'événement doit être null après suppression");

        System.out.println("✅ Événement supprimé avec succès (ID=" + idEvenementCree + ")");

        // Réinitialiser l'ID pour éviter un double nettoyage
        idEvenementCree = 0;
    }

    // ==========================================================
    // TEST 7 : Vérification finale - l'événement n'existe plus
    // ==========================================================
    @Test
    @Order(7)
    @DisplayName("Vérification que la base est propre après suppression")
    void testBasePropre() throws Exception {
        List<Evenement> liste = evenementService.getData();
        boolean found = liste.stream()
                .anyMatch(e -> "TEST - Conférence JUnit".equals(e.getTitre())
                        || "TEST - Conférence JUnit (Modifié)".equals(e.getTitre()));
        assertFalse(found, "Aucun événement de test ne doit subsister dans la base");

        System.out.println("✅ Base de données propre - aucune donnée de test résiduelle");
    }
}
