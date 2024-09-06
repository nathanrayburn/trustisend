# TrustiSend

## Description

**TrustiSend** (trʌst aɪ sɛnd) est une plateforme de transfert de fichiers sécurisée qui intègre une analyse anti-malware avant l'envoi des fichiers. L'application garantit la sécurité des données grâce à un chiffrement de bout en bout et à une authentification à deux facteurs (2FA).

## Fonctionnalités

- **Transfert sécurisé de fichiers** : Upload et téléchargement de fichiers avec chiffrement.
- **Analyse de malware** : Scan des fichiers pour détecter et éliminer les menaces potentielles.
- **Authentification 2FA** : Authentification à deux facteurs pour renforcer la sécurité utilisateur.
- **Interface utilisateur intuitive** : Design simple et réactif pour une expérience utilisateur optimale.

## Technologies Utilisées

- **Backend** : [Java Spring Boot](https://spring.io/projects/spring-boot) - Framework robuste pour le développement web en Java.
- **Frontend** : [Thymeleaf](https://www.thymeleaf.org/) - Moteur de template intégré à Spring Boot pour des vues dynamiques.
- **Base de données** : [Google Cloud Datastore](https://cloud.google.com/datastore) - Solution performante pour le stockage des données.
- **Infrastructure** : [Google App Engine](https://cloud.google.com/appengine) - Plateforme de déploiement offrant scalabilité et haute disponibilité.
- **CI/CD** : [Google Cloud Build](https://cloud.google.com/build) - Pipeline d'intégration et de déploiement continu.
- **Sécurité** : [Spring Security](https://spring.io/projects/spring-security) - Gestion d'accès et d'authentification intégrée dans Spring Boot.
- **Gestion des fichiers** : [Google Cloud Storage](https://cloud.google.com/storage) pour un stockage sécurisé.
- **Analyse de Malware** : [VirusTotal API](https://www.virustotal.com/) pour l'analyse des fichiers avant leur partage.

## Structure du Projet

- `/app` : Code source principal du projet.
- `/dev` : Codes temporaires pour les tests avant déploiement.
- `/docs` : Documentation du projet, incluant schémas d'architecture, cdc, et mockups.
- `/resources` : Fichiers de configuration et ressources statiques.
- `/tests` : Tests unitaires et d'intégration pour garantir la qualité du code.

## Installation et Configuration

### Prérequis

- **Docker**

### Configuration Cloud pour exécuter le projet Spring Boot en local

#### Création d'un compte et projet sur Google Cloud Console

1. Accéder à [Google Cloud Console](https://console.cloud.google.com/welcome?)
2. Créer un nouveau projet.
3. Sélectionner le nouveau projet.

#### Configuration Firestore

1. Rechercher Firestore dans Google Cloud Console.
2. Créer une base de données.
3. Dans "Configure your database" :
   - **Database ID** : `trustisend` (nom au choix)
   - **Location type** : Région -> `europe-west-1`
   - Laisser le reste par défaut.
4. Créer la base de données.

#### Configuration Cloud Storage

1. Rechercher Cloud Storage dans Google Cloud Console.
2. Créer un Bucket.
3. **Bucket name** : `storage-trustisend` (nom au choix).
4. Choisir l'emplacement des données : Région -> `europe-west-1`.
5. Laisser le reste par défaut.
6. Créer le Bucket.
7. Interdire l'accès public : "Enforce public access prevention on this bucket".

#### Création des Credentials

1. Rechercher "Credentials" dans Google Cloud Console.
2. Créer des credentials :
   - **Service Account Name** : `storage`
   - Ajouter le rôle : **Storage Admin**
3. Répéter l'opération pour Firestore :
   - **Service Account Name** : `firestore`
   - Ajouter le rôle : **Firestore Service Agent**
4. Télécharger les clés pour chaque Service Account :
   - Aller dans "Keys" de chaque Service Account.
   - Ajouter une clé JSON et la télécharger.

#### Récupérer l'ID du projet

1. Accéder à la page d'accueil principale.
2. Sélectionner le projet en haut de l'écran.

#### Cloner ou fork le dépôt

#### Configuration des variables d'environnement

```bash
# Chemin pour le fichier application.properties
A compléter
```

```yaml
spring.application.name=trustisend

project.id= # set project ID 
firebase.credentials.path= # set path to credentials
firebase.database.id= # set database name

gcp.credentials.path= # set path to credentials
gcp.bucket.id= # set bucket name

# Ne rien modifier ici
logging.level.org.springframework.security=TRACE
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=5GB
server.tomcat.connection-timeout=60000
server.tomcat.max-swallow-size=5GB
server.tomcat.max-http-form-post-size=5GB
spring.servlet.multipart.enabled=false 
logging.level.org.springframework.web.multipart=DEBUG
spring.resources.enabled=true
```

### Configuration de l'image Docker et Build

## Contribution

Pour contribuer au projet, merci de suivre les instructions de notre guide de contribution.

## Auteurs

- **Équipe TrustiSend** - Créé par Amir Mouti, Nathan Rayburn, Felix Breval et Ouweis Harun.

## Licence

Le projet est sous licence [à définir].

## Note
