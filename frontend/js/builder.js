// Initialisation des données Vis.js
var nodes = new vis.DataSet([]);
var edges = new vis.DataSet([]);
var network = null;

// Initialisation au chargement
document.addEventListener("DOMContentLoaded", () => {
    initNetworkBuilder();
});

function initNetworkBuilder() {
    const container = document.getElementById('network-canvas');
    if(!container) return; // Sécurité si on n'est pas sur la page arena

    // Noeuds par défaut pour l'exemple
    nodes.add([
        { id: 1, label: 'Attaquant_Moi', group: 'kali', color: '#ef4444', shape: 'dot', font: {color:'white'} }, // Correction ici
        { id: 2, label: 'Serveur Web', group: 'server', color: '#3b82f6', shape: 'dot', font: {color:'white'} }
    ]);
    edges.add([
        { from: 1, to: 2, color: {color:'#4b5563'} }
    ]);

    const data = { nodes: nodes, edges: edges };
    const options = {
        nodes: { borderWidth: 2, size: 30 },
        edges: { width: 2, smooth: { type: 'continuous' } },
        physics: { stabilization: false },
        manipulation: { enabled: true } // Permet de relier à la souris
    };
    network = new vis.Network(container, data, options);
}

// Fonction appelée par les boutons de la sidebar
window.addNode = function(type) {
    // Génération d'un ID unique simple (timestamp + random)
    const id = Date.now() + Math.floor(Math.random() * 1000);
    
    let label = type === 'kali' ? 'Attaquant' : (type === 'db' ? 'Database' : 'Serveur Web');
    let color = type === 'kali' ? '#ef4444' : (type === 'db' ? '#eab308' : '#3b82f6');
    
    nodes.add({ 
        id: id, 
        label: label, 
        group: type, 
        color: color, 
        shape: 'dot', 
        font: {color:'white'} 
    });
};

// Fonction appelée par le bouton "DÉPLOYER"
window.deployNetwork = async function() {
    const userId = localStorage.getItem('userId');
    if(!userId) { alert("Erreur session. Reconnectez-vous."); return; }

    // 1. Transformation du graphe Vis.js en JSON pour l'API
    const topology = {
        userId: userId,
        nodes: nodes.get().map(n => ({
            id: n.id.toString(),
            type: n.group, // 'kali', 'server', 'db'
            label: n.label
        })),
        links: edges.get().map(e => ({
            source: e.from.toString(),
            target: e.to.toString()
        }))
    };

    console.log("🚀 Envoi topologie:", topology);
    
    // Feedback UI
    const btn = document.getElementById('deploy-btn');
    const originalText = btn.innerHTML;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Déploiement...';
    btn.disabled = true;

    try {
        const response = await fetch('http://localhost:8080/api/builder/deploy', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(topology)
        });

        const result = await response.json();

        if (response.ok) {
            console.log("✅ Déploiement réussi. ID:", result.deploymentId);
            
            // 🔥 PASSAGE DE RELAIS À ARENA.JS
            if (window.onDeploymentSuccess) {
                window.onDeploymentSuccess(result.deploymentId);
            }
        } else {
            alert("❌ Erreur de déploiement: " + (result.error || "Inconnue"));
        }
    } catch (e) {
        console.error(e);
        alert("Erreur technique : Impossible de joindre le serveur.");
    } finally {
        btn.innerHTML = originalText;
        btn.disabled = false;
    }
};

// Supprimer avec la touche "Suppr"
document.addEventListener('keydown', function(event) {
    if ((event.key === 'Delete' || event.key === 'Backspace') && network) {
        const selection = network.getSelection();
        if (selection.nodes.length > 0) nodes.remove(selection.nodes);
        if (selection.edges.length > 0) edges.remove(selection.edges);
    }
});