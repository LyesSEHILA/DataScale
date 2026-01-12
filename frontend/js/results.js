const API_URL = "http://localhost:8080/api/quiz";

document.addEventListener("DOMContentLoaded", async () => {
    const sessionId = localStorage.getItem('quizSessionId') || "demo_mode"; // Fallback pour tester sans login

    try {
        let data;

        // 1. Essai de récupération des vraies données
        try {
            const response = await fetch(`${API_URL}/results?sessionId=${sessionId}`);
            if (!response.ok) throw new Error("Erreur serveur");
            data = await response.json();
        } catch (serverError) {
            console.warn("Passage en mode Démo (Backend inaccessible) :", serverError);
            // Données de secours pour que la 3D s'affiche quand même
            data = {
                scoreTheory: 7.5,
                scoreTechnique: 8.2,
                recommendations: [
                    { type: 'Conseil', title: 'Approfondir le Modèle OSI', url: '#' },
                    { type: 'Outil', title: 'Scanner avec Nmap', url: '#' },
                    { type: 'Ressource', title: 'Guide OWASP Top 10', url: '#' }
                ]
            };
        }
        
        // 2. Mise à jour des textes
        document.getElementById("scoreTheoryDisplay").textContent = data.scoreTheory.toFixed(1);
        document.getElementById("scoreTechDisplay").textContent = data.scoreTechnique.toFixed(1);

        // 3. Calcul du Score Global pour l'axe Z (Hauteur)
        const globalScore = (data.scoreTheory + data.scoreTechnique) / 2;

        // 4. Lancement du GRAPHIQUE 3D
        render3DChart(data.scoreTheory, data.scoreTechnique, globalScore);

        // 5. Afficher les recommandations
        renderRecommendations(data.recommendations);

    } catch (error) {
        console.error("Erreur critique:", error);
        alert("Impossible de charger l'application.");
    }
});

function render3DChart(theory, tech, global) {
    const chartDom = document.getElementById('resultsChart');
    if (!chartDom) return;

    // Initialisation du moteur ECharts
    const myChart = echarts.init(chartDom, 'dark'); // Thème sombre natif

    const option = {
        backgroundColor: 'rgba(0,0,0,0)', // Fond transparent (pour voir le dégradé CSS)
        tooltip: {},
        visualMap: {
            show: false,
            dimension: 2, // La couleur change selon l'axe Z (Score Global)
            min: 0,
            max: 10,
            inRange: {
                color: ['#ef4444', '#f59e0b', '#10b981', '#3b82f6'] // Rouge -> Jaune -> Vert -> Bleu
            }
        },
        xAxis3D: {
            type: 'value',
            name: 'Théorie',
            min: 0, max: 10,
            axisLine: { lineStyle: { color: '#94a3b8' } }
        },
        yAxis3D: {
            type: 'value',
            name: 'Technique',
            min: 0, max: 10,
            axisLine: { lineStyle: { color: '#94a3b8' } }
        },
        zAxis3D: {
            type: 'value',
            name: 'Global',
            min: 0, max: 10,
            axisLine: { lineStyle: { color: '#94a3b8' } }
        },
        grid3D: {
            viewControl: {
                autoRotate: true,       // Ça tourne tout seul !
                autoRotateSpeed: 10,
                alpha: 20,              // Angle vertical
                beta: 40,               // Angle horizontal
                distance: 240,          // Zoom
                panMouseButton: 'left',
                rotateMouseButton: 'left'
            },
            boxWidth: 100,
            boxDepth: 100,
            boxHeight: 80,
            light: {
                main: {
                    intensity: 1.2,
                    shadow: true,
                    shadowQuality: 'high'
                },
                ambient: {
                    intensity: 0.3
                }
            },
            environment: '#0f172a' // Couleur de fond de la scène 3D
        },
        series: [
            {
                type: 'scatter3D',
                name: 'Mon Profil',
                data: [[theory, tech, global]],
                symbolSize: 50, // Grosse boule pour bien voir
                itemStyle: {
                    borderWidth: 2,
                    borderColor: '#fff',
                    opacity: 0.9
                },
                label: {
                    show: true,
                    formatter: 'MOI',
                    textStyle: {
                        fontSize: 20,
                        fontWeight: 'bold',
                        color: '#fff',
                        backgroundColor: 'rgba(0,0,0,0.5)',
                        padding: [4, 8],
                        borderRadius: 4
                    },
                    position: 'top'
                },
                emphasis: {
                    label: {
                        show: true,
                        fontSize: 24
                    }
                }
            },
            // Ajout d'un point "Cible Expert" pour comparer
            {
                type: 'scatter3D',
                name: 'Objectif Expert',
                data: [[9, 9, 9]],
                symbol: 'diamond',
                symbolSize: 30,
                itemStyle: {
                    color: 'rgba(255, 255, 255, 0.2)', // Translucide
                    borderWidth: 1,
                    borderColor: '#10b981'
                },
                label: {
                    show: true,
                    formatter: 'EXPERT',
                    textStyle: { fontSize: 12, color: '#10b981' }
                }
            }
        ]
    };

    myChart.setOption(option);

    // Responsive : redimensionne le chart si on change la taille de la fenêtre
    window.addEventListener('resize', function() {
        myChart.resize();
    });
}

function renderRecommendations(recos) {
    const container = document.getElementById("recommendationsList");
    
    if (!recos || recos.length === 0) {
        container.innerHTML = "<p class='text-slate-500 col-span-3 text-center'>Aucune recommandation spécifique.</p>";
        return;
    }

    container.innerHTML = recos.map(reco => `
        <div class="bg-slate-800 border border-slate-700 rounded-xl p-6 hover:border-blue-500/50 transition-all shadow-lg flex flex-col justify-between h-full group hover:-translate-y-1">
            <div>
                <span class="inline-block bg-slate-700 text-blue-300 text-xs font-bold px-2 py-1 rounded mb-3 uppercase tracking-wider border border-slate-600">
                    ${reco.type || 'Conseil'}
                </span>
                <h4 class="text-lg font-bold text-white mb-2 group-hover:text-blue-400 transition-colors">
                    ${reco.title}
                </h4>
            </div>
            <a href="${reco.url || '#'}" target="_blank" 
               class="mt-4 inline-flex items-center text-blue-400 hover:text-blue-300 font-semibold text-sm transition-colors">
               Voir la ressource <i class="fas fa-external-link-alt ml-2 text-xs"></i>
            </a>
        </div>
    `).join('');
}