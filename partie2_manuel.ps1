# Partie 2 : déploiement manuel (sans Compose) — à lancer dans PowerShell depuis ce dossier
# Usage : powershell -ExecutionPolicy Bypass -File .\partie2_manuel.ps1

docker build -t counter-app:1.0 .

docker network create --driver bridge app-network
docker volume create redis-data

# Redis : réseau app-network, nom db-service, volume redis-data monté sur /data
docker run -d --name db-service --network app-network -v redis-data:/data redis:7-alpine redis-server --appendonly yes

# Application : port 80 de l'hôte -> port 8080 du conteneur
docker run -d --name web --network app-network -p 80:8080 counter-app:1.0

Write-Host "Attente du démarrage de Spring Boot..."
Start-Sleep -Seconds 15
docker network inspect app-network

# --- Test de robustesse ---
(Invoke-WebRequest -UseBasicParsing http://localhost).Content
(Invoke-WebRequest -UseBasicParsing http://localhost).Content
docker rm -f db-service
docker run -d --name db-service --network app-network -v redis-data:/data redis:7-alpine redis-server --appendonly yes
Start-Sleep -Seconds 3
# Le compteur continue (3) au lieu de repartir à 1 :
(Invoke-WebRequest -UseBasicParsing http://localhost).Content

# Nettoyage avant la Partie 3 (Compose recrée le réseau ; le volume est gardé) :
# docker rm -f web db-service
# docker network rm app-network
