const API_URL = "http://localhost:8080/api/quiz";

document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("onboardingForm");

    // Petit bonus : Pré-remplir les sliders si l'utilisateur a déjà joué ? (Optionnel)
    // Pour l'instant, on se concentre sur l'envoi de l'ID.

    if (form) {
        form.addEventListener("submit", async (e) => {
            e.preventDefault(); 

            const age = document.getElementById("ageInput").value;
            const theory = document.getElementById("theorySlider").value;
            const tech = document.getElementById("techSlider").value;
            
            // RÉCUPÉRATION DE L'ID UTILISATEUR (C'est ça qui manquait !)
            const userId = localStorage.getItem('userId');

            const data = {
                age: parseInt(age),
                selfEvalTheory: parseInt(theory),
                selfEvalTechnique: parseInt(tech),
                // On l'ajoute à la requête (si il existe)
                userId: userId ? parseInt(userId) : null 
            };

            try {
                const response = await fetch(`${API_URL}/start`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)
                });

                if (!response.ok) throw new Error("Erreur API");

                const session = await response.json();
                
                localStorage.setItem('quizSessionId', session.id);
                window.location.href = 'quiz.html';

            } catch (error) {
                console.error(error);
                alert("Impossible de démarrer le quiz. Vérifiez que le backend tourne !");
            }
        });
    }
});