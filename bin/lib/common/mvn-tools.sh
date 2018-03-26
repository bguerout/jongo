
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

function configure_deploy_plugin_for_early() {
    append_maven_options "-DaltDeploymentRepository=cloudbees-release::default::dav:https://repository-jongo.forge.cloudbees.com/release"
}

function configure_deploy_plugin_for_test() {
    local base_dir="${1}"
    append_maven_options "-DaltDeploymentRepository=test.repo::default::file:${base_dir}/target/deploy"
}

function configure_maven_gpg_plugin() {
    local gpg_keyname="${1}"
    append_maven_options "-Dgpg.keyname=${gpg_keyname}"
}