pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        withMaven(maven: 'M3.5', mavenSettingsConfig: 'fb57b2b9-c2e4-4e05-955e-8688bc067515', mavenLocalRepo: "$WORKSPACE/../../.m2/${env.BRANCH_NAME}") {
          sh 'mvn clean install -DskipTests'
        }
      }
    }
    stage('Test') {
      steps {
        withMaven(maven: 'M3.5', mavenSettingsConfig: 'fb57b2b9-c2e4-4e05-955e-8688bc067515', mavenLocalRepo: "$WORKSPACE/../../.m2/${env.BRANCH_NAME}") {
          sh 'mvn test'
        }
      }
      post {
        always {
            step([$class: 'Publisher', reportFilenamePattern: '**/testng-results.xml'])
        }
      }
    }
    stage('Coverage') {
      steps {
        withMaven(maven: 'M3.5', mavenSettingsConfig: 'fb57b2b9-c2e4-4e05-955e-8688bc067515') {
          sh "mvn clean clover:setup test clover:aggregate clover:clover"
        }
        step([
          $class: 'CloverPublisher',
          cloverReportDir: 'target/site',
          cloverReportFileName: 'clover.xml',
          healthyTarget: [methodCoverage: 5, conditionalCoverage: 5, statementCoverage: 5]  // optional, default is: method=70, conditional=80, statement=80
        ])
      }
    }
    stage('Deploy') {
      when {
        branch 'master'
      }
      steps {
        withMaven(maven: 'M3.5', mavenSettingsConfig: 'fb57b2b9-c2e4-4e05-955e-8688bc067515', mavenLocalRepo: "$WORKSPACE/../../.m2/${env.BRANCH_NAME}") {
          sh 'mvn deploy'
        }
      }
    }
  }
}
