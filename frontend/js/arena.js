const API_ARENA_VALIDATE = "http://localhost:8080/api/arena/validate";
const API_CHALLENGE_DETAILS = "http://localhost:8080/api/challenges";

document.addEventListener("DOMContentLoaded", async () => {
    const user = localStorage.getItem('userName') || 'guest';
    const userId = localStorage.getItem('userId');
    const challengeId = localStorage.getItem('currentChallengeId') || 'CTF_LINUX_1';
    const machine = 'node-alpha'; 
    
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

    window.term = term; 

    // 2. Chargement des données du challenge
    let challengeData = { title: "Mission Inconnue", description: "Aucune donnée." };
    
    try {
        const res = await fetch(`${API_CHALLENGE_DETAILS}/${challengeId}`);
        if(res.ok) challengeData = await res.json();
    } catch(e) { 
        console.error("Erreur chargement challenge", e);
        term.writeln(`\x1b[1;31mErreur de connexion au serveur de challenges.\x1b[0m`);
    }

    // 3. Système de Fichiers
    const fileSystem = {
        "readme.txt": `=== ${challengeData.title} ===\n\nOBJECTIF :\n${challengeData.description}\n\nUne fois le flag trouvé, tapez: submit <flag>`,
        "config.yaml": "host: 127.0.0.1\nuser: root",
        "shadow": "ACCESS_DENIED" 
    };

    const prompt = `\r\n\x1b[1;32m${user}@${machine}\x1b[0m:\x1b[1;34m~\x1b[0m$ `;

    term.writeln(`Connecting to environment for: \x1b[1;36m${challengeData.title}\x1b[0m...`);
    term.writeln('System ready.');
    term.writeln('Type "ls" to see available files.');
    term.write(prompt);

    // 4. Gestion Saisie
    let currentLine = "";

    term.onKey(e => {
        const ev = e.domEvent;
        const printable = !ev.altKey && !ev.altGraphKey && !ev.ctrlKey && !ev.metaKey;

        if (ev.key === "Enter") {
            term.write('\r\n');
            processCommand(currentLine);
            currentLine = "";
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

    // 5. Moteur de Commandes
    async function processCommand(input) {
        const args = input.trim().split(/\s+/);
        const cmd = args[0];
        const arg1 = args[1];

        switch(cmd) {
            case '': 
                term.write(prompt);
                break;
            
            case 'help':
                term.writeln('Available commands:');
                term.writeln('  ls       List directory contents');
                term.writeln('  cat      Read file content');
                term.writeln('  sudo     Execute as admin');
                term.writeln('  submit   Validate a captured flag (Ex: submit CTF{...})');
                term.writeln('  clear    Clear screen');
                term.write(prompt);
                break;

            case 'clear':
                term.clear();
                term.write(prompt);
                break;

            case 'ls':
                const files = Object.keys(fileSystem).map(f => `\x1b[1;36m${f}\x1b[0m`).join('  ');
                term.writeln(files);
                term.write(prompt);
                break;

            case 'cat':
                if (!arg1) {
                    term.writeln('cat: missing file operand');
                } else if (fileSystem.hasOwnProperty(arg1)) {
                    if (fileSystem[arg1] === "ACCESS_DENIED") {
                        term.writeln(`cat: ${arg1}: Permission denied`);
                    } else {
                        term.writeln(fileSystem[arg1]);
                    }
                } else {
                    term.writeln(`cat: ${arg1}: No such file or directory`);
                }
                term.write(prompt);
                break;

            case 'sudo':
                if (args[1] === 'cat' && args[2] === 'shadow') {
                    term.writeln('\x1b[1;33m[sudo] password for ' + user + ': *********\x1b[0m');
                    setTimeout(() => {
                        term.writeln('');
                        term.writeln('------------------------------------------------');
                        term.writeln('🚩 FLAG: \x1b[1;32mCTF{LINUX_MASTER_2025}\x1b[0m');
                        term.writeln('------------------------------------------------');
                        term.writeln('Utilisez "submit <flag>" pour gagner vos points.');
                        term.write(prompt);
                    }, 600);
                } else {
                    term.writeln('sudo: command not implemented');
                    term.write(prompt);
                }
                break;

            case 'submit':
                if (!arg1) {
                    term.writeln('Usage: submit <flag>');
                    term.write(prompt);
                    return;
                }

                if (!userId) {
                    term.writeln('\x1b[1;31mErreur: Session expirée. Veuillez vous reconnecter.\x1b[0m');
                    term.write(prompt);
                    return;
                }

                term.writeln('Verifying flag with HQ...');
                
                try {
                    const response = await fetch(API_ARENA_VALIDATE, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            userId: userId,
                            challengeId: challengeId,
                            flag: arg1
                        })
                    });

                    const data = await response.json();

                    if (response.ok) {
                        // --- SUCCÈS & REDIRECTION ---
                        term.writeln(`\x1b[1;32m[SUCCESS] ${data.message} (+50 pts)\x1b[0m 🏆`);
                        term.writeln(`\x1b[1;36mMission accomplie. Retour à la base dans 3 secondes...\x1b[0m`);
                        
                        setTimeout(() => {
                            window.location.href = 'challenges.html';
                        }, 3000);
                        // -----------------------------
                    } else {
                        term.writeln(`\x1b[1;31m[FAIL] ${data.message}\x1b[0m ❌`);
                        term.write(prompt); // On redonne la main si échec
                    }
                } catch (e) {
                    term.writeln(`\x1b[1;31m[ERROR] Connection lost.\x1b[0m`);
                    term.write(prompt);
                }
                // Pas de term.write(prompt) ici en cas de succès pour éviter que l'user tape pendant la redirection
                break;

            default:
                term.writeln(`\x1b[31mbash: ${cmd}: command not found\x1b[0m`);
                term.write(prompt);
        }
    }
});