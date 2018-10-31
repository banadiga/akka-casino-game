#!/usr/bin/env groovy Jenkinsfile

pipeline {

  agent any

  triggers {
    cron('H H 1 1 *')
  }

  stages {

    stage('Build') {
      steps {
        script {
          sh "sbt -no-colors test"
        }
      }
    }

  }
}
