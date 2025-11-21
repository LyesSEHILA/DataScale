const API_URL = "http://localhost:8080/api/quiz";
let currentQuestionIndex = 0;
let questions = [];
let sessionId = null;

document.addEventListener("DOMContentLoaded", async () => {
    // 1. Récupérer l'ID de session stocké par la page d'accueil
    sessionId = localStorage.getItem('quizSessionId');
    
    if (!sessionId) {
        alert("Session introuvable. Veuillez recommencer.");
        window.location.href = 'index.html';
        return;
    }

    // 2. Charger les questions depuis le Backend
    try {
        const response = await fetch(`${API_URL}/questions?sessionId=${sessionId}`);
        if (!response.ok) throw new Error("Erreur réseau");
        
        questions = await response.json();
        
        if (!questions || questions.length === 0) {
            alert("Aucune question trouvée pour ce profil.");
            return;
        }

        // 3. Afficher la première question
        displayQuestion(0);

    } catch (error) {
        console.error(error);
        document.getElementById("quizContent").innerHTML = "<p style='color:red'>Erreur de connexion au serveur.</p>";
    }

    // 4. Écouter le formulaire
    document.getElementById("answerForm").addEventListener("submit", handleNext);
});

function displayQuestion(index) {
    const q = questions[index];
    const total = questions.length;

    // Mettre à jour les textes
    document.getElementById("questionNumber").textContent = index + 1;
    document.querySelector(".total").textContent = `/ ${total}`;
    document.getElementById("questionText").textContent = q.text;
    document.getElementById("questionCategory").textContent = q.categorie + " | " + q.difficulty;

    // Mettre à jour la barre de progression
    const progress = ((index) / total) * 100;
    document.getElementById("progressBar").style.width = `${progress}%`;

    // Générer les options
    const container = document.getElementById("optionsContainer");
    container.innerHTML = ""; // Vider
    document.getElementById("nextButton").disabled = true; // Désactiver bouton

    // Vérifier si q.options existe (grâce à ton fix Backend !)
    if(q.options && q.options.length > 0) {
        q.options.forEach(opt => {
            const div = document.createElement("div");
            // On utilise des template literals pour insérer les variables
            div.innerHTML = `
                <input type="radio" name="option" id="opt_${opt.id}" value="${opt.id}" class="option-input">
                <label for="opt_${opt.id}" class="option-label">${opt.text}</label>
            `;
            container.appendChild(div);
            
            // Activer le bouton au clic
            div.querySelector("input").addEventListener("change", () => {
                document.getElementById("nextButton").disabled = false;
            });
        });
    }
}

async function handleNext(e) {
    e.preventDefault();
    
    const selected = document.querySelector('input[name="option"]:checked');
    if (!selected) return;

    const answerId = selected.value;
    const questionId = questions[currentQuestionIndex].id;

    // 1. Envoyer la réponse au Backend
    try {
        await fetch(`${API_URL}/answer`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                sessionId: parseInt(sessionId),
                questionId: questionId,
                answerOptionId: parseInt(answerId)
            })
        });

        // 2. Passer à la suite
        currentQuestionIndex++;

        if (currentQuestionIndex < questions.length) {
            displayQuestion(currentQuestionIndex);
        } else {
            // FINI -> Page résultats
            window.location.href = 'results.html';
        }

    } catch (error) {
        console.error(error);
        alert("Erreur lors de l'envoi de la réponse.");
    }
}