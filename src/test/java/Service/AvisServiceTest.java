package Service;

import edu.connection3a7.entities.Avis;
import edu.connection3a7.service.Avisservice;
import edu.connection3a7.service.Demandeservice;
import edu.connection3a7.service.Technicienserv;
import edu.connection3a7.tools.MyConnection;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AvisServiceTest {

    private Avisservice avisService;
    private Demandeservice demandeService;
    private Technicienserv technicienService;
    private Connection cnx;

    private Integer testAvisId;
    private Integer testUtilisateurId;
    private Integer testTechnicienId;
    private Integer testDemandeId;

    @BeforeAll
    void setUpBeforeClass() throws SQLException {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║    DÉBUT TESTS CRUD - AvisService     ║");
        System.out.println("╚════════════════════════════════════════╝");

        cnx = MyConnection.getInstance().getCnx();
        avisService = new Avisservice();
        demandeService = new Demandeservice();
        technicienService = new Technicienserv();

        // Récupérer ou créer un utilisateur
        testUtilisateurId = getOrCreateUtilisateur();

        // Récupérer ou créer un technicien
        testTechnicienId = getOrCreateTechnicien();

        // Récupérer ou créer une demande
        testDemandeId = getOrCreateDemande();

        assertNotNull(testUtilisateurId, "❌ Utilisateur non trouvé");
        assertNotNull(testTechnicienId, "❌ Technicien non trouvé");
        assertNotNull(testDemandeId, "❌ Demande non trouvée");

        System.out.println("✅ Connexion OK");
        System.out.println("✅ Utilisateur ID: " + testUtilisateurId);
        System.out.println("✅ Technicien ID: " + testTechnicienId);
        System.out.println("✅ Demande ID: " + testDemandeId);
    }

    private Integer getOrCreateUtilisateur() throws SQLException {
        // Essayer de récupérer un utilisateur existant
        String selectSql = "SELECT id_utilisateur FROM utilisateurs LIMIT 1";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(selectSql)) {
            if (rs.next()) {
                return rs.getInt("id_utilisateur");
            }
        }

        // Créer un utilisateur si aucun n'existe
        String insertSql = "INSERT INTO utilisateurs (nom, prenom, email, password) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "Test");
            ps.setString(2, "User");
            ps.setString(3, "test.avis@email.com");
            ps.setString(4, "password123");
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return null;
    }

    private Integer getOrCreateTechnicien() throws SQLException {
        // Essayer de récupérer un technicien existant
        String selectSql = "SELECT id_tech FROM technicien LIMIT 1";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(selectSql)) {
            if (rs.next()) {
                return rs.getInt("id_tech");
            }
        }

        // Créer un technicien
        String insertSql = "INSERT INTO technicien (nom, prenom, email, specialite, telephone, disponibilite, localisation, image, cin, age, date_naissance) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "Test");
            ps.setString(2, "Technicien");
            ps.setString(3, "tech.avis@email.com");
            ps.setString(4, "Test");
            ps.setString(5, "12345678");
            ps.setBoolean(6, true);
            ps.setString(7, "Tunis");
            ps.setString(8, "default.png");
            ps.setString(9, "CIN" + System.currentTimeMillis());
            ps.setInt(10, 30);
            ps.setDate(11, Date.valueOf(LocalDate.of(1995, 1, 1)));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return null;
    }

    private Integer getOrCreateDemande() throws SQLException {
        // Essayer de récupérer une demande existante
        String selectSql = "SELECT id_demande FROM demande LIMIT 1";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(selectSql)) {
            if (rs.next()) {
                return rs.getInt("id_demande");
            }
        }

        // Créer une demande
        String insertSql = "INSERT INTO demande (id_utilisateur, type_probleme, description, date_demande, statut, id_tech) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, testUtilisateurId);
            ps.setString(2, "Test");
            ps.setString(3, "Demande pour test avis");
            ps.setDate(4, Date.valueOf(LocalDate.now()));
            ps.setString(5, "Terminée");
            ps.setInt(6, testTechnicienId);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return null;
    }

    @AfterAll
    void tearDownAfterClass() throws SQLException {
        // Nettoyer les données de test
        String sql = "DELETE FROM avis WHERE commentaire LIKE ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, "%TEST AVIS%");
            int deleted = ps.executeUpdate();
            System.out.println("🧹 Nettoyage: " + deleted + " avis de test supprimé(s)");
        }

        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║     FIN TESTS CRUD - AvisService      ║");
        System.out.println("╚════════════════════════════════════════╝");
    }

    // ========================================
    // TEST 1: CREATE
    // ========================================
    @Test
    @Order(1)
    @DisplayName("📌 CREATE - Ajouter un avis")
    void testCreate() throws SQLException {
        System.out.println("\n🧪 TEST 1: CREATE");

        Avis avis = new Avis();
        avis.setIdUtilisateur(testUtilisateurId);
        avis.setNote(5);
        avis.setCommentaire("TEST AVIS - Excellent travail!");
        avis.setDateAvis(Date.valueOf(LocalDate.now()));
        avis.setIdTech(testTechnicienId);
        avis.setIdDemande(testDemandeId);

        int avant = avisService.getdata().size();
        avisService.addentitiy(avis);
        int apres = avisService.getdata().size();

        assertEquals(avant + 1, apres, "❌ Le nombre d'avis devrait augmenter de 1");
        assertNotNull(avis.getIdAvis(), "❌ L'ID devrait être généré");

        testAvisId = avis.getIdAvis();

        System.out.println("✅ CREATE réussi - ID: " + testAvisId);
        System.out.println("   ├─ Utilisateur: " + testUtilisateurId);
        System.out.println("   ├─ Technicien: " + testTechnicienId);
        System.out.println("   ├─ Note: 5/5");
        System.out.println("   └─ Commentaire: Excellent travail!");
    }

    // ========================================
    // TEST 2: READ ALL
    // ========================================
    @Test
    @Order(2)
    @DisplayName("📌 READ - Récupérer tous les avis")
    void testReadAll() throws SQLException {
        System.out.println("\n🧪 TEST 2: READ ALL");

        List<Avis> avisList = avisService.getdata();

        assertNotNull(avisList, "❌ La liste ne doit pas être null");
        assertTrue(avisList.size() > 0, "❌ La liste ne doit pas être vide");

        System.out.println("✅ READ ALL réussi - " + avisList.size() + " avis trouvé(s)");
    }

    // ========================================
    // TEST 3: READ BY TECHNICIEN
    // ========================================
    @Test
    @Order(3)
    @DisplayName("📌 READ BY TECH - Avis par technicien")
    void testReadByTechnicien() throws SQLException {
        System.out.println("\n🧪 TEST 3: READ BY TECH");

        List<Avis> avisList = avisService.getAvisByTechnicien(testTechnicienId);

        assertNotNull(avisList, "❌ La liste ne doit pas être null");
        assertTrue(avisList.size() > 0, "❌ Le technicien devrait avoir au moins un avis");

        // Vérifier que notre avis de test est dans la liste
        boolean found = avisList.stream()
                .anyMatch(a -> a.getIdAvis().equals(testAvisId));

        assertTrue(found, "❌ L'avis de test devrait être dans la liste");

        // Calculer la note moyenne
        double moyenne = avisService.getNoteMoyenneTechnicien(testTechnicienId);
        System.out.println("✅ READ BY TECH réussi - " + avisList.size() + " avis");
        System.out.println("   ├─ Note moyenne: " + String.format("%.2f", moyenne) + "/5");

        for (Avis a : avisList) {
            System.out.println("   ├─ ID: " + a.getIdAvis() +
                    " | Note: " + a.getNote() + "/5" +
                    " | " + a.getCommentaire());
        }
    }

    // ========================================
    // TEST 4: READ BY USER
    // ========================================
    @Test
    @Order(4)
    @DisplayName("📌 READ BY USER - Avis par utilisateur")
    void testReadByUser() throws SQLException {
        System.out.println("\n🧪 TEST 4: READ BY USER");

        List<Avis> avisList = avisService.getAvisByUtilisateur(testUtilisateurId);

        assertNotNull(avisList, "❌ La liste ne doit pas être null");
        assertTrue(avisList.size() > 0, "❌ L'utilisateur devrait avoir au moins un avis");

        System.out.println("✅ READ BY USER réussi - " + avisList.size() + " avis");
    }

    // ========================================
    // TEST 5: READ BY DEMANDE
    // ========================================
    @Test
    @Order(5)
    @DisplayName("📌 READ BY DEMANDE - Avis par demande")
    void testReadByDemande() throws SQLException {
        System.out.println("\n🧪 TEST 5: READ BY DEMANDE");

        List<Avis> avisList = avisService.getAvisByDemande(testDemandeId);

        assertNotNull(avisList, "❌ La liste ne doit pas être null");

        System.out.println("✅ READ BY DEMANDE réussi - " + avisList.size() + " avis");
    }

    // ========================================
    // TEST 6: UPDATE
    // ========================================
    @Test
    @Order(6)
    @DisplayName("📌 UPDATE - Modifier un avis")
    void testUpdate() throws SQLException {
        System.out.println("\n🧪 TEST 6: UPDATE");

        assertNotNull(testAvisId, "❌ Aucun avis à modifier");

        // Récupérer l'avis
        List<Avis> avisList = avisService.getdata();
        Avis avis = avisList.stream()
                .filter(a -> a.getIdAvis().equals(testAvisId))
                .findFirst()
                .orElse(null);

        assertNotNull(avis, "❌ Avis non trouvé");

        // Modifier
        String nouveauCommentaire = "TEST AVIS - Modifié: Service excellent!";
        int nouvelleNote = 4;

        System.out.println("📝 Avant modification:");
        System.out.println("   ├─ Note: " + avis.getNote() + "/5");
        System.out.println("   └─ Commentaire: " + avis.getCommentaire());

        avis.setNote(nouvelleNote);
        avis.setCommentaire(nouveauCommentaire);

        avisService.update(avis);

        // Vérifier
        List<Avis> avisListApres = avisService.getdata();
        Avis avisModifie = avisListApres.stream()
                .filter(a -> a.getIdAvis().equals(testAvisId))
                .findFirst()
                .orElse(null);

        assertNotNull(avisModifie);
        assertEquals(nouvelleNote, avisModifie.getNote(), "❌ La note n'a pas été mise à jour");
        assertEquals(nouveauCommentaire, avisModifie.getCommentaire(), "❌ Le commentaire n'a pas été mis à jour");

        System.out.println("📝 Après modification:");
        System.out.println("   ├─ Note: " + avisModifie.getNote() + "/5");
        System.out.println("   └─ Commentaire: " + avisModifie.getCommentaire());
        System.out.println("✅ UPDATE réussi");
    }

    // ========================================
    // TEST 7: DELETE
    // ========================================
    @Test
    @Order(7)
    @DisplayName("📌 DELETE - Supprimer un avis")
    void testDelete() throws SQLException {
        System.out.println("\n🧪 TEST 7: DELETE");

        assertNotNull(testAvisId, "❌ Aucun avis à supprimer");

        int avant = avisService.getdata().size();
        System.out.println("📊 Avant suppression: " + avant + " avis");

        Avis avis = new Avis();
        avis.setIdAvis(testAvisId);
        avisService.delet(avis);

        int apres = avisService.getdata().size();
        System.out.println("📊 Après suppression: " + apres + " avis");

        assertEquals(avant - 1, apres, "❌ Le nombre d'avis devrait diminuer de 1");

        // Vérifier que l'avis n'existe plus
        List<Avis> avisList = avisService.getdata();
        boolean existe = avisList.stream()
                .anyMatch(a -> a.getIdAvis().equals(testAvisId));
        assertFalse(existe, "❌ L'avis devrait être supprimé");

        System.out.println("✅ DELETE réussi");
    }
}