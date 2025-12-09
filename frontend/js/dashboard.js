const API_URL_USER = "http://localhost:8080/api/user";

document.addEventListener("DOMContentLoaded", async () => {
    // 1. Sécurité
    const userId = localStorage.getItem('userId');
    const username = localStorage.getItem('userName');

    if (!userId) {
        window.location.href = 'login.html';
        return;
    }

    // 2. Affichage des pseudos
    const pseudo = username || "Utilisateur";
    const headerPseudo = document.getElementById('headerPseudo');
    const sidebarUsername = document.getElementById('sidebarUsername');
    
    if(headerPseudo) headerPseudo.textContent = pseudo;
    if(sidebarUsername) sidebarUsername.textContent = pseudo;

    // 3. Chargement des données
    await loadHistory(userId);

    // 4. Déconnexion
    const logoutBtn = document.getElementById('logoutBtn');
    if(logoutBtn){
        logoutBtn.addEventListener('click', () => {
            localStorage.clear();
            window.location.href = 'index.html';
        });
    }
});

async function loadHistory(userId) {
    const tbody = document.getElementById('historyBody');
    if(!tbody) return;
    
    try {
        const response = await fetch(`${API_URL_USER}/${userId}/history`);
        if (!response.ok) throw new Error("Erreur API");
        const history = await response.json();

        // Stats simples (Compte total)
        const totalTestsElem = document.getElementById('totalTests');
        if(totalTestsElem) totalTestsElem.textContent = history.length;

        tbody.innerHTML = "";
        
        if (history.length === 0) {
            tbody.innerHTML = `<tr><td colspan="4" class="px-6 py-8 text-center text-gray-500 italic">Aucune activité pour le moment.</td></tr>`;
            return;
        }

        history.forEach(item => {
            const dateObj = new Date(item.date);
            const dateStr = dateObj.toLocaleDateString('fr-FR', { day: 'numeric', month: 'short' });
            
            // Badge pour le TYPE (Quiz vs Examen)
            const typeBadge = item.type === 'EXAMEN' 
                ? `<span class="bg-purple-100 text-purple-700 px-2 py-1 rounded text-xs font-bold">EXAMEN</span>`
                : `<span class="bg-blue-100 text-blue-700 px-2 py-1 rounded text-xs font-bold">QUIZ</span>`;

            // Badge pour le SCORE
            let scoreColor = "text-gray-700";
            if (item.maxScore > 0) {
                const percent = item.score / item.maxScore;
                if(percent >= 0.7) scoreColor = "text-green-600 font-bold";
                else if(percent < 0.5) scoreColor = "text-red-600 font-bold";
            }

            // --- LOGIQUE BOUTON REPRENDRE ---
            let actionHtml = '';
            
            if (item.status === "En cours" && item.type === 'EXAMEN') {
                // Bouton interactif pour reprendre
                actionHtml = `
                    <button onclick="resumeExam(${item.id}, '${item.title}')" 
                        class="bg-orange-100 text-orange-700 px-3 py-1 rounded-lg text-xs font-bold hover:bg-orange-200 transition flex items-center gap-1">
                        <i class="fas fa-play"></i> Reprendre
                    </button>
                `;
            } else {
                // Texte simple pour les statuts finaux
                let statusClass = "text-gray-600";
                if(item.status.includes("Validé")) statusClass = "text-green-600 font-bold";
                if(item.status.includes("Échoué")) statusClass = "text-red-600 font-bold";
                
                actionHtml = `<span class="text-sm font-medium ${statusClass}">${item.status}</span>`;
            }
            // -------------------------------

            const row = `
                <tr class="hover:bg-gray-50 transition border-b border-gray-100">
                    <td class="px-6 py-4 text-gray-500 font-medium">
                        ${dateStr}
                    </td>
                    <td class="px-6 py-4">
                        <div class="flex items-center gap-2">
                            ${typeBadge}
                            <span class="text-gray-900">${item.title}</span>
                        </div>
                    </td>
                    <td class="px-6 py-4 ${scoreColor} text-lg">
                        ${item.score} <span class="text-xs text-gray-400 font-normal">/ ${item.maxScore}</span>
                    </td>
                    <td class="px-6 py-4">
                        ${actionHtml}
                    </td>
                </tr>
            `;
            tbody.innerHTML += row;
        });

    } catch (error) {
        console.error(error);
        tbody.innerHTML = `<tr><td colspan="4" class="px-6 py-4 text-center text-red-500">Impossible de charger l'historique.</td></tr>`;
    }
}

// Fonction appelée quand on clique sur "Reprendre"
window.resumeExam = function(sessionId, title) {
    localStorage.setItem('examSessionId', sessionId);
    localStorage.setItem('currentExamRef', title);
    window.location.href = 'exam.html';
};

// Fonction optionnelle pour voir le détail d'un résultat fini (Quiz)
window.viewResult = function(sessionId) {
    localStorage.setItem('quizSessionId', sessionId);
    window.location.href = 'results.html';
};