// frontend/js/auth-guard.js

// Vérifie si l'utilisateur est connecté
const token = localStorage.getItem('userId'); // Ou 'userEmail'

if (!token) {
    console.log("Accès refusé : utilisateur non connecté");
    window.location.href = 'login.html';
}