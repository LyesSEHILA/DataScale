document.addEventListener("DOMContentLoaded", () => {
    // 1. Initialisation de Xterm.js
    const term = new Terminal({
        cursorBlink: true,
        theme: {
            background: '#0f0f0f',
            foreground: '#00ff00', // Vert Hacker
            cursor: '#00ff00'
        },
        fontSize: 14,
        fontFamily: '"Menlo", "Meslo LG", "Courier New", monospace'
    });

    // 2. Intégration de l'addon "Fit" pour que le terminal prenne toute la place
    const fitAddon = new FitAddon.FitAddon();
    term.loadAddon(fitAddon);

    // 3. Montage dans le DOM
    term.open(document.getElementById('terminal'));
    fitAddon.fit();

    // Gestion du redimensionnement de la fenêtre
    window.addEventListener('resize', () => fitAddon.fit());

    // 4. Définition du Prompt
    const user = localStorage.getItem('userName') || 'guest';
    const machine = 'cyberscale';
    const prompt = `\r\n\x1b[1;32m${user}@${machine}\x1b[0m:\x1b[1;34m~\x1b[0m$ `;

    // Message de bienvenue
    term.writeln('Welcome to CyberScale Training Environment v1.0');
    term.writeln('Type "help" to see available commands.');
    term.write(prompt);

    // 5. Gestion de la saisie (Input Loop simulé)
    let currentLine = "";

    term.onKey(e => {
        const printable = !e.domEvent.altKey && !e.domEvent.altGraphKey && !e.domEvent.ctrlKey && !e.domEvent.metaKey;

        if (e.domEvent.key === "Enter") {
            // Touche Entrée : On valide la commande
            term.write('\r\n');
            processCommand(currentLine);
            currentLine = "";
            term.write(prompt);
        } else if (e.domEvent.key === "Backspace") {
            // Touche Effacer : On gère visuellement la suppression
            if (currentLine.length > 0) {
                currentLine = currentLine.slice(0, -1);
                term.write('\b \b'); // Recule, Espace (efface), Recule
            }
        } else if (printable) {
            // Caractère normal : On l'ajoute au buffer et on l'affiche
            currentLine += e.key;
            term.write(e.key);
        }
    });

    // 6. Processeur de commandes (Mock)
    function processCommand(cmd) {
        const command = cmd.trim();
        
        if (command === "") return;

        switch (command) {
            case "help":
                term.writeln('Available commands:');
                term.writeln('  help     - Show this help message');
                term.writeln('  clear    - Clear the terminal screen');
                term.writeln('  whoami   - Display current user');
                term.writeln('  date     - Display current date');
                break;
            case "clear":
                term.clear();
                break;
            case "whoami":
                term.writeln(user);
                break;
            case "date":
                term.writeln(new Date().toString());
                break;
            default:
                term.writeln(`\x1b[31mbash: ${command}: command not found\x1b[0m`);
        }
    }
});