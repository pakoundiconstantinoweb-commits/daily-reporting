# Rapports quotidiens

Application web de reporting journalier pour ITC Innovation. Les employés rédigent et envoient leurs activités quotidiennes ; le Directeur / Manager consulte les reportings, gère les comptes et suit les réactions.

## Structure du dépôt

```text
daily-reporting/
├── itc-innovation-backend/    # API Java / Spring Boot
├── itc-innovation-frontend/   # Application Angular
├── README.md
└── .gitignore
```

Le frontend Angular et le backend Spring Boot sont regroupés dans le même dépôt GitHub.

## Fonctionnalités

- Connexion sécurisée par email, mot de passe et JWT
- Création des comptes Directeur / Manager uniquement sur invitation du super-administrateur
- Compte super-administrateur provisionné côté backend, sans inscription publique
- Création, activation et désactivation des comptes employés
- Reportings journaliers avec brouillons modifiables et confirmation avant envoi
- Historique personnel pour les employés
- Consultation, filtrage par employé et recherche par date pour le manager
- Détail des reportings et réactions J'aime / Je n'aime pas
- Interface responsive avec espaces séparés par rôle

## Technologies

| Composant | Technologies |
| --- | --- |
| Frontend | Angular, TypeScript, HTML, CSS |
| Backend | Java 21, Spring Boot, Spring Security |
| API | REST, JWT |
| Base de données | PostgreSQL, Spring Data JPA, Hibernate |
| Outils | npm, Maven, Git |

## Prérequis

- Node.js et npm
- JDK 21
- PostgreSQL avec une base `itc_innovation`

## Configuration

Le backend lit les paramètres sensibles depuis l'environnement. Ne mets jamais de vrais mots de passe ou clés JWT dans GitHub.

Dans l'invite de commandes Windows, configure les variables avant de démarrer le backend :

```bat
set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/itc_innovation
set SPRING_DATASOURCE_USERNAME=postgres
set DB_PASSWORD=mot-de-passe-postgres
set JWT_SECRET=cle-secrete-aleatoire-d-au-moins-32-octets
set SUPER_ADMIN_EMAIL=adresse-du-super-admin
set SUPER_ADMIN_PASSWORD=secret-fort-a-generer
set SUPER_ADMIN_FIRST_NAME=Super
set SUPER_ADMIN_LAST_NAME=Administrateur
```

Sous PowerShell :

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/itc_innovation"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:DB_PASSWORD = "mot-de-passe-postgres"
$env:JWT_SECRET = "cle-secrete-aleatoire-d-au-moins-32-octets"
$env:SUPER_ADMIN_EMAIL = "adresse-du-super-admin"
$env:SUPER_ADMIN_PASSWORD = "secret-fort-a-generer"
$env:SUPER_ADMIN_FIRST_NAME = "Super"
$env:SUPER_ADMIN_LAST_NAME = "Administrateur"
```

Le compte super-administrateur est provisionné côté backend avec les variables `SUPER_ADMIN_EMAIL` et `SUPER_ADMIN_PASSWORD`. Les variables `SUPER_ADMIN_FIRST_NAME` et `SUPER_ADMIN_LAST_NAME` sont facultatives. En production, configure ces variables dans les paramètres privés de Render ; ne place jamais le mot de passe dans le dépôt. Utilise un nouveau mot de passe fort, différent de tout mot de passe déjà partagé.

Une fois connecté, le super-administrateur dispose aussi de son propre espace de Manager (équipe et reportings) et de la section « Invitations ». Chaque invitation crée un lien aléatoire à usage unique, valable 24 heures ; le lien peut être révoqué avant son utilisation. La route de création de compte est protégée par la validation de cette invitation.

## Démarrage local

Dans un premier terminal, démarre l'API :

```bat
cd itc-innovation-backend
powershell -ExecutionPolicy Bypass -File .\start-local.ps1
```

Le script demande le mot de passe PostgreSQL local et un nouveau mot de passe super-administrateur (sans les afficher ni les enregistrer dans le dépôt). L'adresse locale par défaut est `pakoundiconstantinoweb@gmail.com` ; elle peut être remplacée en définissant `SUPER_ADMIN_EMAIL` avant le lancement. L'API est disponible sur `http://localhost:8080`.

Dans un deuxième terminal, depuis le dossier frontend :

```bat
cd itc-innovation-frontend
npm install
npm start
```

Le frontend est disponible sur `http://localhost:4200`. La configuration locale du proxy relaie les requêtes `/api` vers le backend.

## Tests

Frontend Angular :

```bat
cd itc-innovation-frontend
npm test -- --watch=false
```

Backend Spring Boot (PostgreSQL et les variables d'environnement doivent être configurés) :

```bat
cd itc-innovation-backend
mvnw.cmd test
```

## Documentation fonctionnelle

Le cahier des charges complet est disponible dans [itc-innovation-frontend/docs/cahier-des-charges.md](itc-innovation-frontend/docs/cahier-des-charges.md).

## Dépôt GitHub

[pakoundiconstantinoweb-commits/daily-reporting](https://github.com/pakoundiconstantinoweb-commits/daily-reporting/tree/main)
