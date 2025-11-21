const API_URL = "http://localhost:8080/api/quiz";

document.addEventListener("DOMContentLoaded", async () => {
    const sessionId = localStorage.getItem('quizSessionId');
    if (!sessionId) {
        window.location.href = 'index.html';
        return;
    }

    try {
        // 1. Récupérer les résultats
        const response = await fetch(`${API_URL}/results?sessionId=${sessionId}`);
        if (!response.ok) throw new Error("Erreur serveur");
        
        const data = await response.json();
        
        // 2. Mettre à jour les textes
        document.getElementById("scoreTheoryDisplay").textContent = data.scoreTheory.toFixed(1) + "/10";
        document.getElementById("scoreTechDisplay").textContent = data.scoreTechnique.toFixed(1) + "/10";

        // 3. Dessiner le graphique (F3)
        renderChart(data.scoreTheory, data.scoreTechnique);

        // 4. Afficher les recommandations (F4)
        renderRecommendations(data.recommendations);

    } catch (error) {
        console.error(error);
        alert("Impossible de charger les résultats.");
    }
});

function renderChart(theory, tech) {
    const ctx = document.getElementById('resultsChart').getContext('2d');
    
    new Chart(ctx, {
        type: 'scatter', // Type "Nuage de points"
        data: {
            datasets: [{
                label: 'Votre Profil',
                data: [{ x: theory, y: tech }], // Ton point unique
                backgroundColor: '#3498db', // Bleu
                pointRadius: 10, // Gros point
                pointHoverRadius: 12
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: {
                    type: 'linear',
                    position: 'bottom',
                    min: 0, max: 10,
                    title: { display: true, text: 'Compétences THÉORIQUES' }
                },
                y: {
                    min: 0, max: 10,
                    title: { display: true, text: 'Compétences TECHNIQUES' }
                }
            },
            plugins: {
                legend: { display: true }
            }
        }
    });
}

function renderRecommendations(recos) {
    const container = document.getElementById("recommendationsList");
    
    if (!recos || recos.length === 0) {
        container.innerHTML = "<p>Aucune recommandation spécifique.</p>";
        return;
    }

    container.innerHTML = recos.map(reco => `
        <div class="reco-card">
            <span class="reco-type">${reco.type}</span>
            <h4>${reco.title}</h4>
            <a href="${reco.url}" target="_blank" class="reco-link">Voir la ressource →</a>
        </div>
    `).join('');
}