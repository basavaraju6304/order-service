pipeline {
    agent any

    environment {
        IMAGE_NAME = 'order-service'
        NETWORK    = 'microservices-deploy_default'
    }

    stages {
        stage('Build and Test') {
            steps {
                sh 'mvn clean test'
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t $IMAGE_NAME:$BUILD_NUMBER -t $IMAGE_NAME:latest .'
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    docker rm -f $IMAGE_NAME || true
                    docker run -d --name $IMAGE_NAME \
                      --network $NETWORK \
                      -p 8082:8082 \
                      -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/orderdb \
                      -e PRODUCT_SERVICE_URL=http://product-service:8081 \
                      --restart unless-stopped \
                      $IMAGE_NAME:latest
                '''
            }
        }

        stage('Verify') {
            steps {
                sh '''
                    for i in 1 2 3 4 5 6 7 8 9 10 11 12; do
                      if curl -sf http://order-service:8082/orders > /dev/null; then
                        echo "Service is up"
                        exit 0
                      fi
                      echo "Waiting for service... ($i)"
                      sleep 5
                    done
                    echo "Service did not start"
                    docker logs --tail 30 $IMAGE_NAME
                    exit 1
                '''
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
        }
        success {
            echo 'CI/CD passed: tested, built and deployed'
        }
        failure {
            echo 'Pipeline failed: check the stage that turned red'
        }
    }
}