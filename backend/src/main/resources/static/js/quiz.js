// URL de l'API pour récupérer les questions et soumettre les réponses
const API_URL_QUESTIONS = 'http://localhost:8080/api/quiz/questions';
const API_URL_ANSWER = 'http://localhost:8080/api/quiz/answer';
const API_URL_END = 'http://localhost:8080/api/quiz/end'; // (Pour F3)

// Variables d'état global
let questions = []; // Tableau qui stockera toutes les questions
let currentQuestionIndex = 0; // Index de la question actuellement affichée
let sessionId = null; // ID de session du localStorage

// --- Fonctions Utilitaires ---

// Affichage des messages
const displayStatus = (message, isError = false) => {
    const statusDiv = document.getElementById('statusMessage');
    statusDiv.textContent = message;
    statusDiv.className = 'text-center p-3 rounded-md mt-4 mb-4';
    if (isError) {
        statusDiv.classList.add('bg-red-100', 'text-red-800');
    } else {
        statusDiv.classList.add('bg-blue-100', 'text-blue-800'); // Note: Bleu pour les infos
    }
    statusDiv.classList.remove('hidden');
};

// Mise à jour de la barre de progression
const updateProgressBar = () => {
    if (questions.length === 0) return;

    const progress = ((currentQuestionIndex) / questions.length) * 100;
    document.getElementById('progressBar').style.width = `${progress}%`;
    document.getElementById('questionCounter').textContent = 
        `Question ${Math.min(currentQuestionIndex + 1, questions.length)} / ${questions.length}`;
};

// --- Logique d'Affichage ---

/**
 * Affiche la question actuelle et ses options.
 */
const displayQuestion = () => {
    // Désactiver le bouton "Suivant" tant qu'une option n'est pas sélectionnée
    document.getElementById('nextButton').disabled = true;

    if (currentQuestionIndex >= questions.length) {
        // Fin du quiz
        handleQuizEnd();
        return;
    }

    const question = questions[currentQuestionIndex];
    const optionsContainer = document.getElementById('optionsContainer');

    // 1. Mise à jour du titre
    document.getElementById('questionTitle').textContent = question.title;

    // 2. Mise à jour de la difficulté
    const difficultyBadge = document.getElementById('difficultyBadge');
    difficultyBadge.textContent = `Niveau: ${question.difficulty}`;
    difficultyBadge.classList.remove('hidden'); 

    // 3. Mise à jour des options
    optionsContainer.innerHTML = ''; // Nettoyer les options précédentes

    question.options.forEach((option) => {
        const optionId = `option-${option.id}`;
        const input = document.createElement('input');
        input.type = 'radio';
        input.id = optionId;
        input.name = 'quiz-option';
        input.value = option.id;
        input.classList.add('quiz-option');

        const label = document.createElement('label');
        label.setAttribute('for', optionId);
        label.classList.add('quiz-option-label');
        label.textContent = option.text;

        // Ajouter un listener pour activer le bouton "Suivant" au clic
        input.addEventListener('change', () => {
            document.getElementById('nextButton').disabled = false;
        });

        optionsContainer.appendChild(input);
        optionsContainer.appendChild(label);
    });

    // 4. Mise à jour de la barre de progression
    updateProgressBar();
};

// --- Logique d'API ---

/**
 * Charge les questions depuis l'API backend.
 * Remplace les données mockées.
 */
