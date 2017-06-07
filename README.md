## Overview
 This project is the research part of the Service Tooling initiative of the ZHAW Service Prototyping Lab(https://blog.zhaw.ch/icclab/category/research-approach/themes/service-tooling/).
 It represents the Java translation tool for AWS Lambda. Being comparably new cloud service FaaS already became perspective area for
researching and development. AWS Lambda is one of the FaaS providers and it's the first announced service of it's kind. You can find some
information about FaaS and it's providers following the link to our recent [blog-post](https://blog.zhaw.ch/icclab/faas-function-hosting-services-and-their-technical-characteristics/).
So in common using this tool allows th e customer to make Java project available for uploading into AWS Lambda cloud service and perform automatic deploying.

## Before start
 For building the project you need Maven to be installed and ${maven.home} environment variable to be set. (If Maven home
 isn't set explicitly as environment variable, Podilizer uses the default Maven home path: '/usr/share/maven/').

 For deploying Lambda functions you need to have account at AWS. Then you need to follow
  [install the AWS CLI](http://docs.aws.amazon.com/cli/latest/userguide/installing.html) and
  [configure it](http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html).

 Example of quick configurations:
 ```
 $ aws configure
 AWS Access Key ID [None]: AKIAIOSFODNN7EXAMPLE
 AWS Secret Access Key [None]: wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
 Default region name [None]: us-west-2
 Default output format [None]: json
 ```

## How to run
 * Checkout from repository.
 * Go to project location and build it using maven:
  ```
  $ mvn install
  ```
 * After building you can find jar in the 'target' folder inside the
  project directory. Tool works in three separated phases: translation, building and uploading.
  To run certain phase use an appropriate option: '-t', '-b' or '-u'.
  * '-t' - create new project at path <arg2> from input project at path <arg1>:
    ```
    $ java -jar [path of the Podilizer-0.1.jar] -t <arg1> <arg2>
    ```
    Example:
    ```
    $ java -jar target/Podilizer-0.1.jar -t /home/user/in /home/user/out
    ```
  * '-b' - build the project at path <arg1> using maven pom.xml file at path <arg2>. The sample of pom file located
  in additional/conf/pom.xml in the Podilizer project folder. If your input project uses some external libraries,
  add appropriate maven dependencies to this pom file before use:
    ```
    $ java -jar [path of the Podilizer-0.1.jar] -b <arg1> <arg2>
    ```
    Example:
    ```
    $ java -jar target/Podilizer-0.1.jar -b /home/user/out additional/conf/pom.xml
    ```
  * '-u' - Creates the Lambda functions from translated project at path <arg1>
    ```
    $ java -jar [path of the Podilizer-0.1.jar] -u <arg1>
    ```
    Example:
    ```
    $ java -jar target/Podilizer-0.1.jar -u /home/user/out
    ```
  * You can combine all of these options to run needed phases. For example if you want to run full process with one command,
  it would look like:
    ```
    $ java -jar target/Podilizer-0.1.jar -t /home/user/in/ /home/user/out/ -b /home/user/out/ additional/conf/pom.xml -u /home/user/out/
    ```
  * Option '-help' shows the usage of tool:
    ```
    $ java -jar target/Podilizer-0.1.jar -help
    ```

## Restrictions for the input project
The research is on the early stage so there are some issues to be implemented:
 * Methods of project shouldn't use 'this'.
 * Could appear bugs connected with namespaces.
 * Methods in classes that contain inner classes are not processed for the Lambda Functions. It means that such methods.
    are not separated on the different Lambda Function but still run.
 * Access methods('get' and 'set') are not being translated. It means that if code calls the 'get'
 or 'set' method it will perform at the same environment without uploading as separated function. The reason of such
 decision is optimisation: uploading the one-line access method doesn't worth the expended network resources during running.
 * May be there are some other restrictions that we lost and it can cause exceptions or incorrect result.
 * Supported Java version: 1.7.
