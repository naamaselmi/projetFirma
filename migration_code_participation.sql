-- Migration : Ajouter la colonne code_participation à la table participations
ALTER TABLE participations
    ADD COLUMN code_participation VARCHAR(20) NULL AFTER commentaire;

-- Générer des codes pour les participations existantes
UPDATE participations
SET code_participation = CONCAT('PART-', LPAD(id_participation, 5, '0'))
WHERE code_participation IS NULL;
