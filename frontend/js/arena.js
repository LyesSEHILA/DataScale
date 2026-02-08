// --- VARIABLES GLOBALES ---
let CURRENT_GAME_MODE = "TUTORIAL"; 
let LAST_USER_COMMAND = ""; 
let CURRENT_CONTAINER_ID = null; 
let CURRENT_CHALLENGE_ID = null; // 👈 AJOUTE CECI (Stocke l'ID du challenge en cours)
let alertTimeout = null;

document.addEventListener("DOMContentLoaded", () => {
    // 1. Auth Init
    const userId = localStorage.getItem('userId');
    const username = localStorage.getItem('userName') || "Utilisateur";
    if (!userId) { window.location.href = 'login.html'; return; }
    if(document.getElementById('sidebarUsername')) document.getElementById('sidebarUsername').textContent = username;

    document.getElementById('logoutBtn')?.addEventListener('click', () => {
        localStorage.clear(); window.location.href = 'index.html';
    });

    // 2. DÉTECTION DU MODE : CHALLENGE OU BUILDER ?
    const urlParams = new URLSearchParams(window.location.search);
    const challengeId = urlParams.get('challengeId'); // Ex: arena.html?challengeId=CTF_LINUX_1

    if (challengeId) {
        // --- CAS A : MODE CHALLENGE DIRECT ---
        console.log("⚔️ Mode Challenge détecté :", challengeId);
        
        // On cache le builder immédiatement
        if(document.getElementById('builder-section')) document.getElementById('builder-section').style.display = 'none';
        if(document.getElementById('builder-tools')) document.getElementById('builder-tools').style.display = 'none';
        if(document.getElementById('deploy-btn')) document.getElementById('deploy-btn').style.display = 'none';
        
        // On affiche le terminal
        document.getElementById('terminal-section').style.display = 'block';
        document.getElementById('terminal-status').style.display = 'block';
        document.getElementById('help-btn').style.display = 'flex';
        
        // On lance le challenge
        startDirectChallenge(challengeId);

    } else {
        // --- CAS B : MODE BUILDER (Défaut) ---
        console.log("🏗️ Mode Builder activé");
        // Le builder.js s'occupe du reste car le builder-section est visible par défaut dans le HTML
    }
});

// --- NOUVELLE FONCTION : Lancer un challenge pré-défini ---
async function startDirectChallenge(challengeId) {
    const term = new Terminal({
        cursorBlink: true, fontSize: 14, fontFamily: 'JetBrains Mono, monospace',
        theme: { background: '#0a0a0a', foreground: '#00ff00', cursor: '#00ff00' }
    });
    const fitAddon = new FitAddon.FitAddon();
    term.loadAddon(fitAddon);
    term.open(document.getElementById('terminal'));
    fitAddon.fit();
    window.addEventListener('resize', () => fitAddon.fit());

    term.write(`\r\n\u001B[1;33m[SYSTEM] Initialisation du Challenge : ${challengeId}...\u001B[0m\r\n`);

    try {
        CURRENT_CHALLENGE_ID = challengeId;
        // Appel API pour démarrer le conteneur du challenge spécifique
        const response = await fetch(`http://localhost:8080/api/arena/start/${challengeId}`, {
            method: 'POST'
        });

        if (!response.ok) throw new Error("Impossible de démarrer le challenge.");

        const data = await response.json();
        CURRENT_CONTAINER_ID = data.containerId;
        CURRENT_GAME_MODE = "CHALLENGE"; // Mode spécial pour l'IA

        term.write(`\u001B[1;32m[OK] Environnement prêt. ID: ${CURRENT_CONTAINER_ID.substring(0,8)}\u001B[0m\r\n`);
        
        // Connexion WebSocket
        connectToTerminal(term);
        connectToRedTeamAlerts();

    } catch (e) {
        console.error(e);
        term.write(`\r\n\u001B[1;31m[ERREUR] ${e.message}\u001B[0m`);
    }
}

// --- 1. TRANSITION : APPELÉE PAR LE BUILDER ---
// Cette fonction est déclenchée par builder.js une fois le déploiement terminé
window.onDeploymentSuccess = function(containerId) {
    console.log("🚀 Transition vers la phase Terminal. ID:", containerId);
    CURRENT_CONTAINER_ID = containerId;
    
    // 1. Cacher le Builder et ses outils
    const builderSection = document.getElementById('builder-section');
    const builderTools = document.getElementById('builder-tools');
    const deployBtn = document.getElementById('deploy-btn'); // ID corrigé pour correspondre à ton HTML

    if(builderSection) builderSection.style.display = 'none';
    if(builderTools) builderTools.style.display = 'none';
    if(deployBtn) deployBtn.style.display = 'none';

    // 2. Afficher la modale de choix de camp
    const modal = document.getElementById('mode-selection-modal');
    if(modal) modal.style.display = 'flex';
};

