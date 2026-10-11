# Visit-Counter – Spring Boot + Redis + Docker + Jenkins

Application web qui affiche « Bonjour ! Cette page a été vue X fois. Je suis le conteneur [ID] ». Le compteur `hits` est stocké dans Redis (`db-service`).

| Fichier | Rôle |
|---|---|
| `src/main/java/...` | Application Spring Boot (contrôleur + service Redis) |
| `src/test/java/...` | 5 tests unitaires (Redis simulé avec Mockito) |
| `Dockerfile` | Multi-stage Maven → `eclipse-temurin:17-jre-alpine`, non-root |
| `docker-compose.yml` + `.env` | web ×3 + Redis + load balancer Nginx |
| `nginx/default.conf.template` | Configuration du load balancer |
| `partie2_manuel.ps1` | Commandes de la Partie 2 (sans Compose) |
| `Jenkinsfile` | Pipeline : tests → image → Docker Hub |
| `jenkins/` | Jenkins local (avec Docker) |

Démarrage rapide :
```powershell
docker compose up -d --build --scale web=3
```
Puis ouvrir http://localhost

Pipeline CI/CD : build automatique a chaque commit sur main.