const fetchQuestions = async () => {
    displayStatus("Chargement des questions adaptatives...");

    try {
        // [Checklist F2] quiz.js appelle GET /api/quiz/questions
        // Nous devons passer le sessionId pour que le backend sache quel quiz charger
        const response = await fetch(`${API_URL_QUESTIONS}?sessionId=${sessionId}`);
        
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Échec du chargement des questions (HTTP ${response.status}). Détail: ${errorText}`);
        }

        questions = await response.json(); // [Checklist F2] stocke les questions

        if (questions.length === 0) {
            throw new Error("Le backend a retourné un quiz vide.");
        }
        
        displayStatus(`Test chargé! Prêt à commencer.`, false);
        document.getElementById('statusMessage').classList.add('hidden'); // Cacher le message
        
        // [Checklist F2] Affiche la première question
        displayQuestion();

    } catch (error) {
        console.error("Erreur lors du chargement des questions:", error);
        displayStatus(`Erreur critique: ${error.message}. Impossible de charger le quiz.`, true);
        document.getElementById('nextButton').disabled = true;
    }
};

/**
 * Envoie la réponse sélectionnée au backend.
 */
const submitAnswer = async (selectedOptionId) => {
    if (!sessionId) {
        displayStatus("Erreur: ID de session manquant. Redémarrez le test.", true);
        return false;
    }

    const currentQuestion = questions[currentQuestionIndex];
    
    // Payload pour le backend (QuizAnswerRequest DTO supposé)
    const payload = {
        sessionId: sessionId,
        questionId: currentQuestion.id,
        selectedOptionId: selectedOptionId
    };

    try {
        // Désactiver le bouton avant l'appel API
        document.getElementById('nextButton').disabled = true;
        displayStatus("Envoi de la réponse...");

        // [Checklist F2] Le clic appelle POST /api/quiz/answer
        const response = await fetch(API_URL_ANSWER, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            // L'API renvoie une erreur (session invalide, réponse invalide, etc.)
            const errorText = await response.text();
            throw new Error(`L'API a renvoyé ${response.status}: ${errorText.substring(0, 50)}`);
        }
        
        // La réponse a été acceptée par le backend
        displayStatus("Réponse enregistrée.", false);
        await new Promise(resolve => setTimeout(resolve, 300)); // Courte pause UX
        document.getElementById('statusMessage').classList.add('hidden');
        
        // [Checklist F2] Le clic appelle displayQuestion(index+1)
        currentQuestionIndex++;
        displayQuestion(); // Afficher la question suivante

        return true;

    } catch (error) {
        console.error("Erreur de soumission de la réponse:", error);
        displayStatus(`Erreur lors de l'envoi de la réponse: ${error.message}. Veuillez réessayer.`, true);
        // NE PAS passer à la question suivante si l'envoi échoue
        document.getElementById('nextButton').disabled = false; // Réactiver pour un nouvel essai
        return false;
    }
};

/**
 * Gère la fin du quiz.
 */
const handleQuizEnd = async () => {
    document.getElementById('nextButton').disabled = true;
    displayStatus("Test terminé ! Calcul des résultats en cours...", false);

    try {
        // TODO: (F3) Appel API pour finaliser la session (POST /api/quiz/end?sessionId=...)
        // Pour l'instant, nous simulons juste le succès.
        await new Promise(resolve => setTimeout(resolve, 1500)); // Simule le calcul

        // [Checklist F2] Redirection vers results.html
        window.location.href = 'results.html';
        
    } catch (error) {
        displayStatus(`Erreur lors de la finalisation du quiz: ${error.message}`, true);
        document.getElementById('nextButton').disabled = false;
    }
};

// --- Initialisation ---

document.addEventListener('DOMContentLoaded', () => {
    const nextButton = document.getElementById('nextButton');
    
    // 1. Récupérer l'ID de session
    sessionId = localStorage.getItem('quizSessionId');
    if (!sessionId) {
        displayStatus("Erreur: ID de session non trouvé. Veuillez redémarrer à partir de la page d'accueil.", true);
        nextButton.disabled = true;
        return;
    }

    // 2. Lancer le chargement des questions
    fetchQuestions();
    
    // 3. Ajouter le listener pour le bouton "Suivant"
    nextButton.addEventListener('click', () => {
        const selectedOption = document.querySelector('input[name="quiz-option"]:checked');
        if (selectedOption) {
            submitAnswer(selectedOption.value);
        } else {
            // Ne devrait pas se produire car le bouton est désactivé
            displayStatus("Veuillez sélectionner une option pour continuer.", true);
        }
    });

    console.log(`Quiz.js chargé. Session ID: ${sessionId}`); // Affiche l'ID complet
});