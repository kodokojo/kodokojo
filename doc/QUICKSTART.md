# Quickstart kodo Kojo

## Requirement

To be able to work on Kodo Kojo and quickstart it, you need to install the following tools:
* [Docker](https://www.docker.com/): You will need to get `docker-compose`. I advise you to install [Docker Toolbox](https://www.docker.com/products/docker-toolbox)

## Go!

### Step 1: Clone this repository
```bash
git clone https://github.com/kodokojo/kodokojo.git
cd kodokojo
```

### Step 2: Retrieve and build
```bash
./quickstart.sh
```

This script will take a while since it will:
* Pull all images that you may need when testing Kodo Kojo (like Gitlab, Mesos, ...).
* Clone some project dependencies not yet deployed in Maven central
* Test, build and install those dependencies
* Test and Build a Docker image for the `kodokojo-ui`
* Test and Build a Docker image for `kodokojo`. This implies:
  * Get all Maven dependencies
  * Running unit test
  * Running all Integration tests which may run some Docker container, like redis, gitlab, jenkins, ...


### Step 3: Run
```bash
docker-compose -f src/test/resources/docker/full/docker-compose.yml up -d
```

You may access to the Kodo Kojo UI on following adresse:
 * http://localhost if you have the chance to run docker natively on your workstation
 * http://192.168.99.100 if you use the first docker-machine used throw a virtual machine

This `docker-compose.yml` file run several containers:
  * kodokojo-ui
  * kodokojo
  * redis
  * zookeeper
  * mesos-master via http://localhost:5050 or http://192.168.99.100:5050
  * mesos-slave
  * marathon via http://localhost:8080 or http://192.168.99.100:8080
  
If you want to get some logs, you may have use following command:

```bash
docker-compose -f src/test/resources/docker/full/docker-compose.yml logs kodokojo     
```

To clean all those containers:
```bash
docker-compose -f src/test/resources/docker/full/docker-compose.yml kill && \
    docker-compose -f src/test/resources/docker/full/docker-compose.yml rm --force
```

### To go further
Each project which composes Kodo Kojo has a `build.sh`script which allows you to test and build them.
