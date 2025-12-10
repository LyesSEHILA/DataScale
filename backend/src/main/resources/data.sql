-- ========= QUESTIONS DE TEST (CyberScale) =========
-- On réinitialise les séquences d'ID pour H2 (évite les conflits)
ALTER TABLE questions ALTER COLUMN id RESTART WITH 1;
ALTER TABLE answers_option ALTER COLUMN id RESTART WITH 1;


-- ---------------------------------------------------
-- Question 1 (Théorie, Facile)
-- ---------------------------------------------------
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (1, 'Qu est-ce qu un "Phishing" (Hameçonnage) ?', 'THEORY', 'EASY', 1, NULL);

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
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (2, 'Quelle commande Nmap est utilisée pour une analyse rapide des ports TCP les plus courants ?', 'TECHNIQUE', 'MEDIUM', 3, NULL);

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
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (3, 'Quel outil est principalement utilisé pour intercepter et analyser le trafic réseau ?', 'TECHNIQUE', 'EASY', 1, NULL);

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
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (4, 'À quelle phase du "Cyber Kill Chain" de Lockheed Martin correspond l exploitation d une vulnérabilité ?', 'THEORY', 'HARD', 5, NULL);

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
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (5, 'Que signifie l acronyme "VPN" ?', 'THEORY', 'EASY', 1, NULL);

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
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (6, 'Quel outil est couramment utilisé pour "craquer" des hashs de mots de passe ?', 'TECHNIQUE', 'MEDIUM', 3, NULL);

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
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (7, 'Que signifie la triade "CIA" en cybersécurité ?', 'THEORY', 'MEDIUM', 3, NULL);

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
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (8, 'Quelle attaque consiste à intercepter et potentiellement altérer la communication entre deux parties ?', 'TECHNIQUE', 'HARD', 5, NULL);

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
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (9, 'Qu est-ce qu un "pare-feu" (Firewall) ?', 'THEORY', 'EASY', 1, NULL);

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
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (10, 'Quel fichier sur un système Linux contient généralement les hashs de mots de passe des utilisateurs ?', 'TECHNIQUE', 'MEDIUM', 3, NULL);

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
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (11, 'Qu est-ce qu un "malware" ?', 'THEORY', 'MEDIUM', 3, NULL);

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
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (12, 'Qu est-ce qu une attaque par "Injection SQL" ?', 'TECHNIQUE', 'HARD', 5, NULL);

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
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (13, 'Que signifie "2FA" ?', 'THEORY', 'EASY', 1, NULL);

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
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (14, 'Quel framework open-source est le plus populaire pour développer et exécuter des exploits ?', 'TECHNIQUE', 'MEDIUM', 3, NULL);

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
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (15, 'Quel est le but principal du protocole HTTPS ?', 'THEORY', 'MEDIUM', 3, NULL);

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
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (16, 'Qu est-ce qu une attaque "DDoS" ?', 'TECHNIQUE', 'HARD', 5, NULL);

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
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (17, 'Quel est le nom du principal règlement de l Union Européenne sur la protection des données personnelles ?', 'THEORY', 'HARD', 5, NULL);

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
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (18, 'Quel outil en ligne de commande est utilisé pour interroger les serveurs DNS ?', 'TECHNIQUE', 'MEDIUM', 3, NULL);

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
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (19, 'Qu est-ce que l "Ingénierie Sociale" (Social Engineering) ?', 'THEORY', 'MEDIUM', 3, NULL);

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
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (20, 'Qu est-ce qu une attaque "Cross-Site Scripting (XSS)" ?', 'TECHNIQUE', 'HARD', 5, NULL);

-- Réponses pour la Question 20
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (77, 'Une attaque où un script malveillant est injecté dans un site web de confiance et s exécute dans le navigateur de la victime.', true, 20);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (78, 'Une attaque qui traverse plusieurs segments de réseau.', false, 20);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (79, 'Une méthode de partage de fichiers entre sites web.', false, 20);
INSERT INTO answers_option (id, text, is_correct, question_id) 
VALUES (80, 'Un type de certificat de sécurité pour les sites web.', false, 20);


-- ===================================================
-- DONNÉES DE RECOMMANDATION (F4)
-- ===================================================

-- On s'assure que les IDs commencent après nos insertions manuelles
ALTER TABLE recommendation ALTER COLUMN id RESTART WITH 100;

