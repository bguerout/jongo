#! /bin/sh

OUTPUT_DIR=./target/mongo-compatibility
MONGO_ARTIFACTS_FILE=./target/mongo-versions
NEXUS_URL="https://oss.sonatype.org/service/local/data_index?g=org.mongodb&a=mongo-java-driver"
MINIMAL_VERSION="2.11.0"
A_VERSION_HAS_FAILED=false
OPTS=$*

echo "Executing tests with mongo-java-driver[$MINIMAL_VERSION+] dependencies available on Nexus http://repository.sonatype.org"

mkdir -p $OUTPUT_DIR;
VERSIONS=`curl -so $MONGO_ARTIFACTS_FILE $NEXUS_URL &&  grep -e "version" $MONGO_ARTIFACTS_FILE | sed 's/<version>//g' | sed 's/<\/version>//g' | tr -s " " | uniq`;

for version in $VERSIONS
do
    IFS='.' read -ra current <<< "$version"
    IFS='.' read -ra minimal <<< "$MINIMAL_VERSION"
    if [ ${current[0]} -ge ${minimal[0]} ] && [ ${current[1]} -ge ${minimal[1]} ];
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

