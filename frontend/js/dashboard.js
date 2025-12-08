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
        const history = await response.json();

        // Stats simples (Compte total)
        document.getElementById('totalTests').textContent = history.length;
        // (Pour les moyennes Théorie/Technique, on peut les masquer ou les garder fixes pour l'instant
        // car le DTO simplifié ne les contient plus séparément, ce n'est pas grave pour cette version).

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
                        <span class="text-sm font-medium text-gray-600">${item.status}</span>
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