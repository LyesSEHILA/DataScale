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

        // --- CORRECTION ICI ---
        // Le backend renvoie du JSON {"containerId": "..."} et non du texte brut
        const data = await response.json(); 
        const containerId = data.containerId; 

        if (!containerId) throw new Error("Aucun ID de conteneur reçu.");

        term.write(`Conteneur prêt. ID: ${containerId.substring(0, 8)}...\r\n`);
        term.write('Connexion WebSocket sécurisée... \r\n');

        // 4. Connexion WebSocket
        const socket = new WebSocket(`ws://localhost:8080/ws/terminal?containerId=${containerId}`);

        socket.onopen = () => {
            term.write('\r\n\x1b[1;32m[SYSTEM] Liaison établie. Accès autorisé.\x1b[0m\r\n\r\n');
            term.focus();
            socket.send('\n'); // Force l'affichage du prompt
        };

        socket.onmessage = (event) => {
            term.write(event.data);
        };

        socket.onclose = () => {
            term.write('\r\n\x1b[1;31m[SYSTEM] Connexion interrompue.\x1b[0m');
        };

        socket.onerror = (error) => {
            console.error("Erreur WebSocket:", error);
            term.write('\r\n\x1b[1;31m[ERREUR] Impossible de joindre le serveur WebSocket.\x1b[0m');
        };

        // 5. Gestion de l'écriture
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