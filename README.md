# 🛡️ CyberScale - Plateforme de Training Cybersécurité

[![CI Build](https://github.com/LyesSEHILA/DataScale/actions/workflows/ci.yml/badge.svg)](https://github.com/LyesSEHILA/DataScale/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=LyesSEHILA_DataScale&metric=alert_status)](https://sonarcloud.io/dashboard?id=LyesSEHILA_DataScale)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=LyesSEHILA_DataScale&metric=coverage)](https://sonarcloud.io/dashboard?id=LyesSEHILA_DataScale)
[![Tests](https://img.shields.io/badge/Tests-Passing-success)](https://github.com/LyesSEHILA/DataScale/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Release](https://img.shields.io/github/v/release/LyesSEHILA/DataScale)](https://github.com/LyesSEHILA/DataScale/releases)

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=flat&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=flat&logo=spring&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=flat&logo=Hibernate&logoColor=white)
![HTML5](https://img.shields.io/badge/html5-%23E34F26.svg?style=flat&logo=html5&logoColor=white)
![TailwindCSS](https://img.shields.io/badge/tailwindcss-%2338B2AC.svg?style=flat&logo=tailwind-css&logoColor=white)
![Xterm.js](https://img.shields.io/badge/Xterm.js-Terminal-black)

---

## 📖 À propos

**CyberScale** est une plateforme éducative innovante permettant d'évaluer et d'améliorer ses compétences en cybersécurité. Contrairement aux plateformes classiques, elle positionne l'utilisateur sur deux axes :
1.  **Théorie :** Connaissances normatives (ISO 27001, RGPD) et concepts.
2.  **Technique :** Maîtrise des outils (Nmap, Wireshark) et pratique.

🚀 **Version actuelle : v1.0.1 (Release "Stabilization & Infra")**

---

## ✨ Fonctionnalités Clés

### 🎮 Cyber Arena (Nouveau !)
Un **terminal Linux réel** orchestré par Docker directement dans le navigateur.
* **Isolation Docker :** Chaque utilisateur dispose de son propre conteneur temporaire.
* **Flags Dynamiques :** Système de protection contre la triche avec des flags générés à la volée.
* **Mode CTF (Capture The Flag) :** Trouvez les flags cachés et validez-les via la commande `submit <flag>`.

### 🏗️ Infrastructure Asynchrone
* **RabbitMQ :** Traitement des événements de jeu et orchestration infra en arrière-plan.
* **Kubernetes Ready :** Service de déploiement de "leurres" (honeypots) intégré.

### 🎓 Mode Certification & Phishing
* **Simulateur d'Examen :** CEH, CompTIA Security+, CISSP avec algorithme prédictif.
* **Module Phishing :** Analyse et simulation de campagnes d'emails malveillants.

### 📊 Dashboard & Gamification
* Suivi du score en temps réel.
* Badges de difficulté (Facile, Moyen, Hardcore).
* Historique détaillé des tentatives.

---

## 🛠️ Stack Technique

* **Backend :** Java 21, Spring Boot 3.4, Spring Security, JPA/Hibernate.
* **Base de Données :** H2 (Développement / Mémoire), PostgreSQL (Production).
* **Frontend :** HTML5, JavaScript (ES6+), Tailwind CSS, Xterm.js.
* **Tests :** JUnit 5, Mockito, MockMvc, Selenium (E2E), Cucumber (BDD).
* **DevOps :** Gradle, GitHub Actions, Docker.

---

## 🚀 Guide de Démarrage (Débutant)

Suivez ces étapes pour lancer le projet sur votre machine locale en moins de 5 minutes.

### 1️⃣ Prérequis
Assurez-vous d'avoir installé :
* **Java 21 (JDK)** : [Télécharger ici](https://adoptium.net/)
* **Git** : [Télécharger ici](https://git-scm.com/)
* Un navigateur web moderne (Chrome, Firefox).

### 2️⃣ Récupérer le projet
Ouvrez votre terminal (Invite de commande ou PowerShell) et tapez :

```bash
git clone [https://github.com/LyesSEHILA/DataScale.git](https://github.com/LyesSEHILA/DataScale.git)
cd DataScale
```
### 3️⃣ Lancer le Backend (Serveur)
Le backend gère la base de données et l'API

* **Sur Windows** : 
```bash
cd backend
.\gradlew.bat bootrun
```
* **Sur Mac/Linux** : 
```bash
cd backend
./gradlew bootRun
```
⏳ Attendez que le message suivant apparaisse : Started BackendApplication in X.XXX seconds. Ne fermez pas cette fenêtre !

### 4️⃣ Lancer le Frontend (Interface)
Ouvrez une nouvelle fenêtre de terminal ou naviguez dans vos dossiers.
1. Allez dans le dossier DataScale/frontend.
2. Ouvrez le fichier `index.html` dans votre navigateur.
* **Recommandé** : Utilisez l'extension "Live Server" de VS Code pour éviter les problèmes de CORS.
* **Sinon** : Double-cliquez simplement sur `index.html`.

### 5️⃣ Premier Test

1. Cliquez sur "S'inscrire".

2. Créez un compte (ex: admin / admin@test.com / password).

3. Connectez-vous.

4. Allez dans "Training Arena" et tapez ls !

### ✅ Lancer les Tests
Pour vérifier que tout le code est robuste (Couverture > 80%), nous utilisons une suite de tests complète.
```bash
# Dans le dossier backend/
./gradlew clean test
```
Cela exécutera :

   * Les tests unitaires (JUnit).

   * Les tests d'intégration (MockMvc).

   * Les tests E2E (Selenium - Nécessite Firefox installé).

   * La génération du rapport de couverture JaCoCo (build/reports/jacoco/test/html/index.html).


### 👥 Auteurs

    Lyes SEHILA - Lead DevOps & Architecte

    Hassan Jatta - Lead Backend

    Abdoulaye - Lead Frontend

### 📄 Licence

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.