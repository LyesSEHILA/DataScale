const API_URL = "http://localhost:8080/api/auth";

document.addEventListener("DOMContentLoaded", () => {
    const loginForm = document.getElementById('loginForm');

    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }
});

const handleLogin = async (e) => {
    e.preventDefault();

    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const loginButton = document.getElementById('loginButton');

    // Désactiver le bouton
    const originalText = loginButton.textContent;
    loginButton.disabled = true;
    loginButton.textContent = "Connexion...";

    // Nettoyer les messages précédents
    const statusDiv = document.getElementById('statusMessage');
    if (statusDiv) statusDiv.classList.add('hidden');

    try {
        const response = await fetch(`${API_URL}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email: emailInput.value,
                password: passwordInput.value
            })
        });

        if (response.ok) {
            const data = await response.json();
            
            // 1. On stocke TOUTES les infos utiles
            localStorage.setItem('userEmail', data.email);
            localStorage.setItem('userName', data.username); 
            localStorage.setItem('userId', data.id);
            
            displayStatus("Connexion réussie ! Redirection...", false);
            
            setTimeout(() => {
                window.location.href = 'dashboard.html'; 
            }, 1000);
        } else {
            // Erreur API
            const errorData = await response.json().catch(() => ({ message: "Erreur de connexion" }));
            throw new Error(errorData.message || "Identifiants incorrects");
        }

    } catch (error) {
        console.error(error);
        displayStatus(error.message, true);
        
        // Réactiver le bouton
        loginButton.disabled = false;
        loginButton.textContent = originalText;
    }
};

function displayStatus(message, isError) {
    const statusDiv = document.getElementById('statusMessage');
    if (!statusDiv) return;

    statusDiv.textContent = message;
    statusDiv.classList.remove('hidden', 'bg-red-100', 'text-red-700', 'bg-green-100', 'text-green-700');
    
    if (isError) {
        statusDiv.classList.add('bg-red-100', 'text-red-700');
    } else {
        statusDiv.classList.add('bg-green-100', 'text-green-700');
    }
    statusDiv.classList.remove('hidden');
}