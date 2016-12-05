## Overview
 This project is the research part of the Service Tooling initiative of the ZHAW Service Prototyping Lab(https://blog.zhaw.ch/icclab/category/research-approach/themes/service-tooling/).
 It represents the Java translation tool for AWS Lambda. Being comparably new cloud service FaaS already became perspective area for
researching and development. AWS Lambda is one of the FaaS providers and it's the first announced service of it's kind. You can find some
information about FaaS and it's providers following the link to our recent [blog-post](https://blog.zhaw.ch/icclab/faas-function-hosting-services-and-their-technical-characteristics/).
So in common using this tool allows th e customer to make Java project available for uploading into AWS Lambda cloud service and perform automatic deploying.

## Before start
 For making the project you need maven to be installed and ${maven.home} environment variable to be set.

 For deploying Lambda functions you need to have account at AWS.

## How to run
 * Checkout from repository.
 * Set up the configure file
  * Rename the "jyaml.yml.dist" file to "jyaml.yml".
  * Fill the renamed file with your configurations
 * Go to project location and build:
     mvn install
 * Run the built jar with one of the options: 'translate' or 'upload'
  * 'translate' - performs creating the new translated project and Lambda Functions building:
  ```
  java -jar target/translator-java-1.0-SNAPSHOT.jar translate
  ```
  * 'upload' - do the same as 'translate' option but additionally uploads Lambda Functions into AWS Lambda service:
  ```
  java -jar target/translator-java-1.0-SNAPSHOT.jar upload
  ```

## Restrictions for the input project
The research is on the early stage so there are some issues to be implemented:
 * Methods of project shouldn't use 'this'
 * All the code should be in the folder named 'src' inside the project folder('path:' in the jyaml config file)
 * Could appear bugs connected with namespaces
 * Methods in classes that contain inner classes are not processed for the Lambda Functions. It means that such methods
    are not separated on the different Lambda Function but still run.
 * May be there are some other restrictions that we lost and it can cause exceptions or incorrect result.
