#!/usr/bin/env bash

OUTPUT_DIR=./target/jackson-compatibility
JACKSON_ARTIFACTS_FILE=./target/jackson-versions
NEXUS_URL="https://oss.sonatype.org/service/local/data_index?g=com.fasterxml.jackson.core&a=jackson-core"
MINIMAL_VERSION="2.4.0"
EXCLUDED_VERSIONS=""
A_VERSION_HAS_FAILED=false
OPTS=$*

mkdir -p "$OUTPUT_DIR";
DB_VERSIONS="2.6 3.0"

echo "Fetching available mongo-java-driver dependencies from Nexus http://repository.sonatype.org"
JACKSON_VERSIONS=$(curl -so "$JACKSON_ARTIFACTS_FILE" "$NEXUS_URL" &&  grep -e "version" "$JACKSON_ARTIFACTS_FILE" | sed 's/<version>//g' | sed 's/<\/version>//g' | tr -s " " | sort | uniq);
echo "jackson versions found\n[$JACKSON_VERSIONS]"

for jackson_version in $JACKSON_VERSIONS
do
    CURRENT=$(echo "$jackson_version" | sed "s/\.//g" | sed "s/-.*//g")
    MINIMAL=$(echo "$MINIMAL_VERSION" | sed "s/\.//g" | sed "s/-.*//g")

    if [ ${CURRENT:0:1} -gt ${MINIMAL:0:1} ] \
            || [ ${CURRENT:0:1} -eq ${MINIMAL:0:1} -a ${CURRENT:1} -ge ${MINIMAL:1} ] \
                    && [[ $EXCLUDED_VERSIONS != *"$jackson_version"* ]] \
                    && [[ $jackson_version != *""-SNAPSHOT""* ]] \
                    && [[ $jackson_version != *"-rc"* ]];
    then

        bson_minor=${CURRENT:1:1}
        if [ ${bson_minor} -eq 5 ];
        then
             bson4jackson_version="2.${bson_minor}.1"
        else
             bson4jackson_version="2.${bson_minor}.0"
        fi

        echo "Running tests against jackson ${jackson_version} and bson4jackson ${bson4jackson_version}"
        mvn verify $OPTS \
            -Djackson.version="$jackson_version" \
            -Dbson4jackson.version="$bson4jackson_version" \
            -DreportFormat=plain \
            -DuseFile=false \
            -l "$OUTPUT_DIR/build-$jackson_version-$bson4jackson_version.log"


      if [ "$?" -ne "0" ];
      then
        echo "${jackson_version} with db ${db_version} FAILED, please check file $log_file"
        A_VERSION_HAS_FAILED=true;
      else
        echo "${jackson_version} SUCCESS"
      fi
    else
       echo "Ignoring version ${jackson_version}"
    fi
done

if $A_VERSION_HAS_FAILED ; then
  echo "***************************************"
  echo "One or more driver versions have FAILED"
  echo "***************************************"
  exit 1;
fi

