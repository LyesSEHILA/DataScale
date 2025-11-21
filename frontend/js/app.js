const API_URL = "http://localhost:8080/api/quiz";

document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("onboardingForm");

    if (form) {
        form.addEventListener("submit", async (e) => {
            e.preventDefault(); // Empêche le rechargement de la page

            const age = document.getElementById("ageInput").value;
            const theory = document.getElementById("theorySlider").value;
            const tech = document.getElementById("techSlider").value;

            const data = {
                age: parseInt(age),
                selfEvalTheory: parseInt(theory),
                selfEvalTechnique: parseInt(tech)
            };

            try {
                const response = await fetch(`${API_URL}/start`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)
                });

                if (!response.ok) throw new Error("Erreur API");

                const session = await response.json();
                
                // 1. Stocker l'ID de session
                localStorage.setItem('quizSessionId', session.id);
                
                // 2. Rediriger vers la page suivante (F2)
                window.location.href = 'quiz.html';

            } catch (error) {
                console.error(error);
                alert("Impossible de démarrer le quiz. Vérifiez que le backend tourne !");
            }
        });
    }
});