#!/bin/bash

VERSION=$1
if [ -z $VERSION ]
then
   echo "Please enter a version of repose to download"
   exit -1
fi

NEXUS_REPO_RELEASES="http://maven.research.rackspacecloud.com/content/repositories/releases"
REPOSE_PACKAGE="/com/rackspace/papi"

PATH_TO_ARTIFACT="${NEXUS_REPO_RELEASES}${REPOSE_PACKAGE}"

#COMPONENTS NEW
curl ${PATH_TO_ARTIFACT}/components/cli-utils/${VERSION}/cli-utils-${VERSION}.jar -O
curl ${PATH_TO_ARTIFACT}/components/echo/${VERSION}/echo-${VERSION}.jar -O

#OTHER
curl ${PATH_TO_ARTIFACT}/components/extensions/api-validator/${VERSION}/api-validator-${VERSION}.jar -O
curl ${PATH_TO_ARTIFACT}/components/compression/${VERSION}/compression/${VERSION}/compression-${VERSION}.jar -O
curl ${PATH_TO_ARTIFACT}/core/core-lib/${VERSION}/core-lib-${VERSION}.jar -O
curl ${PATH_TO_ARTIFACT}/commons/utilities/${VERSION}/utilities-${VERSION}.jar -O

http://maven.research.rackspacecloud.com/content/repositories/releases/com/rackspace/cloud/services/clients/auth/2.8.0/

#SERVICES
curl ${PATH_TO_ARTIFACT}/services/rate-limiting-service/${VERSION}/rate-limiting-service-${VERSION}.jar -O


#COMPONENTS
curl ${PATH_TO_ARTIFACT}/components/client-auth/${VERSION}/client-auth-${VERSION}.jar -O
curl ${PATH_TO_ARTIFACT}/components/client-authorization/${VERSION}/client-authorization-${VERSION}.jar -O
curl ${PATH_TO_ARTIFACT}/components/client-ip-identity/${VERSION}/client-ip-identity-${VERSION}.jar -O
curl ${PATH_TO_ARTIFACT}/components/configuration/${VERSION}/configuration-${VERSION}.jar -O
curl ${PATH_TO_ARTIFACT}/components/content-identity-auth-1.1/${VERSION}/content-identity-auth-1.1-${VERSION}.jar -O
curl ${PATH_TO_ARTIFACT}/components/content-identity-auth-2.0/${VERSION}/content-identity-auth-2.0-${VERSION}.jar -O
curl ${PATH_TO_ARTIFACT}/components/content-normalization/${VERSION}/content-normalization-${VERSION}.jar -O
curl ${PATH_TO_ARTIFACT}/components/default-router/${VERSION}/default-router-${VERSION}.jar -O
curl ${PATH_TO_ARTIFACT}/components/destination-router/${VERSION}/destination-router-${VERSION}.jar -O
curl ${PATH_TO_ARTIFACT}/components/distributed-datastore/${VERSION}/distributed-datastore-${VERSION}.jar -O
curl ${PATH_TO_ARTIFACT}/components/flush-output/${VERSION}/flush-output-${VERSION}.jar -O
curl ${PATH_TO_ARTIFACT}/components/header-id-mapping/${VERSION}/header-id-mapping-${VERSION}.jar -O
curl ${PATH_TO_ARTIFACT}/components/header-identity/${VERSION}/header-identity-${VERSION}.jar -O
curl ${PATH_TO_ARTIFACT}/components/header-normalization/${VERSION}/header-normalization-${VERSION}.jar -O
curl ${PATH_TO_ARTIFACT}/components/http-logging/${VERSION}/http-logging-${VERSION}.jar -O
curl ${PATH_TO_ARTIFACT}/components/rate-limiting/${VERSION}/rate-limiting-${VERSION}.jar -O
curl ${PATH_TO_ARTIFACT}/components/replicated-datastore/${VERSION}/replicated-datastore-${VERSION}.jar -O
curl ${PATH_TO_ARTIFACT}/components/service-authentication/${VERSION}/service-authentication-${VERSION}.jar -O
curl ${PATH_TO_ARTIFACT}/components/translation/${VERSION}/translation-${VERSION}.jar -O
curl ${PATH_TO_ARTIFACT}/components/uri-identity/${VERSION}/uri-identity-${VERSION}.jar -O
curl ${PATH_TO_ARTIFACT}/components/uri-normalization/${VERSION}/uri-normalization-${VERSION}.jar -O
curl ${PATH_TO_ARTIFACT}/components/versioning/${VERSION}/versioning-${VERSION}.jar -O
