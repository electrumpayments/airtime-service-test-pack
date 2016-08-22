#!/bin/bash
basedir=$1
testdir=$2
echo "Starting docker container"
docker build -t="airtime-test-server" ${basedir}/target
docker run -d -p 8080:8080 --name airtime-test-server_container airtime-test-server
/git/circlecitools/bin/waitForServer.sh localhost:8080 5000
${testdir}/run_newman.sh ${testdir}
rc=$?
echo "Cleaning up Docker"
docker stop airtime-test-server_container
docker rm airtime-test-server_container
docker rmi airtime-test-server
exit $rc