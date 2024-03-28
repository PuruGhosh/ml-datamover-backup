pipeline
{
	//Setting up the environment variables for SCM Checkout and Docker push
	environment
	{
      SCM_URL = "https://scm.corp.xperi.com/scm/ml42/ml-datamover.git"
      SCM_CREDENTIAL_ID = "penni-build-access-token"
      DOCKER_ARTIFACT_URL = "https://artifacts.corp.xperi.com/"
      ARTIFACT_URL = "https://artifacts.corp.xperi.com/artifactory/ml2020-gradle-local/"
      DOCKER_REGISTRY = "ml2020-docker-local/ml-datamover"
      DOCKER_REGISTRY_CREDENTIAL_ID = '333d2bd2-5c8e-4ff7-84a2-c4490796409a'
	}
	agent { label 'DOCKER-LINUX' }
	// Setting up parameters
	parameters {
	  string(name: 'SERVICE_NAME', defaultValue: 'ML-datamover', description: 'Enter a service name e.g. ml-datamover')
	  string(name: 'DOCKER_TAG', defaultValue: '', description: 'Enter a Docker Tag e.g. latest')
	}

	//Keeping last 5 build log and artifacts
	options { buildDiscarder(logRotator(numToKeepStr: "5", artifactNumToKeepStr: "5", artifactDaysToKeepStr: "15")) }

	stages
	{
	  //Stage 1: Prerequisite Setup
	  stage ('Prerequisite Setup')
	  {
      steps
      {
        script
        {
          dockerTag = "${params.DOCKER_TAG}"
          if (dockerTag == "")
          {
            dockerTag = sh (
              script: "git rev-parse origin/${env.BRANCH_NAME}",
              returnStdout: true
            ).trim()
          }
        }
      }
    }
	  //Stage 2: Gradle Build
      stage ('Gradle Build')
      {
        agent {
          docker {
            image "artifacts.corp.xperi.com/ml2020-docker-local/ml-jdk-1.15-gradle:latest"
            registryUrl DOCKER_ARTIFACT_URL
            registryCredentialsId DOCKER_REGISTRY_CREDENTIAL_ID
            args "--user root"
          }
        }
        steps
        {
          script
          {
            sh 'chmod +x gradlew'
            withCredentials([usernamePassword(
                credentialsId: DOCKER_REGISTRY_CREDENTIAL_ID,
                usernameVariable: 'ARTIFACT_USERNAME',
                passwordVariable: 'ARTIFACT_PASSWORD'
            )])
            {
              try 
              {
                sh (script: "./gradlew clean build -DARTIFACT_URL=${env.ARTIFACT_URL} -DARTIFACT_USERNAME=${env.ARTIFACT_USERNAME} -DARTIFACT_PASSWORD=${env.ARTIFACT_PASSWORD} -DBUILD_VERSION=${dockerTag} > ${params.SERVICE_NAME}-${dockerTag}.log")
                stash(name: "archive_jar", includes: '**/*.jar')
              }
              finally
              {
                stash(name: "archive_log", includes: '**/*.log')
              }
            }
          }
        }
      }
	  //Stage 3: Build the Dockerfile
	  stage ('Build Dockerfile')
	  {
        when { branch "develop" }
        steps
        {
          unstash("archive_jar")
          sh 'chmod +x Dockerfile'
          script
          {
            withCredentials([usernamePassword(
                credentialsId: DOCKER_REGISTRY_CREDENTIAL_ID,
                usernameVariable: 'ARTIFACT_USERNAME',
                passwordVariable: 'ARTIFACT_PASSWORD'
            )])
            {
              sh (script: "docker login ${env.DOCKER_ARTIFACT_URL} -u ${env.ARTIFACT_USERNAME} -p ${env.ARTIFACT_PASSWORD}")
              sh (script: "docker build -t artifacts.corp.xperi.com/${env.DOCKER_REGISTRY}:${dockerTag} --build-arg SERVICE_NAME=${params.SERVICE_NAME} --build-arg BUILD_VERSION=${dockerTag} .")
              sh (script: "docker logout")
            }
          }
        }
	  }
	  //Stage 4: Push the Docker image to artifacts
	  stage ('Push Docker Image')
	  {
	    when { branch "develop" }
	    steps
        {
          script
          {
            withCredentials([usernamePassword(
              credentialsId: DOCKER_REGISTRY_CREDENTIAL_ID,
              usernameVariable: 'ARTIFACT_USERNAME',
              passwordVariable: 'ARTIFACT_PASSWORD'
            )])
            {
              sh (script: "docker login ${env.DOCKER_ARTIFACT_URL} -u ${env.ARTIFACT_USERNAME} -p ${env.ARTIFACT_PASSWORD}")
              sh (script: "docker push artifacts.corp.xperi.com/${env.DOCKER_REGISTRY}:${dockerTag}")
              sh (script: "docker logout")
            }
          }
        }
	  }
	  //Stage 5: Remove the Docker image from the jenkins box after push
	  stage ('Remove Unused Docker Image')
	  {
	    when { branch "develop" }
	    steps
        {
          script
          {
            sh (script: "docker rmi artifacts.corp.xperi.com/${env.DOCKER_REGISTRY}:${dockerTag}")
          }
        }
	  }
	}
  post {
    always {
      unstash("archive_log")
      archiveArtifacts artifacts: "**/${params.SERVICE_NAME}-${dockerTag}.log"
      cleanWs()
    }
  }
}