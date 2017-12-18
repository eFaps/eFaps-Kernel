pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        withMaven(maven: 'M3.5', mavenSettingsConfig: 'fb57b2b9-c2e4-4e05-955e-8688bc067515') {
          sh 'mvn clean install'
        }
      }
      post {
        always {
            step([$class: 'Publisher', reportFilenamePattern: '**/testng-results.xml'])
        }
      }
    }
    
  }
}
