const API_BASE = "http://localhost:8080/api/exam";
let sessionId = localStorage.getItem('examSessionId');
let questions = [];
let currentIndex = 0;
let timerInterval;

document.addEventListener("DOMContentLoaded", async () => {
    if (!sessionId) {
        alert("Session invalide. Retour au menu.");
        window.location.href = "certifs.html";
        return;
    }

    // 1. Initialiser l'examen (Charger questions)
    await loadQuestions();

    // 2. Démarrer le Timer (ex: 30 minutes)
    startTimer(30 * 60); 
});

async function loadQuestions() {
    try {
        // Appel du NOUVEAU endpoint que nous venons de créer
        const response = await fetch(`${API_BASE}/${sessionId}/questions`);
        if (!response.ok) throw new Error("Erreur chargement questions");
        
        questions = await response.json();
        document.getElementById('totalQuestions').textContent = questions.length;
        
        if (questions.length > 0) {
            displayQuestion(0);
        } else {
            document.getElementById('questionText').textContent = "Aucune question trouvée pour cet examen.";
        }
    } catch (e) {
        console.error(e);
        alert("Impossible de charger l'examen.");
    }
}

function displayQuestion(index) {
    const q = questions[index];
    const container = document.getElementById('optionsContainer');
    
    // Mise à jour UI
    document.getElementById('currentQuestionNum').textContent = index + 1;
    document.getElementById('questionText').textContent = q.text;
    document.getElementById('questionPoints').textContent = (q.pointsWeight || 1) + " pts"; // Affiche les points !
    document.getElementById('progressBar').style.width = `${((index) / questions.length) * 100}%`;
    document.getElementById('nextBtn').disabled = true; // Désactive bouton tant que pas répondu

    // Génération Options
    container.innerHTML = q.options.map(opt => `
        <label class="flex items-center p-4 border-2 border-gray-100 rounded-xl cursor-pointer hover:bg-blue-50 hover:border-blue-200 transition group">
            <input type="radio" name="answer" value="${opt.id}" class="w-5 h-5 text-blue-600 focus:ring-blue-500 border-gray-300" onchange="enableNext()">
            <span class="ml-3 text-gray-700 font-medium group-hover:text-blue-800">${opt.text}</span>
        </label>
    `).join('');
}

function enableNext() {
    document.getElementById('nextBtn').disabled = false;
}

window.nextQuestion = async function() {
    const selectedOption = document.querySelector('input[name="answer"]:checked');
    if (!selectedOption) return;

    const question = questions[currentIndex];
    
    // Envoi de la réponse au serveur (Asynchrone, on attend pas forcément la réponse pour fluidité, mais mieux vaut attendre pour erreur)
    try {
        await fetch(`${API_BASE}/answer`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                sessionId: parseInt(sessionId),
                questionId: question.id,
                optionId: parseInt(selectedOption.value)
            })
        });
    } catch (e) {
        console.error("Erreur sauvegarde réponse", e);
    }

    // Passage à la suivante
    currentIndex++;
    if (currentIndex < questions.length) {
        displayQuestion(currentIndex);
    } else {
        finishExam();
    }
};

window.finishExam = async function() {
    clearInterval(timerInterval);
    
    // On désactive le bouton pour éviter le double-clic
    const btn = document.querySelector('button[onclick="finishExam()"]');
    if(btn) btn.disabled = true;

    if(confirm("Confirmer la fin de l'examen ?")) {
        try {
            const response = await fetch(`${API_BASE}/finish/${sessionId}`, { method: 'POST' });
            
            if(response.ok) {
                const result = await response.json();
                
                // 1. On stocke le résultat pour l'afficher sur la page suivante
                localStorage.setItem('lastExamResult', JSON.stringify(result));
                
                // 2. Redirection vers la page de résultats
                window.location.href = "exam-result.html"; 
            } else {
                alert("Erreur lors de la soumission. Vérifiez la console.");
            }
        } catch(e) {
            console.error("Erreur Finish:", e);
            alert("Erreur technique. Contactez l'administrateur.");
        }
    } else {
        // Si l'utilisateur annule, on relance le timer ? 
        // Non, compliqué. On laisse le bouton réactivé.
        if(btn) btn.disabled = false;
    }
};

// --- TIMER ---
function startTimer(durationSeconds) {
    let timer = durationSeconds, minutes, seconds;
    const display = document.getElementById('timer');
    
    timerInterval = setInterval(function () {
        minutes = parseInt(timer / 60, 10);
        seconds = parseInt(timer % 60, 10);

        minutes = minutes < 10 ? "0" + minutes : minutes;
        seconds = seconds < 10 ? "0" + seconds : seconds;

        display.textContent = minutes + ":" + seconds;

        if (--timer < 0) {
            clearInterval(timerInterval);
            alert("Temps écoulé !");
            finishExam(); // Force la fin
        }
    }, 1000);
}