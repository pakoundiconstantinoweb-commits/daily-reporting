# Daily Reporting

Application web de reporting journalier pour ITC Innovation.

## Vue d’ensemble

Cette application permet :
- aux employés de saisir leurs activités quotidiennes ;
- d’enregistrer un reporting en brouillon avant l’envoi ;
- de confirmer l’envoi définitif ;
- au directeur / manager de consulter les reportings, filtrer par date et réagir avec Like / Dislike ;
- de gérer les comptes employés et leur activation / désactivation.

## Structure du dépôt

```text
itc-innovation/
├── README.md
├── .gitignore
├── itc-innovation-backend/
│   ├── pom.xml
│   ├── mvnw
│   ├── mvnw.cmd
│   └── src/
├── itc-innovation-frontend/
│   ├── package.json
│   ├── angular.json
│   └── src/
└── docs/
```

## Technologies

- Frontend : Angular + TypeScript
- Backend : Java 21 + Spring Boot
- Base de données : PostgreSQL
- Sécurité : Spring Security + JWT
- Accès aux données : Spring Data JPA / Hibernate

## Rôles

### Employé
- se connecter
- créer un reporting du jour
- enregistrer un brouillon
- modifier un brouillon
- valider et envoyer le reporting
- consulter son historique

### Directeur / Manager
- créer les comptes employés
- consulter les reportings
- filtrer par date
- ouvrir le détail d’un reporting
- réagir avec Like / Dislike
- activer / désactiver un compte

## Démarrage rapide

### 1. Backend

```bash
cd itc-innovation-backend
./mvnw spring-boot:run
```

### 2. Frontend

```bash
cd itc-innovation-frontend
npm install
npm start
```

### 3. Base de données

Créer une base PostgreSQL nommée `itc_innovation` et configurer les variables d’environnement nécessaires pour le backend.

## Variables d’environnement backend

Exemple sous Windows PowerShell :

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/itc_innovation"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:DB_PASSWORD = "mot-de-passe-postgres"
$env:JWT_SECRET = "cle-secrete-aleatoire-d-au-moins-32-octets"
```

## Déploiement GitHub

Le dépôt GitHub doit contenir le projet complet à la racine du workspace, avec les dossiers backend et frontend dans le même repository.

## Licence

Projet interne / de démonstration pour ITC Innovation.
