
JONGO_MAVEN_OPTIONS="--errors --batch-mode -P release -Dsurefire.printSummary=false"

function _mvn() {
    mvn ${JONGO_MAVEN_OPTIONS:-""} $@
}

function append_maven_options() {
    JONGO_MAVEN_OPTIONS="${JONGO_MAVEN_OPTIONS} ${1}"
}

function get_maven_options() {
    echo "${JONGO_MAVEN_OPTIONS}"
}