// --- 2. PHASE SÉLECTION DU MODE ---
window.selectGameMode = function(mode) {
    CURRENT_GAME_MODE = mode;
    
    // UI Switch : Cache modale -> Affiche Terminal
    document.getElementById('mode-selection-modal').style.display = 'none';
    document.getElementById('terminal-section').style.display = 'block';
    
    // Mise à jour Header
    const header = document.getElementById('header-status');
    if(header) {
        header.innerHTML = `<i class="fas fa-satellite-dish mr-2"></i> PHASE 2 : Simulation (${mode})`;
        header.classList.add('text-blue-400', 'animate-pulse');
    }
    
    const termStatus = document.getElementById('terminal-status');
    if(termStatus) termStatus.style.display = 'block';
    
    const helpBtn = document.getElementById('help-btn');
    if(helpBtn) helpBtn.style.display = 'flex';

    // Lancement Terminal
    initTerminal();
};

// --- 3. PHASE TERMINAL & JEU ---
function initTerminal() {
    // Configuration Xterm.js (Code Original amélioré)
    const term = new Terminal({
        cursorBlink: true,
        fontSize: 14,
        fontFamily: 'JetBrains Mono, Menlo, monospace',
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

    term.write('Connexion à l\'architecture déployée... \r\n');
    
    connectToTerminal(term);
    connectToRedTeamAlerts(); // Connexion au canal de l'IA
}

function connectToTerminal(term) {
    if (!CURRENT_CONTAINER_ID) {
        term.write('\r\n\x1b[1;31m[ERREUR] ID Conteneur manquant. Veuillez redéployer.\x1b[0m');
        return;
    }

    term.write(`Cible acquise. ID: ${CURRENT_CONTAINER_ID.substring(0, 8)}...\r\n`);
    
    // Connexion WebSocket DIRECTE (le conteneur existe déjà grâce au Builder)
    const socket = new WebSocket(`ws://localhost:8080/ws/terminal?containerId=${CURRENT_CONTAINER_ID}`);
    
    socket.onopen = () => { 
        term.write('\r\n\x1b[1;32m[SYSTEM] Liaison établie. Accès Root autorisé.\x1b[0m\r\n\r\n'); 
        term.focus(); 
        socket.send('\n'); // Simule une frappe pour afficher le prompt
    };
    
    socket.onclose = () => {
        term.write('\r\n\x1b[1;31m[SYSTEM] Connexion interrompue.\x1b[0m');
    };

    socket.onerror = (e) => {
        console.error(e);
        term.write('\r\n\x1b[1;31m[ERREUR] WebSocket déconnecté.\x1b[0m');
    };

    // Espion Clavier pour l'IA (Code Original conservé)
    let currentCommandLine = "";
    term.onData(data => {
        // 1. Envoi au Docker
        if (socket.readyState === WebSocket.OPEN) {
            socket.send(data);
        }
        
        // 2. Capture pour l'IA
        if (data === '\r') { // Entrée
            if (currentCommandLine.trim().length > 0) {
                LAST_USER_COMMAND = currentCommandLine.trim();
                sendToAI(LAST_USER_COMMAND, CURRENT_CONTAINER_ID);
            }
            currentCommandLine = "";
        } else if (data === '\u007F') { // Backspace
            if (currentCommandLine.length > 0) {
                currentCommandLine = currentCommandLine.slice(0, -1);
            }
        } else if (data >= String.fromCharCode(32)) {
            currentCommandLine += data;
        }
    });
    
    // Lecture des retours du Docker + DÉTECTION VICTOIRE (Restauré !)
    socket.onmessage = (event) => { 
        const text = event.data;
        term.write(text);

        // ✅ LOGIQUE DE VICTOIRE RESTAURÉE
        if (text.includes("::VICTORY_DETECTED::")) {
            // On utilise un ID générique ou celui stocké si tu gères les challenges via le builder
            const idToValidate = CURRENT_CHALLENGE_ID || "CTF_CUSTOM_BUILDER";
            handleVictory(term, idToValidate);
        }
    };
}

// --- 4. FONCTIONS UTILITAIRES (IA, VICTOIRE, ALERTES) ---

// ✅ FONCTION RESTAURÉE : GESTION DE LA VICTOIRE
async function handleVictory(term, challengeId) {
    term.write('\r\n\x1b[1;33m[SYSTEM] Validation du challenge en cours...\x1b[0m\r\n');
    try {
        const userId = localStorage.getItem('userId');
        const flagSecret = "CTF{LINUX_MASTER_2025}"; // Ou un flag dynamique selon ton besoin

        const response = await fetch('http://localhost:8080/api/arena/validate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ 
                userId: userId, 
                challengeId: challengeId, 
                flag: flagSecret,
                mode: CURRENT_GAME_MODE // On passe aussi le mode
            })
        });

        if (response.ok) {
            term.write('\x1b[1;32m[SYSTEM] Challenge validé ! Redirection...\x1b[0m');
            setTimeout(() => { window.location.href = "challenges.html?success=true"; }, 2000);
        } else {
            term.write(`\x1b[1;31m[ERREUR] Validation refusée.\x1b[0m`);
        }
    } catch (e) {
        console.error("Erreur victoire:", e);
        term.write(`\x1b[1;31m[ERREUR] Impossible de contacter le serveur de validation.\x1b[0m`);
    }
}

