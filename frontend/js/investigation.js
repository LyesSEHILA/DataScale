document.addEventListener("DOMContentLoaded", () => {
    // Configuration
    const API_URL = "http://localhost:8080/api/challenges";
    const logsBody = document.getElementById("logsBody");
    const searchInput = document.getElementById("searchInput");
    const statusFilter = document.getElementById("statusFilter");
    
    // Variables globales
    let allLogs = [];
    let filteredLogs = [];
    let trafficChart = null; // Instance du graphique

    // Pagination
    let currentPage = 1;
    const logsPerPage = 20;

    // Lancement
    fetchLogs();

    async function fetchLogs() {
        try {
            const response = await fetch(`${API_URL}/logs`);
            if (!response.ok) throw new Error("Erreur réseau");
            
            const rawLogs = await response.json();
            allLogs = rawLogs.map(parseLogLine);
            filteredLogs = [...allLogs];
            
            // Initialiser l'interface
            updateChart(); // Générer le graphe
            renderTable();
            updatePaginationInfo();
            
        } catch (error) {
            console.error(error);
            logsBody.innerHTML = `<tr><td colspan="6" class="p-4 text-center text-red-500 font-mono">Erreur de connexion au système de logs.</td></tr>`;
        }
    }

    // Parser une ligne de log Apache
    function parseLogLine(line) {
        // Regex Apache Log Format
        const regex = /^(\S+) - - \[(.*?)\] "(.*?) (.*?) .*?" (\d+) (\d+)$/;
        const match = line.match(regex);
        
        if (match) {
            return {
                raw: line,
                ip: match[1],
                dateStr: match[2], // "10/Oct/2023:13:55:36 +0000"
                method: match[3],
                url: match[4],
                status: match[5],
                size: match[6]
            };
        }
        return { ip: "???", dateStr: "", method: "???", url: line, status: "000", size: "0" };
    }

    // --- NOUVEAU : FONCTION GRAPHIQUE ---
    function updateChart() {
        const ctx = document.getElementById('trafficChart').getContext('2d');

        // 1. Grouper par minute
        const timeMap = {};
        
        filteredLogs.forEach(log => {
            if (!log.dateStr) return;
            // Format typique: 10/Oct/2000:13:55:36 -> on garde jusqu'à la minute "13:55"
            // Une façon simple est de prendre la partie heure:minute
            // Extraction brute pour simplifier : "13:55"
            const timePart = log.dateStr.split(':')[1] + ':' + log.dateStr.split(':')[2]; 
            
            timeMap[timePart] = (timeMap[timePart] || 0) + 1;
        });

        // 2. Trier les labels (minutes)
        const labels = Object.keys(timeMap).sort();
        const dataPoints = labels.map(label => timeMap[label]);

        // 3. Détruire l'ancien chart s'il existe
        if (trafficChart) {
            trafficChart.destroy();
        }

        // 4. Créer le nouveau Chart
        trafficChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Requêtes / Minute',
                    data: dataPoints,
                    backgroundColor: 'rgba(0, 255, 65, 0.2)', // Vert Hacker transparent
                    borderColor: '#00ff41', // Vert Hacker pur
                    borderWidth: 1,
                    barPercentage: 0.6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        mode: 'index',
                        intersect: false,
                        backgroundColor: 'rgba(0,0,0,0.8)',
                        titleColor: '#00ff41',
                        bodyColor: '#fff',
                        borderColor: '#00ff41',
                        borderWidth: 1
                    }
                },
                scales: {
                    x: {
                        ticks: { color: '#00ff41', font: { family: 'Courier New' } },
                        grid: { color: 'rgba(0, 255, 65, 0.1)' }
                    },
                    y: {
                        beginAtZero: true,
                        ticks: { color: '#00ff41', font: { family: 'Courier New' } },
                        grid: { color: 'rgba(0, 255, 65, 0.1)' }
                    }
                },
                animation: {
                    duration: 1000,
                    easing: 'easeOutQuart'
                }
            }
        });
    }

    // Filtres
    function filterData() {
        const term = searchInput.value.toLowerCase();
        const status = statusFilter.value;

        filteredLogs = allLogs.filter(log => {
            const matchesText = log.ip.includes(term) || log.url.toLowerCase().includes(term);
            const matchesStatus = status === "" || log.status === status;
            return matchesText && matchesStatus;
        });

        currentPage = 1;
        renderTable();
        updatePaginationInfo();
        updateChart(); // Mettre à jour le graphique avec les données filtrées
    }

    searchInput.addEventListener("input", filterData);
    statusFilter.addEventListener("change", filterData);

    // Rendu Tableau
    function renderTable() {
        logsBody.innerHTML = "";
        const start = (currentPage - 1) * logsPerPage;
        const end = start + logsPerPage;
        const pageData = filteredLogs.slice(start, end);

        if (pageData.length === 0) {
            logsBody.innerHTML = `<tr><td colspan="6" class="p-4 text-center text-gray-500 font-mono">Aucune trace trouvée.</td></tr>`;
            return;
        }

        pageData.forEach(log => {
            // Couleurs status
            let statusClass = "text-green-400";
            if (log.status === "404") statusClass = "text-yellow-400 font-bold";
            if (log.status === "500" || log.status === "403") statusClass = "text-red-500 font-bold";

            const row = `
                <tr class="hover:bg-green-900/10 transition-colors border-b border-green-500/10">
                    <td class="p-3 font-mono text-gray-400 text-xs">${log.dateStr}</td>
                    <td class="p-3 font-mono text-blue-300">${log.ip}</td>
                    <td class="p-3 font-mono"><span class="bg-gray-800 text-gray-300 px-2 py-1 rounded text-xs">${log.method}</span></td>
                    <td class="p-3 font-mono text-gray-300 break-all">${log.url}</td>
                    <td class="p-3 font-mono ${statusClass}">${log.status}</td>
                    <td class="p-3 font-mono text-gray-500 text-xs">${log.size}</td>
                </tr>
            `;
            logsBody.innerHTML += row;
        });
    }

    // Pagination
    document.getElementById("prevBtn").addEventListener("click", () => {
        if (currentPage > 1) { currentPage--; renderTable(); updatePaginationInfo(); }
    });
    document.getElementById("nextBtn").addEventListener("click", () => {
        if (currentPage * logsPerPage < filteredLogs.length) { currentPage++; renderTable(); updatePaginationInfo(); }
    });

    function updatePaginationInfo() {
        const total = filteredLogs.length;
        const start = total === 0 ? 0 : (currentPage - 1) * logsPerPage + 1;
        const end = Math.min(currentPage * logsPerPage, total);
        document.getElementById("entriesInfo").textContent = `${start}-${end} / ${total}`;
        document.getElementById("currentPageDisplay").textContent = currentPage;
    }

    // Validation
    document.getElementById("validateIpBtn").addEventListener("click", async () => {
        const ipInput = document.getElementById("suspectIpInput");
        const resultDiv = document.getElementById("validationResult");
        const ip = ipInput.value.trim();

        if (!ip) return;

        try {
            const response = await fetch(`${API_URL}/logs/validate`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ ip: ip })
            });
            const data = await response.json();

            if (data.success) {
                resultDiv.innerHTML = `<div class="p-3 bg-green-900/50 border border-green-500 text-green-400 rounded text-sm font-mono mt-2">✅ ${data.message}</div>`;
            } else {
                resultDiv.innerHTML = `<div class="p-3 bg-red-900/50 border border-red-500 text-red-400 rounded text-sm font-mono mt-2">❌ ${data.message}</div>`;
            }
        } catch (e) {
            resultDiv.innerHTML = `<div class="text-yellow-500 text-sm mt-2">⚠️ Erreur de connexion.</div>`;
        }
    });
});