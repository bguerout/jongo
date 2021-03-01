#!/usr/bin/env bash

set -euo pipefail

function run_tests() {
  local mongo_driver_legacy_version=${1}
  local mongodb_version=${2}
  local output_dir=${3}

  echo "Running tests against mongo-driver-legacy ${mongo_driver_legacy_version} and MongoDB ${mongodb_version}"
  mvn verify \
    -Dmongo-driver-legacy.version="${mongo_driver_legacy_version}" \
    -Dembedmongo.version="${mongodb_version}" \
    -DreportFormat=plain \
    -DuseFile=false \
    -Dmaven.source.skip=true \
    -Dmaven.javadoc.skip=true \
    --log-file "${output_dir}/build-driver-$mongo_driver_legacy_version-db-$mongodb_version.log"
}

function main() {
  local output_dir="./target/jongo-compatibility"

  mkdir -p "./target/jongo-compatibility"

  run_tests "4.0.6" "3.4.15" "${output_dir}"
  run_tests "4.0.6" "3.5.5" "${output_dir}"
  run_tests "4.0.6" "3.6.5" "${output_dir}"
  run_tests "4.0.6" "4.0.12" "${output_dir}"
  #run_tests "4.0.6" "4.4.1" "${output_dir}"
  #see https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo/issues/326

  run_tests "4.1.2" "3.4.15" "${output_dir}"
  run_tests "4.1.2" "3.5.5" "${output_dir}"
  run_tests "4.1.2" "3.6.5" "${output_dir}"
  run_tests "4.1.2" "4.0.12" "${output_dir}"
  #run_tests "4.1.2" "4.4.1" "${output_dir}"

  run_tests "4.2.1" "3.4.15" "${output_dir}"
  run_tests "4.2.1" "3.5.5" "${output_dir}"
  run_tests "4.2.1" "3.6.5" "${output_dir}"
  run_tests "4.2.1" "4.0.12" "${output_dir}"
  #run_tests "4.2.1" "4.4.1" "${output_dir}"
}

main "$@"
