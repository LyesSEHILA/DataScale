-- ========= QUESTIONS DE TEST (CyberScale) =========
-- On réinitialise les séquences d'ID pour H2 (évite les conflits)
ALTER TABLE questions ALTER COLUMN id RESTART WITH 1;
ALTER TABLE answers_option ALTER COLUMN id RESTART WITH 1;


-- ---------------------------------------------------
-- Question 1 (Théorie, Facile)
-- ---------------------------------------------------
INSERT INTO questions (id, text, categorie, difficulty) 
VALUES (1, 'Qu est-ce qu un "Phishing" (Hameçonnage) ?', 'THEORY', 'EASY');

-- Réponses pour la Question 1 (note le "question_id = 1")
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (1, 'Une technique de pêche en haute mer.', false, 1);

INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (2, 'Une attaque visant à voler des identifiants en se faisant passer pour une entité de confiance.', true, 1);

INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (3, 'Un type de logiciel antivirus.', false, 1);

INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (4, 'Un protocole de réseau sans fil sécurisé.', false, 1);


-- ---------------------------------------------------
-- Question 2 (Technique, Moyenne)
-- ---------------------------------------------------
INSERT INTO questions (id, text, categorie, difficulty) 
VALUES (2, 'Quelle commande Nmap est utilisée pour une analyse rapide des ports TCP les plus courants ?', 'TECHNIQUE', 'MEDIUM');

-- Réponses pour la Question 2 (note le "question_id = 2")
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (5, 'nmap -sU -T4 [cible]', false, 2);

INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (6, 'nmap -sP [cible]', false, 2);

INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (7, 'nmap -F [cible]', true, 2); -- (-F = Fast scan)

INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (8, 'nmap -O [cible]', false, 2);


-- ---------------------------------------------------
-- Question 3 (Technique, Facile)
-- ---------------------------------------------------
INSERT INTO questions (id, text, categorie, difficulty) 
VALUES (3, 'Quel outil est principalement utilisé pour intercepter et analyser le trafic réseau ?', 'TECHNIQUE', 'EASY');

-- Réponses pour la Question 3
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (9, 'Wireshark', true, 3);

INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (10, 'Metasploit', false, 3);

INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (11, 'John the Ripper', false, 3);

INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (12, 'Notepad++', false, 3);

-- ---------------------------------------------------
-- Question 4 (Théorie, Difficile)
-- ---------------------------------------------------
INSERT INTO questions (id, text, categorie, difficulty) 
VALUES (4, 'À quelle phase du "Cyber Kill Chain" de Lockheed Martin correspond l exploitation d une vulnérabilité ?', 'THEORY', 'HARD');

-- Réponses pour la Question 4
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (13, 'Phase 1: Reconnaissance', false, 4);

INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (14, 'Phase 4: Exploitation', true, 4);

INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (15, 'Phase 6: Installation', false, 4);

INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (16, 'Phase 7: Actions sur Objectifs', false, 4);


-- ---------------------------------------------------
-- Question 5 (Théorie, Facile)
-- ---------------------------------------------------
INSERT INTO questions (id, text, categorie, difficulty) 
VALUES (5, 'Que signifie l acronyme "VPN" ?', 'THEORY', 'EASY');

-- Réponses pour la Question 5
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (17, 'Virtual Private Network (Réseau Privé Virtuel)', true, 5);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (18, 'Very Private Network (Réseau Très Privé)', false, 5);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (19, 'Virtual Public Node (Nœud Public Virtuel)', false, 5);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (20, 'Verified Private Naming (Nommage Privé Vérifié)', false, 5);

-- ---------------------------------------------------
-- Question 6 (Technique, Medium)
-- ---------------------------------------------------
INSERT INTO questions (id, text, categorie, difficulty) 
VALUES (6, 'Quel outil est couramment utilisé pour "craquer" des hashs de mots de passe ?', 'TECHNIQUE', 'MEDIUM');

-- Réponses pour la Question 6
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (21, 'Nmap', false, 6);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (22, 'John the Ripper', true, 6);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (23, 'Wireshark', false, 6);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (24, 'Metasploit', false, 6);

-- ---------------------------------------------------
-- Question 7 (Théorie, Medium)
-- ---------------------------------------------------
INSERT INTO questions (id, text, categorie, difficulty) 
VALUES (7, 'Que signifie la triade "CIA" en cybersécurité ?', 'THEORY', 'MEDIUM');

