# 1. Image de base légère (Alpine Linux pèse ~5 Mo)
FROM alpine:latest

# 2. Installation des outils de base pour le joueur
# --no-cache permet de ne pas stocker les fichiers d'installation (garde l'image petite)
RUN apk add --no-cache curl grep nano bash

# 3. Création du Flag caché
# On le met dans /var/log et on le nomme avec un point (.) pour qu'il soit caché au simple 'ls'
# On donne les droits de lecture à tout le monde (444) mais pas d'écriture
RUN echo "FLAG{Bienvenue_Dans_La_Matrice_CyberScale}" > /var/log/.flag.txt && \
    chmod 444 /var/log/.flag.txt

# 4. Sécurité : Création d'un utilisateur non-root
# -D : Pas de mot de passe
# -u 1000 : UID standard
RUN adduser -D -u 1000 player

# Configuration du prompt et du dossier de travail
USER player
WORKDIR /home/player

# Commande par défaut au lancement du conteneur
CMD ["/bin/sh"]