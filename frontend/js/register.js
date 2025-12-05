const API_URL_REGISTER = 'http://localhost:8080/api/auth/register'; 

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
 * Gère la soumission du formulaire d'inscription.
 */
const handleRegister = async (e) => {
    e.preventDefault(); // Empêche le rechargement de la page

    const registerButton = document.getElementById('registerButton');
    const usernameInput = document.getElementById('username');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const passwordRepeatInput = document.getElementById('passwordRepeat');

    // 1. Validation côté client (Mots de passe)
    if (passwordInput.value !== passwordRepeatInput.value) {
        displayStatus("Erreur : Les mots de passe ne correspondent pas.", true);
        passwordInput.value = '';
        passwordRepeatInput.value = '';
        return;
    }

    if (passwordInput.value.length < 8) {
        displayStatus("Erreur : Le mot de passe doit contenir au moins 8 caractères.", true);
        return;
    }

    if (usernameInput.value.trim().length < 3) {
        displayStatus("Erreur : Le nom d'utilisateur est trop court.", true);
        return;
    }

    // Désactiver le bouton pendant l'appel
    registerButton.disabled = true;
    displayStatus("Inscription en cours...", false); // Message de "succès" (style vert) pour le chargement

    try {
        const registrationData = {
            username: usernameInput.value,
            email: emailInput.value,
            password: passwordInput.value
            // Le backend gérera le hachage et le stockage
        };
        
        // 2. Tâche F5: Appel de l'API /register (POST)
        const response = await fetch(API_URL_REGISTER, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(registrationData)
        });

        // 3. Gestion de la réponse
        if (response.status === 201 || response.status === 200) { // 201 (Created) ou 200 (OK)
            
            displayStatus("Inscription réussie ! Vous allez être redirigé vers la page de connexion.", false);
            
            // Redirection vers la page de connexion
            setTimeout(() => {
                window.location.href = 'login.html'; 
            }, 2000); // 2 secondes de délai

        } else if (response.status === 409) {
            // 409 Conflict: L'email existe déjà
            displayStatus("Erreur : Un compte existe déjà avec cette adresse email.", true);
        } else {
            // Autres erreurs (ex: 400 Bad Request pour validation)
            const errorBody = await response.json().catch(() => ({ message: 'Réponse invalide du serveur.' }));
            displayStatus(`Erreur ${response.status}: ${errorBody.message}`, true);
        }

    } catch (error) {
        // Erreur réseau (le backend n'est pas lancé ou URL incorrecte)
        console.error("Erreur de connexion:", error);
        displayStatus("Erreur réseau : Impossible de contacter le serveur (Backend non démarré ?).", true);
    } finally {
        // Réactiver le bouton (sauf si la redirection est en cours)
        if (!document.querySelector('.message-box.success') || document.querySelector('.message-box.error')) {
            registerButton.disabled = false;
        }
    }
};

// Écoute de la soumission du formulaire
document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('registerForm');
    if (form) {
        form.addEventListener('submit', handleRegister);
    }
});