const API_URL_LOGIN = 'http://localhost:8080/api/auth/login'; 

/**
 * Affiche un message de statut à l'utilisateur.
 * @param {string} message - Le message à afficher.
 * @param {boolean} isError - True si c'est un message d'erreur, false pour un succès.
 */
const displayStatus = (message, isError = false) => {
    const statusDiv = document.getElementById('statusMessage');
    statusDiv.textContent = message;
    statusDiv.classList.remove('hidden', 'success', 'error');
    
    if (isError) {
        statusDiv.classList.add('error');
    } else {
        // Pour les messages de succès ou d'attente
        statusDiv.classList.add('success'); 
    }
};

/**
 * Gère la soumission du formulaire de connexion.
 */
const handleLogin = async (e) => {
    e.preventDefault(); // Empêche le rechargement de la page

    const loginButton = document.getElementById('loginButton');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');

    // Désactiver le bouton pendant l'appel
    loginButton.disabled = true;
    displayStatus("Connexion en cours...", false); // Message de "succès" (style vert) pour le chargement

    try {
        const loginData = {
            email: emailInput.value,
            password: passwordInput.value
        };
        
        // 2. Tâche F5: Appel de l'API /login (POST)
        const response = await fetch(API_URL_LOGIN, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(loginData)
        });

        // 3. Gestion de la réponse
        if (response.ok) { // Status 200-299
            
            // L'API (Tâche 5.4) doit renvoyer les infos utilisateur (email, token JWT, etc.)
            const userData = await response.json(); 

            // Tâche F5: Stockage de l'info utilisateur (localStorage)
            // Nous stockons l'email pour l'affichage, et le "token" s'il existe.
            localStorage.setItem('userEmail', userData.email);
            
            // Si votre backend renvoie un token JWT (courant pour la F6)
            if (userData.token) {
                localStorage.setItem('authToken', userData.token);
            }
            
            displayStatus("Connexion réussie ! Redirection vers l'évaluation...", false);
            
            // Redirection vers la page d'accueil (index.html)
            setTimeout(() => {
                window.location.href = 'index.html'; 
            }, 1500);

        } else if (response.status === 401 || response.status === 403) {
            // 401 Unauthorized ou 403 Forbidden
            displayStatus("Erreur : Email ou mot de passe incorrect.", true);
        } else {
            // Autres erreurs
            displayStatus(`Erreur ${response.status}: Impossible de se connecter.`, true);
        }

    } catch (error) {
        // Erreur réseau
        console.error("Erreur de connexion:", error);
        displayStatus("Erreur réseau : Impossible de contacter le serveur (Backend non démarré ?).", true);
    } finally {
        // Réactiver le bouton (sauf si la redirection est en cours)
        if (!document.querySelector('.message-box.success')) {
            loginButton.disabled = false;
        }
    }
};

// Écoute de la soumission du formulaire
document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('loginForm');
    if (form) {
        form.addEventListener('submit', handleLogin);
    }
});