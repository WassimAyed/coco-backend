# CoCo Backend – Spring Boot Microservices (FR/EN)

## Table of Contents / Sommaire
- [Français](#francais)
	- [Objectif](#fr-objectif)
	- [Services](#fr-services)
	- [Ports observés](#fr-ports-observes)
	- [Prérequis](#fr-prerequis)
	- [Lancement](#fr-lancement)
	- [Intégration IA](#fr-integration-ia)
	- [Qualité / Ops](#fr-qualite-ops)
- [English](#english)
	- [Purpose](#en-purpose)
	- [Services](#en-services)
	- [Observed ports](#en-observed-ports)
	- [Requirements](#en-requirements)
	- [Run](#en-run)
	- [AI integration](#en-ai-integration)
	- [Quality / Operations](#en-quality-operations)

<a id="francais"></a>
## 🇫🇷 Français

<a id="fr-objectif"></a>
### 1) Objectif
Le backend CoCo suit une architecture **microservices** pour couvrir les domaines métiers étudiants (auth, paiements, collocation, Lost&Found, événement, etc.).

Technologies:
- **Java 17**
- **Spring Boot / Spring Cloud**
- **Maven / `mvnw`**
- **Eureka** (discovery)
- **API Gateway** (entry point)

<a id="fr-services"></a>
### 2) Services
Répertoire: `backendCoCo_DevDynamos/`

- `eurekaServer`
- `apiGateway`
- `userSecurityService`
- `subsPaymentService`
- `lostFoundService`
- `collocationService`
- `covoiturageService`
- `eventService`
- `realEstateService`
- `serviceEtudiant`

<a id="fr-ports-observes"></a>
### 3) Ports observés
- Eureka: `6511`
- Gateway: `9092`
- User Security: `8090`
- Subs Payment: `8085`
- Lost&Found: `8086`
- Collocation: `8091`
- Covoiturage: `8092`
- Real Estate: `8094`
- Service Etudiant: `8100`

Note: `eventService` possède actuellement un `application.properties.example`.

<a id="fr-prerequis"></a>
### 4) Prérequis
- JDK 17
- Maven (ou wrapper `./mvnw`)
- base de données configurée pour chaque service

<a id="fr-lancement"></a>
### 5) Lancement
- mode global recommandé (depuis la racine du projet parent): `./run-all.sh`
- ou service par service: `./mvnw spring-boot:run`

Ordre conseillé: Eureka → Gateway → User Security → services métier.

<a id="fr-integration-ia"></a>
### 6) Intégration IA
`lostFoundService` peut utiliser `AI_SIMILARITY_URL` (ex: `http://localhost:8000`).

<a id="fr-qualite-ops"></a>
### 7) Qualité / Ops
- renforcer tests unitaires + intégration,
- standardiser profils `dev/test/prod`,
- exposer `/actuator/health` partout,
- superviser logs et métriques.

---

<a id="english"></a>
## 🇬🇧 English

<a id="en-purpose"></a>
### 1) Purpose
The CoCo backend uses a **microservices architecture** to support student-focused business domains (auth, payments, colocation, Lost&Found, events, etc.).

Technologies:
- **Java 17**
- **Spring Boot / Spring Cloud**
- **Maven / `mvnw`**
- **Eureka** (service discovery)
- **API Gateway** (single entry point)

<a id="en-services"></a>
### 2) Services
Directory: `backendCoCo_DevDynamos/`

- `eurekaServer`
- `apiGateway`
- `userSecurityService`
- `subsPaymentService`
- `lostFoundService`
- `collocationService`
- `covoiturageService`
- `eventService`
- `realEstateService`
- `serviceEtudiant`

<a id="en-observed-ports"></a>
### 3) Observed ports
- Eureka: `6511`
- Gateway: `9092`
- User Security: `8090`
- Subs Payment: `8085`
- Lost&Found: `8086`
- Collocation: `8091`
- Covoiturage: `8092`
- Real Estate: `8094`
- Student Service: `8100`

Note: `eventService` currently uses an `application.properties.example` file.

<a id="en-requirements"></a>
### 4) Requirements
- JDK 17
- Maven (or wrapper `./mvnw`)
- configured database per service

<a id="en-run"></a>
### 5) Run
- recommended global mode (from parent root): `./run-all.sh`
- or per service: `./mvnw spring-boot:run`

Recommended order: Eureka → Gateway → User Security → business services.

<a id="en-ai-integration"></a>
### 6) AI integration
`lostFoundService` can consume `AI_SIMILARITY_URL` (e.g. `http://localhost:8000`).

<a id="en-quality-operations"></a>
### 7) Quality / Operations
- strengthen unit + integration tests,
- standardize `dev/test/prod` profiles,
- expose `/actuator/health` consistently,
- monitor logs and metrics.
