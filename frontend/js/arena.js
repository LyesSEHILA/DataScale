// Configuration du Terminal (inchangée)
const term = new Terminal({
    cursorBlink: true,
    fontSize: 14,
    fontFamily: 'Consolas, "Courier New", monospace',
    theme: { background: '#1e1e1e', foreground: '#00ff00', cursor: '#00ff00' }
});
const terminalContainer = document.getElementById('terminal');
term.open(terminalContainer);

term.write('Initializing CyberScale Environment... \r\n');

// --- LOGIQUE DYNAMIQUE ---

async function startChallenge() {
    try {
        // 1. Appel API pour lancer le conteneur (Challenge ID "1" pour l'exemple)
        // Dans le futur, on récupèrera l'ID depuis l'URL ou le bouton cliqué
        const challengeId = "CTF_LINUX_1"; 
        const response = await fetch(`http://localhost:8080/api/arena/start/${challengeId}`, {
            method: 'POST'
        });

        if (!response.ok) throw new Error("Impossible de démarrer le challenge");

        const data = await response.json();
        const containerId = data.containerId;

        term.write(`\r\nContainer spawned successfully. ID: ${containerId.substring(0, 8)}...\r\n`);
        term.write('Establishing WebSocket Uplink... \r\n');

        // 2. Connexion WebSocket avec l'ID reçu
        connectWebSocket(containerId);

    } catch (error) {
        term.write(`\r\n\x1b[1;31m[ERROR] ${error.message}\x1b[0m`);
        console.error(error);
    }
}

function connectWebSocket(containerId) {
    const socket = new WebSocket(`ws://localhost:8080/ws/terminal?containerId=${containerId}`);

    socket.onopen = () => {
        term.write('\r\n\x1b[1;32m[SYSTEM] Uplink Established. Access Granted.\x1b[0m\r\n\r\n');
        socket.send('\n'); // Force le prompt
    };

    socket.onmessage = (event) => term.write(event.data);
    
    socket.onclose = () => term.write('\r\n\x1b[1;31m[SYSTEM] Connection Lost.\x1b[0m');

    term.onData(data => {
        if (socket.readyState === WebSocket.OPEN) socket.send(data);
    });
}

// Lancement automatique au chargement de la page
startChallenge();