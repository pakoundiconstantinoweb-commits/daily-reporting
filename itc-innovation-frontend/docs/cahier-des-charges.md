# Cahier des charges fonctionnel

## Application Web de Reporting Journalier

| Information | Valeur |
| --- | --- |
| Entreprise | ITC Innovation |
| Projet | Application de reporting journalier |
| Version | 1.0 |
| Date | Septembre 2026 |
| Document | Cahier des charges fonctionnel |

## 1. Présentation du projet

ITC Innovation souhaite mettre en place une application web simple de reporting journalier permettant aux employés d'enregistrer chaque jour les activités qu'ils ont réalisées.

L'objectif est de permettre à chaque employé de renseigner facilement son travail quotidien, de conserver un historique des reportings et de permettre au Directeur / Manager de consulter les activités réalisées par les employés. L'application doit être simple, claire, rapide à utiliser et adaptée au fonctionnement quotidien de l'entreprise.

## 2. Objectifs du projet

L'application doit permettre de :

- centraliser les reportings journaliers des employés ;
- rédiger un reporting quotidien et l'enregistrer en brouillon ;
- modifier un brouillon avant son envoi ;
- demander une confirmation avant l'envoi définitif ;
- consulter les reportings et conserver leur historique ;
- rechercher un ancien reporting par date ;
- réagir à un reporting avec Like ou Dislike ;
- gérer l'accès des employés à leur compte.

## 3. Utilisateurs

### 3.1 Directeur / Manager

Le compte Directeur / Manager comporte un nom, un prénom, un email professionnel et un téléphone. Il peut créer les comptes employés, consulter leur liste, leurs reportings et le détail d'un reporting, rechercher par date, réagir avec Like ou Dislike et activer ou désactiver un compte.

### 3.2 Employé

L'employé peut se connecter, accéder à son espace de reporting, créer un reporting journalier, l'enregistrer en brouillon, modifier un brouillon, valider et envoyer son reporting, puis consulter ses anciens reportings.

## 4. Gestion des comptes employés

Le Directeur / Manager est responsable de la création des comptes employés. Les informations du compte sont :

- nom et prénom ;
- email professionnel ;
- mot de passe ;
- téléphone ;
- service ;
- rôle ;
- statut du compte : Actif ou Inactif.

Les anciens reportings restent conservés même après la désactivation d'un compte.

## 5. Connexion à l'application

L'utilisateur renseigne son email professionnel et son mot de passe. Après une connexion réussie, l'employé est dirigé vers son espace de reporting et le Directeur / Manager vers son espace de gestion.

## 6. Gestion des comptes actifs et inactifs

Le Directeur / Manager peut désactiver le compte d'un employé. Un compte inactif ne peut plus accéder à l'application. Le message affiché est :

> **Compte inactif**  
> Votre compte est actuellement inactif.  
> Veuillez contacter le Directeur / Manager.

Le Directeur / Manager peut également réactiver le compte. Les anciens reportings de l'employé demeurent disponibles.

## 7. Création d'un reporting journalier

Après connexion, l'employé arrive directement sur la page de reporting. La date du jour est automatiquement renseignée par le système. Le formulaire comprend :

- **Date** : date du jour générée automatiquement ;
- **Titre** : titre de l'activité, par exemple « Intervention à VSF » ;
- **Description** : travail effectué durant la journée.

Exemple de description : « Aujourd'hui, je suis allé à VSF pour effectuer un entretien. J'ai également vérifié un ordinateur qui avait un problème et installé un antivirus. »

## 8. Enregistrement en brouillon

Le bouton **Enregistrer en brouillon** sauvegarde le reporting sans l'envoyer. Le reporting reste modifiable ; l'employé peut le compléter ou le modifier avant l'envoi définitif.

## 9. Validation et envoi

Lorsque l'employé clique sur **Valider**, l'application affiche une confirmation :

> **Confirmer l'envoi ?**  
> Voulez-vous confirmer l'envoi de ce reporting ?

- **Annuler** : le reporting reste en brouillon et peut être modifié ;
- **Confirmer** : le reporting est envoyé définitivement et devient consultable par le Directeur / Manager.

## 10. Consultation des employés

Le Directeur / Manager accède à la liste des employés et peut sélectionner une personne pour consulter ses reportings.

