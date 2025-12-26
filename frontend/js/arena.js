document.addEventListener("DOMContentLoaded", () => {
    // 1. Vérification Authentification
    const userId = localStorage.getItem('userId');
    const username = localStorage.getItem('userName') || "Utilisateur";
    const sidebarUser = document.getElementById('sidebarUsername');

    if (!userId) {
        window.location.href = 'login.html';
        return;
    }

    if(sidebarUser) sidebarUser.textContent = username;

    const logoutBtn = document.getElementById('logoutBtn');
    if(logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            localStorage.clear();
            window.location.href = 'index.html';
        });
    }

    // 2. Initialisation du Terminal Xterm.js
    const term = new Terminal({
        cursorBlink: true,
        fontSize: 14,
        fontFamily: 'Menlo, Monaco, "Courier New", monospace',
        theme: { 
            background: '#1e1e1e', 
            foreground: '#00ff00', 
            cursor: '#00ff00',
            selection: 'rgba(0, 255, 0, 0.3)'
        }
    });
    
    // Addon pour adapter la taille du terminal
    const fitAddon = new FitAddon.FitAddon();
    term.loadAddon(fitAddon);
    
    const terminalContainer = document.getElementById('terminal');
    term.open(terminalContainer);
    fitAddon.fit();

    window.addEventListener('resize', () => fitAddon.fit());

    term.write('Initialisation du système CyberScale... \r\n');

    // 3. Démarrage du Challenge
    startChallengeAndConnect(term);
});

async function startChallengeAndConnect(term) {
    try {
        term.write('Démarrage de l\'environnement Docker... \r\n');

        const challengeId = "CTF_LINUX_1"; 
        
        // Appel API pour créer le conteneur
        const response = await fetch(`http://localhost:8080/api/arena/start/${challengeId}`, {
            method: 'POST'
        });

        if (!response.ok) throw new Error("Erreur lors du démarrage du conteneur (API).");

        const data = await response.json(); 
        const containerId = data.containerId; 

        if (!containerId) throw new Error("Aucun ID de conteneur reçu.");

        term.write(`Conteneur prêt. ID: ${containerId.substring(0, 8)}...\r\n`);
        term.write('Connexion WebSocket sécurisée... \r\n');

        const socket = new WebSocket(`ws://localhost:8080/ws/terminal?containerId=${containerId}`);
        
        // Déclaration unique de l'écouteur de messages
        socket.onmessage = (event) => {
            const text = event.data;
            
            // 1. On écrit dans le terminal
            term.write(text);

            // 2. ESPION : On détecte la victoire
            // Note: Assurez-vous d'avoir reconstruit l'image Docker avec le script verify.sh qui fait cet echo
            if (text.includes("::VICTORY_DETECTED::")) {
                handleVictory(term, challengeId);
            }
        };

        socket.onopen = () => {
            term.write('\r\n\x1b[1;32m[SYSTEM] Liaison établie. Accès autorisé.\x1b[0m\r\n\r\n');
            term.focus();
            socket.send('\n'); // Force l'affichage du prompt
        };

        socket.onclose = () => {
            term.write('\r\n\x1b[1;31m[SYSTEM] Connexion interrompue.\x1b[0m');
        };

        socket.onerror = (error) => {
            console.error("Erreur WebSocket:", error);
            term.write('\r\n\x1b[1;31m[ERREUR] Impossible de joindre le serveur WebSocket.\x1b[0m');
        };

        // Gestion de l'écriture (Clavier -> WebSocket)
        term.onData(data => {
            if (socket.readyState === WebSocket.OPEN) {
                socket.send(data);
            }
        });

    } catch (error) {
        term.write(`\r\n\x1b[1;31m[ERREUR CRITIQUE] ${error.message}\x1b[0m`);
        console.error(error);
    }
}

async function handleVictory(term, challengeId) {
    term.write('\r\n\x1b[1;33m[SYSTEM] Validation du challenge en cours...\x1b[0m\r\n');

    try {
        // CORRECTION MAJEURE ICI : On récupère l'ID utilisateur
        const userIdStr = localStorage.getItem('userId');
        
        if (!userIdStr) {
            term.write('\x1b[1;31m[ERREUR] Utilisateur non identifié.\x1b[0m');
            return;
        }

        const userId = parseInt(userIdStr); // Conversion en entier pour le Backend
        const flagSecret = "CTF{LINUX_MASTER_2025}"; 

        const response = await fetch('http://localhost:8080/api/arena/validate', {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ 
                userId: userId,        // <-- C'était le chaînon manquant !
                challengeId: challengeId, 
                flag: flagSecret 
            })
        });

        if (response.ok) {
            term.write('\x1b[1;32m[SYSTEM] Challenge validé ! Redirection...\x1b[0m');
            
            // Petite pause pour laisser l'utilisateur lire le message vert
            setTimeout(() => {
                window.location.href = "challenges.html?success=true";
            }, 2000);
        } else {
            const errorData = await response.json();
            console.error("Erreur validation:", errorData);
            term.write(`\x1b[1;31m[ERREUR] Validation refusée par le serveur : ${errorData.message}\x1b[0m`);
        }

    } catch (e) {
        console.error(e);
        term.write('\x1b[1;31m[ERREUR] Problème de connexion au serveur.\x1b[0m');
    }
}