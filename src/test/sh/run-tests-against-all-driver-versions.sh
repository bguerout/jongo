#! /bin/sh

OUTPUT_DIR=./target/mongo-compatibility
MONGO_ARTIFACTS_FILE=./target/mongo-versions
NEXUS_URL="https://oss.sonatype.org/service/local/data_index?g=org.mongodb&a=mongo-java-driver"
MINIMAL_VERSION="2.13.0"
EXCLUDED_VERSIONS="2.13.0-rc0 3.0.0-test-SNAPSHOT"
A_VERSION_HAS_FAILED=false
OPTS=$*

mkdir -p "$OUTPUT_DIR";
DB_VERSIONS=( "2.6" "3.0" )
DRIVER_VERSIONS=$(curl -so "$MONGO_ARTIFACTS_FILE" "$NEXUS_URL" &&  grep -e "version" "$MONGO_ARTIFACTS_FILE" | sed 's/<version>//g' | sed 's/<\/version>//g' | tr -s " " | sort | uniq);

echo "Executing tests for mongo-java-driver[$DRIVER_VERSIONS] dependencies available on Nexus http://repository.sonatype.org"

for db_version in $DB_VERSIONS
do
    for driver_version in $DRIVER_VERSIONS
    do
        CURRENT=$(echo "$driver_version" | sed "s/\.//g" | sed "s/-.*//g")
        MINIMAL=$(echo "$MINIMAL_VERSION" | sed "s/\.//g" | sed "s/-.*//g")

        if [ ${CURRENT:0:1} -gt ${MINIMAL:0:1} ] || [ ${CURRENT:0:1} -eq ${MINIMAL:0:1} -a ${CURRENT:1} -ge ${MINIMAL:1} ] && [[ $EXCLUDED_VERSIONS != *"$driver_version"* ]];
        then

          echo "Running tests against java driver ${driver_version} and MongoDB ${db_version}"
          mvn verify $OPTS \
            -Djongo.test.db.version="${db_version}" \
            -Dmongo.version="$driver_version" \
            -DreportFormat=plain \
            -DuseFile=false \
            -l $OUTPUT_DIR/build-"$version".log

          if [ "$?" -ne "0" ];
          then
            echo "${driver_version} with db ${db_version} FAILED, please check file $OUTPUT_DIR/build-$version.log"
            A_VERSION_HAS_FAILED=true;
          else
            echo "${driver_version} SUCCESS"
          fi
        fi
    done
done

if $A_VERSION_HAS_FAILED ; then
  echo "***************************************"
  echo "One or more driver versions have FAILED"
  echo "***************************************"
  exit 1;
fi

