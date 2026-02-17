-- ============================================================
-- Migration: Ajout de la table accompagnants
-- Date: 2026-02-16
-- Description: Table One-to-Many entre participations et accompagnants
-- ============================================================

-- Table des accompagnants (liée aux participations)
CREATE TABLE IF NOT EXISTS `accompagnants` (
  `id_accompagnant` int(11) NOT NULL AUTO_INCREMENT,
  `id_participation` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL,
  PRIMARY KEY (`id_accompagnant`),
  KEY `fk_accompagnant_participation` (`id_participation`),
  CONSTRAINT `fk_accompagnant_participation` FOREIGN KEY (`id_participation`)
    REFERENCES `participations` (`id_participation`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