// API IA
async function sendToAI(command, containerId) {
    const userId = localStorage.getItem('userId');
    try {
        await fetch('http://localhost:8080/api/arena/analyze', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userId, containerId, command, mode: CURRENT_GAME_MODE })
        });
    } catch (e) { console.error("Erreur IA", e); }
}

// Bouton Aide
window.askForHelp = function() {
    if (!CURRENT_CONTAINER_ID) {
        showRedTeamAlert("⚠️ Système non initialisé. Attendez...");
        return;
    }
    showRedTeamAlert("⏳ Analyse du contexte...");
    const cmd = LAST_USER_COMMAND || "GENERAL_CONTEXT";
    sendToAI("HELP|" + cmd, CURRENT_CONTAINER_ID); 
};

// WebSocket Alertes IA
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

// Gestion Alertes (Version Améliorée)
function showRedTeamAlert(text) {
    const container = document.getElementById('ai-alert-container');
    const messageSpan = document.getElementById('ai-alert-message');
    const titleSpan = document.getElementById('ai-alert-title');
    const icon = document.getElementById('ai-alert-icon');

    if (container && messageSpan) {
        if (alertTimeout) { clearTimeout(alertTimeout); alertTimeout = null; }
        
        messageSpan.innerHTML = text.replace(/\n/g, "<br>");
        
        let baseClass = "bg-gray-900/95 border-2 p-6 rounded-lg shadow-[0_0_50px_rgba(0,0,0,0.8)] backdrop-blur-md flex items-start gap-5 cursor-pointer transition-colors max-h-[80vh] overflow-y-auto";
        
        if (text.startsWith("💡") || text.startsWith("ℹ️")) {
            container.querySelector('div').className = `${baseClass} border-blue-500 hover:bg-blue-900/80`;
            titleSpan.textContent = "ℹ️ CONSEIL IA";
            titleSpan.className = "font-mono text-blue-400 font-bold border-b border-blue-800 pb-1";
            icon.className = "fas fa-lightbulb text-4xl text-blue-500 animate-pulse";
            messageSpan.textContent = text.substring(2).trim();
        } else {
            container.querySelector('div').className = `${baseClass} border-red-500 hover:bg-red-900/80`;
            titleSpan.textContent = "⚠️ RED TEAM RESPONSE";
            titleSpan.className = "font-mono text-red-400 font-bold border-b border-red-800 pb-1";
            icon.className = "fas fa-biohazard text-4xl text-red-500 animate-pulse";
            messageSpan.textContent = text;
        }

        container.style.display = 'block'; 
        container.classList.add('alert-shake');
        setTimeout(() => { container.classList.remove('alert-shake'); }, 500);

        const startAutoClose = () => { alertTimeout = setTimeout(() => { container.style.display = 'none'; }, 30000); };
        startAutoClose();
        container.onmouseenter = () => { if (alertTimeout) clearTimeout(alertTimeout); };
        container.onmouseleave = () => { startAutoClose(); };
        container.onclick = () => { container.style.display = 'none'; if (alertTimeout) clearTimeout(alertTimeout); };
    }
}