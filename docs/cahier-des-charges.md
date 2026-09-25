# Cahier des charges fonctionnel

## Application web de reporting journalier

**Projet :** Daily Reporting  
**Version :** 1.0  
**Entreprise porteuse :** ITC Innovation  
**Périmètre :** application multi-entreprises

## 1. Objectif

Daily Reporting centralise les activités quotidiennes des employés. Chaque employé peut rédiger, sauvegarder et envoyer son reporting. Le Directeur / Manager peut consulter l'historique, rechercher un reporting et réagir à son contenu.

## 2. Utilisateurs

### Directeur / Manager

Le Directeur / Manager peut :

- créer les comptes employés ;
- consulter la liste des employés ;
- consulter les reportings envoyés ;
- ouvrir le détail d'un reporting ;
- rechercher un reporting par date ;
- réagir avec Like ou Dislike ;
- activer ou désactiver un compte employé.

### Employé

L'employé peut :

- se connecter ;
- accéder à son espace de reporting ;
- créer un reporting journalier ;
- enregistrer un brouillon ;
- modifier un brouillon ;
- confirmer et envoyer un reporting ;
- consulter son historique ;
- voir la réaction du Directeur / Manager.

## 3. Gestion des comptes

Le Directeur / Manager crée les comptes employés avec :

- prénom et nom ;
- email professionnel ;
- mot de passe ;
- téléphone et indicatif pays ;
- service ;
- rôle ;
- statut Actif ou Inactif.

Un compte inactif ne peut pas se connecter. Les reportings historiques restent conservés.

## 4. Reporting journalier

Un reporting contient :

- une date générée automatiquement ;
- un titre d'activité ;
- une description de l'activité réalisée ;
- un statut Brouillon ou Envoyé ;
- une réaction éventuelle Like ou Dislike.

Le bouton **Enregistrer en brouillon** conserve un reporting modifiable. Le bouton **Valider** demande une confirmation avant l'envoi définitif. Un reporting envoyé n'est plus modifiable.

## 5. Consultation et réactions

Le Directeur / Manager peut consulter les reportings envoyés, afficher leur détail et rechercher par date. Une réaction Like ou Dislike est conservée et visible dans l'espace de l'employé après actualisation.

Le Dislike est uniquement informatif : il ne supprime pas le reporting et ne bloque pas l'employé.

## 6. Permissions

| Fonction | Directeur / Manager | Employé |
| --- | :---: | :---: |
| Se connecter | Oui | Oui |
| Créer un compte employé | Oui | Non |
| Gérer les statuts employés | Oui | Non |
| Créer un reporting | Non | Oui |
| Modifier un brouillon | Non | Oui |
| Envoyer un reporting | Non | Oui |
| Consulter les reportings | Tous | Les siens |
| Rechercher par date | Oui | Historique personnel |
| Réagir Like / Dislike | Oui | Non |

## 7. Technologies

- Angular et TypeScript pour le frontend ;
- Java et Spring Boot pour le backend ;
- API REST ;
- Spring Security et JWT ;
- PostgreSQL ;
- Spring Data JPA / Hibernate ;
- Maven ;
- Git et GitHub.

## 8. Parcours principaux

### Employé

Connexion -> Reporting du jour -> Saisie -> Brouillon éventuel -> Validation -> Confirmation -> Envoi -> Historique et réaction.

### Directeur / Manager

Connexion -> Tableau de bord -> Employés ou Reportings -> Consultation -> Détail -> Réaction -> Recherche par date.

## 9. Critères d'acceptation

- Les utilisateurs sont séparés par rôle.
- Un employé inactif ne peut pas se connecter.
- Un brouillon peut être modifié avant envoi.
- Une confirmation est affichée avant l'envoi définitif.
- Les reportings envoyés sont conservés.
- Les réactions sont persistées et visibles par les deux côtés.
- Les données sensibles sont fournies par variables d'environnement et ne sont pas stockées dans le dépôt.
