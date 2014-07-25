#! /bin/sh

OUTPUT_DIR=./target/mongo-compatibility
MONGO_ARTIFACTS_FILE=./target/mongo-versions
NEXUS_URL="https://oss.sonatype.org/service/local/data_index?g=org.mongodb&a=mongo-java-driver"
MINIMAL_VERSION="2.12.3"
A_VERSION_HAS_FAILED=false
OPTS=$*

echo "Executing tests with mongo-java-driver[$MINIMAL_VERSION+] dependencies available on Nexus http://repository.sonatype.org"

mkdir -p $OUTPUT_DIR;
VERSIONS=`curl -so $MONGO_ARTIFACTS_FILE $NEXUS_URL &&  grep -e "version" $MONGO_ARTIFACTS_FILE | sed 's/<version>//g' | sed 's/<\/version>//g' | tr -s " " | uniq`;

for version in $VERSIONS
do
    CURRENT=$(echo "$version" | sed "s/\.//g" | sed "s/-.*//g")
    MINIMAL=$(echo "$MINIMAL_VERSION" | sed "s/\.//g" | sed "s/-.*//g")

    if [ ${CURRENT[0]} -ge ${MINIMAL[0]} ] && [ ${CURRENT[1]} -ge ${MINIMAL[1]} ];
    then
      mvn verify $OPTS -Dmongo.version=$version -DreportFormat=plain -DuseFile=false -l $OUTPUT_DIR/build-$version.log

      if [ "$?" -ne "0" ];
      then
        echo "$version FAILED, please check file $OUTPUT_DIR/build-$version.log"
        A_VERSION_HAS_FAILED=true;
      else
        echo "$version SUCCESS"
      fi
    fi
done

if $A_VERSION_HAS_FAILED ; then
  echo "***************************************"
  echo "One or more driver versions have FAILED"
  echo "***************************************"
  exit 1;
fi