-- ---------------------------------------------------
-- PROFIL: LOW_TECH (Manque de pratique)
-- ---------------------------------------------------
INSERT INTO recommendation (id, title, type, url, target_profile) 
VALUES (1, 'TryHackMe: Pre-Security Path', 'EXERCICE', 'https://tryhackme.com/path/outline/presecurity', 'LOW_TECH');

INSERT INTO recommendation (id, title, type, url, target_profile) 
VALUES (2, 'Root-Me: Challenges Réseau', 'EXERCICE', 'https://www.root-me.org/fr/Challenges/Reseau/', 'LOW_TECH');

INSERT INTO recommendation (id, title, type, url, target_profile) 
VALUES (3, 'Livre: "Le Guide du Hackeur (Linux)"', 'LIVRE', 'https://www.amazon.fr/Hacking-Art-Exploitation-Jon-Erickson/dp/1593271441', 'LOW_TECH');

-- ---------------------------------------------------
-- PROFIL: LOW_THEORY (Manque de théorie/normes)
-- ---------------------------------------------------
INSERT INTO recommendation (id, title, type, url, target_profile) 
VALUES (4, 'Certification: CompTIA Security+', 'CERTIFICATION', 'https://www.comptia.org/certifications/security', 'LOW_THEORY');

INSERT INTO recommendation (id, title, type, url, target_profile) 
VALUES (5, 'ANSSI: Guide d hygiène informatique', 'LIVRE', 'https://www.ssi.gouv.fr/guide/guide-dhygiene-informatique/', 'LOW_THEORY');

INSERT INTO recommendation (id, title, type, url, target_profile) 
VALUES (6, 'Cours: ISO 27001 Fundamentals', 'EXERCICE', 'https://www.udemy.com/topic/iso-27001/', 'LOW_THEORY');

-- ---------------------------------------------------
-- PROFIL: HIGH_ALL (Profil équilibré / Avancé)
-- ---------------------------------------------------
INSERT INTO recommendation (id, title, type, url, target_profile) 
VALUES (7, 'Certification: OSCP (OffSec Certified Professional)', 'CERTIFICATION', 'https://www.offsec.com/courses/pen-200/', 'HIGH_ALL');

INSERT INTO recommendation (id, title, type, url, target_profile) 
VALUES (8, 'Livre: "Red Team Field Manual (RTFM)"', 'LIVRE', 'https://www.amazon.com/Rtfm-Red-Team-Field-Manual/dp/1494295504', 'HIGH_ALL');

INSERT INTO recommendation (id, title, type, url, target_profile) 
VALUES (9, 'Hack The Box: Pro Labs', 'EXERCICE', 'https://www.hackthebox.com/hacker/pro-labs', 'HIGH_ALL');

-- ==================================================================================
-- NOUVELLES QUESTIONS CERTIFIANTES (IDs 100+)
-- Ref: CISSP, CEH, SECURITY+
-- Poids: 5 points
-- ==================================================================================

ALTER SEQUENCE answers_option_id_seq RESTART WITH 1000;

-- Q100 (CISSP - Sécurité Logicielle)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (100, 'Dans le modèle Bell-LaPadula, quelle règle interdit à un sujet de lire des données d un niveau de sécurité supérieur ?', 'THEORY', 'HARD', 5, 'CISSP');

INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Simple Security Property (No Read Up)', true, 100);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('The * Property (No Write Down)', false, 100);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Biba Integrity Model', false, 100);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Clark-Wilson Model', false, 100);

-- Q101 (CEH - Reconnaissance)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (101, 'Quel type de scan Nmap utilise le flag -sS et est souvent appelé "Stealth Scan" ?', 'TECHNIQUE', 'HARD', 5, 'CEH');

INSERT INTO answers_option (text, is_correct, question_id) VALUES ('TCP Connect Scan', false, 101);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('UDP Scan', false, 101);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('SYN Half-open Scan', true, 101);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('XMAS Scan', false, 101);

-- Q102 (Security+ - Cryptographie)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (102, 'Lequel des algorithmes suivants est considéré comme un algorithme de hachage sécurisé ?', 'THEORY', 'MEDIUM', 3, 'SEC_PLUS');

