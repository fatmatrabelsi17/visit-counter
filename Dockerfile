# =================================================================
# Dockerfile Visit-Counter (Spring Boot + Redis)
# Amélioration du Dockerfile du tuto (FROM openjdk:11 + ADD target/app.jar) :
#   - le JAR est compilé DANS Docker (pas besoin de Java/Maven sur le PC)
#   - image finale Alpine + JRE seulement (pas de JDK, pas de Maven)
#   - utilisateur non-root, cache des dépendances Maven
# =================================================================

# -----------------------------------------------------------------
# Étape 1 : "build" — compile l'application avec Maven
# -----------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build

WORKDIR /build

# On copie UNIQUEMENT pom.xml d'abord, puis on télécharge les dépendances.
# Tant que pom.xml ne change pas, Docker réutilise cette couche en cache :
# modifier le code Java ne retélécharge pas les dépendances.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Le code source est copié ensuite (il change souvent).
COPY src ./src

# Les tests sont exécutés par le pipeline Jenkins (Partie C) avant le build
# de l'image : on ne les rejoue pas ici.
RUN mvn -B -q package -DskipTests

# -----------------------------------------------------------------
# Étape 2 : image finale — seulement le JRE + le JAR
# -----------------------------------------------------------------
# eclipse-temurin:17-jre-alpine :
#   - "jre" : pas de compilateur (javac) ni d'outils de dev → plus léger
#   - "alpine" : distribution minimale (~5 Mo) → moins de paquets, moins
#     de failles, image plus rapide à télécharger
#   - Temurin remplace l'image "openjdk" du tuto, aujourd'hui dépréciée
# Résultat : ~200 Mo contre ~650 Mo pour openjdk:11 (Debian + JDK).
FROM eclipse-temurin:17-jre-alpine

# Utilisateur système non privilégié : si l'application est compromise,
# l'attaquant n'a pas les droits root dans le conteneur.
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# On récupère uniquement le JAR produit à l'étape 1.
COPY --from=build --chown=appuser:appgroup /build/target/app.jar app.jar

# Bascule vers l'utilisateur non-root.
USER appuser

# Valeurs par défaut, surchargées par docker run -e / docker compose (.env)
ENV APP_PORT=8080 \
    REDIS_HOST=db-service \
    REDIS_PORT=6379 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75"

# Port > 1024 : un utilisateur non-root ne peut pas écouter sur le port 80.
EXPOSE 8080

# Docker vérifie régulièrement que l'application répond.
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s \
  CMD wget -qO- http://127.0.0.1:${APP_PORT}/health || exit 1

# CMD : commande exécutée au DÉMARRAGE du conteneur (RUN, lui, s'exécute au build).
# "exec" fait de java le processus principal (PID 1) → il reçoit bien les
# signaux d'arrêt de docker stop.
CMD ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
