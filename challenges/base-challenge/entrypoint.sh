#!/bin/sh

if [ ! -z "$CHALLENGE_FLAG" ]; then
    echo "⚡ Application du flag dynamique..."
    
    # Le Dockerfile contient "CTF{LINUX_MASTER_2025}" en dur.
    # On le remplace par la valeur de $CHALLENGE_FLAG (ex: "898b9537")
    sudo sed -i "s|CTF{LINUX_MASTER_2025}|$CHALLENGE_FLAG|g" /etc/shadow
    
    # Optionnel : Flag dans un fichier texte
    sudo sh -c "echo '$CHALLENGE_FLAG' > /root/flag.txt"
fi

exec "$@"