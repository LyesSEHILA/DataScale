// --- VARIABLES GLOBALES ---
let CURRENT_GAME_MODE = "TUTORIAL"; 
let LAST_USER_COMMAND = ""; 
let CURRENT_CONTAINER_ID = null; // 👈 Indispensable pour le bouton d'aide

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

    // Gestion Logout
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
        fontFamily: 'JetBrains Mono, Menlo, monospace', // Police plus "Cyber"
        theme: { 
            background: '#0a0a0a', 
            foreground: '#00ff00', 
            cursor: '#00ff00',
            selection: 'rgba(0, 255, 0, 0.3)'
        }
    });
    
    const fitAddon = new FitAddon.FitAddon();
    term.loadAddon(fitAddon);
    
    const terminalContainer = document.getElementById('terminal');
    term.open(terminalContainer);
    fitAddon.fit();

    window.addEventListener('resize', () => fitAddon.fit());

    term.write('Initialisation du système CyberScale... \r\n');

    // 3. Lancement du challenge + Connexion Alertes
    startChallengeAndConnect(term);
    connectToRedTeamAlerts();

    // 4. Affichage de la modale de bienvenue
    const modal = document.getElementById('welcome-modal');
    if(modal) modal.style.display = 'flex';
});


// --- FONCTION PRINCIPALE : DOCKER + TERMINAL ---
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

        // 👇 STOCKAGE GLOBAL (CRUCIAL POUR LE BOUTON AIDE)
        CURRENT_CONTAINER_ID = containerId;

        term.write(`Conteneur prêt. ID: ${containerId.substring(0, 8)}...\r\n`);
        term.write('Connexion WebSocket sécurisée... \r\n');

        // Connexion au WebSocket du Terminal
        const socket = new WebSocket(`ws://localhost:8080/ws/terminal?containerId=${containerId}`);
        
        socket.onopen = () => {
            term.write('\r\n\x1b[1;32m[SYSTEM] Liaison établie. Accès autorisé.\x1b[0m\r\n\r\n');
            term.focus();
            socket.send('\n'); 
        };

        socket.onclose = () => {
            term.write('\r\n\x1b[1;31m[SYSTEM] Connexion interrompue.\x1b[0m');
        };

        socket.onerror = (error) => {
            console.error("Erreur WebSocket:", error);
            term.write('\r\n\x1b[1;31m[ERREUR] Impossible de joindre le serveur WebSocket.\x1b[0m');
        };

        // --- 🕵️ ESPION DU CLAVIER (KEY LOGGER POUR L'IA) ---
        let currentCommandLine = "";

        term.onData(data => {
            // 1. Envoi au Docker (Exécution réelle)
            if (socket.readyState === WebSocket.OPEN) {
                socket.send(data);
            }

            // 2. Capture pour l'IA
            if (data === '\r') { // Touche ENTRÉE
                if (currentCommandLine.trim().length > 0) {
                    // On sauvegarde la commande pour le bouton "Aide"
                    LAST_USER_COMMAND = currentCommandLine.trim();
                    
                    // On envoie à l'IA pour analyse (Réaction standard)
                    sendToAI(LAST_USER_COMMAND, CURRENT_CONTAINER_ID);
                }
                currentCommandLine = ""; // Reset du buffer
            } 
            else if (data === '\u007F') { // Backspace
                if (currentCommandLine.length > 0) {
                    currentCommandLine = currentCommandLine.slice(0, -1);
                }
            } 
            else if (data >= String.fromCharCode(32)) { // Caractères imprimables
                currentCommandLine += data;
            }
        });

        // Lecture des retours du Docker
        socket.onmessage = (event) => {
            const text = event.data;
            term.write(text);
            if (text.includes("::VICTORY_DETECTED::")) {
                handleVictory(term, challengeId);
            }
        };

    } catch (error) {
        term.write(`\r\n\x1b[1;31m[ERREUR CRITIQUE] ${error.message}\x1b[0m`);
        console.error(error);
    }
}

// --- GESTION DES MODES DE JEU ---
window.selectGameMode = function(mode) {
    CURRENT_GAME_MODE = mode;
    
    // UI : On cache la modale et on affiche le bouton d'aide
    document.getElementById('welcome-modal').style.display = 'none';
    document.getElementById('help-btn').style.display = 'flex'; 

    console.log("Mode choisi : " + mode);
    
    // Message de bienvenue contextuel
    let msg = "";
    if (mode === 'TUTORIAL') msg = "Mode Entraînement activé. Tape une commande ou clique sur '?' pour de l'aide.";
    if (mode === 'RED_TEAM') msg = "Mode Red Team. Infiltre le système. L'IA SysAdmin te surveille.";
    if (mode === 'BLUE_TEAM') msg = "Mode Blue Team. Sécurise le serveur. L'IA Hacker arrive.";
    
    showRedTeamAlert(msg);
};

// --- API : ENVOI VERS L'IA ---
async function sendToAI(command, containerId) {
    const userId = localStorage.getItem('userId');
    try {
        await fetch('http://localhost:8080/api/arena/analyze', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ 
                userId: userId, 
                containerId: containerId, 
                command: command,
                mode: CURRENT_GAME_MODE
            })
        });
    } catch (e) {
        console.error("Erreur envoi IA:", e);
    }
}

