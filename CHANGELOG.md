# Changelog - CyberScale

Tous les changements notables sur ce projet seront documentés dans ce fichier.

## [1.0.1] - 2026-03-11

### ✨ Nouvelles Fonctionnalités
- **Flags Dynamiques :** Les challenges génèrent désormais des flags uniques par utilisateur via des variables d'environnement Docker (`CHALLENGE_FLAG`).
- **Orchestration RabbitMQ :** Découplage de l'exécution des commandes et de l'analyse via une file de messages asynchrone.
- **Support Kubernetes :** Ajout de `KubernetesService` pour le déploiement dynamique de leurres (honeypots).
- **Module Phishing :** Nouveau contrôleur et service pour simuler des campagnes de phishing et analyser les comportements.

### 🐛 Corrections de Bugs (Bug Fixes)
- **ArenaController :** Correction d'un bug dans `CommandRequest` où le champ `mode` était manquant, empêchant l'analyse correcte des commandes dans certains modes de jeu.
- **Validation des Flags :** Amélioration du système de validation pour prioriser les flags dynamiques avant de retomber sur les flags statiques.
- **Sécurité :** Ajout d'une validation de base pour les commandes potentiellement dangereuses (`isCommandDangerous`) dans le `ContainerService`.

### 🔧 Améliorations Techniques
- Mise à jour vers **Spring Boot 3.4.11**.
- Amélioration de la couverture de tests avec **JaCoCo** et **Cucumber**.
- Refactorisation du `ArenaService` pour une meilleure gestion de l'état des conteneurs.

## [1.0.0] - 2026-02-15
- Version initiale stable.
- Intégration de Docker pour l'arène de challenges.
- Système d'authentification et de profil utilisateur.
- Dashboard de progression.
