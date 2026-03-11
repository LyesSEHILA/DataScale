FROM alpine:latest

# 1. Installation des outils
RUN apk add --no-cache sudo shadow coreutils

# 2. Configuration de l'utilisateur
RUN adduser -D -s /bin/sh player
RUN echo "player ALL=(ALL) NOPASSWD: ALL" > /etc/sudoers.d/player && \
    chmod 0440 /etc/sudoers.d/player

# 3. Placeholder (sera remplacé par entrypoint.sh)
RUN echo "root:CTF{LINUX_MASTER_2025}:19000:0:99999:7:::" >> /etc/shadow

# 4. Copie des scripts
# On copie verify.sh vers /bin/CTF pour qu'il soit exécutable via la commande 'CTF'
COPY verify.sh /bin/CTF
RUN chmod +x /bin/CTF

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# 5. Démarrage
USER player
WORKDIR /home/player

ENTRYPOINT ["/entrypoint.sh"]
CMD ["/bin/sh"]