-- Réponses pour la Question 7
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (25, 'Central Intelligence Agency', false, 7);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (26, 'Confidentiality, Integrity, Availability (Confidentialité, Intégrité, Disponibilité)', true, 7);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (27, 'Control, Identify, Access (Contrôler, Identifier, Accéder)', false, 7);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (28, 'Cyber, Intelligence, Analysis (Cyber, Renseignement, Analyse)', false, 7);

-- ---------------------------------------------------
-- Question 8 (Technique, Hard)
-- ---------------------------------------------------
INSERT INTO questions (id, text, categorie, difficulty) 
VALUES (8, 'Quelle attaque consiste à intercepter et potentiellement altérer la communication entre deux parties ?', 'TECHNIQUE', 'HARD');

-- Réponses pour la Question 8
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (29, 'Attaque DDoS', false, 8);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (30, 'Injection SQL', false, 8);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (31, 'Man-in-the-Middle (MitM)', true, 8);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (32, 'Phishing', false, 8);

-- ---------------------------------------------------
-- Question 9 (Théorie, Facile)
-- ---------------------------------------------------
INSERT INTO questions (id, text, categorie, difficulty) 
VALUES (9, 'Qu est-ce qu un "pare-feu" (Firewall) ?', 'THEORY', 'EASY');

-- Réponses pour la Question 9
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (33, 'Un dispositif (logiciel ou matériel) qui filtre le trafic réseau pour bloquer les accès non autorisés.', true, 9);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (34, 'Un type de virus informatique qui surchauffe l ordinateur.', false, 9);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (35, 'Un gestionnaire de mots de passe sécurisé.', false, 9);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (36, 'Un système de refroidissement pour les serveurs.', false, 9);

-- ---------------------------------------------------
-- Question 10 (Technique, Medium)
-- ---------------------------------------------------
INSERT INTO questions (id, text, categorie, difficulty) 
VALUES (10, 'Quel fichier sur un système Linux contient généralement les hashs de mots de passe des utilisateurs ?', 'TECHNIQUE', 'MEDIUM');

-- Réponses pour la Question 10
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (37, '/etc/passwd', false, 10);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (38, '/etc/shadow', true, 10);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (39, '/var/log/auth.log', false, 10);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (40, '/etc/hosts', false, 10);

-- ---------------------------------------------------
-- Question 11 (Théorie, Medium)
-- ---------------------------------------------------
INSERT INTO questions (id, text, categorie, difficulty) 
VALUES (11, 'Qu est-ce qu un "malware" ?', 'THEORY', 'MEDIUM');

-- Réponses pour la Question 11
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (41, 'Un composant matériel sécurisé.', false, 11);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (42, 'Un logiciel conçu dans l intention de nuire (virus, trojan, ransomware...).', true, 11);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (43, 'Un protocole de messagerie sécurisée.', false, 11);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (44, 'Un vêtement de protection pour technicien.', false, 11);

-- ---------------------------------------------------
-- Question 12 (Technique, Hard)
-- ---------------------------------------------------
INSERT INTO questions (id, text, categorie, difficulty) 
VALUES (12, 'Qu est-ce qu une attaque par "Injection SQL" ?', 'TECHNIQUE', 'HARD');

-- Réponses pour la Question 12
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (45, 'Une technique qui consiste à insérer du code SQL malveillant dans une requête pour manipuler la base de données.', true, 12);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (46, 'Un patch de mise à jour pour un serveur SQL.', false, 12);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (47, 'Une méthode pour accélérer les requêtes de base de données.', false, 12);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (48, 'Un type de câble réseau pour serveurs de base de données.', false, 12);

-- ---------------------------------------------------
-- Question 13 (Théorie, Facile)
-- ---------------------------------------------------
INSERT INTO questions (id, text, categorie, difficulty) 
VALUES (13, 'Que signifie "2FA" ?', 'THEORY', 'EASY');

-- Réponses pour la Question 13
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (49, 'Two-Factor Authentication (Authentification à deux facteurs)', true, 13);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (50, 'Two-File Authentication (Authentification à deux fichiers)', false, 13);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (51, 'Twice-Failed Access (Accès échoué deux fois)', false, 13);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (52, 'Two-Form Access (Accès à deux formulaires)', false, 13);

