PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/../../.."
OUTPUT_DIR="${PROJECT_DIR}/.compatibility"

DB_BASE_PATH=""
if [[ "$(uname)" == "Darwin" ]]; then
    DISK_NAME="MongoRAM"
    DB_BASE_PATH="/Volumes/${DISK_NAME}"
    if [ ! -d "${DB_BASE_PATH}" ]; then
        diskutil eject "${DISK_NAME}" >/dev/null 2>&1 || true
        # shellcheck disable=SC2046
        diskutil erasevolume HFS+ "${DISK_NAME}" $(hdiutil attach -nomount ram://4194304)
    fi

else
    DB_BASE_PATH="/tmp"
fi

function unsupported() {
    local libname=${1}
    local version=${2}
    echo "❌ ${libname} ${version} not supported"
}

function check_compatibility() {
    cd "${PROJECT_DIR}" && mvn clean >/dev/null 2>&1
    cd "${PROJECT_DIR}" && mvn verify \
        -Dembedmongo.dbpath="${DB_BASE_PATH}" \
        -DreportFormat=plain \
        -DuseFile=false \
        -Dmaven.source.skip=true \
        -Dmaven.javadoc.skip=true \
        "$@"
}

function check_lib_compatibility() {
    local libname=${1}
    local version=${2}
    shift
    shift

    mkdir -p "${OUTPUT_DIR}"

    check_compatibility \
        "-D${libname}.version=${version}" \
        --log-file "${OUTPUT_DIR}/${libname}-${version}.log" \
        "$@"

    echo "✅ ${libname} ${version}"
}
