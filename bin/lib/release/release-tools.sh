
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
        git_commit "[release] Set project version to ${next_version}"
    uncheckout
}

function bump_to_next_hotfix_snapshot_version {
    local branch_name="${1}"

    checkout "${branch_name}"
        _mvn build-helper:parse-version versions:set \
            -DnewVersion='${parsedVersion.majorVersion}.${parsedVersion.minorVersion}.${parsedVersion.nextIncrementalVersion}-SNAPSHOT' \
            -DgenerateBackupPoms=false
        git add pom.xml
        git_commit "[release] Prepare for next hotfix version"
    uncheckout
    log_info "Branch ${branch_name} bumped to $(get_current_version "${branch_name}" )"
}

function bump_to_next_minor_snapshot_version {
    local branch_name="${1}"

    checkout "${branch_name}"
        _mvn build-helper:parse-version versions:set \
            -DnewVersion='${parsedVersion.majorVersion}.${parsedVersion.nextMinorVersion}.0-SNAPSHOT' \
            -DgenerateBackupPoms=false
        git add pom.xml
        git_commit "[release] Prepare for next development version"
    uncheckout
    log_info "Branch ${branch_name} bumped to $(get_current_version "${branch_name}" )"
}
