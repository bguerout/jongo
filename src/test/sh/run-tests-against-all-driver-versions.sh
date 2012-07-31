#! /bin/sh

OUTPUT_DIR=./target/mongo-compatibility
MONGO_ARTIFACTS_FILE=./target/mongo-versions
NEXUS_URL="https://oss.sonatype.org/service/local/data_index?g=org.mongodb&a=mongo-java-driver"
LAST_UNSUPPORTED_VERSION="2.8.0"
OPTS=$*

echo "Executing tests with mongo-java-driver[$LAST_UNSUPPORTED_VERSION+] dependencies available on Nexus http://repository.sonatype.org"

mkdir -p $OUTPUT_DIR;
VERSIONS=`curl -so $MONGO_ARTIFACTS_FILE $NEXUS_URL &&  grep -e "version" $MONGO_ARTIFACTS_FILE | sed 's/<version>//g' | sed 's/<\/version>//g' | tr -s " " | sort -u`;

for version in $VERSIONS
do
    if [ "$version" \> "$LAST_UNSUPPORTED_VERSION" ] || [ "$version" = "$LAST_UNSUPPORTED_VERSION" ];
    then
      mvn verify $OPTS -Dmongo.version=$version -DreportFormat=plain -DuseFile=false -l $OUTPUT_DIR/build-$version.log 

      if [ "$?" -ne "0" ];
      then
        echo "$version FAILED, please check file $OUTPUT_DIR/build-$version.log"
        exit 1;
      else
        echo "$version SUCCESS"
      fi
    fi
done
