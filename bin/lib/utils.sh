
function _mvn() {
    mvn ${JONGO_MAVEN_OPTIONS:-""} $@
}

function checkout() {
   git checkout -q $@
}

function uncheckout() {
   git checkout -q -
}

function import_gpg() {
    local gpg_file="${1}"

    echo "Importing gpg file ${gpg_file} into keyring..."
    export GPG_TTY=$(tty)
    gpg -q --no-tty --batch --import "${gpg_file}"
}

function get_current_branch_name {
    echo $(git rev-parse --abbrev-ref HEAD)
}

function get_head_commit {
    local branch_name="${1}"
    echo $(git rev-parse "${branch_name}")
}

function get_pom_content {
    local branch_name="${1}"
    echo "$(git show "${branch_name}:pom.xml")"
}

function get_current_version {
    local branch_name="${1}"
    local pom_xml_content=$(get_pom_content "${branch_name}")

    echo $(echo "${pom_xml_content}" | grep "<artifactId>jongo</artifactId>" -A 1 | grep version | sed -e 's/<[^>]*>//g' | awk '{$1=$1;print}')
}

function determine_release_version {
    local branch_name="${1}"
    local current_version=$(get_current_version "${branch_name}")

    echo $(echo "${current_version}" | sed -e 's/-SNAPSHOT//g')
}

function determine_early_release_version {
    local branch_name="${1}"
    local release_version=$(determine_release_version "${branch_name}")
    local early=$(date +%Y%m%d-%H%M)

    echo "${release_version}-${early}"
}

function determine_hotfix_version_pattern {
    local branch_name="${1}"
    echo $(determine_release_version "${branch_name}") | awk -F  "." '{print $1"."$2;}' | xargs -I % echo "%.x"
}

function set_version {
    local branch_name="${1}"
    local next_version="${2}"

    checkout "${branch_name}"
        _mvn versions:set -DnewVersion="${next_version}" -DgenerateBackupPoms=false
        git add pom.xml
        git commit -q -a -m "[release] Set project version to ${next_version}"
    uncheckout
}

function bump_to_next_hotfix_snapshot_version {
    local branch_name="${1}"

    checkout "${branch_name}"
        _mvn build-helper:parse-version versions:set \
            -DnewVersion='${parsedVersion.majorVersion}.${parsedVersion.minorVersion}.${parsedVersion.nextIncrementalVersion}-SNAPSHOT' \
            -DgenerateBackupPoms=false
        git add pom.xml
        git commit -q -a -m "[release] Prepare for next hotfix version"
    uncheckout
    echo "[INFO] Branch ${branch_name} bumped to $(get_current_version "${branch_name}" )"
}

function bump_to_next_minor_snapshot_version {
    local branch_name="${1}"

    checkout "${branch_name}"
        _mvn build-helper:parse-version versions:set \
            -DnewVersion='${parsedVersion.majorVersion}.${parsedVersion.nextMinorVersion}.0-SNAPSHOT' \
            -DgenerateBackupPoms=false
        git add pom.xml
        git commit -q -a -m "[release] Prepare for next development version"
    uncheckout
    echo "[INFO] Branch ${branch_name} bumped to $(get_current_version "${branch_name}" )"
}

function create_bare_repository {
    local source_repo=${1}
    local bare_repo_dir=$(mktemp -d -t "jongo-release-bare-repo-XXXXX")
    cp -R ${source_repo}/.git/ ${bare_repo_dir}/
    pushd ${bare_repo_dir} > /dev/null
        git config --bool core.bare true
        git fetch -q origin '*:*'
    popd > /dev/null
    echo "${bare_repo_dir}"
}

function clone_repository {
    local clone_dir=$(mktemp -d -t "jongo-release-repo-XXXXX")
    local remote_url="https://github.com/bguerout/jongo.git"
    git clone "${remote_url}" "${clone_dir}"
    echo "${clone_dir}"
}

function update_origin_with_fake_remote {
    local repo_dir="${1}"
    local bare_repo_dir=$(create_bare_repository ${repo_dir})

    pushd ${repo_dir} > /dev/null
        git remote remove origin
        git remote add origin "${bare_repo_dir}"
        git fetch -q origin
    popd > /dev/null
}

function clean_resources {
    echo "Cleaning resources..."
    find ${TMPDIR:-$(dirname $(mktemp))/} -depth -type d -name "jongo-release*" -exec rm -rf {} \;
}
