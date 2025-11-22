# 🛡️ CyberScale - Plateforme de Positionnement en Cybersécurité

[![CI Pipeline](https://github.com/LyesSEHILA/DataScale/actions/workflows/ci.yml/badge.svg)](https://github.com/LyesSEHILA/DataScale/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=LyesSEHILA_DataScale&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=LyesSEHILA_DataScale)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

**CyberScale** est une application web permettant d'évaluer ses compétences en cybersécurité sur deux axes : **Théorique** et **Technique**.

## 🚀 Fonctionnalités (v0.1)

- **F1 - Onboarding :** Formulaire d'auto-évaluation initial.
- **F2 - Quiz Adaptatif :** Moteur de questions intelligent qui s'adapte au niveau déclaré.
- **F3 - Visualisation :** Restitution des résultats sous forme de nuage de points (Scatter Plot).
- **F4 - Recommandations :** Suggestions de ressources (Livres, Certifications) basées sur le score.

## 🛠️ Stack Technique

* **Backend :** Java 21, Spring Boot 3, Gradle, H2 Database (Dev).
* **Frontend :** HTML5, CSS3, JavaScript (Vanilla), Chart.js.
* **DevOps :** GitHub Actions (CI), SonarCloud (Qualité).

## 📦 Installation et Lancement

### Prérequis
* Java 21 (ou laisser Gradle l'installer)
* Navigateur Web récent

### 1. Lancer le Backend (API)
```bash
cd backend
./gradlew bootRun 
```

* L'API sera disponible sur : (http://localhost:8080) Console H2 (BDD) : (http://localhost:8080/h2-console)

### 2. Lancer le Frontend (UI)

- Il n'y a pas d'installation npm nécessaire.

    * Ouvrez le dossier frontend dans VS Code.

    * Utilisez l'extension "Live Server" pour ouvrir index.html. (Ou lancez un serveur python : ```python python3 -m http.server``` dans le dossier frontend).

## 👥 L'Équipe

    * **Lyes SEHILA :** Lead DevOps & Architecte

    * **Hassan Jatta :** Lead Backend

    * **Abdoulaye :** Lead Frontend
