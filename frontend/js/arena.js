// Configuration
const socketUrl = 'http://localhost:8080/ws-cyberscale'; // L'adresse RÉELLE de votre config

document.addEventListener("DOMContentLoaded", () => {
    // 1. Initialisation Terminal (Votre code était bon ici)
    const term = new Terminal({
        cursorBlink: true,
        fontFamily: 'Menlo, Monaco, "Courier New", monospace',
        theme: { background: '#1a1b26', foreground: '#a9b1d6' }
    });
    const fitAddon = new FitAddon.FitAddon();
    term.loadAddon(fitAddon);
    term.open(document.getElementById('terminal'));
    fitAddon.fit();

    term.write('Connexion au système CyberScale...\r\n');

    // 2. Connexion Compatible STOMP (C'est ICI que ça change)
    const socket = new SockJS(socketUrl);
    const stompClient = Stomp.over(socket);
    stompClient.debug = null; // Enlever le bruit dans la console

    stompClient.connect({}, function (frame) {
        term.write('\r\n\x1b[1;32m[SYSTEM] Connexion établie.\x1b[0m\r\n$ ');

        // A. Écouter les réponses du serveur (Echo)
        stompClient.subscribe('/topic/arena', function (message) {
            term.write(message.body);
        });

    }, function (error) {
        term.write('\r\n\x1b[1;31m[ERREUR] Serveur injoignable.\x1b[0m');
    });

    // 3. Envoi des touches (Compatible STOMP)
    term.onData(data => {
        if (stompClient && stompClient.connected) {
            stompClient.send("/app/arena", {}, data);
        }
    });
});