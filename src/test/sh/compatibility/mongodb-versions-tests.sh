#!/usr/bin/env bash

set -euo pipefail

function run_tests {
    local mongodb_java_driver_version=${1}
    local mongodb_version=${2}
    local output_dir=${3}

    echo "Running tests against mongo-java-driver ${mongodb_java_driver_version} and MongoDB ${mongodb_version}"
    mvn verify \
    -Dmongo-java-driver.version="${mongodb_java_driver_version}" \
    -Dembedmongo.version="${mongodb_version}" \
    -DreportFormat=plain \
    -DuseFile=false \
    -Dmaven.source.skip=true \
    -Dmaven.javadoc.skip=true \
    --log-file "${output_dir}/build-driver-$mongodb_java_driver_version-db-$mongodb_version.log"
}

function main {
    local output_dir="./target/jongo-compatibility"

    mkdir -p "./target/jongo-compatibility"

    run_tests   "3.5.0"     "3.4.15"     "${output_dir}"
    run_tests   "3.5.0"     "3.6.5"      "${output_dir}"
    run_tests   "3.5.0"     "4.0.2"      "${output_dir}"

    run_tests   "3.6.0"     "3.4.15"     "${output_dir}"
    run_tests   "3.6.0"     "3.6.5"      "${output_dir}"
    run_tests   "3.6.0"     "4.0.2"      "${output_dir}"

    run_tests   "3.6.1"     "3.4.15"     "${output_dir}"
    run_tests   "3.6.1"     "3.6.5"      "${output_dir}"
    run_tests   "3.6.1"     "4.0.2"      "${output_dir}"

    run_tests   "3.6.2"     "3.4.15"     "${output_dir}"
    run_tests   "3.6.2"     "3.6.5"      "${output_dir}"
    run_tests   "3.6.2"     "4.0.2"      "${output_dir}"

    run_tests   "3.6.3"     "3.4.15"     "${output_dir}"
    run_tests   "3.6.3"     "3.6.5"      "${output_dir}"
    run_tests   "3.6.3"     "4.0.2"      "${output_dir}"

    run_tests   "3.6.4"     "3.4.15"     "${output_dir}"
    run_tests   "3.6.4"     "3.6.5"      "${output_dir}"
    run_tests   "3.6.4"     "4.0.2"      "${output_dir}"

    run_tests   "3.7.0"     "3.4.15"     "${output_dir}"
    run_tests   "3.7.0"     "3.6.5"      "${output_dir}"
    run_tests   "3.7.0"     "4.0.2"      "${output_dir}"

    run_tests   "3.7.1"     "3.4.15"     "${output_dir}"
    run_tests   "3.7.1"     "3.6.5"      "${output_dir}"
    run_tests   "3.7.1"     "4.0.2"      "${output_dir}"

    run_tests   "3.8.0"     "3.4.15"     "${output_dir}"
    run_tests   "3.8.0"     "3.6.5"      "${output_dir}"
    run_tests   "3.8.0"     "4.0.2"      "${output_dir}"

    run_tests   "3.8.1"     "3.4.15"     "${output_dir}"
    run_tests   "3.8.1"     "3.6.5"      "${output_dir}"
    run_tests   "3.8.1"     "4.0.2"      "${output_dir}"

    run_tests   "3.8.2"     "3.4.15"     "${output_dir}"
    run_tests   "3.8.2"     "3.6.5"      "${output_dir}"
    run_tests   "3.8.2"     "4.0.2"      "${output_dir}"

    run_tests   "3.9.0"     "3.4.15"     "${output_dir}"
    run_tests   "3.9.0"     "3.6.5"      "${output_dir}"
    run_tests   "3.9.0"     "4.0.2"      "${output_dir}"

    run_tests   "3.9.1"     "3.4.15"     "${output_dir}"
    run_tests   "3.9.1"     "3.6.5"      "${output_dir}"
    run_tests   "3.9.1"     "4.0.2"      "${output_dir}"

    run_tests   "3.10.1"     "3.4.15"     "${output_dir}"
    run_tests   "3.10.1"     "3.6.5"      "${output_dir}"
    run_tests   "3.10.1"     "4.0.2"      "${output_dir}"

    run_tests   "3.10.2"     "3.4.15"     "${output_dir}"
    run_tests   "3.10.2"     "3.6.5"      "${output_dir}"
    run_tests   "3.10.2"     "4.0.2"      "${output_dir}"

    run_tests   "3.12.0"     "3.4.15"     "${output_dir}"
    run_tests   "3.12.0"     "3.6.5"      "${output_dir}"
    run_tests   "3.12.0"     "4.0.2"      "${output_dir}"

    run_tests   "3.12.1"     "3.4.15"     "${output_dir}"
    run_tests   "3.12.1"     "3.6.5"      "${output_dir}"
    run_tests   "3.12.1"     "4.0.2"      "${output_dir}"

    run_tests   "3.12.2"     "3.4.15"     "${output_dir}"
    run_tests   "3.12.2"     "3.6.5"      "${output_dir}"
    run_tests   "3.12.2"     "4.0.2"      "${output_dir}"
}

main "$@"
