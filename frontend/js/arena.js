document.addEventListener("DOMContentLoaded", () => {
    const user = localStorage.getItem('userName') || 'guest';
    const machine = 'cyberscale';
    
    // 1. Initialisation Xterm
    const term = new Terminal({
        cursorBlink: true,
        theme: {
            background: '#0a0a0a',
            foreground: '#00ff00',
            cursor: '#00ff00',
            selection: '#003300'
        },
        fontSize: 14,
        fontFamily: 'Menlo, Monaco, "Courier New", monospace'
    });

    const fitAddon = new FitAddon.FitAddon();
    term.loadAddon(fitAddon);
    term.open(document.getElementById('terminal'));
    fitAddon.fit();
    window.addEventListener('resize', () => fitAddon.fit());

    // EXPOSITION POUR LES TESTS SELENIUM (Important !)
    window.term = term; 

    // 2. Système de Fichiers Virtuel (Mock)
    const fileSystem = {
        "readme.txt": "Bienvenue sur le noeud Alpha-1.\nVotre mission : Récupérer le contenu du fichier shadow.",
        "config.yaml": "host: 127.0.0.1\nport: 8080\nuser: admin",
        "shadow": "ACCESS_DENIED" // Marqueur spécial pour la protection
    };

    const prompt = `\r\n\x1b[1;32m${user}@${machine}\x1b[0m:\x1b[1;34m~\x1b[0m$ `;

    term.writeln('CyberScale OS [Version 1.0.4]');
    term.writeln('Type "help" for assistance.');
    term.write(prompt);

    // 3. Gestion Saisie
    let currentLine = "";

    term.onKey(e => {
        const ev = e.domEvent;
        const printable = !ev.altKey && !ev.altGraphKey && !ev.ctrlKey && !ev.metaKey;

        if (ev.key === "Enter") {
            term.write('\r\n');
            processCommand(currentLine);
            currentLine = "";
            term.write(prompt);
        } 
        else if (ev.key === "Backspace") {
            if (currentLine.length > 0) {
                currentLine = currentLine.slice(0, -1);
                term.write('\b \b');
            }
        } 
        else if (printable) {
            currentLine += e.key;
            term.write(e.key);
        }
    });

    // 4. Moteur de Commandes (Le Cœur de la Feature)
    function processCommand(input) {
        // Découpage : "cat readme.txt" -> ["cat", "readme.txt"]
        const args = input.trim().split(/\s+/);
        const cmd = args[0];
        const arg1 = args[1];

        switch(cmd) {
            case '': break;
            
            case 'help':
                term.writeln('Available commands:');
                term.writeln('  ls       List directory contents');
                term.writeln('  cat      Concatenate files and print on the standard output');
                term.writeln('  sudo     Execute a command as another user');
                term.writeln('  clear    Clear the terminal screen');
                break;

            case 'clear':
                term.clear();
                break;

            case 'ls':
                // Liste les clés du système de fichiers (en vert clair)
                const files = Object.keys(fileSystem).map(f => `\x1b[1;36m${f}\x1b[0m`).join('  ');
                term.writeln(files);
                break;

            case 'cat':
                if (!arg1) {
                    term.writeln('cat: missing file operand');
                    return;
                }
                if (fileSystem.hasOwnProperty(arg1)) {
                    const content = fileSystem[arg1];
                    if (content === "ACCESS_DENIED") {
                        term.writeln(`cat: ${arg1}: Permission denied`);
                    } else {
                        term.writeln(content);
                    }
                } else {
                    term.writeln(`cat: ${arg1}: No such file or directory`);
                }
                break;

            case 'sudo':
                // Logique du FLAG : sudo cat shadow
                if (args[1] === 'cat' && args[2] === 'shadow') {
                    term.writeln('\x1b[1;33m[sudo] password for ' + user + ': *********\x1b[0m');
                    setTimeout(() => {
                        term.writeln(''); // Saut de ligne après "mot de passe"
                        term.writeln('------------------------------------------------');
                        term.writeln('🚩 FLAG: \x1b[1;32mCTF{LINUX_MASTER_2025}\x1b[0m');
                        term.writeln('------------------------------------------------');
                        term.write(prompt); // On réaffiche le prompt car on est en async
                    }, 500); // Petit délai pour le réalisme
                    return; // On return pour éviter le prompt immédiat du bas
                } else {
                    term.writeln('sudo: command not implemented in simulation');
                }
                break;

            default:
                term.writeln(`\x1b[31mbash: ${cmd}: command not found\x1b[0m`);
        }
    }
});