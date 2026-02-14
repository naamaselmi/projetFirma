-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : sam. 07 fév. 2026 à 12:41
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.1.25

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `mp`
--

-- --------------------------------------------------------

--
-- Structure de la table `achats_fournisseurs`
--

CREATE TABLE `achats_fournisseurs` (
  `id` int(11) NOT NULL,
  `fournisseur_id` int(11) NOT NULL,
  `equipement_id` int(11) NOT NULL,
  `quantite` int(11) NOT NULL,
  `prix_unitaire` decimal(10,2) NOT NULL,
  `montant_total` decimal(10,2) NOT NULL,
  `date_achat` date NOT NULL,
  `numero_facture` varchar(100) DEFAULT NULL,
  `statut_paiement` enum('en_attente','paye','partiel') DEFAULT 'en_attente',
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `achats_fournisseurs`
--

INSERT INTO `achats_fournisseurs` (`id`, `fournisseur_id`, `equipement_id`, `quantite`, `prix_unitaire`, `montant_total`, `date_achat`, `numero_facture`, `statut_paiement`, `date_creation`) VALUES
(1, 1, 1, 50, 30.00, 1500.00, '2024-01-05', 'FACT-AGR-2024-001', 'paye', '2026-02-05 19:23:37'),
(2, 1, 2, 60, 20.00, 1200.00, '2024-01-05', 'FACT-AGR-2024-001', 'paye', '2026-02-05 19:23:37'),
(3, 4, 4, 30, 180.00, 5400.00, '2024-01-08', 'FACT-IRR-2024-003', 'paye', '2026-02-05 19:23:37'),
(4, 2, 6, 10, 800.00, 8000.00, '2024-01-10', 'FACT-EQP-2024-012', 'partiel', '2026-02-05 19:23:37');

--
-- Déclencheurs `achats_fournisseurs`
--
DELIMITER $$
CREATE TRIGGER `after_achat_fournisseur_insert` AFTER INSERT ON `achats_fournisseurs` FOR EACH ROW BEGIN
    UPDATE equipements 
    SET quantite_stock = quantite_stock + NEW.quantite
    WHERE id = NEW.equipement_id;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `before_achat_fournisseur_insert` BEFORE INSERT ON `achats_fournisseurs` FOR EACH ROW BEGIN
    SET NEW.montant_total = NEW.quantite * NEW.prix_unitaire;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `before_achat_fournisseur_update` BEFORE UPDATE ON `achats_fournisseurs` FOR EACH ROW BEGIN
    SET NEW.montant_total = NEW.quantite * NEW.prix_unitaire;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Structure de la table `categories`
--

CREATE TABLE `categories` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `type_produit` enum('equipement','vehicule','terrain') NOT NULL,
  `description` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `categories`
--

INSERT INTO `categories` (`id`, `nom`, `type_produit`, `description`) VALUES
(1, 'Tracteurs', 'vehicule', 'Véhicules de traction agricole'),
(2, 'Moissonneuses', 'vehicule', 'Machines de récolte'),
(3, 'Camions', 'vehicule', 'Véhicules de transport'),
(4, 'Terres arables', 'terrain', 'Terrains pour cultures'),
(5, 'Pâturages', 'terrain', 'Terrains pour élevage'),
(6, 'Vergers', 'terrain', 'Terrains avec arbres fruitiers'),
(7, 'Outils manuels', 'equipement', 'Outils de jardinage'),
(8, 'Irrigation', 'equipement', 'Systèmes d\'arrosage'),
(9, 'Labour', 'equipement', 'Équipements de labour');

-- --------------------------------------------------------

--
-- Structure de la table `commandes`
--

CREATE TABLE `commandes` (
  `id` int(11) NOT NULL,
  `utilisateur_id` int(11) NOT NULL,
  `numero_commande` varchar(50) NOT NULL,
  `montant_total` decimal(10,2) NOT NULL,
  `statut_paiement` enum('en_attente','paye','echoue') DEFAULT 'en_attente',
  `statut_livraison` enum('en_attente','en_preparation','expedie','livre','annule') DEFAULT 'en_attente',
  `adresse_livraison` text NOT NULL,
  `ville_livraison` varchar(100) DEFAULT NULL,
  `date_commande` timestamp NOT NULL DEFAULT current_timestamp(),
  `date_livraison` date DEFAULT NULL,
  `notes` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `commandes`
--

INSERT INTO `commandes` (`id`, `utilisateur_id`, `numero_commande`, `montant_total`, `statut_paiement`, `statut_livraison`, `adresse_livraison`, `ville_livraison`, `date_commande`, `date_livraison`, `notes`) VALUES
(1, 2, 'CMD-20240115-001', 327.50, 'paye', 'livre', 'Avenue Habib Bourguiba, Immeuble Ennour', 'Tunis', '2026-02-05 19:23:37', NULL, NULL),
(2, 3, 'CMD-20240118-002', 575.00, 'paye', 'en_preparation', 'Route de la Corniche, Résidence Yasmine', 'Sousse', '2026-02-05 19:23:37', NULL, NULL);

--
-- Déclencheurs `commandes`
--
DELIMITER $$
CREATE TRIGGER `after_commande_payee` AFTER UPDATE ON `commandes` FOR EACH ROW BEGIN
    IF NEW.statut_paiement = 'paye' AND OLD.statut_paiement != 'paye' THEN
        UPDATE equipements e
        INNER JOIN details_commandes dc ON e.id = dc.equipement_id
        SET e.quantite_stock = e.quantite_stock - dc.quantite
        WHERE dc.commande_id = NEW.id;
    END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `before_commande_insert` BEFORE INSERT ON `commandes` FOR EACH ROW BEGIN
    IF NEW.numero_commande IS NULL OR NEW.numero_commande = '' THEN
        SET NEW.numero_commande = CONCAT('CMD-', DATE_FORMAT(NOW(), '%Y%m%d'), '-', LPAD(FLOOR(RAND() * 9999), 4, '0'));
    END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Structure de la table `details_commandes`
--

CREATE TABLE `details_commandes` (
  `id` int(11) NOT NULL,
  `commande_id` int(11) NOT NULL,
  `equipement_id` int(11) NOT NULL,
  `quantite` int(11) NOT NULL,
  `prix_unitaire` decimal(10,2) NOT NULL,
  `sous_total` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `details_commandes`
--

INSERT INTO `details_commandes` (`id`, `commande_id`, `equipement_id`, `quantite`, `prix_unitaire`, `sous_total`) VALUES
(1, 1, 1, 2, 45.50, 91.00),
(2, 1, 3, 3, 28.00, 84.00),
(3, 1, 5, 2, 75.00, 150.00),
(4, 2, 4, 1, 250.00, 250.00),
(5, 2, 6, 1, 1200.00, 1200.00);

--
-- Déclencheurs `details_commandes`
--
DELIMITER $$
CREATE TRIGGER `before_detail_commande_insert` BEFORE INSERT ON `details_commandes` FOR EACH ROW BEGIN
    SET NEW.sous_total = NEW.quantite * NEW.prix_unitaire;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `before_detail_commande_update` BEFORE UPDATE ON `details_commandes` FOR EACH ROW BEGIN
    SET NEW.sous_total = NEW.quantite * NEW.prix_unitaire;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Structure de la table `equipements`
--

CREATE TABLE `equipements` (
  `id` int(11) NOT NULL,
  `categorie_id` int(11) NOT NULL,
  `fournisseur_id` int(11) NOT NULL,
  `nom` varchar(200) NOT NULL,
  `description` text DEFAULT NULL,
  `prix_achat` decimal(10,2) NOT NULL,
  `prix_vente` decimal(10,2) NOT NULL,
  `quantite_stock` int(11) DEFAULT 0,
  `seuil_alerte` int(11) DEFAULT 5,
  `image_url` varchar(255) DEFAULT NULL,
  `disponible` tinyint(1) DEFAULT 1,
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `equipements`
--

INSERT INTO `equipements` (`id`, `categorie_id`, `fournisseur_id`, `nom`, `description`, `prix_achat`, `prix_vente`, `quantite_stock`, `seuil_alerte`, `image_url`, `disponible`, `date_creation`) VALUES
(1, 7, 1, 'Pelle agricole premium', 'Pelle robuste en acier forgé pour travaux lourds', 30.00, 45.50, 25, 5, 'pelle.jpg', 1, '2026-02-05 19:23:37'),
(2, 7, 1, 'Râteau professionnel 16 dents', 'Râteau en acier inoxydable avec manche en bois', 20.00, 32.00, 30, 8, 'rateau.jpg', 1, '2026-02-05 19:23:37'),
(3, 7, 2, 'Sécateur professionnel', 'Sécateur ergonomique lame acier trempé', 15.00, 28.00, 40, 10, 'secateur.jpg', 1, '2026-02-05 19:23:37'),
(4, 8, 4, 'Kit irrigation goutte-à-goutte 100m', 'Système complet avec programmateur', 180.00, 250.00, 15, 3, 'irrigation.jpg', 1, '2026-02-05 19:23:37'),
(5, 8, 4, 'Arroseur oscillant grande surface', 'Couvre jusqu\'à 300m²', 45.00, 75.00, 20, 5, 'arroseur.jpg', 1, '2026-02-05 19:23:37'),
(6, 9, 2, 'Charrue réversible 2 socs', 'Pour tracteur 60-80CV', 800.00, 1200.00, 5, 2, 'charrue.jpg', 1, '2026-02-05 19:23:37'),
(7, 9, 3, 'Herse rotative 1.5m', 'Herse pour préparation du sol', 650.00, 950.00, 8, 2, 'herse.jpg', 1, '2026-02-05 19:23:37');

-- --------------------------------------------------------

--
-- Structure de la table `fournisseurs`
--

CREATE TABLE `fournisseurs` (
  `id` int(11) NOT NULL,
  `nom_entreprise` varchar(200) NOT NULL,
  `contact_nom` varchar(100) DEFAULT NULL,
  `email` varchar(150) DEFAULT NULL,
  `telephone` varchar(20) DEFAULT NULL,
  `adresse` text DEFAULT NULL,
  `ville` varchar(100) DEFAULT NULL,
  `actif` tinyint(1) DEFAULT 1,
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `fournisseurs`
--

INSERT INTO `fournisseurs` (`id`, `nom_entreprise`, `contact_nom`, `email`, `telephone`, `adresse`, `ville`, `actif`, `date_creation`) VALUES
(1, 'AgriTech Tunisie', 'Ahmed Sassi', 'contact@agritech.tn', '+216 71 234 567', NULL, 'Tunis', 1, '2026-02-05 19:23:37'),
(2, 'EquipAgro SARL', 'Leila Mansouri', 'info@equipagro.tn', '+216 73 456 789', NULL, 'Sousse', 1, '2026-02-05 19:23:37'),
(3, 'FarmTools International', 'Karim Ben Salem', 'sales@farmtools.com', '+216 72 345 678', NULL, 'Sfax', 1, '2026-02-05 19:23:37'),
(4, 'IrrigaPro', 'Sonia Gharbi', 'contact@irrigapro.tn', '+216 74 567 890', NULL, 'Bizerte', 1, '2026-02-05 19:23:37');

-- --------------------------------------------------------

--
-- Structure de la table `locations`
--

CREATE TABLE `locations` (
  `id` int(11) NOT NULL,
  `utilisateur_id` int(11) NOT NULL,
  `type_location` enum('vehicule','terrain') NOT NULL,
  `element_id` int(11) NOT NULL,
  `numero_location` varchar(50) NOT NULL,
  `date_debut` date NOT NULL,
  `date_fin` date NOT NULL,
  `duree_jours` int(11) DEFAULT NULL,
  `prix_total` decimal(10,2) NOT NULL,
  `caution` decimal(10,2) DEFAULT 0.00,
  `statut` enum('en_attente','confirmee','en_cours','terminee','annulee') DEFAULT 'en_attente',
  `date_reservation` timestamp NOT NULL DEFAULT current_timestamp(),
  `notes` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `locations`
--

INSERT INTO `locations` (`id`, `utilisateur_id`, `type_location`, `element_id`, `numero_location`, `date_debut`, `date_fin`, `duree_jours`, `prix_total`, `caution`, `statut`, `date_reservation`, `notes`) VALUES
(1, 2, 'vehicule', 1, 'LOC-20240120-001', '2024-02-01', '2024-02-08', 7, 900.00, 500.00, 'confirmee', '2026-02-05 19:23:37', NULL),
(2, 3, 'terrain', 1, 'LOC-20240121-002', '2024-03-01', '2025-02-28', 365, 8000.00, 2000.00, 'confirmee', '2026-02-05 19:23:37', NULL);

--
-- Déclencheurs `locations`
--
DELIMITER $$
CREATE TRIGGER `before_location_insert` BEFORE INSERT ON `locations` FOR EACH ROW BEGIN
    SET NEW.duree_jours = DATEDIFF(NEW.date_fin, NEW.date_debut) + 1;
    
    -- Générer numéro si vide
    IF NEW.numero_location IS NULL OR NEW.numero_location = '' THEN
        SET NEW.numero_location = CONCAT('LOC-', DATE_FORMAT(NOW(), '%Y%m%d'), '-', LPAD(FLOOR(RAND() * 9999), 4, '0'));
    END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `before_location_update` BEFORE UPDATE ON `locations` FOR EACH ROW BEGIN
    SET NEW.duree_jours = DATEDIFF(NEW.date_fin, NEW.date_debut) + 1;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Structure de la table `terrains`
--

CREATE TABLE `terrains` (
  `id` int(11) NOT NULL,
  `categorie_id` int(11) NOT NULL,
  `titre` varchar(200) NOT NULL,
  `description` text DEFAULT NULL,
  `superficie_hectares` decimal(10,2) NOT NULL,
  `ville` varchar(100) NOT NULL,
  `adresse` text DEFAULT NULL,
  `prix_mois` decimal(10,2) DEFAULT NULL,
  `prix_annee` decimal(10,2) NOT NULL,
  `caution` decimal(10,2) DEFAULT 0.00,
  `image_url` varchar(255) DEFAULT NULL,
  `disponible` tinyint(1) DEFAULT 1,
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `terrains`
--

INSERT INTO `terrains` (`id`, `categorie_id`, `titre`, `description`, `superficie_hectares`, `ville`, `adresse`, `prix_mois`, `prix_annee`, `caution`, `image_url`, `disponible`, `date_creation`) VALUES
(1, 4, 'Terre arable fertile Manouba', 'Terrain plat avec accès eau et électricité, sol argileux fertile', 5.50, 'Manouba', 'Route Borj El Amri Km 12', 700.00, 8000.00, 2000.00, 'terrain1.jpg', 1, '2026-02-05 19:23:37'),
(2, 5, 'Grand pâturage Zaghouan', 'Pâturage clôturé avec point d\'eau naturel', 10.00, 'Zaghouan', 'Douar Henchir Toumia', 550.00, 6000.00, 1500.00, 'terrain2.jpg', 1, '2026-02-05 19:23:37'),
(3, 6, 'Verger d\'oliviers Sfax', 'Verger de 300 oliviers centenaires en production', 8.00, 'Sfax', 'Route Mahres Km 8', 850.00, 10000.00, 2500.00, 'terrain3.jpg', 1, '2026-02-05 19:23:37'),
(4, 4, 'Terre maraîchère Nabeul', 'Proche mer, idéal cultures primeurs', 3.00, 'Nabeul', 'Zone Bou Argoub', 650.00, 7500.00, 1800.00, 'terrain4.jpg', 1, '2026-02-05 19:23:37');

-- --------------------------------------------------------

--
-- Structure de la table `utilisateurs`
--

CREATE TABLE `utilisateurs` (
  `id` int(11) NOT NULL,
  `type_user` enum('client','admin') NOT NULL DEFAULT 'client',
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `mot_de_passe` varchar(255) NOT NULL,
  `telephone` varchar(20) DEFAULT NULL,
  `adresse` text DEFAULT NULL,
  `ville` varchar(100) DEFAULT NULL,
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `utilisateurs`
--

INSERT INTO `utilisateurs` (`id`, `type_user`, `nom`, `prenom`, `email`, `mot_de_passe`, `telephone`, `adresse`, `ville`, `date_creation`) VALUES
(1, 'admin', 'Admin', 'System', 'admin@firma.tn', 'admin123', '+216 70 000 000', NULL, 'Tunis', '2026-02-05 19:23:37'),
(2, 'client', 'Ben Ali', 'Mohamed', 'mohamed@email.tn', 'client123', '+216 20 123 456', NULL, 'Tunis', '2026-02-05 19:23:37'),
(3, 'client', 'Trabelsi', 'Fatma', 'fatma@email.tn', 'client123', '+216 22 345 678', NULL, 'Sousse', '2026-02-05 19:23:37');

-- --------------------------------------------------------

--
-- Structure de la table `vehicules`
--

CREATE TABLE `vehicules` (
  `id` int(11) NOT NULL,
  `categorie_id` int(11) NOT NULL,
  `nom` varchar(200) NOT NULL,
  `description` text DEFAULT NULL,
  `marque` varchar(100) DEFAULT NULL,
  `modele` varchar(100) DEFAULT NULL,
  `immatriculation` varchar(50) DEFAULT NULL,
  `prix_jour` decimal(10,2) NOT NULL,
  `prix_semaine` decimal(10,2) DEFAULT NULL,
  `prix_mois` decimal(10,2) DEFAULT NULL,
  `caution` decimal(10,2) DEFAULT 0.00,
  `image_url` varchar(255) DEFAULT NULL,
  `disponible` tinyint(1) DEFAULT 1,
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `vehicules`
--

INSERT INTO `vehicules` (`id`, `categorie_id`, `nom`, `description`, `marque`, `modele`, `immatriculation`, `prix_jour`, `prix_semaine`, `prix_mois`, `caution`, `image_url`, `disponible`, `date_creation`) VALUES
(1, 1, 'Tracteur John Deere 75CV', 'Tracteur compact avec cabine climatisée, idéal polyculture', 'John Deere', '5075E', 'TUN-1234', 150.00, 900.00, 3200.00, 500.00, 'tracteur1.jpg', 1, '2026-02-05 19:23:37'),
(2, 1, 'Tracteur Massey Ferguson 90CV', 'Tracteur puissant pour grands travaux', 'Massey Ferguson', 'MF 5710', 'TUN-5678', 180.00, 1100.00, 3800.00, 600.00, 'tracteur2.jpg', 1, '2026-02-05 19:23:37'),
(3, 2, 'Moissonneuse Case IH', 'Moissonneuse-batteuse 6m de coupe', 'Case IH', 'Axial-Flow 7140', 'TUN-9012', 300.00, 1800.00, 6500.00, 1000.00, 'moissonneuse1.jpg', 1, '2026-02-05 19:23:37'),
(4, 3, 'Camion benne agricole', 'Camion 12 tonnes pour transport récoltes', 'Renault', 'K-380', 'TUN-3456', 120.00, 700.00, 2500.00, 400.00, 'camion1.jpg', 1, '2026-02-05 19:23:37');

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `achats_fournisseurs`
--
ALTER TABLE `achats_fournisseurs`
  ADD PRIMARY KEY (`id`),
  ADD KEY `equipement_id` (`equipement_id`),
  ADD KEY `idx_fournisseur` (`fournisseur_id`),
  ADD KEY `idx_date` (`date_achat`);

--
-- Index pour la table `categories`
--
ALTER TABLE `categories`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_type` (`type_produit`);

--
-- Index pour la table `commandes`
--
ALTER TABLE `commandes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `numero_commande` (`numero_commande`),
  ADD KEY `idx_numero` (`numero_commande`),
  ADD KEY `idx_utilisateur` (`utilisateur_id`),
  ADD KEY `idx_statut_livraison` (`statut_livraison`),
  ADD KEY `idx_statut_paiement` (`statut_paiement`);

--
-- Index pour la table `details_commandes`
--
ALTER TABLE `details_commandes`
  ADD PRIMARY KEY (`id`),
  ADD KEY `equipement_id` (`equipement_id`),
  ADD KEY `idx_commande` (`commande_id`);

--
-- Index pour la table `equipements`
--
ALTER TABLE `equipements`
  ADD PRIMARY KEY (`id`),
  ADD KEY `categorie_id` (`categorie_id`),
  ADD KEY `fournisseur_id` (`fournisseur_id`),
  ADD KEY `idx_stock` (`quantite_stock`),
  ADD KEY `idx_disponible` (`disponible`);

--
-- Index pour la table `fournisseurs`
--
ALTER TABLE `fournisseurs`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_nom` (`nom_entreprise`);

--
-- Index pour la table `locations`
--
ALTER TABLE `locations`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `numero_location` (`numero_location`),
  ADD KEY `utilisateur_id` (`utilisateur_id`),
  ADD KEY `idx_numero` (`numero_location`),
  ADD KEY `idx_dates` (`date_debut`,`date_fin`),
  ADD KEY `idx_statut` (`statut`);

--
-- Index pour la table `terrains`
--
ALTER TABLE `terrains`
  ADD PRIMARY KEY (`id`),
  ADD KEY `categorie_id` (`categorie_id`),
  ADD KEY `idx_ville` (`ville`),
  ADD KEY `idx_disponible` (`disponible`);

--
-- Index pour la table `utilisateurs`
--
ALTER TABLE `utilisateurs`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `idx_email` (`email`);

--
-- Index pour la table `vehicules`
--
ALTER TABLE `vehicules`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `immatriculation` (`immatriculation`),
  ADD KEY `categorie_id` (`categorie_id`),
  ADD KEY `idx_disponible` (`disponible`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `achats_fournisseurs`
--
ALTER TABLE `achats_fournisseurs`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT pour la table `categories`
--
ALTER TABLE `categories`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT pour la table `commandes`
--
ALTER TABLE `commandes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT pour la table `details_commandes`
--
ALTER TABLE `details_commandes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT pour la table `equipements`
--
ALTER TABLE `equipements`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT pour la table `fournisseurs`
--
ALTER TABLE `fournisseurs`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT pour la table `locations`
--
ALTER TABLE `locations`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT pour la table `terrains`
--
ALTER TABLE `terrains`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT pour la table `utilisateurs`
--
ALTER TABLE `utilisateurs`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT pour la table `vehicules`
--
ALTER TABLE `vehicules`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `achats_fournisseurs`
--
ALTER TABLE `achats_fournisseurs`
  ADD CONSTRAINT `achats_fournisseurs_ibfk_1` FOREIGN KEY (`fournisseur_id`) REFERENCES `fournisseurs` (`id`),
  ADD CONSTRAINT `achats_fournisseurs_ibfk_2` FOREIGN KEY (`equipement_id`) REFERENCES `equipements` (`id`);

--
-- Contraintes pour la table `commandes`
--
ALTER TABLE `commandes`
  ADD CONSTRAINT `commandes_ibfk_1` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateurs` (`id`);

--
-- Contraintes pour la table `details_commandes`
--
ALTER TABLE `details_commandes`
  ADD CONSTRAINT `details_commandes_ibfk_1` FOREIGN KEY (`commande_id`) REFERENCES `commandes` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `details_commandes_ibfk_2` FOREIGN KEY (`equipement_id`) REFERENCES `equipements` (`id`);

--
-- Contraintes pour la table `equipements`
--
ALTER TABLE `equipements`
  ADD CONSTRAINT `equipements_ibfk_1` FOREIGN KEY (`categorie_id`) REFERENCES `categories` (`id`),
  ADD CONSTRAINT `equipements_ibfk_2` FOREIGN KEY (`fournisseur_id`) REFERENCES `fournisseurs` (`id`);

--
-- Contraintes pour la table `locations`
--
ALTER TABLE `locations`
  ADD CONSTRAINT `locations_ibfk_1` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateurs` (`id`);

--
-- Contraintes pour la table `terrains`
--
ALTER TABLE `terrains`
  ADD CONSTRAINT `terrains_ibfk_1` FOREIGN KEY (`categorie_id`) REFERENCES `categories` (`id`);

--
-- Contraintes pour la table `vehicules`
--
ALTER TABLE `vehicules`
  ADD CONSTRAINT `vehicules_ibfk_1` FOREIGN KEY (`categorie_id`) REFERENCES `categories` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
