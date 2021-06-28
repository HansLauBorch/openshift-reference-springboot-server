#!/usr/bin/env groovy
def config = [
    scriptVersion              : 'v7',
    iqOrganizationName         : 'Team AOS',
    pipelineScript             : 'https://git.aurora.skead.no/scm/ao/aurora-pipeline-scripts.git',
    downstreamSystemtestJob    : [branch: env.BRANCH_NAME],
    credentialsId              : "github",
    javaVersion                : 11,
    sonarQubeUrl               : "https://ref-sonar.aurora.skead.no/",
    sonarQubeTempToken         : "e11b33b966887059063d22634e0cf3bf46be2a62",
    forceSonarToken            : true,
    nodeVersion                : '10',
    jiraFiksetIKomponentversjon: true,
    chatRoom                   : "#aos-notifications",
    compileProperties          : "-U",
    versionStrategy            : [
        [branch: 'master', versionHint: '4'],
        [branch: 'release/v3', versionHint: '3'],
        [branch: 'release/v2', versionHint: '2'],
        [branch: 'release/v1', versionHint: '1']
    ]
]
fileLoader.withGit(config.pipelineScript, config.scriptVersion) {
  jenkinsfile = fileLoader.load('templates/leveransepakke')
}
jenkinsfile.maven(config.scriptVersion, config)
