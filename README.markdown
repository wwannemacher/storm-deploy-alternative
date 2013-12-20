Fast tool to deploy [Storm](https://github.com/nathanmarz/storm) on [Amazon EC2](http://aws.amazon.com/ec2/), written entirely in Java.

_Please don't hesitate to contact me. Your feedback will help to further improve this tool._

## Features
+ Runs Storm and Zookeeper daemons under supervision (automatically restarted in case of failure)
+ Only fetch and compile what is needed (can deploy on prepared images in a few minutes)
+ Supports executing user-defined commands both pre-config and post-config
+ Automatically sets up s3cmd, to make it easy to get/put files on [Amazon S3](http://aws.amazon.com/s3/)
+ Supports Storm versions: _0.8.2_ & _0.9.0.1_
+ Supports Zookeeper versions: _3.3.3_

## Configuration
This tool, requires two configurationfiles: `conf/credential.yaml` and `conf/configuration.yaml`. Put your Amazon Web Services (AWS) credentials into the file `conf/credential.yaml`. 

Below is an example of a single cluster configuration, for `conf/configuration.yaml`

```
mycluster:
    - m1.medium {ZK, WORKER, MASTER, UI}
    - storm-version "0.8.2"
    - zk-version "3.3.3"
    - image "eu-west-1/ami-480bea3f" 	#official Ubuntu 13.10 AMI
    - region "eu-west-1"
    - exec-preconfig {cd ~, echo hey > hey.txt}
    - exec-postconfig {}
```
+ WORKER is the Storm Supervisor daemon
+ MASTER is the Storm Nimbus daemon
+ UI is the Storm User-Interface
+ ZK is the [Zookeeper](http://zookeeper.apache.org) daemon

_Please ensure the image resides in the same region as specified._

## Usage

### Deploy
Execute `java -jar storm-deploy-alternative.jar deploy CLUSTER_NAME`

After successful deployment, a small file is written to $HOME/.storm/, which allows you to interact with the cluster directly from the bin/storm script. For details on how to use the bin/storm script,
please refer to the [Storm wiki](https://github.com/nathanmarz/storm/wiki).

### Attach
Execute `java -jar storm-deploy-alternative.jar attach CLUSTER_NAME`

Attaches the `bin/storm` script to a cluster with name CLUSTER_NAME.

### Scaling
Execute `java -jar storm-deploy-alternative.jar scaleout CLUSTER_NAME #NumInstances INSTANCE_TYPE`

Adds new worker instances to an already running cluster. For example, you could execute `java -jar storm-deploy-alternative.jar scaleout test 2 m1.medium`, to add two new instances of the type m1.medium to the cluster called test. When completed, you can see the new nodes in the Storm UI.

## FAQ
+ I am seeing the error: `net.schmizz.sshj.userauth.UserAuthException: publickey auth failed`. This can happen when using multiple computers for deploying, each with different ssh keys. The solution, is to go to AWS EC2 interface -> Key Pairs, and delete the jclouds#CLUSTER_NAME keypair. To prevent this problem from happening, use the same keypair on all machines used to deploy.
+ I am seeing the warning: `cipher strengths apparently limited by JCE policy`. You can improve your security by installing [Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files](http://www.oracle.com/technetwork/java/javase/downloads/index.html).

## Limitations
Currently, only deploying to Ubuntu AMIs on Amazon EC2 is supported.
