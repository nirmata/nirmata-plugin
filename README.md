# Nirmata Plugin
------------
This plugin enables synchronization of YAML/JSON files stored in a Git repository with a Kubernetes application managed on the Nirmata platform.

Nirmata is a Kubernetes platform that enables enterprises to deliver a highly scalable, always-on container management platform, built on Kubernetes. Using Nirmata, enterprise teams can fully automate the delivery and management of applications. At Nirmata, our mission is to empower all enterprises to easily deliver and manage containerized applications across public and private clouds.

You can learn more on [Nirmata Website](https://www.nirmata.com/)

## Features
------------
1. Update of running applications in the environment
2. Update of applications in the catalog
3. Deployment of ephemeral applications
4. Deletion of ephemeral applications

## General information
------------
This Jenkins Plugin supports update/deployment/deletetion of applications in Nirmata.

## Requirement
------------
### Jenkins
Jenkins 2.107.3 or newer is required.

### Git
Git 2.17.0 or newer is required.

## Setup
-------------
### Install
Install this plugin via the Jenkins plugin manager or download the latest version of it from [https://github.com/jenkinsci/nirmata-plugin/releases].

### Create Nirmata API Key
1. To enable access to your Nirmata platform, you must generate api key:
2. Sign-in to Nirmata platform
3. Select Settings -> Users
4. Click Generate API key
5. Copy the generated API key

### Add the Nirmata API key to Jenkins:
1. Navigate to your Jenkins hosting
2. Select "Credentials" from the Jenkins side panel
3. Choose a credentials domain and click 'Add Credentials'
4. From the 'Kind' drop-down, choose 'Secret text'
5. Add the copied API key to 'Secret' textbox
6. Enter a description for the credential and save

	![api1](https://user-images.githubusercontent.com/39581624/41719183-e85e95ce-757c-11e8-97a1-4f8c0f7d9e18.JPG)

## Pre-job configuration
-------------
### Freestyle job configuration

####1. Update a running application in an environment

- Create a new free-style project
- Select ‘Git’ under section ‘Source Code Management’. Enter the URL of the GitHub repository. Specify branch name of the repository

	![git](https://user-images.githubusercontent.com/39581624/41720190-e79cd06c-757f-11e8-9bec-7ae3f9f1a2d4.JPG)

- Enable ‘Poll SCM’ option under ‘Build Triggers’. This is required to remotely trigger a build

	![poll](https://user-images.githubusercontent.com/39581624/41720699-7af80bbe-7581-11e8-9d7e-89690c31d624.JPG)
	
- Furthermore, create a ‘Build step’ in the section ‘Build’ by selecting ‘Invoke Nirmata Service’ and select ‘Update App in Environment’ from the dropdown
	
	- **Nirmata Endpoint** - Enter base URL of platform (default: nirmata.io)
	
	- **Nirmata API Credentials** - Select API key from the dropdown if already configured in the first step or else click ‘Add’ to add credentials
	
	- **Existing Environments** - Select environment from the list in the dropdown
	
	- **Environment Applications** - Select application which should be updated, from the dropdown
	
	- **Timeout** - Enter a time period to wait for to pull the result from Nirmata
	
	- **List of directories** - Enter relative path of directory w.r.t locally stored repository containing YAML/JSON files. Multiple directories can be included using comma separator.
	
	- **Include File Pattern** - Add filename/extension/regex that should also be included for updating an application. By default all .yaml, .yml, .json files present in the specified directories above would be included. Note that multiple filename/extension/regex can be included using comma separator.	
	
	- **Exclude File Pattern**- Add filename/extension/regex for files in the mentioned directories that should be excluded for updating an application. Note that multiple filename/extension/regex can be excluded using comma separator.	
	
	![updateea](https://user-images.githubusercontent.com/39581624/41727307-a860c518-7591-11e8-8089-2274956f45ac.JPG)
	
- Save/Apply configuration of job
- This job would be triggered on detection of a commit/change in the specified Git repo
- The ‘Update App in Environment’ build step is marked successful if all the YAML/JSON files are imported successfully else it is marked as failed if any issue is encountered
		
	![picsart_06-21-11 15 33](https://user-images.githubusercontent.com/39581624/41736937-8206e874-75ab-11e8-899d-5f50e0782089.jpg)

####2. Update an application in a catalog

- Create a new free-style project
- Select ‘Git’ under section ‘Source Code Management’. Enter the URL of the GitHub repository. Specify branch name of the repository

	![git](https://user-images.githubusercontent.com/39581624/41720190-e79cd06c-757f-11e8-9bec-7ae3f9f1a2d4.JPG)

- Enable ‘Poll SCM’ option under ‘Build Triggers’. This is required to remotely trigger a build

	![poll](https://user-images.githubusercontent.com/39581624/41720699-7af80bbe-7581-11e8-9d7e-89690c31d624.JPG)
	
- Furthermore, create a ‘Build step’ in the section ‘Build’ by selecting ‘Invoke Nirmata Service’ and select ‘Update App in Catalog’ from the dropdown	

	- **Nirmata Endpoint** - Enter base URL of platform (default: nirmata.io)
	
	- **Nirmata API Credentials** - Select API key from the dropdown if already configured in the first step or else click ‘Add’ to add credentials

	- **Catalog Applications** - Select application which should be updated, from the dropdown
	
	- **Timeout** - Enter a time period to wait for to pull the result from Nirmata
	
	- **List of directories** - Enter relative path of directory w.r.t locally stored repository containing YAML/JSON files. Multiple directories can be included using comma separator.
	
	- **Include File Pattern** - Add filename/extension/regex that should also be included for updating an application. By default all .yaml, .yml, .json files present in the specified directories above would be included. Note that multiple filename/extension/regex can be included using comma separator.	
	
	- **Exclude File Pattern**- Add filename/extension/regex for files in the mentioned directories that should be excluded for updating an application. Note that multiple filename/extension/regex can be excluded using comma separator.

	![updateca](https://user-images.githubusercontent.com/39581624/41728762-315a8446-7595-11e8-8060-a3a4fe6d8f92.JPG)

- Save/Apply configuration of job
- This job would be triggered on detection of a commit/change in the specified Git repo
- The ‘Update App in Catalog’ build step is marked successful if all the YAML/JSON files are imported successfully else it is marked as failed if any issue is encountered

	![picsart_06-21-11 26 54](https://user-images.githubusercontent.com/39581624/41736821-2bd431be-75ab-11e8-9e6b-9589b8c347ed.jpg)

####3. Deploy an ephemeral application

- Create a new free-style project
- Create a ‘Build step’ in the section ‘Build’ by selecting ‘Invoke Nirmata Service’ and select ‘Deploy App in Environment’ from the dropdown

	- **Nirmata Endpoint** - Enter base URL of platform (default: nirmata.io)
	
	- **Nirmata API Credentials** - Select API key from the dropdown if already configured in the first step or else click ‘Add’ to add credentials
	
	- **Existing Environments** - Select environment from the list in the dropdown
	
	- **Catalog Applications** - Select the application which should be deployed, from the dropdown
	
	- **Name for the application** - Specify the name by which the application should be deployed in the the platform
	
	- **Timeout** - Enter a time period to wait for to pull the result from Nirmata
	
	![deploy](https://user-images.githubusercontent.com/39581624/41730016-51c04f24-7598-11e8-8cb4-9cdb6828644c.JPG)

- Save/Apply configuration of job and execute an initial build by triggering ‘Build Now’ from side panel
- The ‘Deploy App in Environment’ build step is marked successful if the application deployed in the environment else it is marked as failed if any issue is encountered

	![deployresult](https://user-images.githubusercontent.com/39581624/41735516-8354568e-75a7-11e8-9dee-34bfdfc76b56.JPG)

####4. Delete an ephemeral application

- Create a new free-style project
- Create a ‘Build step’ in the section ‘Build’ by selecting ‘Invoke Nirmata Service’ and select ‘Delete App in Environment’ from the dropdown

	- **Nirmata Endpoint** - Enter base URL of platform (default: nirmata.io)
	
	- **Nirmata API Credentials** - Select API key from the dropdown if already configured in the first step or else click ‘Add’ to add credentials
	
	- **Existing Environments** - Select environment from the list in the dropdown
	
	- **Running Applications** - Select the application which should be deleted, from the dropdown
	
	- **Timeout** - Enter a time period to wait for to pull the result from Nirmata
	
	![delete](https://user-images.githubusercontent.com/39581624/41730466-7e1d27f8-7599-11e8-9e78-099150224985.JPG)

- Save/Apply configuration of job and execute an initial build by triggering ‘Build Now’ from side panel
- The ‘Delete App in Environment’ build step is marked successful if the application is deleted successfully else it is marked as failed if any issue is encountered

	![deleteresult](https://user-images.githubusercontent.com/39581624/41735398-2f31c3c0-75a7-11e8-8548-110f24d03a6e.JPG)

## Version history
---------------
### Version 1.0.0

- Initial release

