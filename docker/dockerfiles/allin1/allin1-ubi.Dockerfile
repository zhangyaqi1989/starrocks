# This docker file build the Starrocks allin1 ubi8 image
# Please run this command from the git repo root directory to build:
#
#   - Use artifact image to package runtime container:
#     > DOCKER_BUILDKIT=1 docker build --build-arg ARTIFACT_SOURCE=image --build-arg ARTIFACTIMAGE=starrocks/artifacts-centos7:latest -f docker/dockerfiles/allin1/allin1-ubi.Dockerfile -t allin1-ubi:latest .
#   - Use locally build artifacts to package runtime container:
#     > DOCKER_BUILDKIT=1 docker build --build-arg ARTIFACT_SOURCE=local --build-arg LOCAL_REPO_PATH=. -f docker/dockerfiles/allin1/allin1-ubi.Dockerfile -t allin1-ubi:latest .
#
# The artifact source used for packing the runtime docker image
#   image: copy the artifacts from a artifact docker image.
#   local: copy the artifacts from a local repo. Mainly used for local development and test.
ARG ARTIFACT_SOURCE=image
ARG WITH_DEBUG_INFO=false

ARG ARTIFACTIMAGE=starrocks/artifacts-centos7:latest
FROM ${ARTIFACTIMAGE} as artifacts-from-image

# create a docker build stage that copy locally build artifacts
FROM busybox:latest as artifacts-from-local
ARG LOCAL_REPO_PATH

COPY ${LOCAL_REPO_PATH}/output/fe /release/fe_artifacts/fe
COPY ${LOCAL_REPO_PATH}/output/be /release/be_artifacts/be


FROM artifacts-from-${ARTIFACT_SOURCE} as artifacts
ARG WITH_DEBUG_INFO

RUN if [ "$WITH_DEBUG_INFO" = "false" ]; then rm -f /release/be_artifacts/be/lib/starrocks_be.debuginfo; fi

FROM registry.access.redhat.com/ubi8/ubi:8.7
ARG DEPLOYDIR=/data/deploy
ENV SR_HOME=${DEPLOYDIR}/starrocks

RUN yum install -y java-11-openjdk-devel tzdata openssl curl vim ca-certificates fontconfig gzip tar less hostname procps-ng lsof python3-pip nginx nc && \
    rpm -ivh https://repo.mysql.com/mysql80-community-release-el8-7.noarch.rpm && \
    yum -y install mysql-community-client --nogpgcheck && \
    yum remove -y mysql80-community-release && \
    pip3 install supervisor
ENV JAVA_HOME=/usr/lib/jvm/java-11

WORKDIR $DEPLOYDIR

# Copy all artifacts to the runtime container image
COPY --from=artifacts /release/be_artifacts/ $DEPLOYDIR/starrocks
COPY --from=artifacts /release/fe_artifacts/ $DEPLOYDIR/starrocks

# Copy setup script and config files
COPY docker/dockerfiles/allin1/*.sh docker/dockerfiles/allin1/*.conf docker/dockerfiles/allin1/*.txt $DEPLOYDIR
COPY docker/dockerfiles/allin1/services/ $SR_HOME
RUN cat be.conf >> $DEPLOYDIR/starrocks/be/conf/be.conf && \
    cat fe.conf >> $DEPLOYDIR/starrocks/fe/conf/fe.conf && \
    rm -f be.conf fe.conf && \
    mkdir -p $DEPLOYDIR/starrocks/fe/meta $DEPLOYDIR/starrocks/be/storage && touch /.dockerenv

CMD ./entrypoint.sh
