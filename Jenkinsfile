pipeline {
  environment {
    IMG_TAG='\$(echo \$GIT_COMMIT | cut -c -7)'
    REGISTRY='registry-staging.orosbank.com'
    CLUSTER_NAME='oros-staging'
    APP_NAME='fineract-be'
  }

  agent{
    kubernetes {
      defaultContainer 'jnlp'
      yamlFile 'jenkins/agent.yaml'
    }
  }

  stages {
    // Building and pushing a docker image to CR
    stage('Build image & push') {
      when {
        anyOf {
          branch 'staging'
        }
      }
      steps {
        container('maven-docker-j17') {
          sh script: "./gradlew :fineract-provider:clean :fineract-provider:jibDockerBuild -x test -Djib.to.image=${REGISTRY}/${BRANCH_NAME}-${APP_NAME}:${IMG_TAG}", label: "Build Docker Image"
          sh script: "docker push ${REGISTRY}/${BRANCH_NAME}-${APP_NAME}:${IMG_TAG}", label: "Push Docker Image"
        }
      }
    }



    stage('Deploy to STG cluster') {
      when {
        anyOf {
//          branch 'master'
          branch 'staging'
        }
      }

      steps {
        container('helm-k8s') {
          withCredentials([string(credentialsId: 'do-token', variable: 'DO_ACCESS_TOKEN')]) {

            sh script: '''wget https://github.com/digitalocean/doctl/releases/download/v1.101.0/doctl-1.101.0-linux-amd64.tar.gz
                      tar xf doctl-1.101.0-linux-amd64.tar.gz && mv doctl /usr/local/bin
                      doctl auth init --access-token $DO_ACCESS_TOKEN
                      doctl kubernetes cluster kubeconfig save ${CLUSTER_NAME}
                      ''', label: "install doctl"

            sh script: "helm repo add oros-universe https://oros-helm-charts.netlify.app/", label: "Fetch helm repo"
            sh script: "helm upgrade -i -n fineract ${APP_NAME} oros-universe/generic3 -f jenkins/fineract-be.yml --set image.tag=${IMG_TAG},image.repository=${REGISTRY}/${BRANCH_NAME}-${APP_NAME}", label: "Deploy backend"
            sh script: "helm upgrade -i -n fineract ${APP_NAME}-fe oros-universe/generic3 -f jenkins/fineract-fe.yml", label: "Deploy frontend"

          }
        }
      }
    }
  }
}
