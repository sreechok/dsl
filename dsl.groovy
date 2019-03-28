pipelineJob("Risklink18 Ansible Pipeline") {
	description("""Ansible Risklink18 Release develop branch

 Ansible managed""")
	keepDependencies(false)
	parameters {
		choiceParam("StartAtStage", ["Set Risklink to Premium", "Common", "Add Domain User on SQL Server", "SQL Server", "HPC", "Risklink Common", "Risklink Deployment", "HDFS Configs", "Apply Windows Updates", "Set Risklink to Standard"], "The Stage you want the job to start on. Default is Stage 1")
		stringParam("CLUSTER_INDEX", "", "You must specify cluster index of the risklink that you are deploying to.")
		stringParam("VERBOSE", "", "by default verbose is turned off. You may pass -v, -vv, -vvvv which will be passed to ansible-playbook")
		stringParam("LIMIT", "", "specify limit to run on certain vms (specify the computer name). e.g. aut-rl18-0-d0")
		stringParam("DLMDATA_HOSTNAME", "AUT-RL18-0-0", "specify the DLMDATA hostname from where the dlmdata would be copied to the new cluster")
		stringParam("SERIAL", "100%", "specify serial update percentage for Apply Windows Update stage. default is 100%")
	}
	definition {
		cpsScm {
"""node('jslaves') {
  env.PIPELINE_IMAGE_VER = "0.0.4"
  env.PIPELINE_IMAGE_URL = "eastus-artifactory.azure.rmsonecloud.net:6001/alpine-pipeline"
  currentBuild.result = "SUCCESS"
  env.STACK = "catquake"
  env.ENVIRONMENT = "auto"
  env.SHORT_ENVIRONMENT = "aut"
  env.WIN_LOCAL_ADMIN_USER = "techops"
  env.WIN_DOMAIN_RISKLINK_USER = "rl-admin@auto.azure.rmsonecloud.net"
  env.SLACK_CHANNEL = "#deployments-catquake"
  env.CLUSTER_INDEX = CLUSTER_INDEX
  env.VERBOSE = VERBOSE
  env.LIMIT = LIMIT
  env.DLMDATA_HOSTNAME = DLMDATA_HOSTNAME
  env.SERIAL = SERIAL
  
  skips = []
  risklink_version = 18

  vm_standard_size = 'standard_d4_v2'
  vm_premium_size = 'standard_ds4_v2'
  sa_standard_sku = 'Standard_LRS'
  sa_premium_sku = 'Premium_LRS'
  managedsku_docker_image = 'eastus-artifactory.azure.rmsonecloud.net:6001/rms-manageddisks-update:0.0.1'
  azure_cli_docker_image = 'eastus-artifactory.azure.rmsonecloud.net:6001/azuresdk/azure-cli-python:2.0.9'

  skip_stages = params.SkipStages.split(',').collect{it}
  def BRANCH_OR_TAG = ''
  if (params.Tag) {
      BRANCH_OR_TAG = "refs/tags/\${params.Tag}"
  } else if (params.Branch) {
      BRANCH_OR_TAG = "\${params.Branch}"
  }

  checkout([\$class: 'GitSCM', branches: [[name: "\${BRANCH_OR_TAG}"]], doGenerateSubmoduleConfigurations: false, extensions: [[\$class: 'SubmoduleOption', parentCredentials: true, recursiveSubmodules: true, reference: '', trackingSubmodules: true]], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'a43dfd9e-4097-4cde-b4ce-ad21014cf88a', url: 'git@github.com:RMS/rms-ansible.git']]])

  def rootDir = pwd()
  def pipeline = load "\${rootDir}/build/risklink/risklink18.groovy"
  pipeline.run_pipeline()
}"""		}
	}
	disabled(false)
	configure {
		it / 'properties' / 'com.coravy.hudson.plugins.github.GithubProjectProperty' {
			'projectUrl'('https://github.com/RMS/rms-ansible')
			displayName()
		}
	}
}