INSERT INTO answers_option (text, is_correct, question_id) VALUES ('MD5', false, 102);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('SHA-256', true, 102);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('AES', false, 102);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('RSA', false, 102);

-- Q103 (CISSP - Gestion des Risques)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (103, 'Quelle est la formule pour calculer l ALE (Annualized Loss Expectancy) ?', 'THEORY', 'HARD', 5, 'CISSP');

INSERT INTO answers_option (text, is_correct, question_id) VALUES ('SLE * ARO', true, 103);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('AV * EF', false, 103);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('TCO / ROI', false, 103);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('SLE / ARO', false, 103);

-- Q104 (CEH - Web App)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (104, 'Quelle vulnérabilité du TOP 10 OWASP permet à un attaquant d exécuter des scripts dans le navigateur de la victime ?', 'TECHNIQUE', 'MEDIUM', 3, 'CEH');

INSERT INTO answers_option (text, is_correct, question_id) VALUES ('SQL Injection', false, 104);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Cross-Site Scripting (XSS)', true, 104);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('CSRF', false, 104);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('IDOR', false, 104);

-- Q105 (Security+ - Réseau)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (105, 'Quel port est utilisé par défaut pour le protocole RDP (Remote Desktop Protocol) ?', 'TECHNIQUE', 'EASY', 2, 'SEC_PLUS');

INSERT INTO answers_option (text, is_correct, question_id) VALUES ('22', false, 105);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('443', false, 105);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('3389', true, 105);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('8080', false, 105);

-- Q106 (CISSP - Sécurité Physique)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (106, 'Quel type d extincteur doit être utilisé pour un feu d équipement électrique (Classe C) ?', 'THEORY', 'MEDIUM', 3, 'CISSP');

INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Eau', false, 106);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('CO2 ou Gaz inerte', true, 106);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Mousse', false, 106);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Poudre de métal', false, 106);

-- Q107 (CEH - Sniffing)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (107, 'Quelle technique consiste à inonder la table CAM d un switch pour le forcer à agir comme un hub ?', 'TECHNIQUE', 'HARD', 5, 'CEH');

INSERT INTO answers_option (text, is_correct, question_id) VALUES ('ARP Spoofing', false, 107);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('MAC Flooding', true, 107);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('DHCP Starvation', false, 107);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('DNS Poisoning', false, 107);

-- Q108 (CISSP - IAM)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (108, 'Kerberos utilise un système de tickets pour l authentification. Quel composant délivre le TGT ?', 'THEORY', 'HARD', 5, 'CISSP');

INSERT INTO answers_option (text, is_correct, question_id) VALUES ('KDC (Key Distribution Center)', true, 108);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Resource Server', false, 108);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('LDAP', false, 108);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Active Directory', false, 108);

-- Q109 (CEH - Malware)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (109, 'Quel type de malware demande une rançon pour déchiffrer les fichiers de l utilisateur ?', 'THEORY', 'EASY', 2, 'CEH');

INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Spyware', false, 109);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Rootkit', false, 109);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Ransomware', true, 109);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Worm', false, 109);

-- Q110 (CISSP – Stateful Firewall)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (110, 'Quel type de pare-feu analyse l’état des connexions pour prendre des décisions de filtrage ?', 'THEORY', 'MEDIUM', 3, 'CISSP');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Packet-filtering Firewall', false, 110);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Stateful Firewall', true, 110);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Application Proxy Firewall', false, 110);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Next-Gen Firewall', false, 110);

-- Q111 (CEH – Clickjacking)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (111, 'Quel en-tête HTTP est essentiel pour atténuer les attaques de type Clickjacking ?', 'TECHNIQUE', 'MEDIUM', 5, 'CEH');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('X-Frame-Options', true, 111);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Content-Type', false, 111);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Cache-Control', false, 111);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Strict-Transport-Security', false, 111);

-- Q112 (Security+ – Email)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (112, 'Quel protocole permet un chiffrement de bout en bout pour sécuriser les emails ?', 'THEORY', 'MEDIUM', 3, 'SEC_PLUS');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('POP3', false, 112);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('SMTP', false, 112);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('PGP', true, 112);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('IMAP', false, 112);

