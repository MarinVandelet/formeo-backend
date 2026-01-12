# TXLFORMA – Backend
## Présentation du projet

TXLFORMA est une plateforme web de gestion de formations en présentiel destinée aux entreprises et aux particuliers.
Ce dépôt correspond à la partie backend de l’application, développée avec Spring Boot et exposant une API REST sécurisée, consommée par un frontend React.

Le backend prend en charge :

l’authentification et la gestion des utilisateurs,

la gestion des formations, catégories et sessions,

l’inscription aux formations,

le paiement en ligne,

le suivi de présence (émargement numérique),

les évaluations des participants,

la génération d’attestations PDF.

## Architecture générale

L’application suit une architecture en couches :

Controller
   ↓
Service
   ↓
Repository
   ↓
Base de données


Des DTO et Mappers sont utilisés afin de séparer les modèles internes des données exposées via l’API.

## Technologies utilisées

Spring Boot – Framework principal

Spring Security – Sécurisation de l’API

JWT (JSON Web Token) – Authentification stateless

Spring Data JPA – Accès aux données

BCrypt – Hashage des mots de passe

Stripe – Paiement en ligne

OpenPDF – Génération des attestations PDF

REST API – Communication frontend / backend

DTO / Mapper Pattern – Structuration des échanges

## Sécurité et authentification

Authentification basée sur JWT

Mots de passe chiffrés avec BCrypt

Filtrage des requêtes via un JwtAuthFilter

Gestion des rôles utilisateurs (enum Role)

Flux d’authentification :

Inscription ou connexion

Génération d’un token JWT

Envoi du token dans le header Authorization

Accès sécurisé aux endpoints protégés

# Fonctionnalités principales
## Utilisateurs

Inscription

Connexion

Gestion des rôles

Sécurisation des accès

## Formations

Catégories de formations

Liste des formations par catégorie

Sessions avec dates et capacité limitée

## Inscriptions

Inscription à une session

Vérification des règles métier :

une seule formation à la fois

paiement obligatoire

nombre de places limité

## Paiement

Paiement en ligne via Stripe

Validation automatique de l’inscription après paiement

## Émargement

Suivi numérique de la présence

Association utilisateur / session / date

📊 Évaluations

Attribution d’une note par session

Stockage en base de données

📄 Attestations

Génération automatique de PDF :

Attestation de succès (note ≥ 10)

Attestation de présence (note < 10)

⚙️ Lancement du projet
Prérequis

Java 17+

Maven

Base de données (MySQL / PostgreSQL)

Compte Stripe (clé API)

Installation
git clone https://github.com/ton-repo/txlforma-backend.git
cd txlforma-backend
mvn clean install
mvn spring-boot:run


L’API est accessible par défaut sur :

http://localhost:8080

🧪 Tests et monitoring

Endpoint de santé disponible :

GET /health


Gestion centralisée des erreurs via ApiExceptionHandler

🚀 Déploiement

Le backend est conçu pour être :

déployé sur un serveur cloud (AWS, OVH, Azure),

conteneurisé avec Docker,

interfacé avec une application mobile à terme.

📌 Auteur

Projet réalisé dans le cadre d’un projet de formation / académique
Backend développé avec Spring Boot

Si tu veux, je peux aussi :

te fournir la version README en anglais,

l’adapter pour un rendu scolaire,

ou te faire un README avec badges GitHub (Spring, Java, Stripe, etc.).

Dis-moi 🔥
