document.addEventListener("DOMContentLoaded", () => {
    const API_URL = "http://localhost:8080/api/challenges";
    const INTEL_API_URL = "http://localhost:8080/api/intelligence";
    const logsBody = document.getElementById("logsBody");
    const searchInput = document.getElementById("searchInput");
    const statusFilter = document.getElementById("statusFilter");
    
    let allLogs = [];
    let filteredLogs = [];
    let trafficChart = null; 

    let currentPage = 1;
    const logsPerPage = 20;

    fetchLogs();

    async function fetchLogs() {
        try {
            const response = await fetch(`${API_URL}/logs`);
            if (!response.ok) throw new Error("Erreur réseau");
            
            const rawLogs = await response.json();
            allLogs = rawLogs.map(parseLogLine);
            filteredLogs = [...allLogs];
            
            updateChart(); 
            renderTable();
            updatePaginationInfo();
            
        } catch (error) {
            console.error(error);
            logsBody.innerHTML = `<tr><td colspan="6" class="p-4 text-center text-red-500 font-mono">Erreur de connexion au système de logs.</td></tr>`;
        }
    }

    function parseLogLine(line) {
        const regex = /^(\S+) - - \[(.*?)\] "(.*?) (.*?) .*?" (\d+) (\d+)$/;
        const match = line.match(regex);
        
        if (match) {
            return {
                raw: line,
                ip: match[1],
                dateStr: match[2],
                method: match[3],
                url: match[4],
                status: match[5],
                size: match[6]
            };
        }
        return { ip: "???", dateStr: "", method: "???", url: line, status: "000", size: "0" };
    }

    function updateChart() {
        const ctx = document.getElementById('trafficChart').getContext('2d');
        const timeMap = {};
        
        filteredLogs.forEach(log => {
            if (!log.dateStr) return;
            const timePart = log.dateStr.split(':')[1] + ':' + log.dateStr.split(':')[2]; 
            timeMap[timePart] = (timeMap[timePart] || 0) + 1;
        });

        const labels = Object.keys(timeMap).sort();
        const dataPoints = labels.map(label => timeMap[label]);

        if (trafficChart) trafficChart.destroy();

        trafficChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Requêtes / Minute',
                    data: dataPoints,
                    backgroundColor: 'rgba(0, 255, 65, 0.2)',
                    borderColor: '#00ff41',
                    borderWidth: 1,
                    barPercentage: 0.6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    x: { ticks: { color: '#00ff41', font: { family: 'Courier New' } }, grid: { color: 'rgba(0, 255, 65, 0.1)' } },
                    y: { beginAtZero: true, ticks: { color: '#00ff41', font: { family: 'Courier New' } }, grid: { color: 'rgba(0, 255, 65, 0.1)' } }
                }
            }
        });
    }

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
        updateChart(); 
    }

    searchInput.addEventListener("input", filterData);
    statusFilter.addEventListener("change", filterData);

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


    const analyzeIpBtn = document.getElementById("analyzeIpBtn");
    const threatIntelResult = document.getElementById("threatIntelResult");
    const validateIpBtn = document.getElementById("validateIpBtn");

    analyzeIpBtn.addEventListener("click", async () => {
        const ipInput = document.getElementById("suspectIpInput").value.trim();
        if (!ipInput) return;

        analyzeIpBtn.innerHTML = '<i class="fas fa-circle-notch fa-spin"></i> En cours...';
        analyzeIpBtn.disabled = true;
        validateIpBtn.classList.add("hidden");
        threatIntelResult.classList.remove("hidden");
        threatIntelResult.innerHTML = '<div class="text-blue-400 text-sm animate-pulse mt-4"><i class="fas fa-satellite-dish mr-2"></i>Interrogation de la base Threat Intelligence globale...</div>';
        document.getElementById("validationResult").innerHTML = "";

        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`${INTEL_API_URL}/analyze-ip?ip=${ipInput}`, {
                headers: token ? { "Authorization": `Bearer ${token}` } : {}
            });

            if (!response.ok) throw new Error("Erreur de l'API Intelligence");
            
            const data = await response.json();
            
            let scoreColor = "text-green-500";
            let progressColor = "bg-green-500";
            let threatLevel = "Sûr";

            if (data.abuseConfidenceScore > 40) { 
                scoreColor = "text-orange-500"; 
                progressColor = "bg-orange-500"; 
                threatLevel = "Suspect";
            }
            if (data.abuseConfidenceScore > 75) { 
                scoreColor = "text-red-500"; 
                progressColor = "bg-red-500"; 
                threatLevel = "Critique";
            }

            const flagUrl = data.countryCode && data.countryCode !== "LOCAL" 
                ? `https://flagcdn.com/20x15/${data.countryCode.toLowerCase()}.png` 
                : `https://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/World_Flag_%282004%29.svg/20px-World_Flag_%282004%29.svg.png`;

            threatIntelResult.innerHTML = `
                <div class="p-4 bg-slate-800 border border-slate-700 rounded-lg text-sm font-mono mt-4 shadow-lg relative overflow-hidden">
                    <div class="absolute top-0 right-0 w-32 h-32 ${progressColor} opacity-5 rounded-full blur-2xl -mr-10 -mt-10"></div>
                    
                    <h4 class="text-blue-400 font-bold mb-3 border-b border-slate-700 pb-2 flex items-center gap-2">
                        <i class="fas fa-shield-virus"></i> Rapport d'Intelligence
                    </h4>
                    
                    <div class="grid grid-cols-2 gap-3 relative z-10">
                        <div class="text-slate-400">Cible Analysée :</div>
                        <div class="text-white font-bold text-right tracking-wider">${data.ipAddress}</div>
                        
                        <div class="text-slate-400">Localisation (GeoIP) :</div>
                        <div class="text-white text-right flex justify-end items-center gap-2">
                            ${data.countryCode} <img src="${flagUrl}" class="rounded-sm border border-slate-600" alt="${data.countryCode}">
                        </div>
                        
                        <div class="text-slate-400">Type d'Infrastructure :</div>
                        <div class="text-white text-right">${data.usageType}</div>
                    </div>

                    <div class="mt-5 bg-slate-900/50 p-3 rounded border border-slate-700/50 relative z-10">
                        <div class="flex justify-between text-xs mb-2">
                            <span class="text-slate-400">Score de Menace Communautaire :</span>
                            <span class="font-bold ${scoreColor} text-sm">${data.abuseConfidenceScore}% (${threatLevel})</span>
                        </div>
                        <div class="w-full bg-slate-950 rounded-full h-2.5 border border-slate-800 overflow-hidden">
                            <div class="h-full rounded-full ${progressColor} transition-all duration-1000 ease-out" style="width: ${data.abuseConfidenceScore}%"></div>
                        </div>
                    </div>
                </div>
            `;

            if (data.abuseConfidenceScore > 0) {
                validateIpBtn.classList.remove("hidden");
            }

        } catch (e) {
            threatIntelResult.innerHTML = `<div class="p-3 bg-red-900/20 border border-red-500 text-red-400 rounded text-sm font-mono mt-4"><i class="fas fa-exclamation-triangle mr-2"></i>Le service Threat Intelligence est indisponible ou l'IP est invalide.</div>`;
        } finally {
            analyzeIpBtn.innerHTML = '<i class="fas fa-search"></i> Analyser';
            analyzeIpBtn.disabled = false;
        }
    });

    validateIpBtn.addEventListener("click", async () => {
        const ipInput = document.getElementById("suspectIpInput").value.trim();
        const resultDiv = document.getElementById("validationResult");

        try {
            const response = await fetch(`${API_URL}/logs/validate`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ ip: ipInput })
            });
            const data = await response.json();

            if (data.success) {
                resultDiv.innerHTML = `<div class="p-3 bg-green-900/50 border border-green-500 text-green-400 rounded text-sm font-mono mt-3"><i class="fas fa-check-circle mr-2"></i>${data.message} - Menace bloquée au niveau du Pare-feu.</div>`;
                validateIpBtn.classList.add("hidden");
            } else {
                resultDiv.innerHTML = `<div class="p-3 bg-red-900/50 border border-red-500 text-red-400 rounded text-sm font-mono mt-3"><i class="fas fa-times-circle mr-2"></i>${data.message}</div>`;
            }
        } catch (e) {
            resultDiv.innerHTML = `<div class="text-yellow-500 text-sm mt-3 font-mono">⚠️ Erreur lors de la configuration du pare-feu.</div>`;
        }
    });
});