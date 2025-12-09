const API_CHALLENGES = "http://localhost:8080/api/challenges";

document.addEventListener("DOMContentLoaded", () => {

    const username = localStorage.getItem('userName') || "Utilisateur";
    const sidebarUser = document.getElementById('sidebarUsername');
    if(sidebarUser) sidebarUser.textContent = username;

    const logoutBtn = document.getElementById('logoutBtn');
    if(logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            localStorage.clear();
            window.location.href = 'index.html';
        });
    }

    loadChallenges();
});

async function loadChallenges() {
    const grid = document.getElementById("challengesGrid");

    try {
        const response = await fetch(API_CHALLENGES);
        if (!response.ok) throw new Error("Erreur serveur");
        
        const challenges = await response.json();
        renderChallenges(challenges, grid);

    } catch (error) {
        console.error(error);
        grid.innerHTML = `<div class="col-span-3 text-center text-red-500 bg-red-50 p-4 rounded-lg">Impossible de charger les challenges. Vérifiez que le backend est lancé.</div>`;
    }
}

function renderChallenges(challenges, container) {
    if (!challenges || challenges.length === 0) {
        container.innerHTML = `<p class="col-span-3 text-gray-500 text-center">Aucun challenge disponible pour le moment.</p>`;
        return;
    }

    container.innerHTML = challenges.map(challenge => {
        let headerColor = "bg-green-600";
        let badgeColor = "bg-green-500 text-white";
        let icon = "🧩"; 

        if (challenge.difficulty === "MOYEN") {
            headerColor = "bg-yellow-500";
            badgeColor = "bg-yellow-400 text-yellow-900";
            icon = "⚡";
        } else if (challenge.difficulty === "HARDCORE") {
            headerColor = "bg-red-600";
            badgeColor = "bg-red-500 text-white";
            icon = "💀";
        }

        const isValidated = challenge.isValidated;
        const btnClass = isValidated 
            ? "bg-green-50 text-green-700 border border-green-200 cursor-default" 
            : "bg-gray-900 text-white hover:bg-gray-800 shadow-lg transition transform hover:-translate-y-0.5";
        
        const btnText = isValidated ? "Challenge Validé ✅" : "Lancer le défi";
        const btnAction = isValidated ? "" : `onclick="startChallenge(${challenge.id}, '${challenge.title}')"`;
        
        const validatedBadge = isValidated 
            ? `<span class="absolute top-4 left-4 bg-white/90 text-green-700 text-xs font-bold px-2 py-1 rounded shadow-sm flex items-center gap-1"><i class="fas fa-check-circle"></i> FAIT</span>` 
            : "";

        // --- Template HTML ---
        return `
            <div class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-md transition relative flex flex-col h-full group">
                
                <div class="h-32 ${headerColor} flex items-center justify-center relative shrink-0">
                    <span class="text-4xl drop-shadow-md group-hover:scale-110 transition duration-300">${icon}</span>
                    ${validatedBadge}
                    <div class="absolute top-4 right-4 ${badgeColor} text-xs font-bold px-2 py-1 rounded uppercase tracking-wider shadow-sm">
                        ${challenge.difficulty}
                    </div>
                </div>

                <div class="p-6 flex flex-col flex-1">
                    <h3 class="text-xl font-bold text-gray-900 mb-2 line-clamp-1" title="${challenge.title}">
                        ${challenge.title}
                    </h3>
                    <p class="text-sm text-gray-500 mb-4 flex-1 line-clamp-3">
                        ${challenge.description}
                    </p>
                    
                    <div class="flex items-center gap-2 mb-6">
                        <span class="inline-flex items-center gap-1 bg-blue-50 text-blue-700 px-3 py-1 rounded-full text-xs font-bold">
                            <i class="fas fa-gem"></i> ${challenge.points} pts
                        </span>
                    </div>

                    <button ${btnAction} class="w-full font-bold py-3 rounded-xl flex items-center justify-center gap-2 ${btnClass}">
                        ${btnText}
                    </button>
                </div>
            </div>
        `;
    }).join('');
}

// Fonction pour démarrer un challenge (Placeholder pour l'instant)
function startChallenge(id, title) {
    console.log(`Démarrage du challenge ID ${id}: ${title}`);
    alert(`🚀 Lancement du challenge : ${title}\n\n(Cette fonctionnalité sera connectée au moteur de jeu prochainement)`);
    // Ici, on pourrait rediriger vers une page spécifique : window.location.href = `play-challenge.html?id=${id}`;
}