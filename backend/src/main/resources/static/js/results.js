// URL de l'API pour récupérer les résultats
const API_URL_RESULTS = 'http://localhost:8080/api/quiz/results';

/**
 * Tâche 5.1 (Action 4): Dessine le graphique Scatter (nuage de points)
 * Affiche le point unique de l'utilisateur (X=Théorie, Y=Technique)
 */
const renderChart = (scoreTheory, scoreTechnique) => {
    // Tâche 5.1 (Action 3): Cible le canvas
    const ctx = document.getElementById('resultsChart').getContext('2d');
    
    // [Image of a 2D scatter plot]
    new Chart(ctx, {
        type: 'scatter', // Tâche 5.1: Type "Scatter"
        data: {
            datasets: [{
                label: 'Votre Profil',
                // Tâche 5.1: Un seul point (X=Théorie, Y=Technique)
                data: [{
                    x: scoreTheory,
                    y: scoreTechnique
                }],
                backgroundColor: 'rgb(16, 185, 129)', // Vert CyberScale
                borderColor: 'rgb(16, 185, 129)',
                pointRadius: 10,
                pointHoverRadius: 12
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: {
                    title: {
                        display: true,
                        text: 'Score Théorique (Concepts & Lois)',
                        font: { size: 14, weight: 'bold' }
                    },
                    suggestedMin: 0,
                    suggestedMax: 100, // Score max
                    grid: {
                        color: '#F3F4F6' // Grille de fond légère
                    }
                },
                y: {
                    title: {
                        display: true,
                        text: 'Score Technique (Outils & Pratique)',
                        font: { size: 14, weight: 'bold' }
                    },
                    suggestedMin: 0,
                    suggestedMax: 100, // Score max
                    grid: {
                        color: '#F3F4F6' // Grille de fond légère
                    }
                }
            },
            plugins: {
                legend: {
                    display: false // Pas de légende pour un seul point
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return ` Théorie: ${context.parsed.x}, Technique: ${context.parsed.y}`;
                        }
                    }
                }
            }
        }
    });
};

/**
 * Tâche 5.2 (Action 4): Affiche les recommandations
 */
const renderRecommendations = (recos) => {
    // Tâche 5.2 (Action 2): Cible le div
    const listDiv = document.getElementById('recommendationsList');
    
    if (!recos || recos.length === 0) {
        listDiv.innerHTML = '<p class="text-center text-gray-500">Aucune recommandation spécifique pour le moment.</p>';
        return;
    }

    let html = '<h3 class="text-xl font-bold text-gray-800 mb-6 text-center">Vos Pistes d\'Amélioration</h3>';
    
    recos.forEach(rec => {
        // Déterminer une icône basée sur le type (si le backend le fournit, sinon icône par défaut)
        // Pour l'instant, nous utilisons une icône par défaut.
        const icon = rec.type === 'THEORY' ? 'book-open-check' : 'wrench';

        html += `
            <div class="recommendation-card">
                <div class="icon">
                    <i data-lucide="${icon}" class="w-5 h-5"></i>
                </div>
                <div class="content">
                    <h4>${rec.title}</h4>
                    <p>${rec.description}</p>
                </div>
            </div>
        `;
    });

    listDiv.innerHTML = html;
    lucide.createIcons(); // Doit être appelé à nouveau pour que les icônes injectées s'affichent
};


/**
 * Tâche 5.1 (Action 4): Logique de chargement de la page
 */
const fetchResults = async () => {
    // Récupère le sessionId
    const sessionId = localStorage.getItem('quizSessionId');
    const resultsTitle = document.getElementById('resultsTitle');
    const profileDescription = document.getElementById('profileDescription');

    if (!sessionId) {
        resultsTitle.textContent = "Erreur: ID de session non trouvé.";
        profileDescription.textContent = "Veuillez recommencer le test depuis la page d'accueil.";
        return;
    }

    console.log(`Récupération des résultats pour la session: ${sessionId}`);

    try {
        // Tâche 5.1: Appelle GET /api/quiz/results?sessionId=...
        const response = await fetch(`${API_URL_RESULTS}?sessionId=${sessionId}`);
        
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Échec de la récupération des résultats (HTTP ${response.status}). Détail: ${errorText}`);
        }

        const results = await response.json();

        // Tâche 5.1 (Action 4): Récupère les scores
        // Note: J'utilise les noms de champs de votre Tâche 5.1 (finalScoreTheory, finalScoreTechnique)
        const { finalScoreTheory, finalScoreTechnique, profileTitle, profileDescription, recommendations } = results;

        if (finalScoreTheory === undefined || finalScoreTechnique === undefined) {
             throw new Error("La réponse de l'API ne contient pas les scores attendus (finalScoreTheory, finalScoreTechnique).");
        }

        // Afficher le titre et la description du profil
        resultsTitle.textContent = `Votre Profil CyberScale : ${profileTitle || 'Analyse Terminée'}`;
        profileDescription.textContent = profileDescription || 'Voici votre positionnement actuel.';

        // Tâche 5.1 (Action 4): Affiche le graphique Scatter
        renderChart(finalScoreTheory, finalScoreTechnique);

        // Tâche 5.2 (Action 3 & 4): Affiche les recommandations
        renderRecommendations(recommendations);

    } catch (error) {
        console.error("Erreur lors de la récupération des résultats:", error);
        resultsTitle.textContent = "Erreur lors de la récupération des résultats.";
        profileDescription.textContent = error.message;
    }
};

// Initialisation au chargement de la page
document.addEventListener('DOMContentLoaded', () => {
    fetchResults();
});