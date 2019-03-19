#!/usr/bin/env bash

set -euo pipefail

readonly JONGO_BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/.."
source "${JONGO_BASE_DIR}/bin/lib/utils/mvn-utils.sh"
source "${JONGO_BASE_DIR}/bin/lib/utils/git-utils.sh"
source "${JONGO_BASE_DIR}/bin/lib/utils/logger-utils.sh"
source "${JONGO_BASE_DIR}/bin/lib/tasks.sh"

function usage {
    echo "Usage: $0 [option...] <tag|tag_early|tag_hotfix|deploy|test>"
    echo
    echo "Command line interface to release and deploy Jongo"
    echo "Note that by default all tasks are ran in dry mode. Set '--dry-run false' to run it for real. "
    echo
    echo "   --branch <branch>              The branch to release"
    echo "   --maven-options <options>      Maven options (eg. '--settings /path/to/settings.xml')"
    echo "   --remote-repository-url <url>  The remote repository url used to clone the project (default https://github.com/bguerout/jongo.git)"
    echo "   --docker                       Build image from Dockerfile and run this script into a Docker container"
    echo "   --mount <path>                 Mount an host dir to '/opt/jongo/conf' in the Docker container (default: target/docker)"
    echo "   --dry-run                      Run task in dry mode. Nothing will be pushed nor deployed (default: true)"
    echo "   --dirty                        Do not clean resources generated during the execution (eg. cloned repository / default: false)"
    echo "   --debug                        Print all executed commands and run Maven in debug mode"
    echo
    echo "Usage examples:"
    echo ""
    echo " Create a tag for the current branch:"
    echo ""
    echo "      ./bin/release.sh tag --docker"
    echo ""
    echo " Create an hotfix tag for the branch releases/1.4.x inside a docker container:"
    echo ""
    echo "      ./bin/release.sh tag_hotfix --branch releases/1.4.x --docker"
    echo ""
    echo " Run CLI tests: "
    echo ""
    echo "      ./bin/release.sh test"
    echo ""
}

function safeguard() {
    while true; do
        read -p "[WARN] Do you really want to run this task for real (y/n)?" yn
        case $yn in
            [Yy]* ) break;;
            [Nn]* ) exit;;
            * ) echo "Please answer yes or no.";;
        esac
    done
}

function clean_resources {
    log_info "Cleaning resources..."
    pkill -P $$
    find ${TMPDIR:-$(dirname $(mktemp))/} -depth -type d -name "jongo-release*" -exec rm -rf {} \;
}

function activate_profiles() {
    local git_revision="${1}"

    if [[ "${git_revision}" = *"-early-"* ]]; then
        append_maven_options "-Pcloudbees"
    else
        append_maven_options "-Psonatype"
    fi
    safeguard
}

function activate_test_profiles() {
    local repo_dir="${1}"

    append_maven_options "-Ptest -Dgpg.passphrase='' -Dgpg.keyname=test@jongo.org \
        -Dgpg.file=${JONGO_BASE_DIR}/src/test/sh/cli/jongo-fake-secret-key.asc"
    update_origin_with_fake_remote "${repo_dir}"
    log_warn "Script is running in dry mode."
}

function check_if_running_inside_docker() {
    if [ -f /.dockerenv ]; then
        log_info "Script is running inside a Docker container"
    fi
}

function run_script_inside_docker() {
    local arguments="${1}"
    local mount_dir="${2}"
    local docker_command=$(echo "${arguments}" | sed -e 's/\(--docker\)*//g')
    local maven_options=(
        "-Dmaven.repo.local=/opt/jongo/conf/.m2/repository"
        "-Djongo.test.embedmongo.dir=/opt/jongo/conf/.m2/mongodb"
    )

    log_info "Host dir ${mount_dir} is mounted into container on path '/opt/jongo/conf'"

    docker build . -t jongo
    docker run -it --volume "${mount_dir}:/opt/jongo/conf:cached" jongo bash -c \
        "./bin/release.sh ${docker_command} --maven-options \"$( IFS=$' '; echo "${maven_options[*]}" )\""
}

function main() {

    local git_revision="$(git rev-parse --abbrev-ref HEAD)"
    local dry_run=true
    local dirty=false
    local docker=false
    local remote_repository_url="https://github.com/bguerout/jongo.git"
    local positional=()
    local docker_options=()

    if [ $# -eq 0 ]; then
        usage
        exit 1
    fi

    while [[ $# -gt 0 ]]
    do
    key="$1"
    case $key in
        --branch)
            git_revision="${2}"
            shift
            shift
        ;;
        --maven-options)
            append_maven_options "${2}"
            docker_options+=("${key} '${2}'")
            shift
            shift
        ;;
        --remote-repository-url)
            readonly remote_repository_url="${2}"
            docker_options+=("${key} ${2}")
            shift
            shift
        ;;
        --docker)
            readonly docker=true
            docker_options+=("")
            log_warn "Docker mode activated."
            shift
        ;;
         --mount)
            readonly mount="${2}"
            shift
            shift
        ;;
        --dirty)
            readonly dirty=true
            docker_options+=("${key}")
            log_warn "Dirty mode activated."
            shift
        ;;
        --debug)
            append_maven_options "-Dsurefire.printSummary=true -X"
            set -x
            docker_options+=("${key}")
            shift
        ;;
        -d|--dry-run)
            readonly dry_run="$2"
            docker_options+=("${key}")
            shift
            shift
        ;;
        -?|--help)
            usage
            exit 0;
        ;;
        *)
            positional+=("$1")
            shift
        ;;
    esac
    done

    set -- "${positional[@]}"
    local task="${1}"

    if [[ "${docker}" = true ]]; then
        local raw_arguments="${positional[@]} ${docker_options[@]}"
        run_script_inside_docker "${raw_arguments}" "${mount:-"${JONGO_BASE_DIR}/target/docker"}"
        exit 0
    else
        check_if_running_inside_docker
    fi

    [[ "${dirty}" = false ]] &&  trap clean_resources EXIT HUP INT QUIT PIPE TERM

    local repo_dir=$(clone_repository "${remote_repository_url}")

    if [[ "${dry_run}" = false ]]; then
        activate_profiles "${git_revision}"
    else
        activate_test_profiles "${repo_dir}"
    fi

    pushd "${repo_dir}" > /dev/null
        case "${task}" in
            test)
                source "${JONGO_BASE_DIR}/src/test/sh/cli/tasks-tests.sh"
                run_test_suite "${git_revision}" "${repo_dir}"
            ;;
            tag_early)
                create_early_tag "${git_revision}"
            ;;
            tag)
                create_tag "${git_revision}"
            ;;
            tag_hotfix)
                create_hotfix_tag "${git_revision}"
            ;;
            deploy)
                if [[ "${git_revision}" = *"-SNAPSHOT"* ]]; then
                    deploy_snapshot "${git_revision}"
                else
                    deploy "${git_revision}"
                fi
            ;;
            *)
             log_error "Unknown task '${task}'"
             usage
             exit 1;
            ;;
        esac
    popd > /dev/null
}

main "$@"
