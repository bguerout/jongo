#! /bin/sh

VERSIONS=./target/mongo-versions
NEXUS_URL="http://repository.sonatype.org/service/local/data_index?g=org.mongodb&a=mongo-java-driver"

echo "Executing tests with all mongo-java-driver dependencies available on Nexus http://repository.sonatype.org"

mkdir ./target;

for version in `curl -so $VERSIONS $NEXUS_URL &&  grep -e "version" $VERSIONS | sed 's/<version>//g' | sed 's/<\/version>//g' | tr -s " " | sort -u`;
do
mvn -l target/mongo-compatibility/build-$version.log verify -Dmongo.version=$version;

if [ "$?" -ne "0" ];
then
  echo "$version FAILED"
else
  echo "$version SUCCESS"
fi

done
