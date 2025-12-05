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
    document.getElementById('headerPseudo').textContent = pseudo;
    document.getElementById('sidebarUsername').textContent = pseudo;

    // 3. Chargement des données
    await loadHistory(userId);

    // 4. Déconnexion
    document.getElementById('logoutBtn').addEventListener('click', () => {
        localStorage.clear();
        window.location.href = 'index.html';
    });
});

async function loadHistory(userId) {
    const tbody = document.getElementById('historyBody');
    
    try {
        const response = await fetch(`${API_URL_USER}/${userId}/history`);
        if (!response.ok) throw new Error("Erreur API");
        const sessions = await response.json();

        // --- Calculs Statistiques ---
        const total = sessions.length;
        document.getElementById('totalTests').textContent = total;

        if (total > 0) {
            // Calcul des moyennes (si données dispos)
            const sumTheory = sessions.reduce((acc, s) => acc + (s.finalScoreTheory || 0), 0);
            const sumTech = sessions.reduce((acc, s) => acc + (s.finalScoreTechnique || 0), 0);
            
            document.getElementById('avgTheory').textContent = (sumTheory / total).toFixed(1) + "/10";
            document.getElementById('avgTech').textContent = (sumTech / total).toFixed(1) + "/10";
        } else {
            document.getElementById('avgTheory').textContent = "-";
            document.getElementById('avgTech').textContent = "-";
        }

        // --- Remplissage Tableau ---
        tbody.innerHTML = "";
        
        if (total === 0) {
            tbody.innerHTML = `<tr><td colspan="4" class="px-6 py-8 text-center text-gray-500 italic">Aucun quiz passé pour le moment. Lancez-vous !</td></tr>`;
            return;
        }

        sessions.forEach(session => {
            const dateObj = new Date(session.createdAt);
            const dateStr = dateObj.toLocaleDateString('fr-FR', { day: 'numeric', month: 'short', year: 'numeric' });
            const timeStr = dateObj.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });

            const thScore = session.finalScoreTheory != null ? session.finalScoreTheory.toFixed(1) : "-";
            const teScore = session.finalScoreTechnique != null ? session.finalScoreTechnique.toFixed(1) : "-";

            const row = `
                <tr class="hover:bg-gray-50 transition">
                    <td class="px-6 py-4 text-gray-900">
                        <div class="font-medium">${dateStr}</div>
                        <div class="text-xs text-gray-400">${timeStr}</div>
                    </td>
                    <td class="px-6 py-4">
                        <span class="${getBadgeClass(session.finalScoreTheory)}">${thScore}</span>
                    </td>
                    <td class="px-6 py-4">
                        <span class="${getBadgeClass(session.finalScoreTechnique)}">${teScore}</span>
                    </td>
                    <td class="px-6 py-4 text-right">
                        <a href="#" onclick="viewResult(${session.id})" class="text-blue-600 hover:text-blue-800 font-medium text-sm hover:underline">Voir les détails</a>
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

function getBadgeClass(score) {
    const base = "inline-block px-2.5 py-0.5 rounded-full text-xs font-bold";
    if (score == null) return `${base} bg-gray-100 text-gray-500`;
    if (score >= 7) return `${base} bg-green-100 text-green-700`;
    if (score >= 4) return `${base} bg-yellow-100 text-yellow-700`;
    return `${base} bg-red-100 text-red-700`;
}

window.viewResult = function(sessionId) {
    localStorage.setItem('quizSessionId', sessionId);
    window.location.href = 'results.html';
};