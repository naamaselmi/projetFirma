package edu.connection3a7.services;

import edu.connection3a7.entities.*;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour AccompagnantService.
 * Ordre CRUD : Create → Read → Update → Delete.
 * 
 * Crée un événement + une participation temporaires pour tester les accompagnants.
 * Nettoyage complet en fin de tests.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccompagnantServiceTest {

    private static AccompagnantService accompagnantService;
    private static ParticipationService participationService;
    private static EvenementService evenementService;

    private static int idEvenementTest;
    private static int idParticipationTest;

    private static final int ID_UTILISATEUR_TEST = 1;

    /**
     * Prépare l'environnement : crée un événement et une participation
     * pour pouvoir tester les accompagnants.
     */
    @BeforeAll
    static void setUp() throws Exception {
        accompagnantService = new AccompagnantService();
        participationService = new ParticipationService();
        evenementService = new EvenementService();

        // 1. Créer un événement temporaire
        Evenement evenementTest = new Evenement();
        evenementTest.setTitre("TEST_ACCOMPAGNANT - Événement temporaire");
        evenementTest.setDescription("Pour tester les accompagnants");
        evenementTest.setImageUrl("http://test.com/img.jpg");
        evenementTest.setTypeEvenement(Type.atelier);
        evenementTest.setDateDebut(LocalDate.of(2026, 11, 20));
        evenementTest.setDateFin(LocalDate.of(2026, 11, 20));
        evenementTest.setHoraireDebut(LocalTime.of(10, 0));
        evenementTest.setHoraireFin(LocalTime.of(16, 0));
        evenementTest.setLieu("Salle Accompagnants");
        evenementTest.setAdresse("Adresse Test Accompagnants");
        evenementTest.setCapaciteMax(30);
        evenementTest.setPlacesDisponibles(30);
        evenementTest.setOrganisateur("JUnit");
        evenementTest.setContactEmail("test@accompagnant.org");
        evenementTest.setContactTel("+21611111111");
        evenementTest.setStatut(Statutevent.actif);

        evenementService.addEntity(evenementTest);

        List<Evenement> listeEv = evenementService.getData();
        idEvenementTest = listeEv.stream()
                .filter(e -> "TEST_ACCOMPAGNANT - Événement temporaire".equals(e.getTitre()))
                .mapToInt(Evenement::getIdEvenement)
                .max()
                .orElse(-1);
        assertTrue(idEvenementTest > 0, "L'événement de test doit être créé");

        // 2. Créer une participation temporaire
        Participation participation = new Participation();
        participation.setIdEvenement(idEvenementTest);
        participation.setIdUtilisateur(ID_UTILISATEUR_TEST);
        participation.setStatut(Statut.CONFIRME);
        participation.setDateInscription(LocalDateTime.now());
        participation.setNombreAccompagnants(0);
        participation.setCommentaire("Participation pour test accompagnants");

        participationService.addEntity(participation);
        idParticipationTest = participation.getIdParticipation();
        assertTrue(idParticipationTest > 0, "La participation de test doit être créée");

        System.out.println("🔧 Env. de test prêt : Événement ID=" + idEvenementTest
                + ", Participation ID=" + idParticipationTest);
    }

    /**
     * Nettoyage après chaque test.
     */
    @AfterEach
    void afterEach() {
        System.out.println("--- Fin du test accompagnant ---");
    }

    // ========================
    // TEST 1 : CREATE (Ajout unitaire)
    // ========================
    @Test
    @Order(1)
    @DisplayName("Ajout d'un accompagnant")
    void testAddAccompagnant() throws SQLException {
        Accompagnant a = new Accompagnant();
        a.setIdParticipation(idParticipationTest);
        a.setNom("Dupont");
        a.setPrenom("Jean");

        assertDoesNotThrow(() -> accompagnantService.addAccompagnant(a));

        // Vérifier que l'accompagnant a été ajouté
        List<Accompagnant> liste = accompagnantService.getByParticipation(idParticipationTest);
        assertFalse(liste.isEmpty(), "Il doit y avoir au moins un accompagnant");

        boolean found = liste.stream()
                .anyMatch(acc -> "Dupont".equals(acc.getNom()) && "Jean".equals(acc.getPrenom()));
        assertTrue(found, "L'accompagnant Dupont Jean doit exister");

        System.out.println("✅ Accompagnant ajouté : Dupont Jean");
    }

    // ==========================
    // TEST 2 : CREATE (Lot/Batch)
    // ==========================
    @Test
    @Order(2)
    @DisplayName("Ajout d'accompagnants en lot (batch)")
    void testAddAccompagnants() throws SQLException {
        List<Accompagnant> accompagnants = Arrays.asList(
                new Accompagnant("Martin", "Sophie"),
                new Accompagnant("Bernard", "Pierre")
        );

        assertDoesNotThrow(() ->
                accompagnantService.addAccompagnants(idParticipationTest, accompagnants));

        // Vérifier : on doit avoir au moins 3 accompagnants (1 du test 1 + 2 du batch)
        List<Accompagnant> liste = accompagnantService.getByParticipation(idParticipationTest);
        assertTrue(liste.size() >= 3,
                "Il doit y avoir au moins 3 accompagnants après l'ajout en lot");

        System.out.println("✅ " + accompagnants.size() + " accompagnants ajoutés en lot");
    }

    // ========================
    // TEST 3 : READ (Par participation)
    // ========================
    @Test
    @Order(3)
    @DisplayName("Lecture des accompagnants par participation")
    void testGetByParticipation() throws SQLException {
        List<Accompagnant> liste = accompagnantService.getByParticipation(idParticipationTest);

        assertNotNull(liste, "La liste ne doit pas être null");
        assertTrue(liste.size() >= 3, "Il doit y avoir au moins 3 accompagnants");

        // Vérifier les données de chaque accompagnant
        for (Accompagnant a : liste) {
            assertNotNull(a.getNom(), "Le nom ne doit pas être null");
            assertNotNull(a.getPrenom(), "Le prénom ne doit pas être null");
            assertEquals(idParticipationTest, a.getIdParticipation());
            assertTrue(a.getIdAccompagnant() > 0, "L'ID de l'accompagnant doit être positif");
        }

        System.out.println("✅ " + liste.size() + " accompagnant(s) récupéré(s) pour la participation");
    }

    // ================================
    // TEST 4 : READ (Par événement)
    // ================================
    @Test
    @Order(4)
    @DisplayName("Lecture des accompagnants par événement")
    void testGetByEvenement() throws SQLException {
        List<Accompagnant> liste = accompagnantService.getByEvenement(idEvenementTest);

        assertNotNull(liste, "La liste ne doit pas être null");
        assertFalse(liste.isEmpty(),
                "Il doit y avoir des accompagnants pour l'événement de test");

        System.out.println("✅ " + liste.size() + " accompagnant(s) récupéré(s) pour l'événement");
    }

    // ============================
    // TEST 5 : UPDATE (Mise à jour)
    // ============================
    @Test
    @Order(5)
    @DisplayName("Mise à jour des accompagnants (remplacement)")
    void testUpdateAccompagnants() throws SQLException {
        // Nouveaux accompagnants (remplacent les anciens)
        List<Accompagnant> nouveaux = Arrays.asList(
                new Accompagnant("Nouvel", "Accompagnant1"),
                new Accompagnant("Nouvel", "Accompagnant2")
        );

        assertDoesNotThrow(() ->
                accompagnantService.updateAccompagnants(idParticipationTest, nouveaux));

        // Vérifier : on doit avoir exactement 2 accompagnants (les anciens sont supprimés)
        List<Accompagnant> liste = accompagnantService.getByParticipation(idParticipationTest);
        assertEquals(2, liste.size(),
                "Après mise à jour, il doit y avoir exactement 2 accompagnants");

        boolean found1 = liste.stream()
                .anyMatch(a -> "Accompagnant1".equals(a.getPrenom()));
        boolean found2 = liste.stream()
                .anyMatch(a -> "Accompagnant2".equals(a.getPrenom()));
        assertTrue(found1 && found2, "Les nouveaux accompagnants doivent être présents");

        System.out.println("✅ Accompagnants mis à jour (remplacement complet)");
    }

    // ============================
    // TEST 6 : DELETE (Suppression)
    // ============================
    @Test
    @Order(6)
    @DisplayName("Suppression des accompagnants d'une participation")
    void testDeleteByParticipation() throws SQLException {
        assertDoesNotThrow(() ->
                accompagnantService.deleteByParticipation(idParticipationTest));

        List<Accompagnant> liste = accompagnantService.getByParticipation(idParticipationTest);
        assertTrue(liste.isEmpty(),
                "Après suppression, il ne doit plus y avoir d'accompagnants");

        System.out.println("✅ Tous les accompagnants supprimés pour la participation");
    }

    // ============================================
    // TEST 7 : Nettoyage complet de l'environnement
    // ============================================
    @Test
    @Order(7)
    @DisplayName("Nettoyage : suppression participation et événement de test")
    void testNettoyageComplet() throws SQLException {
        // Supprimer la participation de test
        if (idParticipationTest > 0) {
            Participation p = participationService.getById(idParticipationTest);
            if (p != null) {
                participationService.deleteEntity(p);
                System.out.println("🧹 Participation de test supprimée (ID=" + idParticipationTest + ")");
            }
        }

        // Supprimer l'événement de test
        if (idEvenementTest > 0) {
            Evenement e = evenementService.getById(idEvenementTest);
            if (e != null) {
                evenementService.deleteEntity(e);
                System.out.println("🧹 Événement de test supprimé (ID=" + idEvenementTest + ")");
            }
        }

        // Vérifications finales
        Evenement ev = evenementService.getById(idEvenementTest);
        assertNull(ev, "L'événement de test doit être null");

        Participation pa = participationService.getById(idParticipationTest);
        assertNull(pa, "La participation de test doit être null");

        List<Accompagnant> acc = accompagnantService.getByParticipation(idParticipationTest);
        assertTrue(acc.isEmpty(), "Aucun accompagnant ne doit rester");

        System.out.println("✅ Nettoyage complet terminé - base propre");
    }
}
