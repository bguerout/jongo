
function get_head_commit {
    local base_commit="${1}"
    echo $(git rev-parse "${base_commit}")
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

function determine_release_version {
    local base_commit="${1}"
    local current_version=$(get_current_version "${base_commit}")

    echo $(echo "${current_version}" | sed -e 's/-SNAPSHOT//g')
}

function determine_early_release_version {
    local base_commit="${1}"
    local release_version=$(determine_release_version "${base_commit}")
    local early=$(date +%Y%m%d-%H%M)

    echo "${release_version}-early-${early}"
}

function determine_hotfix_version_pattern {
    local base_commit="${1}"
    echo $(determine_release_version "${base_commit}") | awk -F  "." '{print $1"."$2;}' | xargs -I % echo "%.x"
}

function set_version {
    local base_branch="${1}"
    local next_version="${2}"

    checkout "${base_branch}"
        _mvn versions:set -DnewVersion="${next_version}" -DgenerateBackupPoms=false
        git add pom.xml
        git_commit "[release] Set project version to ${next_version}"
    uncheckout
}

function bump_to_next_hotfix_snapshot_version {
    local base_branch="${1}"

    checkout "${base_branch}"
        _mvn build-helper:parse-version versions:set \
            -DnewVersion='${parsedVersion.majorVersion}.${parsedVersion.minorVersion}.${parsedVersion.nextIncrementalVersion}-SNAPSHOT' \
            -DgenerateBackupPoms=false
        git add pom.xml
        git_commit "[release] Prepare for next hotfix version"
    uncheckout
    log_info "Branch ${base_branch} bumped to $(get_current_version "${base_branch}" )"
}

function bump_to_next_minor_snapshot_version {
    local base_branch="${1}"

    checkout "${base_branch}"
        _mvn build-helper:parse-version versions:set \
            -DnewVersion='${parsedVersion.majorVersion}.${parsedVersion.nextMinorVersion}.0-SNAPSHOT' \
            -DgenerateBackupPoms=false
        git add pom.xml
        git_commit "[release] Prepare for next development version"
    uncheckout
    log_info "Branch ${base_branch} bumped to $(get_current_version "${base_branch}" )"
}
