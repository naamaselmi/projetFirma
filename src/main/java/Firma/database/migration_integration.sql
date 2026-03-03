-- ============================================================================
-- MIGRATION: Intégration base collègue (mp) dans firma
-- Remplace la table utilisateurs par la version du collègue
-- Ajoute les données marketplace + triggers
-- ============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- ÉTAPE 1: Supprimer toutes les FK qui référencent utilisateurs(id_utilisateur)
-- ============================================================================

ALTER TABLE `avis` DROP FOREIGN KEY `fk_avis_utilisateur`;
ALTER TABLE `commandes` DROP FOREIGN KEY `fk_commandes_utilisateur`;
ALTER TABLE `commentaire` DROP FOREIGN KEY `fk_commentaire_utilisateur`;
ALTER TABLE `demande` DROP FOREIGN KEY `fk_demande_utilisateur`;
ALTER TABLE `locations` DROP FOREIGN KEY `fk_locations_utilisateur`;
ALTER TABLE `participations` DROP FOREIGN KEY `participations_ibfk_2`;
ALTER TABLE `post` DROP FOREIGN KEY `fk_post_utilisateur`;
ALTER TABLE `profile` DROP FOREIGN KEY `fk_profile_utilisateur`;
ALTER TABLE `technicien` DROP FOREIGN KEY `fk_technicien_utilisateur`;

-- ============================================================================
-- ÉTAPE 2: Modifier la table utilisateurs pour correspondre au collègue
-- ============================================================================

-- Renommer la colonne PK id_utilisateur → id
ALTER TABLE `utilisateurs` CHANGE `id_utilisateur` `id` int(11) NOT NULL AUTO_INCREMENT;

-- Supprimer la colonne code_postal
ALTER TABLE `utilisateurs` DROP COLUMN `code_postal`;

-- Remplacer la colonne 'role' par 'type_user'
-- D'abord mapper: utilisateur→client, technicien→client, admin→admin
UPDATE `utilisateurs` SET `role` = 'client' WHERE `role` IN ('utilisateur', 'technicien');
-- Puis changer le type de la colonne
ALTER TABLE `utilisateurs` CHANGE `role` `type_user` enum('client','admin') NOT NULL DEFAULT 'client';

-- Renommer date_inscription → date_creation
ALTER TABLE `utilisateurs` CHANGE `date_inscription` `date_creation` timestamp NOT NULL DEFAULT current_timestamp();

-- ============================================================================
-- ÉTAPE 3: Recréer toutes les FK vers utilisateurs(id) 
-- (les colonnes FK dans les autres tables gardent leur nom)
-- ============================================================================

ALTER TABLE `avis` ADD CONSTRAINT `fk_avis_utilisateur` 
  FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateurs` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `commandes` ADD CONSTRAINT `fk_commandes_utilisateur` 
  FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateurs` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `commentaire` ADD CONSTRAINT `fk_commentaire_utilisateur` 
  FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateurs` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `demande` ADD CONSTRAINT `fk_demande_utilisateur` 
  FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateurs` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `locations` ADD CONSTRAINT `fk_locations_utilisateur` 
  FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateurs` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `participations` ADD CONSTRAINT `participations_ibfk_2` 
  FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateurs` (`id`) ON DELETE CASCADE;

ALTER TABLE `post` ADD CONSTRAINT `fk_post_utilisateur` 
  FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateurs` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `profile` ADD CONSTRAINT `fk_profile_utilisateur` 
  FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateurs` (`id`) ON DELETE CASCADE;

