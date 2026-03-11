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

**CyberScale** est une plateforme éducative "360 degrés" permettant d'évaluer et d'améliorer ses compétences en cybersécurité. Elle combine théorie et pratique dans un environnement gamifié.

🚀 **Version actuelle : v1.0.1 (Release "Stabilization & Infra")**

🚀 **Version actuelle : v1.0.0 (Release Officielle)**

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

### 🎮 Cyber Arena (Red Team)
Un véritable **terminal Linux** isolé dans un conteneur Docker, accessible via le navigateur.
* **Isolation Totale :** Chaque utilisateur possède son propre conteneur éphémère.
* **Missions CTF :** Trouvez les flags cachés (`submit CTF{...}`) en utilisant `ls`, `grep`, `cat`, etc.

### 🕵️ Module Investigation (Blue Team)
Analysez des logs serveurs générés dynamiquement par une IA.
* Détectez les intrusions (SQL Injection, Brute Force).
* Identifiez les IP malveillantes parmi le trafic légitime.

### 🎣 Module Phishing (Facteur Humain)
Simulation réaliste d'une boîte mail d'entreprise. Apprenez à repérer les indices d'ingénierie sociale (headers suspects, liens piégés).

### 📊 Dashboard Unifié
Suivi de progression global avec calcul de score pondéré et recommandations de ressources (livres, certifications) adaptées à votre niveau.

---

## 🚀 Guide de Démarrage Rapide

Suivez ces étapes pour lancer CyberScale en local en **moins de 5 minutes**.

### 1️⃣ Prérequis (À installer avant de commencer)
* **Java 21 (JDK)** : [Télécharger ici](https://adoptium.net/)
* **Docker Desktop** (Obligatoire pour l'Arena) : [Télécharger ici](https://www.docker.com/products/docker-desktop/)
* **Git** : [Télécharger ici](https://git-scm.com/)

### 2️⃣ Récupérer le projet
Ouvrez votre terminal et clonez le dépôt :

```bash
git clone [https://github.com/LyesSEHILA/DataScale.git](https://github.com/LyesSEHILA/DataScale.git)
cd DataScale

```

### 3️⃣ Démarrer l'infrastructure

## 🐳 Démarrage via Docker (Méthode DevOps)

Le projet est entièrement conteneurisé. C'est la méthode recommandée pour tester l'application dans un environnement iso-prod.

### 1️⃣ Lancement Rapide (Docker Compose)

Cette commande va compiler le projet, construire l'image, lancer la base de données PostgreSQL et démarrer le Backend automatiquement.

```bash
# À la racine du projet
docker-compose up --build -d

```

* `up` : Démarre les conteneurs.
* `--build` : Force la reconstruction de l'image (utile si vous avez modifié le code).
* `-d` : Mode "détaché" (tourne en arrière-plan).

🔍 **Vérifier que tout tourne :**

```bash
docker-compose ps

```

*Vous devriez voir `cyberscale-backend` et `cyberscale-db` en statut "Up".*

---

### 2️⃣ Commandes Manuelles (Build & Run)

Si vous préférez gérer les conteneurs un par un :

**Étape A : Construire l'image du Backend**

```bash
docker build -t cyberscale-backend:1.0.0 ./backend

```

**Étape B : Lancer le conteneur**
⚠️ *Note : Pour que l'Arena (qui lance des conteneurs) fonctionne DANS un conteneur, il faut monter le socket Docker.*

```bash
docker run -d \
  -p 8080:8080 \
  --name cyberscale-app \
  -v /var/run/docker.sock:/var/run/docker.sock \
  cyberscale-backend:1.0.0

```

---

### 3️⃣ Maintenance & Nettoyage (Important)

L'application crée des conteneurs éphémères pour chaque challenge "Arena". Si vous voulez faire un grand nettoyage :

**Arrêter l'application :**

```bash
docker-compose down

```

**Nettoyer les conteneurs d'entraînement orphelins :**
Si vous avez fait beaucoup de tests Arena, nettoyez les conteneurs "exécutés" mais arrêtés :

```bash
docker container prune -f

```

**Voir les logs en temps réel (Debugging) :**

```bash
docker logs -f cyberscale-backend

```

---

### 💡 Note sur l'Architecture Docker

L'application utilise une architecture **Docker-out-of-Docker (DooD)** :
Le conteneur du Backend a accès au `docker.sock` de l'hôte. Cela lui permet d'ordonner à votre Docker Desktop de créer les conteneurs Linux pour les utilisateurs (Ubuntu/Alpine) à côté de lui, et non à l'intérieur de lui.

---

### 4️⃣ Lancer le Backend (Serveur)

Le backend va démarrer sur le port `8080`.

**Sur Windows (PowerShell/CMD) :**

```bash
cd backend
.\gradlew.bat bootRun

```

**Sur Mac / Linux :**

```bash
cd backend
chmod +x gradlew
./gradlew bootRun

```

⏳ **Attendez** de voir le message : `Started BackendApplication in X.XXX seconds`.
*Le premier lancement peut être un peu long (téléchargement des dépendances).*

### 5️⃣ Lancer le Frontend (Interface)

Le frontend est statique (HTML/JS). Pour éviter les erreurs de sécurité du navigateur (CORS), il ne faut pas juste double-cliquer sur le fichier.

**Méthode Recommandée (VS Code) :**

1. Ouvrez le dossier `DataScale` dans VS Code.
2. Faites un clic droit sur le fichier `frontend/index.html`.
3. Choisissez **"Open with Live Server"** (Extension à installer si besoin).

**Méthode Alternative (Python) :**

```bash
cd frontend
python -m http.server 5500
# Puis ouvrez http://localhost:5500 dans votre navigateur

```

---

## 🧪 Comment tester l'application ?

Une fois lancé :

1. Cliquez sur **"S'inscrire"** et créez un compte (ex: `user` / `test`).
2. Connectez-vous.
3. Allez dans l'onglet **"Arena"**.
4. Cliquez sur **"Démarrer"** une mission (ex: Alpha-1).
5. *Si Docker est bien lancé*, un terminal noir apparaît. Tapez `ls -la` pour vérifier !

---

## 🛠️ Dépannage (Troubleshooting)

| Erreur Rencontrée | Solution |
| --- | --- |
| **"Port 8080 already in use"** | Un autre programme utilise le port. Tuez le processus Java ou changez le port dans `application.properties`. |
| **"Connection refused" (Docker)** | Vérifiez que Docker Desktop est bien lancé. Le backend doit pouvoir communiquer avec le socket Docker. |
| **Erreur CORS (Frontend)** | N'ouvrez pas le fichier `index.html` directement en double-cliquant. Utilisez un serveur local (Live Server). |
| **Pas de logs dans "Investigation"** | Cliquez sur le bouton "Générer du trafic" pour que l'IA crée des données simulées. |

---

## ✅ Lancer les Tests Techniques

Le projet assure une couverture de code > 80%.

```bash
cd backend
./gradlew clean test

```

Cela générera un rapport HTML dans `backend/build/reports/jacoco/test/html/index.html`.

---

### 👥 L'Équipe

Projet réalisé dans le cadre du cursus DevOps.

* **Lyes SEHILA** - Lead DevOps & Architecte
* **Hassan Jatta** - Lead Backend
* **Abdoulaye** - Lead Frontend

### 📄 Licence

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.
