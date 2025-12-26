#!/bin/sh

INPUT_FLAG="$1"
# Le vrai flag
EXPECTED_FLAG="CTF{LINUX_MASTER_2025}"

if [ "$INPUT_FLAG" = "$EXPECTED_FLAG" ]; then
    echo ""
    echo -e "\033[0;32m  ██████╗  ██████╗  ██████╗ ██████╗ \033[0m"
    echo -e "\033[0;32m ██╔════╝ ██╔═══██╗██╔═══██╗██╔══██╗\033[0m"
    echo -e "\033[0;32m ██║  ███╗██║   ██║██║   ██║██║  ██║\033[0m"
    echo -e "\033[0;32m ██║   ██║██║   ██║██║   ██║██║  ██║\033[0m"
    echo -e "\033[0;32m ╚██████╔╝╚██████╔╝╚██████╔╝██████╔╝\033[0m"
    echo -e "\033[0;32m  ╚═════╝  ╚═════╝  ╚═════╝ ╚═════╝ \033[0m"
    echo "::VICTORY_DETECTED::"
    echo -e "\033[1;32m [SUCCESS] Bravo ! Le flag est correct.\033[0m"
    
else
    echo ""
    echo -e "\033[0;31m [ERROR] Flag incorrect.\033[0m"
fi