// Templates HTML pour l'intérieur des mails (Aspect réaliste Gmail)
const scenariosHTML = {
    "SCENARIO_1": `
        <div style="margin-bottom: 20px;">
            <h2 style="margin: 0 0 10px 0; font-size: 22px;">URGENT : Validation de sécurité requise</h2>
            <div style="display:flex; align-items:center;">
                <div style="width:40px; height:40px; background:#ccc; border-radius:50%; margin-right:10px; display:flex; align-items:center; justify-content:center; color:white; font-weight:bold;">S</div>
                <div>
                    <div><strong>Support IT</strong> <span style="font-size:12px; color:#555;">&lt;<span id="sender-email" class="interactive">security-check@google-account-update.com</span>&gt;</span></div>
                    <div style="font-size:12px; color:#555;">À moi <span class="material-icons" style="font-size:12px;">arrow_drop_down</span></div>
                </div>
            </div>
        </div>
        <div style="font-family: Arial, sans-serif; color: #333;">
            <p>Bonjour <span id="generic-greeting" class="interactive">utilisateur</span>,</p>
            <p>Nous avons détecté une connexion inhabituelle sur votre compte.</p>
            <p style="background-color:#fff3cd; padding:5px; border-left:4px solid #ffc107;"><span id="urgency-text" class="interactive">Action requise sous 2 heures pour éviter le blocage.</span></p>
            <br>
            <a id="fake-link-btn" class="interactive" href="#" style="background-color: #1a73e8; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px; display: inline-block;">Vérifier mon activité</a>
            <br><br>
            <p style="font-size:12px; color:#888;">Merci,<br>L'équipe Sécurité.</p>
        </div>
    `,
    "SCENARIO_2": `
        <div style="margin-bottom: 20px;">
            <h2 style="margin: 0 0 10px 0;">Facture Impayée #9928</h2>
            <div><strong>Équipe RH</strong> <span style="color:#555;">&lt;<span id="sender-hr" class="interactive">rh-service@gmail.com</span>&gt;</span></div>
        </div>
        <div>
            <p>Bonjour,</p>
            <p>Veuillez trouver ci-joint la facture pour <span id="typo-body" class="interactive">votre salair</span>.</p>
            <div style="background:#f1f3f4; padding:10px; border-radius:4px; width:200px; margin-top:20px;">
                <span class="material-icons" style="color:red; vertical-align:middle;">description</span> 
                <span id="attachment-exe" class="interactive" style="font-weight:bold;">facture.exe</span>
            </div>
        </div>
    `,
    "SCENARIO_3": `
        <div style="margin-bottom: 20px;">
            <h2 style="margin: 0 0 10px 0;">Virement Urgent - Confidentiel</h2>
            <div><strong>Michel (PDG)</strong> <span style="color:#555;">&lt;<span id="fake-ceo" class="interactive">michel.pdg@company-group.net</span>&gt;</span></div>
        </div>
        <div>
            <p>Salut,</p>
            <p>J'ai besoin d'un virement urgent pour un fournisseur. C'est confidentiel.</p>
            <p>IBAN : <span id="iban-foreign" class="interactive">LT89 3300 4400...</span> (Lituanie)</p>
            <p>Fais-le maintenant, je compte sur toi.</p>
            <p>Michel.</p>
        </div>
    `
};

// Variables d'état
let currentScenarioId = null;
let trapsFound = 0;
let totalTraps = -1; // -1 = Pas encore chargé (Empêche la victoire immédiate)
let foundIds = new Set();

// Ouvre un mail (Passe de la liste à la lecture)
async function openScenario(scenarioId) {
    currentScenarioId = scenarioId;
    
    // Reset complet
    trapsFound = 0;
    totalTraps = -1; 
    foundIds.clear();
    
    // UI : Masquer liste / Afficher lecteur
    document.getElementById('inbox-view').classList.add('hidden');
    document.getElementById('reading-view').classList.remove('hidden');
    document.getElementById('feedback-alert').classList.add('hidden');
    
    // Injecter HTML
    const contentArea = document.getElementById('email-content-area');
    contentArea.innerHTML = scenariosHTML[scenarioId] || "<p>Erreur de chargement.</p>";

    // Reset Compteur UI
    updateCounterUI();

    // Appel Backend pour savoir combien de pièges chercher
    try {
        const response = await fetch(`http://localhost:8080/api/phishing/info/${scenarioId}`);
        const data = await response.json();
        
        totalTraps = data.totalTraps;
        document.getElementById('lesson-text').textContent = data.lesson;
        
        updateCounterUI(); // Mettre à jour avec le vrai total (ex: 0 / 4)
        
        // Activer les clics
        attachInteraction();

    } catch (e) {
        console.error("Erreur API:", e);
    }
}

// Ferme le mail (Retour Inbox)
function closeEmail() {
    document.getElementById('reading-view').classList.add('hidden');
    document.getElementById('inbox-view').classList.remove('hidden');
    document.getElementById('victory-modal').classList.add('hidden');
}

// Gère les clics sur les éléments interactifs
function attachInteraction() {
    const elements = document.querySelectorAll('.interactive');
    elements.forEach(el => {
        el.addEventListener('click', async (e) => {
            e.preventDefault();
            e.stopPropagation();

            const elementId = e.target.id;
            if (foundIds.has(elementId)) return; // Déjà trouvé

            // Appel API d'analyse
            const response = await fetch('http://localhost:8080/api/phishing/analyze', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ scenarioId: currentScenarioId, elementId: elementId })
            });
            const result = await response.json();

            showFeedback(result, e.target);
        });
    });
}

// Affiche le feedback (Vert/Rouge)
function showFeedback(result, element) {
    const alertBox = document.getElementById('feedback-alert');
    alertBox.classList.remove('hidden', 'success', 'error');
    alertBox.textContent = result.message;

    if (result.isTrap) {
        // C'est un piège !
        alertBox.classList.add('success');
        element.classList.add('trap-found');
        
        if (!foundIds.has(element.id)) {
            foundIds.add(element.id);
            trapsFound++;
            updateCounterUI();
            checkVictory();
        }
    } else {
        // C'est safe (erreur utilisateur)
        alertBox.classList.add('error');
        element.classList.add('trap-safe');
        setTimeout(() => element.classList.remove('trap-safe'), 500);
    }
}

function updateCounterUI() {
    document.getElementById('found-count').textContent = trapsFound;
    document.getElementById('total-count').textContent = (totalTraps === -1) ? "?" : totalTraps;
}

function checkVictory() {
    // La victoire n'est possible que si totalTraps a été chargé (> 0)
    if (totalTraps > 0 && trapsFound >= totalTraps) {
        setTimeout(() => {
            document.getElementById('victory-modal').classList.remove('hidden');
        }, 800);
    }
}