| Employé | Dernier reporting |
| --- | --- |
| Koffi | Intervention à VSF |
| Ama | Installation antivirus |
| Jean | Entretien ordinateur |

## 11. Consultation des reportings

Les reportings d'un employé sont présentés avec leur date et leur titre. Le Directeur / Manager peut sélectionner un reporting pour afficher son contenu complet.

| Employé | Date | Titre |
| --- | --- | --- |
| Koffi | 23 septembre 2026 | Intervention à VSF |
| Koffi | 22 septembre 2026 | Installation antivirus |
| Koffi | 21 septembre 2026 | Entretien ordinateur |

## 12. Détail d'un reporting

La page de détail affiche la date, le titre, la description et les réactions. Le Directeur / Manager peut réagir au reporting depuis cette page.

## 13. Réactions sur les reportings

Le Directeur / Manager peut réagir avec **Like** ou **Dislike**. Le Dislike ne supprime pas le reporting et ne bloque pas l'employé ; il indique seulement qu'un point peut nécessiter une discussion ou une vérification.

## 14. Recherche par date

Une recherche simple par date permet de retrouver les reportings correspondants à la journée demandée, par exemple le 5 janvier 2025.

## 15. Historique des reportings

Les reportings envoyés sont conservés. L'employé consulte son historique personnel ; le Directeur / Manager consulte les reportings des employés. La désactivation d'un compte ne supprime aucun reporting historique.

## 16. Affichage de la date

La date du jour apparaît automatiquement, notamment dans l'espace de création de reporting de l'employé. Elle est associée à la date réelle du reporting.

## 17. Parcours principaux

### Employé

Connexion -> Espace de reporting -> Saisie du titre et de la description -> Brouillon éventuel -> Validation -> Confirmation -> Envoi -> Historique.

### Directeur / Manager

Connexion -> Gestion des employés et des reportings -> Consultation d'un employé -> Détail d'un reporting -> Réaction -> Recherche par date.

## 18. Structure générale des écrans

### 18.1 Page de connexion

- ITC Innovation ;
- email professionnel ;
- mot de passe ;
- bouton **Se connecter**.

### 18.2 Espace employé

- date du jour ;
- formulaire **Mon reporting du jour** avec titre et description ;
- boutons **Enregistrer en brouillon** et **Valider** ;
- accès à l'historique personnel.

### 18.3 Espace Directeur / Manager

- date du jour ;
- navigation vers les reportings et les employés ;
- recherche par date ;
- liste des reportings et réactions Like / Dislike.

## 19. Rôles et permissions

| Fonction | Directeur / Manager | Employé |
| --- | :---: | :---: |
| Se connecter | Oui | Oui |
| Créer un compte employé | Oui | Non |
| Consulter les employés | Oui | Non |
| Créer un reporting | Non | Oui |
| Modifier un brouillon | Non | Oui |
| Envoyer un reporting | Non | Oui |
| Consulter les reportings | Tous | Les siens |
| Rechercher un ancien reporting | Tous | Les siens |
| Voir le détail d'un reporting | Tous | Les siens |
| Réagir Like / Dislike | Oui | Non |
| Activer / désactiver un compte | Oui | Non |

## 20. Technologies et architecture

| Élément | Technologie |
| --- | --- |
| Frontend | Angular, TypeScript, HTML, CSS |
| Backend | Java, Spring Boot |
| API | REST |
| Base de données | PostgreSQL |
| Accès aux données | Spring Data JPA / Hibernate |
| Sécurité | Spring Security, JWT |
| Gestion des dépendances | npm, Maven |
| Gestion du code source | Git, GitHub |

```text
Utilisateur
	-> Angular / TypeScript / HTML / CSS
	-> API REST
	-> Java / Spring Boot / Spring Security
	-> Spring Data JPA / Hibernate
	-> PostgreSQL
```

## 21. Résultat attendu

ITC Innovation doit disposer d'une application web simple et fonctionnelle qui centralise et conserve les activités journalières des employés, tout en facilitant leur consultation par le Directeur / Manager.

- **Employé** : Connexion -> Reporting du jour -> Brouillon -> Validation -> Confirmation -> Envoi.
- **Directeur / Manager** : Connexion -> Employés -> Reportings -> Détail -> Réaction -> Recherche par date.
