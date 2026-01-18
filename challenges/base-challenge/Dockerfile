FROM alpine:latest

# 1. Installation des outils
RUN apk add --no-cache sudo shadow coreutils

# 2. Création de l'utilisateur
RUN adduser -D -s /bin/sh player

# 3. La faille de sécurité (Shadow lisible)
RUN chmod 644 /etc/shadow

# 4. Configuration sudo
RUN echo "player ALL=(ALL) NOPASSWD: ALL" > /etc/sudoers.d/player && \
    chmod 0440 /etc/sudoers.d/player

# 5. --- INJECTION DU VRAI FLAG ---
# Le flag est caché comme s'il était un hash de mot de passe root
RUN echo "root:CTF{LINUX_MASTER_2025}:19000:0:99999:7:::" >> /etc/shadow

# 6. Installation de la commande de validation CTF
COPY verify.sh /bin/CTF
RUN chmod +x /bin/CTF

USER player
WORKDIR /home/player

CMD ["/bin/sh"]
