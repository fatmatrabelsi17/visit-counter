// =================================================================
// Partie C : pipeline CI/CD Visit-Counter
// Déclenché à chaque commit sur la branche principale (main) :
//   1. build Maven + tests unitaires
//   2. build de l'image Docker (Partie B) + publication sur Docker Hub
// =================================================================
pipeline {
    agent any

    triggers {
        // Webhook GitHub (Settings > Webhooks > http://<jenkins>/github-webhook/)
        githubPush()
        // Repli si Jenkins tourne en local (non joignable par GitHub) : vérifie le dépôt chaque minute
        pollSCM('* * * * *')
    }

    environment {
        DOCKERHUB_USER = 'fatmatrabelsi17'
        IMAGE          = "${DOCKERHUB_USER}/counter-app"
        TAG            = "${env.BUILD_NUMBER}"
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }

        stage('Build & tests unitaires') {
            // Maven tourne dans un conteneur jetable (plugin Docker Pipeline) :
            // l'agent Jenkins n'a besoin que de Docker, pas de Java ni de Maven.
            agent {
                docker {
                    image 'maven:3.9.9-eclipse-temurin-17-alpine'
                    // Volume nommé : les dépendances Maven sont gardées d'un build à l'autre
                    args '-v maven-repo:/root/.m2'
                    reuseNode true
                }
            }
            steps {
                // Téléchargement des dépendances, réessayé en cas de coupure réseau
                retry(3) {
                    sh 'mvn -B -q dependency:go-offline'
                }
                // "package" compile, exécute les tests unitaires puis produit target/app.jar
                sh 'mvn -B clean package'
            }
            post {
                always { junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml' }
                success { archiveArtifacts artifacts: 'target/app.jar', fingerprint: true }
            }
        }

        stage('Build image Docker') {
            steps {
                // Réessayé si le téléchargement des images de base est interrompu
                retry(3) {
                    sh 'docker build -t $IMAGE:$TAG -t $IMAGE:latest .'
                }
            }
        }

        stage('Push Docker Hub') {
            // Uniquement pour la branche principale
            when { anyOf { branch 'main'; expression { env.BRANCH_NAME == null } } }
            steps {
                // Identifiants créés dans Jenkins : Manage Jenkins > Credentials
                // (type "Username with password", ID = dockerhub-creds, mot de passe = access token Docker Hub)
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                                                  usernameVariable: 'DH_USER',
                                                  passwordVariable: 'DH_TOKEN')]) {
                    sh 'echo "$DH_TOKEN" | docker login -u "$DH_USER" --password-stdin'
                    retry(3) {
                        sh 'docker push $IMAGE:$TAG'
                        sh 'docker push $IMAGE:latest'
                    }
                }
            }
        }
    }

    post {
        always {
            sh 'docker logout || true'
            sh 'docker image rm $IMAGE:$TAG || true'
        }
        success { echo "Image publiée : ${IMAGE}:${TAG}" }
        failure { echo 'Pipeline en échec : voir les logs ci-dessus.' }
    }
}
