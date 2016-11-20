#!/usr/bin/env bash

set -euo pipefail

BASEDIR=$(dirname "$0")
JOB="CI"
MVN_COMMONS_OPTS="--errors --batch-mode --show-version"
SCM_URL=https://github.com/bguerout/jongo.git
import_gpg() {
    echo "Importing gpg file"
    gpg --no-tty --batch --import $GPG_FILE || true
}
clone_repo() {
    mvn clean
    git clone ${SCM_URL} target/checkout
}

while [[ $# > 1 ]]
do
key="$1"
case $key in
    -j|--job)
        JOB="$2"
        shift
    ;;
    -g|--gpg-file)
        GPG_FILE="$2"
        shift
    ;;
    -d|--dry-run)
        DRY_RUN="$2"
        shift
    ;;
    -r|--release-version)
        RELEASE_VERSION="$2"
        shift
    ;;
    -n|--next-version)
        NEXT_VERSION="$2"
        shift
    ;;
    -t|--tag-name)
        TAG_NAME="$2"
        shift
    ;;
    *)
     # unknown option
    ;;
esac
shift
done

case "$JOB" in
CI)
    mvn clean install ${MVN_COMMONS_OPTS}
    ;;
IT)
    mvn clean install ${MVN_COMMONS_OPTS}
    bash $BASEDIR/src/test/sh/run-tests-against-all-driver-versions.sh
    bash BASEDIR/src/test/sh/run-tests-against-all-jackson-versions.sh
    ;;
EARLY)
    import_gpg
    mvn validate release:clean release:prepare release:perform \
        ${MVN_COMMONS_OPTS} \
        -DdryRun=$DRY_RUN \
        -Pearly
    ;;
TAG)
    import_gpg
    clone_repo
    pushd target/checkout

    mvn versions:set ${MVN_COMMONS_OPTS} -DnewVersion=$RELEASE_VERSION -DgenerateBackupPoms=false
    mvn clean verify
    git commit -a -m "[release] Update version to ${RELEASE_VERSION}"

    mvn versions:set ${MVN_COMMONS_OPTS} -DnewVersion=$NEXT_VERSION -DgenerateBackupPoms=false
    mvn clean verify
    git commit -a -m "[release] Prepare for next development iteration ${NEXT_VERSION}"

    git push
    popd
    ;;
RELEASE)
    import_gpg
    clone_repo
    pushd target/checkout
    git checkout -b ${TAG_NAME} ${TAG_NAME}
    mvn clean deploy \
        ${MVN_COMMONS_OPTS} \
         -s /private/jongo/cloudbees-settings.xml \
         -Psonatype-oss-release,cloudbees,jongo
    popd
    ;;
esac