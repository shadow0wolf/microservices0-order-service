/*
  Jenkinsfile
  ------------
  Pipeline-as-Code definition for user-service.

  Responsibilities:
  - Checkout source code
  - Build with Maven
  - Run unit tests & publish reports
  - Build Docker image
  - Verify Docker image exists locally

  Designed for:
  - Single branch pipeline
  - Jenkins agent with Docker + Maven installed
*/

pipeline {

    /*
     * Agent definition
     * ----------------
     * 'any' means Jenkins will pick any available agent.
     * In production, you may restrict this to a labeled node.
     */
    agent any

    /*
     * Global environment variables
     */
    environment {
        GIT_REPO_URL = 'https://github.com/shadow0wolf/microservices0-order-service.git'
        GIT_BRANCH   = 'main'

        IMAGE_NAME   = 'order-service'
        IMAGE_TAG    = "${env.BUILD_NUMBER}"   // Unique per build
        CLUSTER_NAME = 'clstrx'
    }

    /*
     * Pipeline options
     */
    options {
        disableConcurrentBuilds()  // Prevent parallel execution of same job
        timestamps()               // Add timestamps to console logs
    }

    /*
     * SCM polling trigger
     * Every 2 minutes
     */
    triggers {
        pollSCM('H/2 * * * *')
    }

    stages {

        /*
         * Stage 1: Checkout
         */
        stage('Checkout') {
            steps {
                echo "Checking out ${GIT_REPO_URL} @ ${GIT_BRANCH}"

                git branch: "${GIT_BRANCH}",
                    url: "${GIT_REPO_URL}"
            }
        }

        /*
         * Stage 2: Maven Build + Tests
         */
        stage('Maven Build') {
            steps {
                echo 'Running Maven build...'

                sh 'mvn clean package -DskipTests=false'
            }

            /*
             * Always publish test reports,
             * even if build fails.
             */
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        /*
         * Stage 3: Docker Build
         */
        stage('Docker Build') {
            steps {
                echo "Building Docker image: ${IMAGE_NAME}:${IMAGE_TAG}"

                sh """
                    docker build \
                        -t ${IMAGE_NAME}:${IMAGE_TAG} \
                        -t ${IMAGE_NAME}:latest \
                        .
                """
            }
        }

        /*
         * Stage 4: Verify Image
         */
        stage('Verify Image') {
            steps {
                echo 'Verifying image exists locally...'

                sh "docker image inspect ${IMAGE_NAME}:${IMAGE_TAG}"
                sh "docker images ${IMAGE_NAME}"
            }
        }

        stage('Import Image to k3d') {
                steps {
                    echo "Importing image into k3d cluster..."

                    sh """
                        k3d image import ${IMAGE_NAME}:${IMAGE_TAG} -c ${CLUSTER_NAME}
                    """
                }
            }

        stage('Deploy to Kubernetes') {
            steps {
                echo "Injecting image tag into manifest..."

                sh """
                    sed -i 's|IMAGE_PLACEHOLDER|${IMAGE_NAME}:${IMAGE_TAG}|g' ./k8/deploy.yaml
                """

                sh "kubectl apply -f ./k8/deploy.yaml"

                sh "kubectl rollout status deployment/user-service -n services"
            }
        }
    }



    /*
     * Global post conditions
     */
    post {
        success {
            echo "SUCCESS: Docker image ${IMAGE_NAME}:${IMAGE_TAG} built successfully."
        }

        failure {
            echo 'FAILED: Check stage logs above for errors.'
        }

        always {
            cleanWs()  // Cleanup workspace after every build
        }
    }
}