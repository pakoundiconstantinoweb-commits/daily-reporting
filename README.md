# Daily Reporting

Application web multi-entreprises de reporting journalier.

Daily Reporting permet aux employés de saisir leurs activités quotidiennes et aux Directeurs / Managers de consulter, rechercher et réagir aux reportings envoyés.

## Fonctionnalités

- Authentification par email et mot de passe avec JWT
- Création initiale du compte Directeur / Manager
- Création, activation et désactivation des comptes employés
- Création et modification des reportings en brouillon
- Confirmation avant l'envoi définitif
- Historique des reportings employés
- Recherche de son historique par date côté employé
- Consultation manager des reportings et de leur détail
- Recherche par date
- Filtrage des reportings par employé côté manager
- Réactions Like / Dislike
- Interface Angular responsive

## Stack technique

- Frontend : Angular, TypeScript, HTML, CSS
- Backend : Java, Spring Boot, Spring Security
- API : REST avec JWT
- Base de données : PostgreSQL
- Accès aux données : Spring Data JPA / Hibernate
- Dépendances frontend : npm
- Dépendances backend : Maven

## Prérequis

- Node.js et npm
- Java 25 ou une version compatible avec le projet
- PostgreSQL
- Une base PostgreSQL nommée `itc_innovation`

## Configuration backend

Définir les variables d'environnement avant de lancer Spring Boot :

```bash
set DB_PASSWORD=mot-de-passe-postgres
set JWT_SECRET=cle-secrete-d-au-moins-32-octets
```

Sous PowerShell :

```powershell
$env:DB_PASSWORD = "mot-de-passe-postgres"
$env:JWT_SECRET = "cle-secrete-d-au-moins-32-octets"
```

## Lancer le projet

Terminal frontend :

```bash
npm install
npm start
```

Frontend : `http://localhost:4200`

Terminal backend :

```bash
cd backend
./mvnw spring-boot:run
```

Sous Windows :

```cmd
cd backend
mvnw.cmd spring-boot:run
```

Le backend est disponible sur `http://localhost:8080`.

## Tests

Frontend :

```bash
npm test
```

Backend :

```bash
cd backend
./mvnw test
```

## Documentation

Le cahier des charges fonctionnel est disponible dans [docs/cahier-des-charges.md](docs/cahier-des-charges.md).

## Dépôt

Projet GitHub : https://github.com/pakoundiconstantinoweb-commits/daily-reporting