-- Q113 (CISSP – Crypto)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (113, 'Quel type de chiffrement repose sur une seule clé pour chiffrer et déchiffrer ?', 'THEORY', 'EASY', 2, 'CISSP');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Chiffrement symétrique', true, 113);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Chiffrement asymétrique', false, 113);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Hachage', false, 113);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('ECC', false, 113);

-- Q114 (CEH – Network Sniffing)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (114, 'Quel outil est utilisé pour capturer des mots de passe sur un réseau non chiffré ?', 'TECHNIQUE', 'HARD', 5, 'CEH');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Wireshark', true, 114);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Burp Suite', false, 114);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Nikto', false, 114);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('OpenVAS', false, 114);

-- Q115 (Security+ – Patching)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (115, 'Quel type de mise à jour corrige une faille critique de sécurité ?', 'THEORY', 'EASY', 2, 'SEC_PLUS');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Feature update', false, 115);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Security patch', true, 115);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Service pack', false, 115);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Rollback update', false, 115);

-- Q116 (CISSP – Modèle OSI)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (116, 'À quelle couche du modèle OSI appartiennent les routeurs ?', 'THEORY', 'EASY', 2, 'CISSP');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Couche 2', false, 116);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Couche 3', true, 116);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Couche 4', false, 116);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Couche 7', false, 116);

-- Q117 (CISSP – Biometrie)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (117, 'Quel indicateur biométrique combine FAR et FRR pour évaluer la performance d’un système ?', 'THEORY', 'MEDIUM', 3, 'CISSP');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('CER (Cross-over Error Rate)', true, 117);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('MTTF', false, 117);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('ALE', false, 117);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('SLE', false, 117);

-- Q118 (CEH – Buffer Overflow)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (118, 'Quel élément est généralement injecté lors d une attaque Buffer Overflow ?', 'TECHNIQUE', 'HARD', 5, 'CEH');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Shellcode', true, 118);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Metadata', false, 118);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('CSS Payload', false, 118);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('SQL query', false, 118);

-- Q119 (Security+ – Attaques)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (119, 'Quel type d attaque consiste à intercepter et modifier les communications entre deux parties ?', 'THEORY', 'MEDIUM', 3, 'SEC_PLUS');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Phishing', false, 119);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Man-in-the-Middle', true, 119);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Brute force', false, 119);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Rogue AP', false, 119);

-- Q120 (CISSP – Risques)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (120, 'Quel terme désigne la réduction délibérée de l’impact ou de la probabilité d un risque ?', 'THEORY', 'MEDIUM', 3, 'CISSP');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Évitement', false, 120);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Atténuation', true, 120);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Transfert', false, 120);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Acceptation', false, 120);

-- Q121 (CEH – Scans)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (121, 'Quel type de scan Nmap identifie les ports sans établir de connexion complète ?', 'TECHNIQUE', 'HARD', 5, 'CEH');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('TCP Connect', false, 121);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('SYN Scan', true, 121);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('FIN Scan', false, 121);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('NULL Scan', false, 121);

-- Q122 (CISSP – IAM)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (122, 'Quelle méthode d authentification repose sur deux facteurs distincts ?', 'THEORY', 'MEDIUM', 3, 'CISSP');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('MFA', true, 122);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Password only', false, 122);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('PIN only', false, 122);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Biométrie seule', false, 122);

-- Q123 (Security+ – Wi-Fi)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (123, 'Quel protocole Wi-Fi est considéré comme le plus sécurisé actuellement ?', 'THEORY', 'MEDIUM', 3, 'SEC_PLUS');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('WEP', false, 123);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('WPA2', false, 123);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('WPA3', true, 123);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Open Wi-Fi', false, 123);

-- Q124 (CISSP – Politiques)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (124, 'Quel document définit les obligations de sécurité globales d une organisation ?', 'THEORY', 'MEDIUM', 3, 'CISSP');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Procédure', false, 124);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Politique de sécurité', true, 124);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Standard technique', false, 124);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Guide utilisateur', false, 124);

-- Q125 (CEH – IDS Evasion)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (125, 'Quel type de technique consiste à fragmenter un paquet pour contourner un IDS ?', 'TECHNIQUE', 'HARD', 5, 'CEH');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Packet fragmentation', true, 125);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('MAC spoofing', false, 125);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('DNS poisoning', false, 125);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('ARP injection', false, 125);

