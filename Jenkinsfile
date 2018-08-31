#!/usr/bin/env groovy

def scriptVersion  = 'feature/AOS-2708'
def pipelineScript = 'https://git.aurora.skead.no/scm/ao/aurora-pipeline-scripts.git'
fileLoader.withGit(pipelineScript,scriptVersion) {
   jenkinsfile = fileLoader.load('templates/leveransepakke')
}

def config = [
  pipelineScript              : pipelineScript,
  scriptVersion               : scriptVersion,
  affiliation                 : "paas",
  downstreamSystemtestJob     : [ jobName: 'systemtest-refapp',  branch: env.BRANCH_NAME],
  piTests: false,
  credentialsId: "github",
  suggestVersionAndTagReleases: [
          [branch: 'master', versionHint: '2.0'],
          [branch: 'release/v1', versionHint: '1.0', tagsToPush:'major,minor,patch']]
  ]

jenkinsfile.run(scriptVersion, config)


