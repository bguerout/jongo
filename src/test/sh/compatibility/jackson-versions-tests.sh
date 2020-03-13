#!/usr/bin/env bash

set -euo pipefail

function run_tests {
    local jackson_version=${1}
    local output_dir=${2}

    echo "Running tests against jackson ${jackson_version}"
    mvn verify \
    -Djackson.version="${jackson_version}" \
    -DreportFormat=plain \
    -DuseFile=false \
    -Dmaven.source.skip=true \
    -Dmaven.javadoc.skip=true \
    --log-file "${output_dir}/build-jackson-${jackson_version}.log"
}

function main {
    local output_dir="./target/jongo-compatibility"

    mkdir -p "./target/jongo-compatibility"

    run_tests 	"2.7.0" 	"${output_dir}"
    run_tests 	"2.7.1" 	"${output_dir}"
    run_tests 	"2.7.2" 	"${output_dir}"
    run_tests 	"2.7.3" 	"${output_dir}"
    run_tests 	"2.7.4" 	"${output_dir}"
    run_tests 	"2.7.5" 	"${output_dir}"
    run_tests 	"2.7.6" 	"${output_dir}"
    run_tests 	"2.7.7" 	"${output_dir}"
    run_tests 	"2.7.8" 	"${output_dir}"
    run_tests 	"2.7.9" 	"${output_dir}"
    run_tests 	"2.8.0" 	"${output_dir}"
    run_tests 	"2.8.1" 	"${output_dir}"
    run_tests 	"2.8.10" 	"${output_dir}"
    run_tests 	"2.8.11" 	"${output_dir}"
    run_tests 	"2.8.2" 	"${output_dir}"
    run_tests 	"2.8.3" 	"${output_dir}"
    run_tests 	"2.8.4" 	"${output_dir}"
    run_tests 	"2.8.5" 	"${output_dir}"
    run_tests 	"2.8.6" 	"${output_dir}"
    run_tests 	"2.8.7" 	"${output_dir}"
    run_tests 	"2.8.8" 	"${output_dir}"
    run_tests 	"2.8.9" 	"${output_dir}"
    run_tests 	"2.9.0" 	"${output_dir}"
    run_tests 	"2.9.1" 	"${output_dir}"
    run_tests 	"2.9.2" 	"${output_dir}"
    run_tests 	"2.9.3" 	"${output_dir}"
    run_tests 	"2.9.4" 	"${output_dir}"
    run_tests 	"2.9.5" 	"${output_dir}"
    run_tests 	"2.9.6" 	"${output_dir}"
    run_tests 	"2.9.7" 	"${output_dir}"
    run_tests 	"2.9.8" 	"${output_dir}"
    run_tests 	"2.9.9" 	"${output_dir}"
    run_tests 	"2.9.10" 	"${output_dir}"
}

main "$@"
