// Constante pour l'URL de l'API (à ajuster si nécessaire)
const API_URL = 'http://localhost:8080/api/quiz/start';

// Fonction utilitaire pour l'attente avec backoff exponentiel pour les appels API
const exponentialBackoffFetch = async (url, options, retries = 3) => {
    for (let i = 0; i < retries; i++) {
        try {
            const response = await fetch(url, options);
            if (!response.ok) {
                // Si la réponse n'est pas OK mais n'est pas une erreur réseau
                const errorBody = await response.text();
                // Affiche une partie du corps de l'erreur pour le debug
                throw new Error(`Erreur HTTP ${response.status}: ${errorBody.substring(0, 100)}...`);
            }
            return response;
        } catch (error) {
            console.error(`Tentative ${i + 1} échouée:`, error.message);
            if (i < retries - 1) {
                const delay = Math.pow(2, i) * 1000; // 1s, 2s, 4s
                await new Promise(resolve => setTimeout(resolve, delay));
            } else {
                // Dernier essai échoué
                throw new Error("Échec de la connexion après plusieurs tentatives. Vérifiez le serveur backend (http://localhost:8080). Détail: " + error.message);
            }
        }
    }
};

// Fonction pour afficher les messages de statut à l'utilisateur
const displayStatus = (message, isError = false) => {
    const statusDiv = document.getElementById('statusMessage');
    statusDiv.textContent = message;
    statusDiv.className = 'text-center p-3 rounded-md mt-4'; // Réinitialiser les classes
    if (isError) {
        statusDiv.classList.add('bg-red-100', 'text-red-800');
    } else {
        statusDiv.classList.add('bg-green-100', 'text-green-800');
    }
    statusDiv.classList.remove('hidden');
};

// Fonction pour mettre à jour la valeur affichée du slider
const updateSliderValue = (sliderId, valueSpanId) => {
    const slider = document.getElementById(sliderId);
    const valueSpan = document.getElementById(valueSpanId);
    if (slider && valueSpan) {
        // Met à jour la valeur lors de l'interaction (input event)
        slider.addEventListener('input', () => {
            valueSpan.textContent = slider.value;
        });
    }
};

// Fonction principale de soumission du formulaire (Logique du 'click' - Tâche 3.2)
const handleSubmit = async (e) => {
    // 5. Action : Utilise e.preventDefault() pour empêcher le rechargement
    e.preventDefault();

    const startButton = document.getElementById('startButton');
    const ageInput = document.getElementById('ageInput');
    const theorySlider = document.getElementById('theorySlider');
    const techSlider = document.getElementById('techSlider');

    // Désactiver le bouton pendant le traitement
    startButton.disabled = true;
    displayStatus("Démarrage du test en cours...", false);

    try {
        // Récupère les valeurs des champs (ageInput.value, ...)
        const age = ageInput.value;
        const theory = theorySlider.value;
        const technical = techSlider.value;

        // Validation simple (critère DoD: âge > 0)
        if (parseInt(age) <= 0 || parseInt(age) > 120 || !age) {
            throw new Error("Veuillez entrer un âge valide (entre 10 et 120 ans).");
        }

        // Construit un objet data
        const data = {
            age: parseInt(age),
            theory: parseInt(theory),
            technical: parseInt(technical)
        };
        
        // Utilise fetch pour appeler l'API du Backend (en method: 'POST')
        const response = await exponentialBackoffFetch(API_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data)
        });

        const session = await response.json();

        // Validation de la réponse
        if (!session.id) {
            throw new Error("Le backend n'a pas retourné d'ID de session valide.");
        }

        // Stocke l'ID de session (localStorage.setItem)
        localStorage.setItem('quizSessionId', session.id);

        // Affiche un message de succès
        displayStatus(`Quiz démarré avec succès! ID de session : ${session.id}. Redirection en cours...`, false);
        console.log(`ID de Session sauvé: ${session.id}`);

        // Redirection vers le début du quiz (F2) - Décommenter pour un usage réel
        setTimeout(() => {
            // window.location.href = `/quiz-start?sessionId=${session.id}`;
            console.log("Simuler la redirection vers /quiz-start");
        }, 1500);

    } catch (error) {
        // Gérer les erreurs de la requête ou de la validation
        console.error("Erreur de soumission:", error);
        displayStatus(`Erreur lors du démarrage du test. Détails : ${error.message}`, true);
        startButton.disabled = false; // Réactiver le bouton en cas d'erreur
    }
};

// 3. Action : Ajoute un listener DOMContentLoaded.
document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('onboardingForm');
    
    // 4. Action : Récupère le bouton startButton et ajoute-lui un listener click (via le submit du form).
    if (form) {
        form.addEventListener('submit', handleSubmit);
    }
    
    // Initialiser les mises à jour des valeurs de slider
    updateSliderValue('theorySlider', 'theoryValue');
    updateSliderValue('techSlider', 'techValue');

    // Mise à jour initiale des valeurs des sliders au chargement
    document.getElementById('theoryValue').textContent = document.getElementById('theorySlider').value;
    document.getElementById('techValue').textContent = document.getElementById('techSlider').value;
});