// --- FEATURE : DEMANDER DE L'AIDE ---
window.askForHelp = function() {
    // Si le conteneur n'est pas prêt, on ne fait rien
    if (!CURRENT_CONTAINER_ID) {
        showRedTeamAlert("⚠️ Système non initialisé. Attendez...");
        return;
    }

    const contextCommand = LAST_USER_COMMAND || "GENERAL_CONTEXT";
    
    // Feedback visuel immédiat
    showRedTeamAlert("⏳ Analyse du contexte en cours... L'IA réfléchit.");

    // On prépare le payload spécial "HELP|"
    const helpPayload = "HELP|" + contextCommand;

    // On envoie à l'IA
    sendToAI(helpPayload, CURRENT_CONTAINER_ID); 
};

// --- WEBSOCKET : ALERTES IA (Red Team/Coach) ---
function connectToRedTeamAlerts() {
    const socket = new SockJS('http://localhost:8080/ws-cyberscale');
    const stompClient = Stomp.over(socket);
    stompClient.debug = null; 

    stompClient.connect({}, function (frame) {
        console.log('✅ [System] Canal IA connecté.');

        stompClient.subscribe('/topic/arena/alerts', function (message) {
            showRedTeamAlert(message.body);
        });
    });
}

// Variable pour gérer le timer de l'alerte (à ajouter juste avant la fonction showRedTeamAlert ou tout en haut du fichier)
// Variable pour le timer
let alertTimeout = null;

function showRedTeamAlert(text) {
    const container = document.getElementById('ai-alert-container');
    const messageSpan = document.getElementById('ai-alert-message');
    const titleSpan = document.getElementById('ai-alert-title');
    const icon = document.getElementById('ai-alert-icon');

    if (container && messageSpan) {
        if (alertTimeout) {
            clearTimeout(alertTimeout);
            alertTimeout = null;
        }

        // --- CORRECTION 1 : Nettoyage du texte ---
        // On remplace les sauts de ligne pour qu'ils soient visibles
        messageSpan.innerHTML = text.replace(/\n/g, "<br>");

        // --- CORRECTION 2 : Classes CSS améliorées ---
        // Ajout de : max-h-[80vh] overflow-y-auto (Scroll) et break-words (Lecture propre)
        const commonClasses = "border-2 text-white p-6 rounded-lg shadow-[0_0_50px_rgba(0,0,0,0.8)] backdrop-blur-md flex items-start gap-5 cursor-pointer transition-colors max-h-[80vh] overflow-y-auto";

        if (text.startsWith("💡") || text.startsWith("ℹ️")) {
            // Mode Conseil
            container.querySelector('div').className = `bg-blue-900/95 border-blue-500 hover:bg-blue-900 ${commonClasses}`;
            titleSpan.textContent = "ℹ️ CONSEIL IA";
            titleSpan.className = "font-mono text-blue-400 font-bold uppercase tracking-[0.2em] text-sm mb-1 border-b border-blue-800 pb-1 sticky top-0 bg-blue-900/95"; // Sticky title
            icon.className = "fas fa-lightbulb text-4xl text-blue-500 animate-pulse";
            messageSpan.textContent = text.substring(2).trim();
        } else {
            // Mode Alerte
            container.querySelector('div').className = `bg-red-950/95 border-red-500 hover:bg-red-900 ${commonClasses}`;
            titleSpan.textContent = "⚠️ RED TEAM RESPONSE";
            titleSpan.className = "font-mono text-red-400 font-bold uppercase tracking-[0.2em] text-sm mb-1 border-b border-red-800 pb-1 sticky top-0 bg-red-950/95"; // Sticky title
            icon.className = "fas fa-biohazard text-4xl text-red-500 animate-pulse";
            messageSpan.textContent = text;
        }

        // Correction lecture : break-words au lieu de break-all
        messageSpan.className = "font-mono text-lg font-medium text-white leading-relaxed break-words whitespace-pre-wrap";

        // Affichage
        container.style.display = 'block'; 
        container.classList.add('alert-shake');
        
        setTimeout(() => { container.classList.remove('alert-shake'); }, 500);

        // Timer intelligent (30s)
        const startAutoClose = () => {
            alertTimeout = setTimeout(() => { container.style.display = 'none'; }, 30000);
        };
        startAutoClose();

        container.onmouseenter = () => { if (alertTimeout) clearTimeout(alertTimeout); };
        container.onmouseleave = () => { startAutoClose(); };
        container.onclick = () => { 
            container.style.display = 'none'; 
            if (alertTimeout) clearTimeout(alertTimeout); 
        };
    }
}

// --- FONCTIONS UTILITAIRES ---
async function handleVictory(term, challengeId) {
    term.write('\r\n\x1b[1;33m[SYSTEM] Validation du challenge en cours...\x1b[0m\r\n');
    try {
        const userId = localStorage.getItem('userId');
        const flagSecret = "CTF{LINUX_MASTER_2025}"; 

        const response = await fetch('http://localhost:8080/api/arena/validate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userId: userId, challengeId: challengeId, flag: flagSecret })
        });

        if (response.ok) {
            term.write('\x1b[1;32m[SYSTEM] Challenge validé ! Redirection...\x1b[0m');
            setTimeout(() => { window.location.href = "challenges.html?success=true"; }, 2000);
        } else {
            term.write(`\x1b[1;31m[ERREUR] Validation refusée.\x1b[0m`);
        }
    } catch (e) {
        console.error(e);
    }
}