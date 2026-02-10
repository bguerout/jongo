#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/utils.sh"

if ! command -v docker &>/dev/null; then
    echo "docker command is missing."
    exit 1
fi

if ! command -v jq &>/dev/null; then
    echo "jq command is missing."
    exit 1
fi

function get_mongodb_major_final_versions() {
    local url="https://registry.hub.docker.com/v2/repositories/library/mongo/tags?page_size=100"
    local json

    while [ "$url" != "null" ] && [ -n "$url" ]; do
        json="$(curl -fsSL "$url")" || {
            echo "Erreur lors de la récupération des versions MongoDB" >&2
            return 1
        }
        echo "$json" | jq -r '.results[].name'
        url="$(echo "$json" | jq -r '.next')"
    done |
        grep -E '^[0-9]+\.[0-9]+\.[0-9]+$' |
        awk -F. '$1 >= 3' |
        sort -t. -k1,1n -k2,2n -k3,3nr |
        awk -F. '!seen[$1"."$2]++' |
        sort -t. -k1,1n -k2,2n -k3,3n
}

function wait_for_mongodb() {
    local mongodb_port=${1}
    local max_attempts=30
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if nc -z localhost "${mongodb_port}" 2>/dev/null; then
            sleep 2
            echo "MongoDB is ready!" >&2
            return 0
        fi

        sleep 1
        attempt=$((attempt + 1))
    done

    echo "MongoDB failed to become ready after ${max_attempts} seconds" >&2
    return 1
}

function find_available_port() {
    local port=27018
    while lsof -Pi :$port -sTCP:LISTEN -t >&2; do
        port=$((port + 1))
    done
    echo $port
}

function remove_mongodb_container() {
    local mongodb_version=${1}
    local container_name="jongo-mongo-${mongodb_version}"

    if docker ps -a --format '{{.Names}}' | grep -q "^${container_name}$" >&2; then
        docker stop "${container_name}" >&2
        docker rm "${container_name}" >&2
    fi
}

function start_mongodb_container() {
    local mongodb_version=${1}
    local mongodb_port=${2}
    local image_name="mongo:${mongodb_version}"
    local container_name="jongo-mongo-${mongodb_version}"

    remove_mongodb_container "${mongodb_version}"

    docker run -d \
        --platform linux/amd64 \
        --tmpfs /data/db:rw,size=1g \
        -p "${mongodb_port}":27017 \
        --name "${container_name}" \
        "${image_name}" \
        mongod --wiredTigerCacheSizeGB 0.5 >&2

    # Wait for MongoDB to be ready
    wait_for_mongodb "${mongodb_port}" || {
        echo "Failed to start MongoDB ${mongodb_version}" >&2
        remove_mongodb_container "${mongodb_version}"
        return 1
    }
}

function check_driver_compatibility() {
    local mongo_driver_legacy_version=${1}
    local mongodb_version=${2}
    local mongodb_port
    mongodb_port="$(find_available_port)"

    start_mongodb_container "${mongodb_version}" "${mongodb_port}" || {
        echo "❌ Failed to start MongoDB ${mongodb_version}"
        return 1
    }

    check_compatibility \
        -Dmongo-driver-legacy.version="${mongo_driver_legacy_version}" \
        -Dembedmongo.disabled=true \
        -Dlocalmongo.port="${mongodb_port}" \
        --log-file "${OUTPUT_DIR}/mongodb-${mongo_driver_legacy_version}-${mongodb_version}.log"

    remove_mongodb_container "${mongodb_version}"

    echo "✅ mongo-driver-legacy ${mongo_driver_legacy_version} on MongoDB ${mongodb_version}"
}

function run_tests() {
    local mongo_driver_legacy_version=${1}

    # Run get_mongodb_major_final_versions to obtain versions
    check_driver_compatibility "${mongo_driver_legacy_version}" "3.7.1"
    check_driver_compatibility "${mongo_driver_legacy_version}" "4.4.30"
    check_driver_compatibility "${mongo_driver_legacy_version}" "5.0.32"
    check_driver_compatibility "${mongo_driver_legacy_version}" "6.0.27"
    check_driver_compatibility "${mongo_driver_legacy_version}" "7.0.29"
    check_driver_compatibility "${mongo_driver_legacy_version}" "8.0.18"
    check_driver_compatibility "${mongo_driver_legacy_version}" "8.2.4"
}

function main() {
    run_tests "4.0.6"
    run_tests "4.1.2"
    run_tests "4.2.3"
    run_tests "4.3.4"
    run_tests "4.4.2"
    run_tests "4.5.1"
    run_tests "4.6.1"
    run_tests "4.7.2"
    run_tests "4.8.2"
    run_tests "4.9.1"
    run_tests "4.10.2"
    run_tests "4.11.5"
    run_tests "5.0.1"
    run_tests "5.1.4"
    run_tests "5.2.1"
    run_tests "5.3.1"
    run_tests "5.4.0"
    run_tests "5.6.3"
}

main "$@"
