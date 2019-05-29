#!/usr/bin/env bash

set -euo pipefail

function run_tests {
    local mongodb_driver_version=${1}
    local mongo_version=${2}
    local output_dir=${3}

    echo "Running tests against mongo-java-driver ${mongodb_driver_version} and MongoDB ${mongo_version}"
    mvn verify \
    -Dmongo.version="${mongodb_driver_version}" \
    -Dmongodb.version="${mongo_version}" \
    -DreportFormat=plain \
    -DuseFile=false \
    -Dmaven.source.skip=true \
    -Dmaven.javadoc.skip=true \
    --log-file "${output_dir}/build-driver-$mongodb_driver_version-db-$mongo_version.log"
}

function main {
    local output_dir="./target/jongo-compatibility"

    mkdir -p "./target/jongo-compatibility"

    run_tests 	"3.3.0" 	"2.6.11" 	"${output_dir}"
    run_tests 	"3.3.0" 	"3.0.8" 	"${output_dir}"
    run_tests 	"3.3.0" 	"3.2.20" 	"${output_dir}"

    run_tests 	"3.4.0" 	"2.6.11" 	"${output_dir}"
    run_tests 	"3.4.0" 	"3.0.8" 	"${output_dir}"
    run_tests 	"3.4.0" 	"3.2.20" 	"${output_dir}"
    run_tests 	"3.4.0" 	"3.4.15" 	"${output_dir}"

    run_tests 	"3.4.1" 	"2.6.11" 	"${output_dir}"
    run_tests 	"3.4.1" 	"3.0.8" 	"${output_dir}"
    run_tests 	"3.4.1" 	"3.2.20" 	"${output_dir}"
    run_tests 	"3.4.1" 	"3.4.15" 	"${output_dir}"

    run_tests 	"3.4.2" 	"2.6.11" 	"${output_dir}"
    run_tests 	"3.4.2" 	"3.0.8" 	"${output_dir}"
    run_tests 	"3.4.2" 	"3.2.20" 	"${output_dir}"
    run_tests 	"3.4.2" 	"3.4.15" 	"${output_dir}"

    run_tests   "3.5.0"     "2.6.11"     "${output_dir}"
    run_tests   "3.5.0"     "3.0.8"      "${output_dir}"
    run_tests   "3.5.0"     "3.2.20"     "${output_dir}"
    run_tests   "3.5.0"     "3.4.15"     "${output_dir}"
    run_tests   "3.5.0"     "3.6.5"     "${output_dir}"

    run_tests   "3.6.0"     "2.6.11"     "${output_dir}"
    run_tests   "3.6.0"     "3.0.8"      "${output_dir}"
    run_tests   "3.6.0"     "3.2.20"     "${output_dir}"
    run_tests   "3.6.0"     "3.4.15"     "${output_dir}"
    run_tests   "3.6.0"     "3.6.5"      "${output_dir}"

    run_tests   "3.6.1"     "2.6.11"     "${output_dir}"
    run_tests   "3.6.1"     "3.0.8"      "${output_dir}"
    run_tests   "3.6.1"     "3.2.20"     "${output_dir}"
    run_tests   "3.6.1"     "3.4.15"     "${output_dir}"
    run_tests   "3.6.1"     "3.6.5"      "${output_dir}"

    run_tests   "3.6.2"     "2.6.11"     "${output_dir}"
    run_tests   "3.6.2"     "3.0.8"      "${output_dir}"
    run_tests   "3.6.2"     "3.2.20"     "${output_dir}"
    run_tests   "3.6.2"     "3.4.15"     "${output_dir}"
    run_tests   "3.6.2"     "3.6.5"      "${output_dir}"

    run_tests   "3.6.3"     "2.6.11"     "${output_dir}"
    run_tests   "3.6.3"     "3.0.8"      "${output_dir}"
    run_tests   "3.6.3"     "3.2.20"     "${output_dir}"
    run_tests   "3.6.3"     "3.4.15"     "${output_dir}"
    run_tests   "3.6.3"     "3.6.5"      "${output_dir}"

    run_tests   "3.6.4"     "2.6.11"     "${output_dir}"
    run_tests   "3.6.4"     "3.0.8"      "${output_dir}"
    run_tests   "3.6.4"     "3.2.20"     "${output_dir}"
    run_tests   "3.6.4"     "3.4.15"     "${output_dir}"
    run_tests   "3.6.4"     "3.6.5"      "${output_dir}"

    run_tests   "3.7.0"     "2.6.11"     "${output_dir}"
    run_tests   "3.7.0"     "3.0.8"      "${output_dir}"
    run_tests   "3.7.0"     "3.2.20"     "${output_dir}"
    run_tests   "3.7.0"     "3.4.15"     "${output_dir}"
    run_tests   "3.7.0"     "3.6.5"      "${output_dir}"

    run_tests   "3.7.1"     "2.6.11"     "${output_dir}"
    run_tests   "3.7.1"     "3.0.8"      "${output_dir}"
    run_tests   "3.7.1"     "3.2.20"     "${output_dir}"
    run_tests   "3.7.1"     "3.4.15"     "${output_dir}"
    run_tests   "3.7.1"     "3.6.5"      "${output_dir}"

    run_tests   "3.8.0"     "2.6.11"     "${output_dir}"
    run_tests   "3.8.0"     "3.0.8"      "${output_dir}"
    run_tests   "3.8.0"     "3.2.20"     "${output_dir}"
    run_tests   "3.8.0"     "3.4.15"     "${output_dir}"
    run_tests   "3.8.0"     "3.6.5"      "${output_dir}"
    run_tests   "3.8.0"     "4.0.2"      "${output_dir}"

    run_tests   "3.8.1"     "2.6.11"     "${output_dir}"
    run_tests   "3.8.1"     "3.0.8"      "${output_dir}"
    run_tests   "3.8.1"     "3.2.20"     "${output_dir}"
    run_tests   "3.8.1"     "3.4.15"     "${output_dir}"
    run_tests   "3.8.1"     "3.6.5"      "${output_dir}"
    run_tests   "3.8.1"     "4.0.2"      "${output_dir}"

    run_tests   "3.8.2"     "2.6.11"     "${output_dir}"
    run_tests   "3.8.2"     "3.0.8"      "${output_dir}"
    run_tests   "3.8.2"     "3.2.20"     "${output_dir}"
    run_tests   "3.8.2"     "3.4.15"     "${output_dir}"
    run_tests   "3.8.2"     "3.6.5"      "${output_dir}"
    run_tests   "3.8.2"     "4.0.2"      "${output_dir}"

    run_tests   "3.9.0"     "2.6.11"     "${output_dir}"
    run_tests   "3.9.0"     "3.0.8"      "${output_dir}"
    run_tests   "3.9.0"     "3.2.20"     "${output_dir}"
    run_tests   "3.9.0"     "3.4.15"     "${output_dir}"
    run_tests   "3.9.0"     "3.6.5"      "${output_dir}"
    run_tests   "3.9.0"     "4.0.2"      "${output_dir}"

    run_tests   "3.9.1"     "2.6.11"     "${output_dir}"
    run_tests   "3.9.1"     "3.0.8"      "${output_dir}"
    run_tests   "3.9.1"     "3.2.20"     "${output_dir}"
    run_tests   "3.9.1"     "3.4.15"     "${output_dir}"
    run_tests   "3.9.1"     "3.6.5"      "${output_dir}"
    run_tests   "3.9.1"     "4.0.2"      "${output_dir}"

    run_tests   "3.10.1"     "2.6.11"     "${output_dir}"
    run_tests   "3.10.1"     "3.0.8"      "${output_dir}"
    run_tests   "3.10.1"     "3.2.20"     "${output_dir}"
    run_tests   "3.10.1"     "3.4.15"     "${output_dir}"
    run_tests   "3.10.1"     "3.6.5"      "${output_dir}"
    run_tests   "3.10.1"     "4.0.2"      "${output_dir}"
}

main "$@"