ALTER TABLE `technicien` ADD CONSTRAINT `fk_technicien_utilisateur` 
  FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateurs` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- ============================================================================
-- ÉTAPE 4: Mettre à jour les données utilisateurs existants + ajouter ceux du collègue
-- ============================================================================

-- Votre utilisateur admin (id=1) reste
-- Votre utilisateur normal (id=2) reste
-- Ajouter l'utilisateur du collègue (id=3) s'il n'existe pas
INSERT IGNORE INTO `utilisateurs` (`id`, `type_user`, `nom`, `prenom`, `email`, `mot_de_passe`, `telephone`, `adresse`, `ville`, `date_creation`) VALUES
(3, 'client', 'Slimani', 'hamza', 'hamza.slimani@esprit.tn', '123', '+216 21788895', 'bizerte 7000', 'bizerte ', '2026-02-05 19:23:37');

-- ============================================================================
-- ÉTAPE 5: Ajouter les données marketplace (categories, fournisseurs, equipements, etc.)
-- ============================================================================

-- Vider les tables marketplace existantes (elles étaient vides de toute façon)
DELETE FROM `details_commandes`;
DELETE FROM `commandes`;
DELETE FROM `locations`;
DELETE FROM `equipements`;
DELETE FROM `fournisseurs`;
DELETE FROM `vehicules`;
DELETE FROM `terrains`;
DELETE FROM `categories`;

-- Categories
INSERT INTO `categories` (`id`, `nom`, `type_produit`, `description`) VALUES
(1, 'Tracteurs', 'vehicule', 'Tracteurs agricoles de différentes puissances'),
(2, 'Moissonneuses', 'vehicule', 'Moissonneuses-batteuses et récolteuses'),
(3, 'Camions et Remorques', 'vehicule', 'Véhicules de transport agricole'),
(4, 'Terres Arables', 'terrain', 'Terrains destinés aux cultures'),
(5, 'Pâturages', 'terrain', 'Terrains pour élevage'),
(6, 'Vergers et Plantations', 'terrain', 'Terrains avec arbres fruitiers'),
(7, 'Outils Manuels', 'equipement', 'Outils agricoles à main'),
(8, 'Systèmes d''Irrigation', 'equipement', 'Équipements d''arrosage et irrigation'),
(9, 'Machines Agricoles', 'equipement', 'Machines et équipements motorisés'),
(10, 'Tracteurs Compacts', 'vehicule', 'Petits tracteurs polyvalents'),
(11, 'Engins de Récolte', 'vehicule', 'Machines spécialisées pour la récolte'),
(12, 'Véhicules Utilitaires', 'vehicule', 'Fourgons, pick-ups et utilitaires'),
(13, 'Terres Céréalières', 'terrain', 'Grandes parcelles pour céréales'),
(14, 'Serres', 'terrain', 'Structures de culture protégée'),
(15, 'Vignobles', 'terrain', 'Terrains viticoles'),
(16, 'Semoirs', 'equipement', 'Machines de semis'),
(17, 'Pulvérisateurs', 'equipement', 'Équipements de traitement'),
(18, 'Stockage', 'equipement', 'Solutions de stockage agricole'),
(19, 'Prairies', 'terrain', 'Terrains naturels d''élevage'),
(20, 'Palmeraies', 'terrain', 'Plantations de palmiers'),
(21, 'Agrumes', 'terrain', 'Vergers d''agrumes'),
(22, 'Amandiers/Pistachiers', 'terrain', 'Plantations de fruits secs'),
(23, 'test', 'equipement', 'test de catégorie'),
(24, 'Matériel Viticole', 'equipement', 'Équipements pour la viticulture');

-- Fournisseurs
INSERT INTO `fournisseurs` (`id`, `nom_entreprise`, `contact_nom`, `email`, `telephone`, `adresse`, `ville`, `actif`, `date_creation`) VALUES
(1, 'AgriTech Tunisie', 'Ahmed Sassi', 'contact@agritech.tn', '+216 71 234 567', 'fgfdgfdg', 'Tunis', 1, '2026-02-05 19:23:37'),
(2, 'EquipAgro SARL', 'Leila Mansouri', 'info@equipagro.tn', '+216 73 456 789', NULL, 'Sousse', 0, '2026-02-05 19:23:37'),
(3, 'FarmTools International', 'Karim Ben Salem', 'sales@farmtools.com', '+216 72 345 678', NULL, 'Sfax', 0, '2026-02-05 19:23:37'),
(4, 'IrrigaPro', 'Sonia Gharbi', 'contact@irrigapro.tn', '+216 74 567 890', NULL, 'Bizerte', 1, '2026-02-05 19:23:37'),
(6, 'retretre', 'retretrt', 'ezrre@ezre.tn', '+216 12 345 655', 'uiuyiuyi', 'uiuyi', 1, '2026-02-14 20:01:14'),
(9, 'MachinesAgri Plus', 'Youssef Mejri', 'contact@machinesagri.tn', '+216 71 456 123', 'Zone Industrielle Ben Arous', 'Ben Arous', 1, '2026-02-14 22:07:57'),
(10, 'SemaPlant SARL', 'Nadia Belhadj', 'info@semaplant.tn', '+216 72 789 456', 'Route de Sousse Km 5', 'Monastir', 1, '2026-02-14 22:07:57'),
(11, 'TractoPieces Tunisie', 'Riadh Bouazizi', 'ventes@tractopieces.tn', '+216 74 321 654', 'Avenue de la République', 'Sfax', 1, '2026-02-14 22:07:57'),
(12, 'AgroSolutions', 'Samia Khemiri', 'contact@agrosolutions.tn', '+216 78 852 963', 'Rue Ibn Khaldoun', 'Kairouan', 1, '2026-02-14 22:07:57'),
(13, 'Ferme&Jardin', 'Mohamed Chaabane', 'service@fermeetjardin.tn', '+216 71 147 258', 'Centre Urbain Nord', 'Tunis', 1, '2026-02-14 22:07:57'),
(14, 'IrrigaTech', 'Olfa Maalej', 'contact@irrigatech.tn', '+216 73 369 741', 'Zone Industrielle Agba', 'Sousse', 1, '2026-02-14 22:07:57'),
(15, 'MatérielAgro', 'Slim Hammami', 'info@materielagro.tn', '+216 75 258 147', 'Route de Gabès', 'Médenine', 1, '2026-02-14 22:07:57'),
(16, 'BioAgri Distribution', 'Ines Zouari', 'bio@bioagri.tn', '+216 76 963 852', 'Avenue Farhat Hached', 'Nabeul', 1, '2026-02-14 22:07:57'),
(17, 'TunisieEquip', 'Hatem Jlassi', 'contact@tunisieequip.tn', '+216 70 741 852', 'Rue de Palestine', 'Tunis', 1, '2026-02-14 22:07:57'),
(18, 'SudAgri Import', 'Mouna Masmoudi', 'import@sudagri.tn', '+216 75 159 357', 'Zone Commerciale', 'Gabès', 1, '2026-02-14 22:07:57'),
(19, 'NordFerme', 'Khaled Triki', 'info@nordferme.tn', '+216 72 753 159', 'Avenue Habib Thameur', 'Bizerte', 1, '2026-02-14 22:07:57'),
(20, 'AgriCap Tunisie', 'Sarra Bouzid', 'cap@agricap.tn', '+216 79 456 789', 'Cité Olympique', 'Tunis', 0, '2026-02-14 22:07:57'),
(21, 'MatAgri Express', 'Fathi Gharbi', 'express@matagri.tn', '+216 71 852 456', 'Route de la Marsa', 'La Marsa', 1, '2026-02-14 22:07:57'),
(22, 'ProFarm Équipements', 'Wafa Amri', 'pro@profarm.tn', '+216 73 147 963', 'Zone Industrielle', 'Mahdia', 1, '2026-02-14 22:07:57'),
(23, 'Delta Agri Services', 'Nizar Ayari', 'delta@deltaagri.tn', '+216 74 369 852', 'Avenue Majida Boulila', 'Sfax', 1, '2026-02-14 22:07:57');

-- Equipements
INSERT INTO `equipements` (`id`, `categorie_id`, `fournisseur_id`, `nom`, `description`, `prix_achat`, `prix_vente`, `quantite_stock`, `seuil_alerte`, `image_url`, `disponible`, `date_creation`) VALUES
(1, 7, 1, 'Pelle agricole premium', 'Pelle robuste en acier forgé pour travaux lourds', 30.00, 45.50, 25, 5, 'image/i4.png', 1, '2026-02-05 19:23:37'),
(2, 7, 1, 'Râteau professionnel 16 dents', 'Râteau en acier inoxydable avec manche en bois', 20.00, 32.00, 26, 8, 'image/i4.png', 1, '2026-02-05 19:23:37'),
(3, 7, 2, 'Sécateur professionnel', 'Sécateur ergonomique lame acier trempé', 15.00, 28.00, 40, 10, 'image/i4.png', 1, '2026-02-05 19:23:37'),
(4, 8, 4, 'Kit irrigation goutte-à-goutte 100m', 'Système complet avec programmateur', 180.00, 250.00, 100, 3, 'image/i4.png', 1, '2026-02-05 19:23:37'),
(5, 8, 4, 'Arroseur oscillant grande surface', 'Couvre jusqu''à 300m²', 45.00, 1000.00, 2, 5, 'image/i4.png', 1, '2026-02-05 19:23:37'),
(6, 9, 4, 'Charrue réversible 2 socs', 'Pour tracteur 60-80CV', 800.00, 1200.00, 55, 2, 'image/i4.png', 1, '2026-02-05 19:23:37'),
(7, 9, 3, 'Herse rotative 1.5m', 'Herse pour préparation du sol', 650.00, 950.00, 8, 2, 'image/i4.png', 0, '2026-02-05 19:23:37'),
(11, 7, 1, 'Semoir mécanique 12 rangs', 'Semoir précis pour céréales et légumineuses', 2500.00, 3500.00, 8, 2, 'image/i4.png', 1, '2026-02-14 22:08:14'),
(12, 8, 2, 'Pulvérisateur porté 600L', 'Pulvérisateur avec rampe 12m', 1800.00, 2600.00, 6, 2, 'image/i4.png', 1, '2026-02-14 22:08:14'),
(13, 7, 1, 'Brouette galvanisée 100L', 'Brouette robuste pour travaux lourds', 85.00, 145.00, 25, 5, 'image/i4.png', 1, '2026-02-14 22:08:14'),
(14, 8, 4, 'Tuyau irrigation 50m', 'Tuyau renforcé diamètre 25mm', 35.00, 55.00, 100, 20, 'image/i4.png', 1, '2026-02-14 22:08:14'),
(15, 9, 14, 'Cultivateur à dents 2.5m', 'Cultivateur pour travail superficiel', 950.00, 1400.00, 30, 2, 'image/i4.png', 1, '2026-02-14 22:08:14'),
(16, 7, 2, 'Filet anti-grêle 4x100m', 'Protection cultures sensibles', 180.00, 280.00, 30, 10, 'image/i4.png', 1, '2026-02-14 22:08:14'),
(17, 8, 1, 'Bac de stockage 500L', 'Cuve plastique alimentaire', 120.00, 195.00, 15, 5, 'image/i4.png', 1, '2026-02-14 22:08:14'),
(18, 9, 3, 'Épandeur centrifuge 300L', 'Distribution uniforme engrais', 450.00, 680.00, 9, 3, 'image/i4.png', 1, '2026-02-14 22:08:14'),
(19, 7, 4, 'Motobineuse 5CV', 'Motobineuse thermique professionnelle', 380.00, 550.00, 8, 3, 'image/i4.png', 1, '2026-02-14 22:08:14'),
(20, 8, 4, 'Pompe immergée 1.5CV', 'Pompe pour puits jusqu''à 30m', 220.00, 350.00, 100, 4, 'image/i4.png', 1, '2026-02-14 22:08:14'),
(21, 7, 2, 'Cueille-olives électrique', 'Peigne vibreur rechargeable', 290.00, 420.00, 20, 5, 'image/i4.png', 1, '2026-02-14 22:08:14'),
(22, 8, 1, 'Atomiseur dos 20L', 'Pulvérisateur motorisé portable', 350.00, 520.00, 13, 5, 'image/i4.png', 1, '2026-02-14 22:08:14'),
(23, 7, 13, 'Planteuse manuelle', 'Planteuse pour plants en mottes', 65.00, 95.00, 4, 10, 'image/i4.png', 1, '2026-02-14 22:08:14'),
(24, 9, 2, 'Disques de labour 24 pouces', 'Jeu de 4 disques pour charrue', 280.00, 420.00, 18, 5, 'image/i4.png', 1, '2026-02-14 22:08:14'),
(25, 8, 4, 'Silo souple 5000L', 'Stockage grains et aliments', 450.00, 680.00, 6, 2, 'image/i4.png', 1, '2026-02-14 22:08:14'),
(30, 23, 1, 'test 2', 'test gl 1', 11.00, 100.00, 196, 8, 'image/i4.png', 1, '2026-02-24 01:12:46');

-- Vehicules
INSERT INTO `vehicules` (`id`, `categorie_id`, `nom`, `description`, `marque`, `modele`, `immatriculation`, `prix_jour`, `prix_semaine`, `prix_mois`, `caution`, `image_url`, `disponible`, `date_creation`) VALUES
(1, 1, 'Tracteur John Deere 75CV', 'Tracteur compact avec cabine climatisée, idéal polyculture', 'John Deere', '5075E', 'TUN-1234', 150.00, 900.00, 3200.00, 500.00, 'image/i1.png', 1, '2026-02-05 19:23:37'),
(2, 1, 'Tracteur Massey Ferguson 90CV', 'Tracteur puissant pour grands travaux', 'Massey Ferguson', 'MF 5710', 'TUN-5678', 180.00, 1100.00, 3800.00, 600.00, 'image/i1.png', 1, '2026-02-05 19:23:37'),
(3, 2, 'Moissonneuse Case IH', 'Moissonneuse-batteuse 6m de coupe', 'Case IH', 'Axial-Flow 7140', 'TUN-9012', 300.00, 1800.00, 6500.00, 1000.00, 'image/i1.png', 1, '2026-02-05 19:23:37'),
(4, 3, 'Camion benne agricole', 'Camion 12 tonnes pour transport récoltes', 'Renault', 'K-380', '123TUN3456', 120.00, 700.00, 2500.00, 400.00, 'image/i1.png', 0, '2026-02-05 19:23:37'),
(7, 3, 'oooooooo', 'bbb', 'hjjhkgkg', '222222222', '123tun1234', 66666.00, 6666.00, 6666.00, 99999.00, 'image/i1.png', 1, '2026-02-14 20:47:48'),
(8, 1, 'Tracteur New Holland 65CV', 'Tracteur compact polyvalent avec chargeur frontal', 'New Holland', 'T4.65', '123 TUN 4567', 140.00, 850.00, 3000.00, 450.00, 'image/i1.png', 1, '2026-02-14 22:08:47'),
(9, 1, 'Tracteur Kubota 50CV', 'Mini-tracteur idéal maraîchage et vergers', 'Kubota', 'M5040', '234 TUN 5678', 110.00, 650.00, 2300.00, 350.00, 'image/i1.png', 1, '2026-02-14 22:08:47'),
(10, 2, 'Moissonneuse Claas', 'Moissonneuse-batteuse 5m coupe céréales', 'Claas', 'Tucano 320', '345 TUN 6789', 280.00, 1700.00, 6000.00, 900.00, 'image/i1.png', 1, '2026-02-14 22:08:47'),
(11, 3, 'Camion plateau Isuzu', 'Camion 8 tonnes plateau aluminium', 'Isuzu', 'NPR75', '456 TUN 7890', 100.00, 600.00, 2100.00, 350.00, 'image/i1.png', 1, '2026-02-14 22:08:47'),
(12, 3, 'Remorque basculante 8T', 'Remorque agricole basculante hydraulique', 'Rolland', 'RollSpeed', '567 TUN 8901', 45.00, 270.00, 950.00, 200.00, 'image/i1.png', 1, '2026-02-14 22:08:47'),
(13, 1, 'Quad Can-Am agricole', 'Quad 4x4 pour exploitation agricole', 'Can-Am', 'Outlander 570', '678 TUN 9012', 65.00, 380.00, 1350.00, 300.00, 'image/i1.png', 1, '2026-02-14 22:08:47'),
(14, 3, 'Fourgon Renault Master', 'Fourgon grand volume 15m³', 'Renault', 'Master L3H2', '789 TUN 0123', 85.00, 500.00, 1800.00, 300.00, 'image/i1.png', 1, '2026-02-14 22:08:47'),
(15, 1, 'Chargeur télescopique JCB', 'Télescopique 7m hauteur levage', 'JCB', '535-95', '890 TUN 1234', 200.00, 1200.00, 4200.00, 700.00, 'image/i1.png', 1, '2026-02-14 22:08:47'),
(16, 1, 'Tracteur Deutz-Fahr 100CV', 'Tracteur puissant avec prises hydrauliques', 'Deutz-Fahr', '5100', '901 TUN 2345', 190.00, 1150.00, 4000.00, 650.00, 'image/i1.png', 1, '2026-02-14 22:08:47'),
(17, 3, 'Bétaillère 12 bovins', 'Remorque transport animaux aluminium', 'Ifor Williams', 'TA510', '012 TUN 3456', 175.00, 450.00, 1600.00, 400.00, 'image/i1.png', 0, '2026-02-14 22:08:47'),
(18, 3, 'Camion citerne eau 10000L', 'Camion citerne pour irrigation mobile', 'MAN', 'TGS 18.360', '111 TUN 4444', 150.00, 900.00, 3200.00, 500.00, 'image/i1.png', 1, '2026-02-14 22:08:47'),
(19, 3, 'Remorque plateau 12T', 'Grande remorque plateau pour balles', 'Pronar', 'T026', '222 TUN 5555', 55.00, 320.00, 1100.00, 250.00, 'image/i1.png', 1, '2026-02-14 22:08:47'),
(20, 2, 'Ensileuse automotrice', 'Ensileuse pour maïs et sorgho', 'Krone', 'Big X 480', '333 TUN 6666', 350.00, 2100.00, 7500.00, 1200.00, 'image/i1.png', 1, '2026-02-14 22:08:47'),
(21, 1, 'Chargeuse compacte Bobcat', 'Mini chargeuse pour travaux divers', 'Bobcat', 'S450', '444 TUN 7777', 130.00, 780.00, 2800.00, 450.00, 'image/i1.png', 1, '2026-02-14 22:08:47'),
(22, 3, 'Pick-up Toyota Hilux', 'Pick-up 4x4 double cabine', 'Toyota', 'Hilux DC', '555 TUN 8888', 95.00, 560.00, 2000.00, 400.00, 'image/i1.png', 1, '2026-02-14 22:08:47'),
(24, 3, 'test1', 'fklqsfhdkqfhkh', 'WV', 'stest', '123 TUN 1234', 12.00, 14.00, 20.00, 100.00, 'image/i1.png', 1, '2026-02-18 10:52:06');

-- Terrains
INSERT INTO `terrains` (`id`, `categorie_id`, `titre`, `description`, `superficie_hectares`, `ville`, `adresse`, `prix_mois`, `prix_annee`, `caution`, `image_url`, `disponible`, `date_creation`) VALUES
(1, 4, 'Terre arable fertile Manouba', 'Terrain plat avec accès eau et électricité, sol argileux fertile', 5.50, 'Manouba', 'Route Borj El Amri Km 12', 700.00, 8000.00, 2000.00, 'image/i1.png', 1, '2026-02-05 19:23:37'),
(2, 5, 'Grand pâturage Zaghouan', 'Pâturage clôturé avec point d''eau naturel', 10.00, 'Zaghouan', 'Douar Henchir Toumia', 550.00, 6000.00, 1500.00, 'image/i1.png', 1, '2026-02-05 19:23:37'),
(3, 6, 'Verger d''oliviers Sfax', 'Verger de 300 oliviers centenaires en production', 8.00, 'Sfax', 'Route Mahres Km 8', 850.00, 10000.00, 2500.00, 'image/i1.png', 1, '2026-02-05 19:23:37'),
(4, 4, 'Terre maraîchère Nabeul', 'Proche mer, idéal cultures primeurs', 44.00, 'Nabeul', 'Zone Bou Argoub', 650.00, 7500.00, 1800.00, 'image/i1.png', 1, '2026-02-05 19:23:37'),
(6, 5, 'test 1', 'fghfgh', 14.00, 'dkfhskjfh', 'dgfkgsdkfjg', 123121.00, 21212.00, 0.00, 'image/i1.png', 1, '2026-02-14 19:57:46'),
(7, 4, 'Serre moderne Ariana', 'Serre équipée irrigation automatique, 2000m²', 0.20, 'Ariana', 'Route Raoued Km 3', 1200.00, 14000.00, 3500.00, 'image/i1.png', 1, '2026-02-14 22:08:29'),
(8, 5, 'Vignoble Grombalia', 'Vignoble AOC Mornag, cépage Muscat', 12.00, 'Grombalia', 'Domaine Ben Khélifa', 1500.00, 18000.00, 5000.00, 'image/i1.png', 1, '2026-02-14 22:08:29'),
(9, 6, 'Palmeraie Tozeur', 'Palmeraie 500 palmiers Deglet Nour', 15.00, 'Tozeur', 'Oasis Ibn Chabbat', 2000.00, 24000.00, 6000.00, 'image/i1.png', 1, '2026-02-14 22:08:29'),
(10, 4, 'Parcelle irriguée Jendouba', 'Terrain plat avec forage, sol fertile', 7.00, 'Jendouba', 'Plaine de Bulla Regia', 600.00, 7000.00, 1800.00, 'image/i1.png', 1, '2026-02-14 22:08:29'),
(11, 5, 'Prairie Béja', 'Pâturage naturel avec bergerie', 18.00, 'Béja', 'Route Nefza Km 15', 480.00, 5500.00, 1400.00, 'image/i1.png', 1, '2026-02-14 22:08:29'),
(12, 6, 'Orangeraie Cap Bon', 'Verger agrumes 400 arbres production', 6.00, 'Hammamet', 'Zone Bir Bouregba', 950.00, 11000.00, 2800.00, 'image/i1.png', 1, '2026-02-14 22:08:29'),
(13, 6, 'Oliveraie Sousse', 'Plantation 200 oliviers Chemlali', 5.00, 'Sousse', 'Route Enfidha', 700.00, 8000.00, 2000.00, 'image/i1.png', 1, '2026-02-14 22:08:29'),
(14, 4, 'Terrain maraîcher Bizerte', 'Sol sablonneux, idéal cultures précoces', 4.00, 'Bizerte', 'Zone Utique', 550.00, 6500.00, 1600.00, 'image/i1.png', 1, '2026-02-14 22:08:29'),
(15, 4, 'Complexe serres Sousse', 'Ensemble 4 serres chauffées totalisant 8000m²', 0.80, 'Sousse', 'Zone Agricole Msaken', 2500.00, 30000.00, 8000.00, 'image/i1.png', 1, '2026-02-14 22:08:29'),
(16, 6, 'Palmeraie Kébili', 'Palmeraie traditionnelle 300 palmiers', 10.00, 'Kébili', 'Oasis Douz', 1800.00, 20000.00, 5000.00, 'image/i1.png', 1, '2026-02-14 22:08:29'),
(17, 5, 'Ranch Siliana', 'Grand pâturage avec étable et hangar', 25.00, 'Siliana', 'Route Le Kef Km 8', 750.00, 8500.00, 2200.00, 'image/i1.png', 1, '2026-02-14 22:08:29'),
(18, 6, 'Citronneraie Nabeul', 'Verger citrons et clémentines 250 arbres', 4.50, 'Tunis', 'Ahmed Tlili, Délégation El Omrane Supérieur, Tunis, Gouvernorat Tunis, 1091, Tunisie', 800.00, 9000.00, 2300.00, 'image/i1.png', 1, '2026-02-14 22:08:29'),
(19, 4, 'Grande parcelle Kairouan', 'Terrain céréalier irrigable par pivot', 30.00, 'Kairouan', 'Plaine El Ala', 1100.00, 12000.00, 3000.00, 'image/i1.png', 1, '2026-02-14 22:08:29'),
(20, 5, 'Domaine viticole Mornag', 'Vignoble avec cave de vinification', 8.00, 'Ben Arous', 'Collines Mornag', 1400.00, 16000.00, 4000.00, 'image/i1.png', 1, '2026-02-14 22:08:29'),
(21, 6, 'Verger amandiers Kasserine', 'Plantation amandiers et pistachiers', 7.50, 'Kasserine', 'Route Sbeitla', 500.00, 5800.00, 1500.00, 'image/i1.png', 1, '2026-02-14 22:08:29');

-- Commandes (référencent utilisateur_id 2 et 3 du collègue → gardons id_utilisateur)
INSERT INTO `commandes` (`id`, `id_utilisateur`, `numero_commande`, `montant_total`, `statut_paiement`, `statut_livraison`, `adresse_livraison`, `ville_livraison`, `date_commande`, `date_livraison`, `notes`) VALUES
(1, 2, 'CMD-20260205-0001', 325.00, 'paye', 'livre', '15 Rue de Tunis', 'Tunis', '2026-02-05 19:23:37', '2026-02-08', NULL),
(2, 2, 'CMD-20260205-0002', 1450.00, 'en_attente', 'en_preparation', '20 Rue de Sousse', 'Sousse', '2026-02-05 19:23:37', NULL, NULL),
(3, 3, 'CMD-20260216-5429', 1400.00, 'en_attente', 'en_attente', 'bizerte 7000', 'bizerte', '2026-02-16 10:08:09', NULL, NULL),
(4, 2, 'CMD-20260218-3805', 128.00, 'paye', 'en_preparation', 'hhfjkhef', 'bgvfvvfv', '2026-02-18 10:47:56', NULL, NULL),
(5, 2, 'CMD-20260218-7490', 3500.00, 'en_attente', 'en_attente', 'hhfjkhef', 'bgvfvvfv', '2026-02-18 10:48:14', NULL, NULL),
(6, 2, 'CMD-20260218-3825', 18000.00, 'en_attente', 'en_attente', 'hhfjkhef', 'bgvfvvfv', '2026-02-18 10:49:42', NULL, NULL);

-- Details commandes
INSERT INTO `details_commandes` (`id`, `commande_id`, `equipement_id`, `quantite`, `prix_unitaire`, `sous_total`) VALUES
(1, 1, 1, 2, 45.50, 91.00),
(2, 1, 3, 3, 28.00, 84.00),
(3, 1, 5, 2, 75.00, 150.00),
(4, 2, 4, 1, 250.00, 250.00),
(5, 2, 6, 1, 1200.00, 1200.00),
(7, 3, 15, 1, 1400.00, 1400.00),
(8, 4, 2, 4, 32.00, 128.00),
(9, 5, 4, 14, 250.00, 3500.00),
(10, 6, 5, 18, 1000.00, 18000.00);

-- ============================================================================
-- ÉTAPE 6: Ajouter la table achats_fournisseurs (nouvelle du collègue)
-- ============================================================================

CREATE TABLE IF NOT EXISTS `achats_fournisseurs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fournisseur_id` int(11) NOT NULL,
  `equipement_id` int(11) NOT NULL,
  `quantite` int(11) NOT NULL,
  `prix_unitaire` decimal(10,2) NOT NULL,
  `montant_total` decimal(10,2) NOT NULL,
  `date_achat` timestamp NOT NULL DEFAULT current_timestamp(),
  `notes` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `equipement_id` (`equipement_id`),
  KEY `idx_fournisseur` (`fournisseur_id`),
  KEY `idx_date` (`date_achat`),
  CONSTRAINT `achats_fournisseurs_ibfk_1` FOREIGN KEY (`fournisseur_id`) REFERENCES `fournisseurs` (`id`),
  CONSTRAINT `achats_fournisseurs_ibfk_2` FOREIGN KEY (`equipement_id`) REFERENCES `equipements` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `achats_fournisseurs` (`id`, `fournisseur_id`, `equipement_id`, `quantite`, `prix_unitaire`, `montant_total`, `date_achat`, `notes`) VALUES
(1, 1, 1, 50, 30.00, 1500.00, '2026-02-05 19:23:37', 'Commande initiale pelles'),
(2, 4, 4, 30, 180.00, 5400.00, '2026-02-05 19:23:37', 'Stock irrigation'),
(3, 4, 6, 10, 800.00, 8000.00, '2026-02-10 19:23:37', 'Charrues pour la saison'),
(4, 1, 2, 40, 20.00, 800.00, '2026-02-15 19:23:37', 'Réapprovisionnement râteaux');

-- ============================================================================
-- ÉTAPE 7: Ajouter les triggers du collègue
-- ============================================================================

-- Triggers pour achats_fournisseurs (mise à jour stock automatique)
DELIMITER $$
CREATE TRIGGER IF NOT EXISTS `after_achat_insert` AFTER INSERT ON `achats_fournisseurs` FOR EACH ROW BEGIN
    UPDATE equipements SET quantite_stock = quantite_stock + NEW.quantite WHERE id = NEW.equipement_id;
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER IF NOT EXISTS `after_achat_delete` AFTER DELETE ON `achats_fournisseurs` FOR EACH ROW BEGIN
    UPDATE equipements SET quantite_stock = quantite_stock - OLD.quantite WHERE id = OLD.equipement_id;
END$$
DELIMITER ;

-- Triggers pour commandes
DELIMITER $$
CREATE TRIGGER IF NOT EXISTS `before_commande_insert` BEFORE INSERT ON `commandes` FOR EACH ROW BEGIN
    IF NEW.numero_commande IS NULL OR NEW.numero_commande = '' THEN
        SET NEW.numero_commande = CONCAT('CMD-', DATE_FORMAT(NOW(), '%Y%m%d'), '-', LPAD(FLOOR(RAND() * 9999), 4, '0'));
    END IF;
END$$
DELIMITER ;

-- Triggers pour details_commandes (calcul automatique sous_total)
DELIMITER $$
CREATE TRIGGER IF NOT EXISTS `before_detail_commande_insert` BEFORE INSERT ON `details_commandes` FOR EACH ROW BEGIN
    SET NEW.sous_total = NEW.quantite * NEW.prix_unitaire;
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER IF NOT EXISTS `before_detail_commande_update` BEFORE UPDATE ON `details_commandes` FOR EACH ROW BEGIN
    SET NEW.sous_total = NEW.quantite * NEW.prix_unitaire;
END$$
DELIMITER ;

-- ============================================================================
-- ÉTAPE 8: Mettre à jour les AUTO_INCREMENT
-- ============================================================================

ALTER TABLE `utilisateurs` AUTO_INCREMENT = 4;
ALTER TABLE `categories` AUTO_INCREMENT = 34;
ALTER TABLE `commandes` AUTO_INCREMENT = 17;
ALTER TABLE `details_commandes` AUTO_INCREMENT = 11;
ALTER TABLE `equipements` AUTO_INCREMENT = 31;
ALTER TABLE `fournisseurs` AUTO_INCREMENT = 29;
ALTER TABLE `terrains` AUTO_INCREMENT = 22;
ALTER TABLE `vehicules` AUTO_INCREMENT = 25;
ALTER TABLE `achats_fournisseurs` AUTO_INCREMENT = 6;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- MIGRATION TERMINÉE !
-- La table utilisateurs utilise maintenant:
--   - 'id' au lieu de 'id_utilisateur' (PK)
--   - 'type_user' au lieu de 'role' (enum: client/admin)
--   - 'date_creation' au lieu de 'date_inscription'
--   - Plus de colonne 'code_postal'
-- Toutes les données marketplace ont été importées.
-- ============================================================================
