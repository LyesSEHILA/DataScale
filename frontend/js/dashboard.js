const API_URL_USER = "http://localhost:8080/api/user";

document.addEventListener("DOMContentLoaded", async () => {
    const userId = localStorage.getItem('userId');
    const username = localStorage.getItem('userName');

    if (!userId) {
        window.location.href = 'login.html';
        return;
    }

    // Affichage Pseudo
    const pseudo = username || "Agent";
    if(document.getElementById('headerPseudo')) document.getElementById('headerPseudo').textContent = pseudo;
    if(document.getElementById('sidebarUsername')) document.getElementById('sidebarUsername').textContent = pseudo;

    // Déconnexion
    const logoutBtn = document.getElementById('logoutBtn');
    if(logoutBtn) logoutBtn.addEventListener('click', () => {
        localStorage.clear();
        window.location.href = 'index.html';
    });

    // CHARGEMENT PARALLÈLE
    await Promise.all([
        loadDashboardStats(userId),
        loadHistory(userId)
    ]);
});

// 1. Charger les Statistiques Globales
async function loadDashboardStats(userId) {
    try {
        const response = await fetch(`${API_URL_USER}/${userId}/dashboard`);
        if (!response.ok) return; 
        
        const data = await response.json();

        // Animation des chiffres
        animateValue("totalTests", 0, data.totalQuizzes, 1000);
        animateValue("totalCertifs", 0, data.certifications.length, 1000);
        
        const avgTheory = Math.round(data.averageTheory || 0);
        const avgTech = Math.round(data.averageTechnique || 0);

        document.getElementById("avgTheory").textContent = avgTheory;
        document.getElementById("avgTech").textContent = avgTech;
        
        // Animation Barres
        setTimeout(() => {
            const barTheory = document.getElementById("barTheory");
            const barTech = document.getElementById("barTech");
            if(barTheory) barTheory.style.width = `${avgTheory}%`;
            if(barTech) barTech.style.width = `${avgTech}%`;
        }, 500);

        // Certifications
        const certifList = document.getElementById("certifList");
        
        if (data.certifications.length > 0) {
            certifList.innerHTML = data.certifications.map(c => `
                <li class="flex items-center gap-3 text-sm text-slate-300 bg-slate-900/50 p-2 rounded border border-yellow-500/20">
                    <i class="fas fa-medal text-yellow-500"></i> ${c}
                </li>
            `).join('');
        }

        // Ressources Recommandées
        const recoList = document.getElementById("resourcesList");
        if (data.resources && data.resources.length > 0) {
            recoList.innerHTML = data.resources.map(r => `
                <a href="${r.url || '#'}" target="_blank" class="block group">
                    <div class="flex items-start gap-3 p-3 rounded-lg hover:bg-slate-700/50 transition border border-transparent hover:border-slate-600">
                        <div class="mt-1"><i class="fas fa-book-open text-blue-400"></i></div>
                        <div>
                            <p class="text-sm font-semibold text-slate-200 group-hover:text-blue-300 transition-colors">${r.title}</p>
                            <span class="text-[10px] uppercase tracking-wider text-slate-500 border border-slate-600 px-1 rounded">${r.type || 'Conseil'}</span>
                        </div>
                    </div>
                </a>
            `).join('');
        } else {
            recoList.innerHTML = `<p class="text-sm text-slate-500 italic">Terminez un quiz pour obtenir des recommandations.</p>`;
        }

    } catch (e) {
        console.error("Erreur chargement dashboard stats:", e);
    }
}

// 2. Charger l'Historique
async function loadHistory(userId) {
    const tbody = document.getElementById('historyBody');
    if(!tbody) return;
    
    try {
        const response = await fetch(`${API_URL_USER}/${userId}/history`);
        if (!response.ok) throw new Error("Erreur API");
        const history = await response.json();

        tbody.innerHTML = "";
        
        if (history.length === 0) {
            tbody.innerHTML = `<tr><td class="p-6 text-center text-slate-500 italic">Aucune activité récente.</td></tr>`;
            return;
        }

        history.slice(0, 5).forEach(item => {
            const dateStr = new Date(item.date).toLocaleDateString('fr-FR', { day: 'numeric', month: 'short' });
            
            const isExam = item.type === 'EXAMEN';
            const badgeColor = isExam ? 'bg-purple-500/10 text-purple-400 border-purple-500/20' : 'bg-blue-500/10 text-blue-400 border-blue-500/20';
            const icon = isExam ? 'fa-file-signature' : 'fa-list-check';

            const row = `
                <tr class="hover:bg-slate-700/30 transition border-b border-slate-700/30 last:border-0">
                    <td class="px-6 py-4 whitespace-nowrap text-slate-400 font-mono text-xs">${dateStr}</td>
                    <td class="px-6 py-4 w-full">
                        <div class="flex items-center gap-3">
                            <span class="w-8 h-8 rounded-lg flex items-center justify-center border ${badgeColor}">
                                <i class="fas ${icon} text-xs"></i>
                            </span>
                            <div>
                                <p class="text-sm font-bold text-white">${item.title}</p>
                                <p class="text-xs text-slate-500">${item.type}</p>
                            </div>
                        </div>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap">
                        <span class="text-lg font-bold ${item.score >= 7 ? 'text-green-400' : 'text-orange-400'}">${item.score}</span>
                        <span class="text-xs text-slate-600">/${item.maxScore}</span>
                    </td>
                    <td class="px-6 py-4 text-right">
                        ${getStatusAction(item)}
                    </td>
                </tr>
            `;
            tbody.innerHTML += row;
        });

    } catch (error) {
        console.error(error);
        tbody.innerHTML = `<tr><td class="p-4 text-center text-red-400 text-sm">Erreur historique.</td></tr>`;
    }
}

function getStatusAction(item) {
    if (item.status === "En cours" && item.type === 'EXAMEN') {
        return `<button onclick="resumeExam(${item.id}, '${item.title}')" class="text-xs bg-orange-500/20 text-orange-400 px-3 py-1.5 rounded-lg hover:bg-orange-500/30 transition border border-orange-500/30">
            <i class="fas fa-play mr-1"></i> Reprendre
        </button>`;
    }
    return `<span class="text-xs font-medium text-slate-500 px-2 py-1 rounded bg-slate-800 border border-slate-700">${item.status}</span>`;
}

function animateValue(id, start, end, duration) {
    const obj = document.getElementById(id);
    if(!obj) return;
    let startTimestamp = null;
    const step = (timestamp) => {
        if (!startTimestamp) startTimestamp = timestamp;
        const progress = Math.min((timestamp - startTimestamp) / duration, 1);
        obj.innerHTML = Math.floor(progress * (end - start) + start);
        if (progress < 1) {
            window.requestAnimationFrame(step);
        }
    };
    window.requestAnimationFrame(step);
}

// Fonction globale pour reprendre l'examen
window.resumeExam = function(sessionId, title) {
    localStorage.setItem('examSessionId', sessionId);
    localStorage.setItem('currentExamRef', title);
    window.location.href = 'exam.html';
};