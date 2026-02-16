package Service;

import edu.connection3a7.entities.Technicien;
import edu.connection3a7.service.Technicienserv;
import edu.connection3a7.tools.MyConnection;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TechnicienServiceTest {

    private Technicienserv technicienService;
    private Connection cnx;
    private Integer testTechnicienId;
    private final String TEST_CIN = "TEST" + System.currentTimeMillis(); // CIN unique

    @BeforeAll
    void setUpBeforeClass() throws SQLException {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║  DÉBUT TESTS CRUD - TechnicienService ║");
        System.out.println("╚════════════════════════════════════════╝");

        cnx = MyConnection.getInstance().getCnx();
        technicienService = new Technicienserv();

        assertNotNull(cnx, "❌ Connexion à la base de données échouée");
        assertNotNull(technicienService, "❌ Service non initialisé");

        System.out.println("✅ Connexion OK");
        System.out.println("✅ CIN de test: " + TEST_CIN);
    }

    @AfterAll
    void tearDownAfterClass() throws SQLException {
        String sql = "DELETE FROM technicien WHERE cin = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, TEST_CIN);
            int deleted = ps.executeUpdate();
            System.out.println("🧹 Nettoyage: " + deleted + " technicien(s) de test supprimé(s)");
        }

        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║    FIN TESTS CRUD - TechnicienService ║");
        System.out.println("╚════════════════════════════════════════╝");
    }

    @Test
    @Order(1)
    @DisplayName("📌 CREATE - Ajouter un technicien")
    void testCreate() throws SQLException {
        System.out.println("\n🧪 TEST 1: CREATE");

        Technicien technicien = new Technicien();
        technicien.setNom("Martin");
        technicien.setPrenom("Sophie");
        technicien.setEmail("sophie.martin@email.com");
        technicien.setSpecialite("Réseau");
        technicien.setTelephone("+216 98 765 432");
        technicien.setDisponibilite(true);
        technicien.setLocalisation("Tunis");
        technicien.setImage("default-avatar.png");
        technicien.setCin(TEST_CIN);
        technicien.setAge(28);
        technicien.setDateNaissance(LocalDate.of(1997, 5, 15));

        int avant = technicienService.getdata().size();
        technicienService.addentitiy(technicien);
        int apres = technicienService.getdata().size();

        assertEquals(avant + 1, apres, "❌ Le nombre de techniciens devrait augmenter de 1");
        assertTrue(technicien.getId_tech() > 0, "❌ L'ID devrait être généré");

        testTechnicienId = technicien.getId_tech();
        System.out.println("✅ CREATE réussi - ID: " + testTechnicienId);
        System.out.println("   ├─ Nom: " + technicien.getPrenom() + " " + technicien.getNom());
        System.out.println("   ├─ Spécialité: " + technicien.getSpecialite());
        System.out.println("   ├─ Disponibilité: " + (technicien.isDisponibilite() ? "Disponible" : "Non disponible"));
        System.out.println("   └─ CIN: " + technicien.getCin());
    }

    @Test
    @Order(2)
    @DisplayName("📌 READ - Récupérer tous les techniciens")
    void testReadAll() throws SQLException {
        System.out.println("\n🧪 TEST 2: READ ALL");

        List<Technicien> techniciens = technicienService.getdata();

        assertNotNull(techniciens, "❌ La liste ne doit pas être null");
        assertTrue(techniciens.size() > 0, "❌ La liste ne doit pas être vide");

        System.out.println("✅ READ ALL réussi - " + techniciens.size() + " technicien(s)");

        // Afficher les 3 premiers
        techniciens.stream()
                .limit(3)
                .forEach(t -> System.out.println("   ├─ ID: " + t.getId_tech() +
                        " | " + t.getPrenom() + " " + t.getNom() +
                        " | " + t.getSpecialite() +
                        " | " + (t.isDisponibilite() ? "✅" : "❌")));
    }

    @Test
    @Order(3)
    @DisplayName("📌 READ BY CIN - Rechercher par CIN")
    void testReadByCin() throws SQLException {
        System.out.println("\n🧪 TEST 3: READ BY CIN");

        Technicien technicien = technicienService.chercherParCin(TEST_CIN);

        assertNotNull(technicien, "❌ Technicien non trouvé avec CIN: " + TEST_CIN);
        assertEquals(TEST_CIN, technicien.getCin(), "❌ Le CIN ne correspond pas");
        assertEquals(testTechnicienId, technicien.getId_tech(), "❌ L'ID ne correspond pas");

        System.out.println("✅ READ BY CIN réussi");
        System.out.println("   ├─ ID trouvé: " + technicien.getId_tech());
        System.out.println("   ├─ Nom: " + technicien.getPrenom() + " " + technicien.getNom());
        System.out.println("   └─ Spécialité: " + technicien.getSpecialite());

        // Test avec CIN inexistant
        Technicien inexistant = technicienService.chercherParCin("CIN_INEXISTANT_999");
        assertNull(inexistant, "❌ La recherche d'un CIN inexistant devrait retourner null");
        System.out.println("   └─ Recherche CIN inexistant: OK (retourne null)");
    }

    @Test
    @Order(4)
    @DisplayName("📌 UPDATE - Modifier un technicien")
    void testUpdate() throws SQLException {
        System.out.println("\n🧪 TEST 4: UPDATE");

        assertNotNull(testTechnicienId, "❌ Aucun technicien à modifier");

        // Récupérer le technicien
        Technicien technicien = technicienService.getdata().stream()
                .filter(t -> t.getId_tech() == testTechnicienId)
                .findFirst()
                .orElse(null);

        assertNotNull(technicien, "❌ Technicien non trouvé");

        System.out.println("📝 Avant modification:");
        System.out.println("   ├─ Spécialité: " + technicien.getSpecialite());
        System.out.println("   ├─ Téléphone: " + technicien.getTelephone());
        System.out.println("   ├─ Disponibilité: " + (technicien.isDisponibilite() ? "Disponible" : "Non disponible"));
        System.out.println("   └─ Localisation: " + technicien.getLocalisation());

        // Modifier
        String nouvelleSpecialite = "Sécurité Informatique";
        String nouveauTelephone = "+216 99 888 777";
        boolean nouvelleDisponibilite = false;
        String nouvelleLocalisation = "Sousse";

        technicien.setSpecialite(nouvelleSpecialite);
        technicien.setTelephone(nouveauTelephone);
        technicien.setDisponibilite(nouvelleDisponibilite);
        technicien.setLocalisation(nouvelleLocalisation);

        technicienService.update(technicien);

        // Vérifier
        Technicien technicienModifie = technicienService.getdata().stream()
                .filter(t -> t.getId_tech() == testTechnicienId)
                .findFirst()
                .orElse(null);

        assertNotNull(technicienModifie);
        assertEquals(nouvelleSpecialite, technicienModifie.getSpecialite(), "❌ Spécialité non mise à jour");
        assertEquals(nouveauTelephone, technicienModifie.getTelephone(), "❌ Téléphone non mis à jour");
        assertEquals(nouvelleDisponibilite, technicienModifie.isDisponibilite(), "❌ Disponibilité non mise à jour");
        assertEquals(nouvelleLocalisation, technicienModifie.getLocalisation(), "❌ Localisation non mise à jour");

        System.out.println("📝 Après modification:");
        System.out.println("   ├─ Spécialité: " + technicienModifie.getSpecialite());
        System.out.println("   ├─ Téléphone: " + technicienModifie.getTelephone());
        System.out.println("   ├─ Disponibilité: " + (technicienModifie.isDisponibilite() ? "Disponible" : "Non disponible"));
        System.out.println("   └─ Localisation: " + technicienModifie.getLocalisation());
        System.out.println("✅ UPDATE réussi");
    }

    @Test
    @Order(5)
    @DisplayName("📌 UPDATE DISPO - Changer disponibilité")
    void testUpdateDisponibilite() throws SQLException {
        System.out.println("\n🧪 TEST 5: UPDATE DISPO");

        assertNotNull(testTechnicienId, "❌ Aucun technicien à modifier");

        // Récupérer le technicien
        Technicien technicien = technicienService.getdata().stream()
                .filter(t -> t.getId_tech() == testTechnicienId)
                .findFirst()
                .orElse(null);

        assertNotNull(technicien);

        boolean ancienneDispo = technicien.isDisponibilite();
        boolean nouvelleDispo = !ancienneDispo;

        System.out.println("📝 Disponibilité: " + (ancienneDispo ? "Disponible → Non disponible" : "Non disponible → Disponible"));

        technicien.setDisponibilite(nouvelleDispo);
        technicienService.update(technicien);

        // Vérifier
        Technicien technicienModifie = technicienService.getdata().stream()
                .filter(t -> t.getId_tech() == testTechnicienId)
                .findFirst()
                .orElse(null);

        assertNotNull(technicienModifie);
        assertEquals(nouvelleDispo, technicienModifie.isDisponibilite(), "❌ Disponibilité non mise à jour");

        System.out.println("✅ UPDATE DISPO réussi - Nouvelle disponibilité: " +
                (technicienModifie.isDisponibilite() ? "Disponible" : "Non disponible"));
    }

    @Test
    @Order(6)
    @DisplayName("📌 DELETE - Supprimer un technicien")
    void testDelete() throws SQLException {
        System.out.println("\n🧪 TEST 6: DELETE");

        assertNotNull(testTechnicienId, "❌ Aucun technicien à supprimer");

        int avant = technicienService.getdata().size();

        Technicien technicien = new Technicien();
        technicien.setId_tech(testTechnicienId);
        technicienService.delet(technicien);

        int apres = technicienService.getdata().size();

        assertEquals(avant - 1, apres, "❌ Le nombre de techniciens devrait diminuer de 1");

        // Vérifier que le technicien n'existe plus
        List<Technicien> techniciens = technicienService.getdata();
        boolean existe = techniciens.stream()
                .anyMatch(t -> t.getId_tech() == testTechnicienId);
        assertFalse(existe, "❌ Le technicien devrait être supprimé");

        // Vérifier aussi par CIN
        Technicien recherche = technicienService.chercherParCin(TEST_CIN);
        assertNull(recherche, "❌ Le technicien ne devrait pas être trouvé par CIN");

        System.out.println("✅ DELETE réussi");
        System.out.println("   ├─ Avant: " + avant + " techniciens");
        System.out.println("   └─ Après: " + apres + " techniciens");
    }

    @Test
    @Order(7)
    @DisplayName("📌 READ FILTERED - Compter les disponibles")
    void testGetDisponibles() throws SQLException {
        System.out.println("\n🧪 TEST 7: READ FILTERED");

        List<Technicien> tous = technicienService.getdata();
        long disponibles = tous.stream()
                .filter(Technicien::isDisponibilite)
                .count();

        System.out.println("✅ Statistiques:");
        System.out.println("   ├─ Total techniciens: " + tous.size());
        System.out.println("   └─ Techniciens disponibles: " + disponibles);

        assertTrue(disponibles >= 0, "❌ Le nombre de disponibles ne peut pas être négatif");
    }
}