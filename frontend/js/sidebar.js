/**
 * sidebar.js — Composant nav partagé CyberScale
 * Ce script est exécuté de façon synchrone, placé juste après
 * <div id="sidebar-placeholder"> dans chaque page HTML.
 * Il génère et injecte la sidebar en détectant la page active.
 */
(function () {
    const placeholder = document.getElementById('sidebar-placeholder');
    if (!placeholder) return;

    const currentFile = window.location.pathname.split('/').pop() || 'dashboard.html';
    const username = localStorage.getItem('userName') || 'Utilisateur';
    const extraClass = placeholder.dataset.extraClass || '';
    const showScore = placeholder.dataset.showScore === 'true';

    // Définition des éléments de nav
    const navItems = [
        { file: 'dashboard.html', icon: 'fa-chart-pie',      label: 'Tableau de bord' },
        { separator: 'Entraînement' },
        { file: 'challenges.html', icon: 'fa-flag',          label: 'Challenges CTF' },
        { file: 'arena.html',      icon: 'fa-terminal',      label: 'Training Arena' },
        { file: 'decoy.html',      icon: 'fa-ghost',         label: 'Cyber Deception' },
        { file: 'investigation.html', icon: 'fa-search',     label: 'Investigation' },
        { file: 'phishing.html',   icon: 'fa-envelope',      label: 'Phishing Sim' },
        { separator: 'Évaluation' },
        { file: 'quiz-intro.html', icon: 'fa-graduation-cap',label: 'Quiz' },
        { file: 'certifs.html',    icon: 'fa-certificate',   label: 'Certifications' },
    ];

    const ACTIVE  = 'flex items-center gap-3 px-4 py-3 bg-blue-600/10 text-blue-400 border border-blue-600/20 rounded-xl font-medium transition-all shadow-inner';
    const INACTIVE = 'flex items-center gap-3 px-4 py-3 text-slate-400 hover:bg-slate-700/50 hover:text-white rounded-xl transition-all';

    const navHTML = navItems.map(item => {
        if (item.separator) {
            return `<p class="px-4 text-[10px] font-bold text-slate-500 uppercase tracking-[2px] mt-8 mb-2">${item.separator}</p>`;
        }
        const isActive = currentFile === item.file;
        return `<a href="${item.file}" class="${isActive ? ACTIVE : INACTIVE}">
            <i class="fas ${item.icon} w-5"></i> 
            <span class="text-sm">${item.label}</span>
        </a>`;
    }).join('\n');

    // Widget score
    const scoreWidget = showScore ? `
        <div class="px-4 pb-3">
            <div class="bg-slate-900/50 rounded-xl p-3 flex items-center justify-between border border-slate-700">
                <span class="text-[10px] text-slate-500 font-bold uppercase tracking-wider">Mon Score</span>
                <span class="text-yellow-400 font-bold text-lg flex items-center gap-1">
                    <i class="fas fa-trophy"></i> <span id="userPoints">--</span>
                </span>
            </div>
        </div>` : '';

    const sidebarHTML = `
    <aside class="ds-sidebar w-64 h-full flex flex-col border-r border-slate-800 bg-[#020617] relative z-[100] ${extraClass}">
        <div class="p-6 mb-4">
            <div class="flex items-center gap-3">
                <div class="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center text-white shadow-lg shadow-blue-900/40">
                    <i class="fas fa-shield-alt"></i>
                </div>
                <span class="text-xl font-extrabold tracking-tighter text-white">Cyber<span class="text-blue-500">Scale</span></span>
            </div>
        </div>

        <nav class="flex-1 px-4 space-y-1 overflow-y-auto custom-scrollbar">
            ${navHTML}
        </nav>

        ${scoreWidget}

        <div class="p-4 mt-auto border-t border-slate-800 bg-slate-950/50">
            <div class="flex items-center gap-3 px-2">
                <div class="w-10 h-10 rounded-xl bg-slate-800 flex items-center justify-center text-slate-400 border border-slate-700 shadow-inner overflow-hidden flex-shrink-0">
                    <i class="fas fa-user-secret text-lg"></i>
                </div>
                <div class="flex-1 min-w-0">
                    <p class="text-xs font-bold text-white truncate uppercase tracking-wider" id="sidebarUsername">${username}</p>
                    <p class="text-[10px] text-slate-500 truncate flex items-center gap-1">
                        <span class="w-1.5 h-1.5 rounded-full bg-green-500 animate-pulse"></span>
                        AGREEMENT ACTIVE
                    </p>
                </div>
                <button id="logoutBtn" class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:text-red-400 hover:bg-red-400/10 transition-all flex-shrink-0">
                    <i class="fas fa-power-off text-sm"></i>
                </button>
            </div>
        </div>
    </aside>`;

    // Injection synchrone
    placeholder.outerHTML = sidebarHTML;

    // Gestion déconnexion
    document.addEventListener('DOMContentLoaded', function () {
        const btn = document.getElementById('logoutBtn');
        if (btn && !btn.dataset.bound) {
            btn.dataset.bound = 'true';
            btn.addEventListener('click', function () {
                localStorage.clear();
                window.location.href = 'index.html';
            });
        }
    });
})();
