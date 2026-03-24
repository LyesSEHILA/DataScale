// Variables d'état
let currentScenarioId = null;
let trapsFound = 0;
let totalTraps = -1; 
let foundIds = new Set();

const API_PHISHING = "http://localhost:8080/api/phishing";

document.addEventListener('DOMContentLoaded', () => {
    loadInbox();
});

async function loadInbox() {
    try {
        const response = await fetch(`${API_PHISHING}/inbox`);
        if (!response.ok) return;

        const emails = await response.json();
        const emailList = document.querySelector('.email-list');
        emailList.innerHTML = "";

        if (!Array.isArray(emails)) return;

        emails.sort((a, b) => b.id - a.id).forEach(email => {
            const row = `
                <div class="email-row unread" id="email-row-${email.id}" onclick="openScenario(${email.id})">
                    <div class="col-check"><span class="material-icons">check_box_outline_blank</span></div>
                    <div class="col-star"><span class="material-icons">star_border</span></div>
                    <div class="col-sender">${(email.sender || "Inconnu").split('<')[0].trim()}</div>
                    <div class="col-content">
                        <span class="subject">${email.subject || "(Pas d'objet)"}</span>
                        <span class="separator">-</span>
                        <span class="snippet">${(email.contentHtml || "").replace(/<[^>]*>?/gm, '').substring(0, 60)}...</span>
                    </div>
                    <div class="col-date">À l'instant</div>
                </div>
            `;
            emailList.innerHTML += row;
        });
    } catch (e) {
        console.error("Erreur loadInbox:", e);
    }
}

async function openScenario(scenarioId) {
    currentScenarioId = scenarioId;
    trapsFound = 0;
    totalTraps = -1; 
    foundIds.clear();
    
    document.getElementById('inbox-view').classList.add('hidden');
    document.getElementById('reading-view').classList.remove('hidden');
    document.getElementById('feedback-alert').classList.add('hidden');

    const row = document.getElementById(`email-row-${scenarioId}`);
    if (row) {
        row.classList.remove('unread');
        row.classList.add('read');
    }
    
    try {
        const response = await fetch(`${API_PHISHING}/info/${scenarioId}`);
        if (!response.ok) return;

        const data = await response.json();
        const contentArea = document.getElementById('email-content-area');
        
        const sender = data.sender || "Inconnu <inconnu@fake.com>";
        const senderInitial = sender[0];
        const senderName = sender.split('<')[0];
        const senderMail = sender.includes('<') ? sender.split('<')[1].replace('>', '') : sender;

        contentArea.innerHTML = `
            <div style="margin-bottom: 20px;">
                <h2 style="margin: 0 0 10px 0; font-size: 22px;">${data.subject || "(Pas d'objet)"}</h2>
                <div style="display:flex; align-items:center;">
                    <div style="width:40px; height:40px; background:#1a73e8; border-radius:50%; margin-right:10px; display:flex; align-items:center; justify-content:center; color:white; font-weight:bold;">${senderInitial}</div>
                    <div>
                        <div><strong>${senderName}</strong> <span id="sender-email" class="interactive" style="font-size:12px; color:#555;">&lt;${senderMail}&gt;</span></div>
                        <div style="font-size:12px; color:#555;">À moi</div>
                    </div>
                </div>
            </div>
            <div class="email-body-text" style="color: #202124;">${data.contentHtml || "<p><i>Contenu vide</i></p>"}</div>
        `;

        totalTraps = data.totalTraps || 0;
        document.getElementById('lesson-text').textContent = data.lesson || "Méfiez-vous des emails suspects.";
        
        updateCounterUI();
        attachInteraction();

    } catch (e) {
        console.error("Erreur openScenario:", e);
    }
}

function attachInteraction() {
    const elements = document.querySelectorAll('.interactive');
    elements.forEach(el => {
        el.addEventListener('click', async (e) => {
            e.preventDefault();
            const target = e.currentTarget;
            const elementId = target.id;
            
            if (!elementId || foundIds.has(elementId)) return;

            try {
                const response = await fetch(`${API_PHISHING}/analyze`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ scenarioId: currentScenarioId, elementId: elementId })
                });
                if (response.ok) {
                    const result = await response.json();
                    showFeedback(result, target);
                }
            } catch (err) {
                console.error("Erreur d'analyse:", err);
            }
        });
    });
}

function showFeedback(result, element) {
    const alertBox = document.getElementById('feedback-alert');
    alertBox.classList.remove('hidden', 'success', 'error');
    alertBox.textContent = result.message;

    if (result.isTrap) {
        alertBox.classList.add('success');
        element.classList.add('trap-found');
        
        if (!foundIds.has(element.id)) {
            foundIds.add(element.id);
            trapsFound++;
            updateCounterUI();
            checkVictory();
        }
    } else {
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
    if (totalTraps > 0 && trapsFound >= totalTraps) {
        const scanOverlay = document.getElementById('scan-overlay');
        scanOverlay.classList.remove('hidden');
        setTimeout(() => {
            scanOverlay.classList.add('hidden');
            document.getElementById('victory-modal').classList.remove('hidden');
        }, 3000); 
    }
}

function closeEmail() {
    document.getElementById("reading-view").classList.add("hidden");
    document.getElementById("inbox-view").classList.remove("hidden");
    document.getElementById("feedback-alert").classList.add("hidden");
    document.getElementById("victory-modal").classList.add("hidden");
    document.getElementById("scan-overlay").classList.add("hidden");
}