-- ---------------------------------------------------
-- Question 14 (Technique, Medium)
-- ---------------------------------------------------
INSERT INTO questions (id, text, categorie, difficulty) 
VALUES (14, 'Quel framework open-source est le plus populaire pour développer et exécuter des exploits ?', 'TECHNIQUE', 'MEDIUM');

-- Réponses pour la Question 14
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (53, 'Burp Suite', false, 14);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (54, 'Metasploit', true, 14);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (55, 'Ghidra', false, 14);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (56, 'Snort', false, 14);

-- ---------------------------------------------------
-- Question 15 (Théorie, Medium)
-- ---------------------------------------------------
INSERT INTO questions (id, text, categorie, difficulty) 
VALUES (15, 'Quel est le but principal du protocole HTTPS ?', 'THEORY', 'MEDIUM');

-- Réponses pour la Question 15
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (57, 'Rendre les sites web plus rapides.', false, 15);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (58, 'Afficher des images en haute résolution.', false, 15);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (59, 'Chiffrer la communication entre le navigateur et le serveur web.', true, 15);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (60, 'Bloquer les publicités pop-up.', false, 15);

-- ---------------------------------------------------
-- Question 16 (Technique, Hard)
-- ---------------------------------------------------
INSERT INTO questions (id, text, categorie, difficulty) 
VALUES (16, 'Qu est-ce qu une attaque "DDoS" ?', 'TECHNIQUE', 'HARD');

-- Réponses pour la Question 16
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (61, 'Une attaque qui vole les données d une base de données.', false, 16);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (62, 'Une attaque qui essaie de deviner un mot de passe par force brute.', false, 16);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (63, 'Une attaque qui submerge un serveur de trafic provenant de multiples sources pour le rendre indisponible.', true, 16);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (64, 'Une attaque qui installe un virus sur un ordinateur personnel.', false, 16);

-- ---------------------------------------------------
-- Question 17 (Théorie, Hard)
-- ---------------------------------------------------
INSERT INTO questions (id, text, categorie, difficulty) 
VALUES (17, 'Quel est le nom du principal règlement de l Union Européenne sur la protection des données personnelles ?', 'THEORY', 'HARD');

-- Réponses pour la Question 17
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (65, 'HIPAA', false, 17);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (66, 'RGPD (ou GDPR)', true, 17);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (67, 'PCI-DSS', false, 17);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (68, 'SOX', false, 17);

-- ---------------------------------------------------
-- Question 18 (Technique, Medium)
-- ---------------------------------------------------
INSERT INTO questions (id, text, categorie, difficulty) 
VALUES (18, 'Quel outil en ligne de commande est utilisé pour interroger les serveurs DNS ?', 'TECHNIQUE', 'MEDIUM');

-- Réponses pour la Question 18
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (69, 'ping', false, 18);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (70, 'traceroute', false, 18);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (71, 'ipconfig', false, 18);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (72, 'nslookup (ou dig)', true, 18);

-- ---------------------------------------------------
-- Question 19 (Théorie, Medium)
-- ---------------------------------------------------
INSERT INTO questions (id, text, categorie, difficulty) 
VALUES (19, 'Qu est-ce que l "Ingénierie Sociale" (Social Engineering) ?', 'THEORY', 'MEDIUM');

-- Réponses pour la Question 19
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (73, 'L art de manipuler psychologiquement les gens pour obtenir des informations confidentielles.', true, 19);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (74, 'Un domaine du génie civil pour les réseaux sociaux.', false, 19);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (75, 'Un logiciel pour construire des communautés en ligne.', false, 19);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (76, 'Une méthode de chiffrement de données.', false, 19);

-- ---------------------------------------------------
-- Question 20 (Technique, Hard)
-- ---------------------------------------------------
INSERT INTO questions (id, text, categorie, difficulty) 
VALUES (20, 'Qu est-ce qu une attaque "Cross-Site Scripting (XSS)" ?', 'TECHNIQUE', 'HARD');

-- Réponses pour la Question 20
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (77, 'Une attaque où un script malveillant est injecté dans un site web de confiance et s exécute dans le navigateur de la victime.', true, 20);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (78, 'Une attaque qui traverse plusieurs segments de réseau.', false, 20);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (79, 'Une méthode de partage de fichiers entre sites web.', false, 20);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (80, 'Un type de certificat de sécurité pour les sites web.', false, 20);