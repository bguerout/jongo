
JONGO_MAVEN_OPTIONS="--errors --batch-mode -Psign-artefacts -Dsurefire.printSummary=false"

function _mvn() {
    mvn ${JONGO_MAVEN_OPTIONS:-""} $@
}

function append_maven_options() {
    JONGO_MAVEN_OPTIONS="${JONGO_MAVEN_OPTIONS} ${1}"
}

function get_maven_options() {
    echo "${JONGO_MAVEN_OPTIONS}"
}

function get_pom_content {
    local base_commit="${1}"
    echo "$(git show "${base_commit}:pom.xml")"
}

function get_current_version {
    local base_commit="${1}"
    local pom_xml_content=$(get_pom_content "${base_commit}")

    echo $(echo "${pom_xml_content}" | grep "<artifactId>jongo</artifactId>" -A 1 | grep version | sed -e 's/<[^>]*>//g' | awk '{$1=$1;print}')
}

function set_version {
    local base_branch="${1}"
    local next_version="${2}"

    checkout "${base_branch}"
        _mvn --quiet versions:set -DnewVersion="${next_version}" -DgenerateBackupPoms=false
        git add pom.xml
        git_commit "[release] Set project version to ${next_version}"
    uncheckout
}