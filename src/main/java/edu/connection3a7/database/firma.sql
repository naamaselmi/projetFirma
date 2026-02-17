-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : lun. 16 fév. 2026 à 19:40
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
-- Base de données : `firma`
--

-- --------------------------------------------------------

--
-- Structure de la table `avis`
--

CREATE TABLE `avis` (
  `id_avis` int(11) NOT NULL,
  `id_utilisateur` int(11) NOT NULL,
  `note` int(11) DEFAULT NULL CHECK (`note` between 1 and 5),
  `commentaire` text DEFAULT NULL,
  `date_avis` date DEFAULT NULL,
  `id_tech` int(11) DEFAULT NULL,
  `id_demande` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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

-- --------------------------------------------------------

--
-- Structure de la table `commandes`
--

CREATE TABLE `commandes` (
  `id` int(11) NOT NULL,
  `id_utilisateur` int(11) NOT NULL,
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

-- --------------------------------------------------------

--
-- Structure de la table `commentaire`
--

CREATE TABLE `commentaire` (
  `id` int(11) NOT NULL,
  `post_id` int(11) NOT NULL,
  `utilisateur_id` int(11) NOT NULL,
  `contenu` text NOT NULL,
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `demande`
--

CREATE TABLE `demande` (
  `id_demande` int(11) NOT NULL,
  `id_utilisateur` int(11) DEFAULT NULL,
  `type_probleme` varchar(150) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `date_demande` date DEFAULT NULL,
  `statut` varchar(50) DEFAULT NULL,
  `id_tech` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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

-- --------------------------------------------------------

--
-- Structure de la table `evenements`
--

CREATE TABLE `evenements` (
  `id_evenement` int(11) NOT NULL,
  `titre` varchar(200) NOT NULL,
  `description` text DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `type_evenement` enum('exposition','atelier','conference','salon','formation','autre') DEFAULT NULL,
  `date_debut` datetime NOT NULL,
  `date_fin` datetime DEFAULT NULL,
  `horaire_debut` time DEFAULT NULL,
  `horaire_fin` time DEFAULT NULL,
  `lieu` varchar(200) DEFAULT NULL,
  `adresse` text DEFAULT NULL,
  `capacite_max` int(11) DEFAULT NULL,
  `places_disponibles` int(11) DEFAULT NULL,
  `organisateur` varchar(100) DEFAULT NULL,
  `contact_email` varchar(100) DEFAULT NULL,
  `contact_tel` varchar(20) DEFAULT NULL,
  `statut` enum('actif','annule','termine','complet') DEFAULT 'actif',
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp(),
  `date_modification` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `evenements`
--

INSERT INTO `evenements` (`id_evenement`, `titre`, `description`, `image_url`, `type_evenement`, `date_debut`, `date_fin`, `horaire_debut`, `horaire_fin`, `lieu`, `adresse`, `capacite_max`, `places_disponibles`, `organisateur`, `contact_email`, `contact_tel`, `statut`, `date_creation`, `date_modification`) VALUES
(1, 'Conférence Innovation Digitale 2026', 'Grande conférence sur les dernières innovations en matière de transformation digitale et intelligence artificielle', 'C:/Users/selmi/OneDrive - ESPRIT/Bureau/event.jpg', 'conference', '2026-03-15 00:00:00', '2026-03-15 00:00:00', '09:00:00', '17:00:00', 'Palais des Congrès', '1 Place de la Porte Maillot, 75017 Paris', 500, 350, 'TechEvents SA', 'contact@techevents.fr', '+33142567890', 'actif', '2026-01-31 23:00:00', '2026-02-09 23:00:00'),
(2, 'Salon de l\'Agriculture Bio 2026', 'Salon dédié à l\'agriculture biologique et aux pratiques agricoles durables', 'C:/Users/selmi/OneDrive - ESPRIT/Bureau/event.jpg', 'salon', '2026-04-20 00:00:00', '2026-04-25 00:00:00', '10:00:00', '19:00:00', 'Parc des Expositions', 'Place de la Concorde, 75008 Paris', 8000, 6000, 'Fédération Agriculture Bio', 'contact@agribio-salon.fr', '+33144556677', 'actif', '2026-01-19 23:00:00', '2026-02-07 23:00:00'),
(3, 'Exposition d\'Art Contemporain \"Visions 2026\"', 'Exposition majeure d\'art contemporain avec 50 artistes internationaux', 'C:/Users/selmi/OneDrive - ESPRIT/Bureau/event.jpg', 'exposition', '2026-05-01 00:00:00', '2026-06-30 00:00:00', '11:00:00', '19:00:00', 'Musée d\'Art Moderne', '11 Avenue du Président Wilson, 75116 Paris', 200, 200, 'Musée d\'Art Moderne de Paris', 'exposition@mam-paris.fr', '+33147237261', 'actif', '2026-01-31 23:00:00', '2026-02-14 23:00:00'),
(4, 'Atelier Cuisine Méditerranéenne', 'Atelier pratique de cuisine méditerranéenne animé par un chef étoilé', 'C:/Users/selmi/OneDrive - ESPRIT/Bureau/event.jpg', 'atelier', '2026-03-22 00:00:00', '2026-03-22 00:00:00', '14:00:00', '18:00:00', 'École de Cuisine Le Cordon Bleu', '8 Rue Léon Delhomme, 75015 Paris', 25, 0, 'Le Cordon Bleu Paris', 'ateliers@cordonbleu.edu', '+33153687050', 'complet', '2026-02-04 23:00:00', '2026-02-09 23:00:00'),
(5, 'Formation DevOps & Cloud Computing', 'Formation intensive de 3 jours sur DevOps, Docker et Kubernetes', 'C:/Users/selmi/OneDrive - ESPRIT/Bureau/event.jpg', 'formation', '2026-04-14 00:00:00', '2026-04-16 00:00:00', '09:00:00', '17:00:00', 'Centre de Formation IT', '45 Rue de la Victoire, 75009 Paris', 30, 12, 'IT Training Academy', 'formations@it-academy.fr', '+33145678901', 'actif', '2026-02-04 23:00:00', '2026-02-11 23:00:00'),
(6, 'Conférence Leadership & Management', 'Conférence internationale sur les nouvelles approches du leadership', 'C:/Users/selmi/OneDrive - ESPRIT/Bureau/event.jpg', 'conference', '2026-03-25 00:00:00', '2026-03-26 00:00:00', '08:30:00', '17:30:00', 'Hôtel Mercure', '20 Rue Jean Jaurès, 92800 Puteaux', 150, 95, 'Business Leaders Institute', 'conferences@bli.fr', '+33147892345', 'actif', '2026-02-07 23:00:00', '2026-02-13 23:00:00'),
(7, 'Salon du Livre et de l\'Édition', 'Rencontres avec des auteurs, dédicaces et conférences littéraires', 'C:/Users/selmi/OneDrive - ESPRIT/Bureau/event.jpg', 'salon', '2026-06-05 00:00:00', '2026-06-08 00:00:00', '10:00:00', '19:00:00', 'Espace Culturel', '15 Boulevard Raspail, 75007 Paris', 5000, 3500, 'Association des Éditeurs', 'salon@livres-edition.fr', '+33144567890', 'actif', '2026-01-31 23:00:00', '2026-02-09 23:00:00'),
(8, 'Atelier Photographie de Portrait', 'Atelier pratique de photographie avec un photographe professionnel', 'C:/Users/selmi/OneDrive - ESPRIT/Bureau/event.jpg', 'atelier', '2026-04-18 00:00:00', '2026-04-10 00:00:00', '10:00:00', '17:00:00', 'Studio Photo Paris', '28 Rue de la République, 75011 Paris', 16, 16, 'Paris Photo Studio', NULL, NULL, 'actif', '2026-02-11 23:00:00', '2026-02-15 18:43:21'),
(9, 'Exposition Sculptures Modernes', 'Découverte des œuvres de sculpteurs contemporains européens', 'C:/Users/selmi/OneDrive - ESPRIT/Bureau/event.jpg', 'exposition', '2026-01-15 00:00:00', '2026-02-10 00:00:00', '10:00:00', '18:00:00', 'Galerie d\'Art Moderne', '42 Rue de Turenne, 75003 Paris', 150, 0, 'Galerie Moderne Paris', 'info@galerie-moderne.fr', '+33142345678', 'termine', '2025-12-07 23:00:00', '2026-02-10 23:00:00'),
(10, 'Formation Gestion de Projet Agile', 'Formation certifiante Scrum Master et méthodologies agiles', 'C:/Users/selmi/OneDrive - ESPRIT/Bureau/event.jpg', 'formation', '2026-05-20 00:00:00', '2026-05-22 00:00:00', '09:00:00', '18:00:00', 'Centre de Formation Agile', '10 Avenue des Champs-Élysées, 75008 Paris', 40, 22, 'Agile Training Center', 'formation@agile-center.fr', '+33145234567', 'actif', '2026-02-09 23:00:00', '2026-02-13 23:00:00'),
(11, 'Conférence Cybersécurité 2026', 'Conférence sur les menaces cyber et les stratégies de protection', 'C:/Users/selmi/OneDrive - ESPRIT/Bureau/event.jpg', 'conference', '2026-03-30 00:00:00', '2026-03-30 00:00:00', '09:00:00', '18:00:00', 'Centre de Conventions', '5 Place de la Défense, 92400 Courbevoie', 800, 550, 'CyberSec Institute', 'conference@cybersec-institute.com', '+33142345678', 'actif', '2026-02-07 23:00:00', '2026-02-11 23:00:00'),
(12, 'Salon des Startups et Innovation', 'Salon dédié aux startups innovantes et aux investisseurs', 'C:/Users/selmi/OneDrive - ESPRIT/Bureau/event.jpg', 'salon', '2026-05-12 00:00:00', '2026-05-14 00:00:00', '09:00:00', '19:00:00', 'Paris Expo Porte de Versailles', 'Place de la Porte de Versailles, 75015 Paris', 10000, 7500, 'Innovation Hub France', 'salon@innovation-hub.fr', '+33156789234', 'actif', '2026-02-04 23:00:00', '2026-02-15 18:39:57'),
(13, 'Atelier Développement Web React', 'Atelier pratique de développement d\'applications web avec React.js', 'C:/Users/selmi/OneDrive - ESPRIT/Bureau/event.jpg', 'atelier', '2026-04-25 00:00:00', '2026-04-25 00:00:00', '14:00:00', '19:00:00', 'Coding Academy', '18 Rue du Faubourg Saint-Antoine, 75012 Paris', 20, 12, 'Web Dev Academy', 'atelier@webdev-academy.fr', '+33145678234', 'actif', '2026-02-13 23:00:00', '2026-02-14 23:00:00'),
(14, 'Exposition Photographie \"Regards sur l\'Afrique\"', 'Exposition de photographies sur les cultures et paysages africains', 'C:/Users/selmi/OneDrive - ESPRIT/Bureau/event.jpg', 'exposition', '2026-06-10 00:00:00', '2026-08-10 00:00:00', '11:00:00', '19:00:00', 'Musée du Quai Branly', '37 Quai Branly, 75007 Paris', 300, 300, 'Musée du Quai Branly', 'expo@quaibranly.fr', '+33156617000', 'actif', '2026-01-31 23:00:00', '2026-02-09 23:00:00'),
(15, 'Formation Intelligence Artificielle', 'Formation complète sur le Machine Learning et Deep Learning', 'C:/Users/selmi/OneDrive - ESPRIT/Bureau/event.jpg', 'formation', '2026-06-01 00:00:00', '2026-06-05 00:00:00', '09:00:00', '17:00:00', 'AI Training Center', '25 Rue de Clichy, 75009 Paris', 35, 0, 'AI Institute Paris', 'formation@ai-institute.fr', '+33147892456', 'complet', '2026-02-11 23:00:00', '2026-02-14 23:00:00'),
(16, 'Conférence Blockchain et Finance', 'Conférence internationale sur la blockchain et les cryptomonnaies', 'C:/Users/selmi/OneDrive - ESPRIT/Bureau/event.jpg', 'conference', '2026-02-10 00:00:00', '2026-02-10 00:00:00', '08:30:00', '18:00:00', 'Station F', '5 Parvis Alan Turing, 75013 Paris', 600, 0, 'FinTech Innovation Hub', 'conference@fintechhub.io', '+33142567891', 'annule', '2026-01-09 23:00:00', '2026-02-04 23:00:00'),
(17, 'Salon de l\'Emploi Tech 2026', 'Salon de recrutement dédié aux métiers de la tech et du digital', 'C:/Users/selmi/OneDrive - ESPRIT/Bureau/event.jpg', 'salon', '2026-04-08 00:00:00', '2026-04-09 00:00:00', '09:00:00', '18:00:00', 'Grande Halle de la Villette', '211 Avenue Jean Jaurès, 75019 Paris', 5000, 3800, 'JobTech France', 'salon@jobtech.fr', '+33144567123', 'actif', '2026-02-07 23:00:00', '2026-02-12 23:00:00'),
(18, 'Atelier Design Thinking', 'Atelier interactif sur les méthodologies de Design Thinking', 'C:/Users/selmi/OneDrive - ESPRIT/Bureau/event.jpg', 'atelier', '2026-05-15 00:00:00', '2026-05-15 00:00:00', '09:30:00', '17:30:00', 'Innovation Lab', '32 Rue de Rivoli, 75004 Paris', 30, 16, 'Design Lab Paris', 'atelier@designlab.fr', '+33156234789', 'actif', '2026-02-10 23:00:00', '2026-02-16 13:01:15'),
(19, 'Exposition Peinture Impressionniste', 'Rétrospective des grands maîtres de l\'impressionnisme français', 'C:/Users/selmi/OneDrive - ESPRIT/Bureau/event.jpg', 'exposition', '2026-07-01 00:00:00', '2026-09-30 00:00:00', '10:00:00', '18:00:00', 'Musée d\'Orsay', '1 Rue de la Légion d\'Honneur, 75007 Paris', 500, 500, 'Musée d\'Orsay', 'reservation@musee-orsay.fr', '+33140494814', 'actif', '2026-02-04 23:00:00', '2026-02-11 23:00:00'),
(20, 'Formation Marketing Digital', 'Formation intensive sur le marketing digital et les réseaux sociaux', 'C:/Users/selmi/OneDrive - ESPRIT/Bureau/event.jpg', 'formation', '2026-04-28 00:00:00', '2026-04-30 00:00:00', '09:00:00', '18:00:00', 'Marketing Academy', '50 Rue de la Boétie, 75008 Paris', 45, 28, 'Digital Marketing Pro', 'formation@digitalmarketing.fr', '+33145789123', 'actif', '2026-02-12 23:00:00', '2026-02-14 23:00:00');

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

-- --------------------------------------------------------

--
-- Structure de la table `locations`
--

CREATE TABLE `locations` (
  `id` int(11) NOT NULL,
  `id_utilisateur` int(11) NOT NULL,
  `type_location` enum('vehicule','terrain') NOT NULL,
  `vehicule_id` int(11) DEFAULT NULL,
  `terrain_id` int(11) DEFAULT NULL,
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

-- --------------------------------------------------------

--
-- Structure de la table `participations`
--

CREATE TABLE `participations` (
  `id_participation` int(11) NOT NULL,
  `id_evenement` int(11) NOT NULL,
  `id_utilisateur` int(11) NOT NULL,
  `statut` enum('en_attente','confirme','annule') DEFAULT 'confirme',
  `date_inscription` timestamp NOT NULL DEFAULT current_timestamp(),
  `date_annulation` datetime DEFAULT NULL,
  `nombre_accompagnants` int(11) DEFAULT 0,
  `commentaire` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `participations`
--

INSERT INTO `participations` (`id_participation`, `id_evenement`, `id_utilisateur`, `statut`, `date_inscription`, `date_annulation`, `nombre_accompagnants`, `commentaire`) VALUES
(14, 18, 2, 'confirme', '2026-02-16 13:01:15', NULL, 1, 'je vais participer avec deux potes');

-- --------------------------------------------------------

--
-- Structure de la table `personne`
--

CREATE TABLE `personne` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `personne`
--

INSERT INTO `personne` (`id`, `nom`, `prenom`) VALUES
(1, 'naama', 'selmi'),
(2, 'naama', 'selmi'),
(3, 'hamzaa', 'slimani'),
(4, 'naaaa', 'hhbhjg'),
(5, 'hjbgkhhk', 'b n,bn,nb'),
(6, 'n,bn,b,', 'nbnjbnjnj'),
(7, 'hhhhhhhh', 'jhuhuh');

-- --------------------------------------------------------

--
-- Structure de la table `post`
--

CREATE TABLE `post` (
  `id` int(11) NOT NULL,
  `utilisateur_id` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `contenu` text NOT NULL,
  `categorie` varchar(100) DEFAULT NULL,
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp(),
  `statut` varchar(50) DEFAULT 'actif'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `profile`
--

CREATE TABLE `profile` (
  `id_profile` int(11) NOT NULL,
  `id_utilisateur` int(11) NOT NULL,
  `photo_profil` varchar(255) DEFAULT NULL,
  `bio` text DEFAULT NULL,
  `date_naissance` date DEFAULT NULL,
  `genre` enum('homme','femme','autre') DEFAULT NULL,
  `pays` varchar(100) DEFAULT NULL,
  `ville` varchar(100) DEFAULT NULL,
  `derniere_mise_a_jour` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `technicien`
--

CREATE TABLE `technicien` (
  `id_tech` int(11) NOT NULL,
  `id_utilisateur` int(11) DEFAULT NULL,
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL,
  `email` varchar(150) DEFAULT NULL,
  `specialite` varchar(100) DEFAULT NULL,
  `telephone` varchar(20) DEFAULT NULL,
  `disponibilite` tinyint(1) DEFAULT 1,
  `localisation` varchar(100) DEFAULT NULL,
  `image` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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

-- --------------------------------------------------------

--
-- Structure de la table `utilisateurs`
--

CREATE TABLE `utilisateurs` (
  `id_utilisateur` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `telephone` varchar(20) DEFAULT NULL,
  `adresse` text DEFAULT NULL,
  `ville` varchar(100) DEFAULT NULL,
  `code_postal` varchar(10) DEFAULT NULL,
  `role` enum('utilisateur','admin','technicien') DEFAULT 'utilisateur',
  `mot_de_passe` varchar(255) NOT NULL,
  `date_inscription` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `utilisateurs`
--

INSERT INTO `utilisateurs` (`id_utilisateur`, `nom`, `prenom`, `email`, `telephone`, `adresse`, `ville`, `code_postal`, `role`, `mot_de_passe`, `date_inscription`) VALUES
(1, 'naama', 'selmi', 'naama@firma.tn', '55555858', 'bizerte', 'nbhjbh', '7000', 'admin', '123', '2026-02-11 00:08:46'),
(2, 'hkjdhjkhf', 'hgsfhhdf', 'user@firma.tn', '52635879', 'hhfjkhef', 'bgvfvvfv', '7000', 'utilisateur', '123', '2026-02-15 12:27:30');

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
-- Index pour les tables déchargées
--

--
-- Index pour la table `avis`
--
ALTER TABLE `avis`
  ADD PRIMARY KEY (`id_avis`),
  ADD KEY `fk_avis_technicien` (`id_tech`),
  ADD KEY `fk_avis_demande` (`id_demande`),
  ADD KEY `idx_avis_utilisateur` (`id_utilisateur`),
  ADD KEY `idx_avis_technicien` (`id_tech`),
  ADD KEY `idx_avis_demande` (`id_demande`);

--
-- Index pour la table `categories`
--
ALTER TABLE `categories`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `commandes`
--
ALTER TABLE `commandes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `numero_commande` (`numero_commande`),
  ADD KEY `idx_commandes_utilisateur` (`id_utilisateur`);

--
-- Index pour la table `commentaire`
--
ALTER TABLE `commentaire`
  ADD PRIMARY KEY (`id`),
  ADD KEY `post_id` (`post_id`),
  ADD KEY `fk_commentaire_utilisateur` (`utilisateur_id`);

--
-- Index pour la table `demande`
--
ALTER TABLE `demande`
  ADD PRIMARY KEY (`id_demande`),
  ADD KEY `fk_demande_technicien` (`id_tech`),
  ADD KEY `fk_demande_utilisateur` (`id_utilisateur`);

--
-- Index pour la table `details_commandes`
--
ALTER TABLE `details_commandes`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_details_commande` (`commande_id`),
  ADD KEY `idx_details_equipement` (`equipement_id`);

--
-- Index pour la table `equipements`
--
ALTER TABLE `equipements`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_equipements_categorie` (`categorie_id`),
  ADD KEY `idx_equipements_fournisseur` (`fournisseur_id`);

--
-- Index pour la table `evenements`
--
ALTER TABLE `evenements`
  ADD PRIMARY KEY (`id_evenement`);

--
-- Index pour la table `fournisseurs`
--
ALTER TABLE `fournisseurs`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `locations`
--
ALTER TABLE `locations`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `numero_location` (`numero_location`),
  ADD KEY `idx_locations_utilisateur` (`id_utilisateur`),
  ADD KEY `idx_locations_vehicule` (`vehicule_id`),
  ADD KEY `idx_locations_terrain` (`terrain_id`);

--
-- Index pour la table `participations`
--
ALTER TABLE `participations`
  ADD PRIMARY KEY (`id_participation`),
  ADD UNIQUE KEY `unique_participation` (`id_evenement`,`id_utilisateur`),
  ADD KEY `id_utilisateur` (`id_utilisateur`);

--
-- Index pour la table `personne`
--
ALTER TABLE `personne`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `post`
--
ALTER TABLE `post`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_post_utilisateur` (`utilisateur_id`);

--
-- Index pour la table `profile`
--
ALTER TABLE `profile`
  ADD PRIMARY KEY (`id_profile`),
  ADD KEY `fk_profile_utilisateur` (`id_utilisateur`);

--
-- Index pour la table `technicien`
--
ALTER TABLE `technicien`
  ADD PRIMARY KEY (`id_tech`),
  ADD UNIQUE KEY `email` (`email`),
  ADD UNIQUE KEY `id_utilisateur` (`id_utilisateur`),
  ADD KEY `idx_technicien_utilisateur` (`id_utilisateur`);

--
-- Index pour la table `terrains`
--
ALTER TABLE `terrains`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_terrains_categorie` (`categorie_id`);

--
-- Index pour la table `utilisateurs`
--
ALTER TABLE `utilisateurs`
  ADD PRIMARY KEY (`id_utilisateur`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Index pour la table `vehicules`
--
ALTER TABLE `vehicules`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `immatriculation` (`immatriculation`),
  ADD KEY `idx_vehicules_categorie` (`categorie_id`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `avis`
--
ALTER TABLE `avis`
  MODIFY `id_avis` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `categories`
--
ALTER TABLE `categories`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `commandes`
--
ALTER TABLE `commandes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `commentaire`
--
ALTER TABLE `commentaire`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `demande`
--
ALTER TABLE `demande`
  MODIFY `id_demande` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `details_commandes`
--
ALTER TABLE `details_commandes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `equipements`
--
ALTER TABLE `equipements`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `evenements`
--
ALTER TABLE `evenements`
  MODIFY `id_evenement` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=27;

--
-- AUTO_INCREMENT pour la table `fournisseurs`
--
ALTER TABLE `fournisseurs`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `locations`
--
ALTER TABLE `locations`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `participations`
--
ALTER TABLE `participations`
  MODIFY `id_participation` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT pour la table `personne`
--
ALTER TABLE `personne`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT pour la table `post`
--
ALTER TABLE `post`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `profile`
--
ALTER TABLE `profile`
  MODIFY `id_profile` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `technicien`
--
ALTER TABLE `technicien`
  MODIFY `id_tech` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `terrains`
--
ALTER TABLE `terrains`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `utilisateurs`
--
ALTER TABLE `utilisateurs`
  MODIFY `id_utilisateur` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT pour la table `vehicules`
--
ALTER TABLE `vehicules`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `avis`
--
ALTER TABLE `avis`
  ADD CONSTRAINT `fk_avis_demande` FOREIGN KEY (`id_demande`) REFERENCES `demande` (`id_demande`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_avis_technicien` FOREIGN KEY (`id_tech`) REFERENCES `technicien` (`id_tech`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_avis_utilisateur` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Contraintes pour la table `commandes`
--
ALTER TABLE `commandes`
  ADD CONSTRAINT `fk_commandes_utilisateur` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Contraintes pour la table `commentaire`
--
ALTER TABLE `commentaire`
  ADD CONSTRAINT `commentaire_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`),
  ADD CONSTRAINT `fk_commentaire_utilisateur` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Contraintes pour la table `demande`
--
ALTER TABLE `demande`
  ADD CONSTRAINT `fk_demande_technicien` FOREIGN KEY (`id_tech`) REFERENCES `technicien` (`id_tech`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_demande_utilisateur` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Contraintes pour la table `details_commandes`
--
ALTER TABLE `details_commandes`
  ADD CONSTRAINT `fk_details_commandes_commande` FOREIGN KEY (`commande_id`) REFERENCES `commandes` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_details_commandes_equipement` FOREIGN KEY (`equipement_id`) REFERENCES `equipements` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Contraintes pour la table `equipements`
--
ALTER TABLE `equipements`
  ADD CONSTRAINT `fk_equipements_categorie` FOREIGN KEY (`categorie_id`) REFERENCES `categories` (`id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_equipements_fournisseur` FOREIGN KEY (`fournisseur_id`) REFERENCES `fournisseurs` (`id`) ON UPDATE CASCADE;

--
-- Contraintes pour la table `locations`
--
ALTER TABLE `locations`
  ADD CONSTRAINT `fk_locations_terrain` FOREIGN KEY (`terrain_id`) REFERENCES `terrains` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_locations_utilisateur` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_locations_vehicule` FOREIGN KEY (`vehicule_id`) REFERENCES `vehicules` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Contraintes pour la table `participations`
--
ALTER TABLE `participations`
  ADD CONSTRAINT `participations_ibfk_1` FOREIGN KEY (`id_evenement`) REFERENCES `evenements` (`id_evenement`) ON DELETE CASCADE,
  ADD CONSTRAINT `participations_ibfk_2` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE CASCADE;

--
-- Contraintes pour la table `post`
--
ALTER TABLE `post`
  ADD CONSTRAINT `fk_post_utilisateur` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Contraintes pour la table `profile`
--
ALTER TABLE `profile`
  ADD CONSTRAINT `fk_profile_utilisateur` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE CASCADE;

--
-- Contraintes pour la table `technicien`
--
ALTER TABLE `technicien`
  ADD CONSTRAINT `fk_technicien_utilisateur` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Contraintes pour la table `terrains`
--
ALTER TABLE `terrains`
  ADD CONSTRAINT `fk_terrains_categorie` FOREIGN KEY (`categorie_id`) REFERENCES `categories` (`id`) ON UPDATE CASCADE;

--
-- Contraintes pour la table `vehicules`
--
ALTER TABLE `vehicules`
  ADD CONSTRAINT `fk_vehicules_categorie` FOREIGN KEY (`categorie_id`) REFERENCES `categories` (`id`) ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
