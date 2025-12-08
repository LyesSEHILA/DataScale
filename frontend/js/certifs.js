const API_EXAM_START = "http://localhost:8080/api/exam/start";

document.addEventListener("DOMContentLoaded", () => {
    // Affiche le pseudo dans la sidebar
    const username = localStorage.getItem('userName') || "Utilisateur";
    const sidebarUser = document.getElementById('sidebarUsername');
    if(sidebarUser) sidebarUser.textContent = username;

    // Gestion Logout
    const logoutBtn = document.getElementById('logoutBtn');
    if(logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            localStorage.clear();
            window.location.href = 'index.html';
        });
    }
});

async function startExam(examRef) {
    const candidateName = localStorage.getItem('userName') || "Anonyme";
    
    // 1. Récupérer l'ID utilisateur stocké lors du login
    const userId = localStorage.getItem('userId'); 

    // 2. Construire l'URL avec le paramètre userId (si connecté)
    let url = `${API_EXAM_START}?candidateName=${encodeURIComponent(candidateName)}`;
    if (userId && userId !== "undefined") {
        url += `&userId=${userId}`;
    }

    try {
        const response = await fetch(url, {
            method: 'POST', // Le backend utilise @PostMapping
            headers: { 'Content-Type': 'application/json' }
        });

        if (!response.ok) throw new Error("Impossible de démarrer l'examen");

        const session = await response.json();
        
        // Stockage de la session pour la page suivante
        localStorage.setItem('examSessionId', session.id);
        localStorage.setItem('currentExamRef', examRef);

        window.location.href = 'exam.html';

    } catch (error) {
        console.error(error);
        alert("Erreur lors du démarrage : " + error.message);
    }
}