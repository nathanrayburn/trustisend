## PDG - Cahier des Charges pour TrustiSend (Java)

### **Organisation de la Première Semaine pour TrustiSend (Java)**

#### **Objectifs de la Première Semaine**

L'objectif de la première semaine est de poser les bases solides du projet TrustiSend. Cela inclut la définition claire du projet, la planification de l'architecture, la création des premiers mockups, le choix des technologies (y compris l'intégration complète avec Google App Engine et Java Spring Boot), ainsi que la mise en place des outils et des processus de travail nécessaires pour le développement.

### **Tâches à Réaliser**

#### 1. **Description du Projet**

- **Objectif** : Définir clairement l'objectif de TrustiSend, qui est de permettre un transfert sécurisé de fichiers, incluant une analyse de malware avant l'envoi.
- 
# Description du projet:
Website de filesharing sécurisé qui scanne automatiquement les fichiers


## Exigences fonctionnelles:
- Un utilisateur pourra se connecter avec son compte google/github ou avec un compte du site
- Un utilisateur pourra uploader des fichiers et les chiffrer en toute confidentialité
- Un utilisateur pourra partager les fichiers qu'il a uploadé à l'aide d'une clé unique à chaque fichier
- Un utilisateur ou invité (personne pas connectée avec un compte) pourra télécharger un fichier grâce à la clé correspondante
- Les fichiers uploadés sont automatiquement scannés par un antivirus et les utilisateurs ne pourront pas partager les fichiers considérés commes dangereux
- Les fichiers qui ne sont pas téléchargés pendant 30 jours sont supprimés

## Exigences non-fonctionnelles:
- Le site doit être intuitif et utilisable sans connaissances préalable
- Le système ne stocke aucun fichier sous forme non-chiffrée
- Le système doit pouvoir être scalé pour supporter un grand nombre de fichiers/utilisateurs
- L'analyse de malware doit être rapide (moins de 5 minutes par fichier)
- Le système doit pouvoir gérer des fichiers de 1Go ou moins
- Le système doit être robuste face aux pannes et se relancer automatiquement
- Le système ne doit pas corrompre les fichiers stockés en cas de panne
- Le système limite le nombre d'uploads par utilisateur à 10 par jour

   **Responsables** : Toute l'équipe contribue à cette tâche pour s'assurer que chacun comprend les objectifs et les exigences.

#### 2. **Description Préliminaire de l'Architecture et les potentielles idées**

- **Architecture Backend** :
  - **Langage** : Java avec Spring Boot pour la gestion des API et la logique métier.
  - **Base de données** : Utilisation de Google Cloud Datastore (NoSQL) pour une approche flexible, ou Cloud SQL (MySQL/PostgreSQL) pour une base relationnelle.
  - **Sécurité** : Utilisation de Spring Security pour l'authentification et la protection des endpoints, intégration avec Google Identity Platform pour la gestion des utilisateurs et 2FA..
  - **Analyse de Malware** : Intégration avec des API externes comme VirusTotal via Google Cloud Functions pour une exécution asynchrone et scalable.

- **Architecture Frontend** :
  - **Langage** : HTML, CSS, on va trouver un framework CSS pour faciliter le design. ou des templates.
  - **Framework** : (Angular ou React.js pour une interface utilisateur réactive.)
  - **Design** : Utilisation de Figma pour les mockups et les prototypes.

- **Infrastructure** :
  - **Déploiement** : Déploiement sur Google App Engine pour une gestion automatique de la scalabilité et de la haute disponibilité.
  - **Stockage des fichiers** : Google Cloud Storage pour le stockage sécurisé des fichiers.
  - **CI/CD** : Utilisation de Google Cloud Build pour l'automatisation des tests et du déploiement.

   **Responsables** : Un membre de l'équipe se concentre sur l'architecture backend, un autre sur l'architecture frontend, et le reste de l'équipe sur l'infrastructure.

#### 3. **Mockups (Figma, papier-crayon, etc.) / Landing Page**

- **Création de Mockups** : Utiliser Figma pour concevoir l'interface utilisateur de l'application, y compris les écrans d'upload, de téléchargement, de gestion des fichiers, et de résultat de l'analyse.
- **Landing Page** : Création d'une landing page simple expliquant le service TrustiSend, avec un formulaire d'inscription pour les bêta-testeurs.

#### 4. **Description des Choix Techniques**

- **Langage de programmation** : Java avec Spring Boot pour le backend, React.js ou Angular pour le frontend.
- **Base de données** : Google Cloud Datastore pour la flexibilité ou Cloud SQL pour la gestion relationnelle des données.
- **Outils de sécurité** : Spring Security pour l'authentification, intégration avec Google Identity Platform pour la gestion des utilisateurs et le 2FA.
- **Cloud** : Google App Engine pour le déploiement de l'application, Google Cloud Storage pour le stockage des fichiers, et Google Cloud Functions ou pour l'analyse des malwares.
- **CI/CD** : Google Cloud Build pour l'automatisation des tests et du déploiement.

#### 5. **Description du Processus de Travail (Git Flow, DevOps, etc.)**

- **Gestion des versions** : Utiliser Git avec GitFlow pour gérer les branches et les versions.
- **Suivi des tâches** : Mise en place de Jira ou de la gestion des projets sur GitHub (Kanban) pour suivre les tâches et les bugs.
- **Revue de code** : Mettre en place un processus de revue de code sur GitHub.

#### 6. **Mise en Place des Outils de Développement (VCM, Issue Tracker)**

- **VCM** : Mise en place d'un dépôt GitHub privé pour le code source.
- **Issue Tracker** : Configuration de GitHub Issues ou Jira pour le suivi des tâches.

#### 7. **Mise en Place d’un Environnement de Déploiement**

- **Docker** : Création des fichiers Docker pour le backend et le frontend, afin de garantir la portabilité et la cohérence des environnements.
- **Base de données** : Utilisation de Google Cloud SQL ou Cloud Datastore pour le déploiement de la base de données.

#### 8. **Mise en Place d’un Pipeline de Livraison et de Déploiement (CI/CD)**

- **Pipeline CI/CD** : Configuration d'un pipeline CI/CD avec Google Cloud Build pour automatiser les tests et les déploiements sur Google App Engine.
- **Tests automatiques** : Écrire des tests unitaires et d'intégration pour vérifier le bon fonctionnement du pipeline.

#### 9. **Démonstration du Déploiement d’une Modification**

- Préparer une démonstration montrant comment une modification du code (par exemple, un changement sur la landing page) déclenche automatiquement un pipeline CI/CD qui déploie la modification sur un environnement de test.

   **Responsables** : Toute l'équipe peut participer à cette démonstration, avec une préparation dirigée par le responsable CI/CD.

### **Planification des 3 Premières Semaines**

1. **Semaine 1** : Mise en place des bases, choix technologiques, et configuration des outils.
2. **Semaine 2** : Développement des fonctionnalités principales (transfert de fichiers, analyse de malware, authentification avec 2FA).
3. **Semaine 3** : Finalisation, tests, documentation, et préparation pour la démonstration.

---

### **Calendrier et Répartition des Tâches pour la Première Semaine**

| **Jour**     | **Objectifs Journaliers**                                                                                                           | **Personne 1 Leader**                                   | **Personne 2 (Backend)**                                 | **Personne 3 (Frontend/UI-UX)**                          | **Personne 4 (DevOps/Infra)**                           |
|--------------|-------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------|----------------------------------------------------------|----------------------------------------------------------|----------------------------------------------------------|
| **Lundi**    |Définition du projet et rédaction des exigences <br> Rédaction du pseudo cahier des charges                       | Rédaction et validation des objectifs                | Début de l'architecture backend                          | Esquisses UI et planification des mockups                | Exploration des options d'infrastructure sur Google Cloud                 |
| **Mardi**    | Finaliser l'architecture <br> Continuer les mockups et commencer la landing page                                                 | Finaliser la description du projet et valider l'architecture       | Définition des schémas de données                         | Création des mockups détaillés et début de la landing page | Configuration des outils de développement (VCM, Tracker) |
| **Mercredi** | Finaliser les mockups et la landing page <br> Rédiger la description des choix techniques                                        | Validation des mockups et de la landing page                       | Rédaction des choix techniques backend                 | Finalisation des mockups et coordination frontend/backend | Configuration de l'environnement de déploiement sur Google App Engine          |
| **Jeudi**    | Mise en place de l’environnement de déploiement <br> Mise en place du pipeline CI/CD                                             | Supervision de la mise en place de l'environnement et du pipeline  | Intégration backend avec CI/CD                             | Intégration frontend dans l'environnement de déploiement | Mise en place et tests du pipeline CI/CD avec Google Cloud Build                 |
| **Vendredi** | Démonstration du déploiement <br> Réunion de récapitulation et planification pour la semaine suivante                            | Organisation de la démonstration et finalisation des livrables     | Test et démonstration du backend via le pipeline CI/CD    | Test de l'intégration frontend et démonstration          | Finalisation du pipeline CI/CD pour la démonstration     |
---
