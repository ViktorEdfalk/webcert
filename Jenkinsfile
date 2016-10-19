#!groovy

def buildVersion = "5.1.${BUILD_NUMBER}"
def commonVersion = "3.1.+"
def typerVersion = "3.1.+"

stage('checkout') {
    node {
        util.run { checkout scm }
    }
}

stage('build') {
    node {
        shgradle "--refresh-dependencies clean camelTest build sonarqube -PcodeQuality -DgruntColors=false \
                  -DbuildVersion=${buildVersion} -DcommonVersion=${commonVersion} -DtyperVersion=${typerVersion}"
    }
}

stage('deploy') {
    node {
        util.run {
            ansiblePlaybook extraVars: [version: buildVersion, ansible_ssh_port: "22", deploy_from_repo: "false"], \
                installation: 'ansible-yum', inventory: 'ansible/hosts_test', playbook: 'ansible/deploy.yml'
        }
    }
}

stage('restAssured') {
    node {
        try {
            shgradle "restAssuredTest -DbaseUrl=http://webcert.inera.nordicmedtest.se/ \
                  -DbuildVersion=${buildVersion} -DcommonVersion=${commonVersion} -DtyperVersion=${typerVersion}"
        } finally {
            publishHTML allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'web/build/reports/tests/restAssuredTest', \
                reportFiles: 'index.html', reportName: 'RestAssured results'
        }
    }
}

stage('protractor') {
    node {
        try {
            wrap([$class: 'Xvfb']) {
                shgradle "protractorTests -Dprotractor.env=build-server \
                      -DbuildVersion=${buildVersion} -DcommonVersion=${commonVersion} -DtyperVersion=${typerVersion}"
            }
        } finally {
            publishHTML allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'test/dev/report', \
                reportFiles: 'index.html', reportName: 'Protractor results'
        }
    }
}

stage('fitnesse') {
    node {
        try {
            wrap([$class: 'Xvfb']) {
                shgradle "fitnesseTest -PfileOutput -PoutputFormat=html -Dgeb.env=firefoxRemote -Dweb.baseUrl=https://webcert.inera.nordicmedtest.se/ \
                      -DbaseUrl=https://webcert.inera.nordicmedtest.se/ -Dlogsender.baseUrl=https://webcert.inera.nordicmedtest.se/log-sender/ \
                      -Dcertificate.baseUrl=https://intygstjanst.inera.nordicmedtest.se/inera-certificate/ \
                      -DbuildVersion=${buildVersion} -DcommonVersion=${commonVersion} -DtyperVersion=${typerVersion}"
            }
        } finally {
            publishHTML allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'specifications/', \
                reportFiles: 'fitnesse-results.html', reportName: 'Fitnesse results'
        }
    }
}

stage('tag and upload') {
    node {
        shgradle "uploadArchives tagRelease -DbuildVersion=${buildVersion} -DcommonVersion=${commonVersion} -DtyperVersion=${typerVersion}"
    }
}