-- Q126 (Security+ – PKI)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (126, 'Quelle entité délivre les certificats numériques dans une infrastructure PKI ?', 'THEORY', 'EASY', 2, 'SEC_PLUS');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('OCSP', false, 126);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('CA (Certificate Authority)', true, 126);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('CRL Server', false, 126);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Directory Server', false, 126);

-- Q127 (CISSP – BCP/DRP)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (127, 'Quel paramètre définit la durée maximale d indisponibilité acceptable d un service ?', 'THEORY', 'MEDIUM', 3, 'CISSP');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('RPO', false, 127);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('RTO', true, 127);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('MTBF', false, 127);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('ALE', false, 127);

-- Q128 (CEH – SQL Injection)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (128, 'Quel caractère est souvent utilisé pour casser une requête SQL vulnérable ?', 'TECHNIQUE', 'MEDIUM', 5, 'CEH');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('''', true, 128);
INSERT INTO answers_option (text, is_correct, question_id) VALUES (';', false, 128);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('%', false, 128);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('#', false, 128);

-- Q129 (Security+ – Cloud)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (129, 'Quel modèle cloud donne le plus haut niveau de contrôle au client ?', 'THEORY', 'MEDIUM', 3, 'SEC_PLUS');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('SaaS', false, 129);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('PaaS', false, 129);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('IaaS', true, 129);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('FaaS', false, 129);

-- Q130 (CISSP – Contrôle d’accès)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (130, 'Quel modèle de contrôle d accès repose sur la classification des objets et des sujets ?', 'THEORY', 'HARD', 5, 'CISSP');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('DAC', false, 130);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('MAC', true, 130);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('RBAC', false, 130);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('ABAC', false, 130);

-- Q131 (CEH – Reconnaissance passive)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (131, 'Quel outil est couramment utilisé pour collecter des informations passives via les DNS ?', 'TECHNIQUE', 'EASY', 2, 'CEH');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Dig', true, 131);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Hydra', false, 131);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Nessus', false, 131);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Metasploit', false, 131);

-- Q132 (Security+ – Logs)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (132, 'Quel système centralise les logs provenant de multiples sources ?', 'THEORY', 'MEDIUM', 3, 'SEC_PLUS');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('SIEM', true, 132);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Syslog local', false, 132);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('IDS', false, 132);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Firewall', false, 132);

-- Q133 (CISSP – Secure SDLC)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (133, 'Quelle phase du SDLC inclut les tests de sécurité ?', 'THEORY', 'MEDIUM', 3, 'CISSP');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Conception', false, 133);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Déploiement', false, 133);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Vérification', true, 133);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Maintenance', false, 133);

-- Q134 (CEH – Wi-Fi Attacks)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (134, 'Quelle attaque Wi-Fi consiste à envoyer de fausses trames de désauthentification ?', 'TECHNIQUE', 'HARD', 5, 'CEH');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Deauthentication attack', true, 134);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Beacon flooding', false, 134);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Probe request spam', false, 134);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('WEP cracking', false, 134);

-- Q135 (Security+ – Vulnérabilités)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (135, 'Quel outil est principalement utilisé pour identifier les vulnérabilités au sein d un système ?', 'TECHNIQUE', 'MEDIUM', 3, 'SEC_PLUS');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Nessus', true, 135);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Aircrack-ng', false, 135);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Nikto', false, 135);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Tcpdump', false, 135);

-- Q136 (CISSP – Cryptanalyse)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (136, 'Quel type d attaque consiste à analyser des cryptogrammes pour tenter de retrouver le texte clair ?', 'THEORY', 'HARD', 5, 'CISSP');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Ciphertext-only attack', true, 136);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Chosen-plaintext attack', false, 136);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Birthday attack', false, 136);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Fault injection attack', false, 136);

-- Q137 (CEH – Pivoting)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (137, 'Quel terme décrit la technique permettant d utiliser une machine compromise pour attaquer d autres machines ?', 'TECHNIQUE', 'HARD', 5, 'CEH');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Pivoting', true, 137);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Scanning', false, 137);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Enumeration', false, 137);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Sniffing', false, 137);

-- Q138 (Security+ – Segmentation réseau)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (138, 'Quel mécanisme permet de diviser un réseau en segments isolés ?', 'THEORY', 'MEDIUM', 3, 'SEC_PLUS');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('VLAN', true, 138);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('ARP', false, 138);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('SNMP', false, 138);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('DHCP', false, 138);

-- Q139 (CISSP – Ressources humaines)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (139, 'Quel document doit obligatoirement être signé par un employé avant d accéder aux systèmes sensibles ?', 'THEORY', 'EASY', 2, 'CISSP');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('NDA (Non-Disclosure Agreement)', true, 139);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Lettre de motivation', false, 139);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Procédure interne', false, 139);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Description de poste', false, 139);

-- Q140 (CEH – Malware)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (140, 'Quel malware utilise une machine infectée comme relais pour diffuser d autres infections ?', 'THEORY', 'MEDIUM', 3, 'CEH');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Botnet', true, 140);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Adware', false, 140);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Spyware', false, 140);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Trojan', false, 140);

-- Q141 (Security+ – DDoS)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (141, 'Quel type d attaque consiste à saturer un service avec un volume massif de requêtes ?', 'THEORY', 'MEDIUM', 3, 'SEC_PLUS');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('DDoS', true, 141);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('MITM', false, 141);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Phishing', false, 141);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('XSS', false, 141);

-- Q142 (CISSP – Routage)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (142, 'Quel protocole de routage est basé sur l’algorithme SPF (Shortest Path First) ?', 'THEORY', 'HARD', 5, 'CISSP');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('OSPF', true, 142);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('RIP', false, 142);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('BGP', false, 142);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('EIGRP', false, 142);

-- Q143 (CEH – MITM)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (143, 'Quelle technique repose sur la falsification d adresses MAC pour intercepter le trafic ?', 'TECHNIQUE', 'HARD', 5, 'CEH');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('ARP Spoofing', true, 143);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('MAC Flooding', false, 143);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('DNS Hijacking', false, 143);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('SSL Stripping', false, 143);

-- Q144 (Security+ – Access Control)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (144, 'Quel principe consiste à limiter les accès d un utilisateur au strict nécessaire ?', 'THEORY', 'EASY', 2, 'SEC_PLUS');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Principe de moindre privilège', true, 144);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Single Sign-On', false, 144);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Principe du besoin de savoir', false, 144);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Zéro confiance', false, 144);

-- Q145 (CISSP – Sécurité physique)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (145, 'Quel mécanisme de sécurité empêche un véhicule non autorisé d entrer dans une zone protégée ?', 'THEORY', 'MEDIUM', 3, 'CISSP');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Bollards', true, 145);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('CCTV', false, 145);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Badge RFID', false, 145);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Tripwire', false, 145);

-- Q146 (CEH – OWASP)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (146, 'Quelle vulnérabilité OWASP permet à un attaquant d injecter des commandes système ?', 'TECHNIQUE', 'HARD', 5, 'CEH');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Command Injection', true, 146);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('XSS', false, 146);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('CSRF', false, 146);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('IDOR', false, 146);

-- Q147 (Security+ – IoT)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (147, 'Quel est le principal risque associé aux appareils IoT mal sécurisés ?', 'THEORY', 'MEDIUM', 3, 'SEC_PLUS');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Botnet', true, 147);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Spam', false, 147);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Phishing', false, 147);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Cross-site scripting', false, 147);

-- Q148 (CISSP – Audit)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (148, 'Quel principe d’audit garantit que les actions d’un utilisateur peuvent être retracées ?', 'THEORY', 'HARD', 5, 'CISSP');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Traçabilité', true, 148);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Disponibilité', false, 148);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Redondance', false, 148);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Haute disponibilité', false, 148);

-- Q149 (CEH – Metasploit)
INSERT INTO questions (id, text, categorie, difficulty, points_weight, exam_ref) 
VALUES (149, 'Quel module Metasploit est utilisé pour exécuter du code malveillant sur une cible après exploitation ?', 'TECHNIQUE', 'MEDIUM', 5, 'CEH');
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Payload', true, 149);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Auxiliary', false, 149);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Exploit', false, 149);
INSERT INTO answers_option (text, is_correct, question_id) VALUES ('Encoder', false, 149);

-- CHALLENGES ARENA
INSERT INTO challenges (id, name, description, flag_secret, points_reward) 
VALUES ('CTF_LINUX_1', 'Mission Alpha-1', 'Récupérer le fichier shadow', 'CTF{LINUX_MASTER_2025}', 50);