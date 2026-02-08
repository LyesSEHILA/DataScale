#!/bin/sh

# Définition des couleurs ANSI
GREEN="\033[0;32m"
RED="\033[0;31m"
NC="\033[0m" # No Color

INPUT="$1"

# Comparaison directe avec la variable d'environnement injectée par Docker
if [ "$INPUT" = "$CHALLENGE_FLAG" ]; then
    echo -e "${GREEN}"
    echo "  ____  ____    _    __     __ ___ "
    echo " | __ )|  _ \  / \   \ \   / // _ \\"
    echo " |  _ \| |_) |/ _ \   \ \ / /| | | |"
    echo " | |_) |  _ <| ___ \   \ V / | |_| |"
    echo " |____/|_| \_\_| \_\   \_/   \___/"
    echo ""
    echo "      >>> VICTOIRE CONFIRMÉE <<<"
    echo -e "${NC}"
    # Le mot clé attendu par le Frontend pour valider :
    echo "::VICTORY_DETECTED::"
else
    echo -e "${RED}"
    echo " [X] ACCÈS REFUSÉ "
    echo -e "${NC}"
    echo " Le code est incorrect. Indice : regarde dans /etc/shadow"
fi