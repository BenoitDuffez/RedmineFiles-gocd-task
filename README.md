# Redmine File upload GoCD task plugin

This GoCD plugin allows upload of files to a Redmine instance.

Typical use is to publish a binary to Redmine.

## Requirements

* GoCD 17 (could work with earlier versions, as low as 14)
* Redmine: one of these:
  * Redmine v3.4
  * SVN release [r16109](http://www.redmine.org/projects/redmine/repository/revisions/16109)
  * apply manually [this patch](http://www.redmine.org/projects/redmine/repository/revisions/16109/diff/)

## Getting started

* define `REDMINE_API_KEY` as a (secure) variable, I did it in the environment
* define `REDMINE_URL` as a variable, I did it in the environment
* add a job/task and configure:
  * the artifact you wish to upload
  * the Redmine project
  * the project version

The project ID is either the numeric or the string ID.

There is an artifact generated, it's called `redmine_output.html` (at the root). It only contains a link to the Redmine 'Files' page of the project.

## Building the code base

To build the jar, run `./gradlew clean test assemble`

## Step by step

1. Set up your environment:
  ![Environment: URL and API key](environment.png)
  Be sure to set the API key as a secure variable, otherwise it may be logged.
1. Set up your template:
  ![Template: add job/task](template.png)
  Here everything is parameterized in the template
1. Set up your pipeline parameters:
  ![Pipeline: parameters](pipeline.png)
  The pipeline defines the missing parameters: project and version IDs
1. Result in redmine:
  ![Redmine: result](redmine.png)

## License

```plain
Copyright 2017 Benoit Duffez
Copyright 2017 ThoughtWorks, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
