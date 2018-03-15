
source "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/release-tools.sh"

function create_early_release {
    local base_branch="${1}"
    local current_version=$(get_current_version "origin/${base_branch}")
    local tag_early_version=$(determine_early_release_version "origin/${base_branch}")

    test_jongo "${base_branch}"

    checkout "${base_branch}"
        set_version "${base_branch}" "${tag_early_version}"
        log_info "Branch ${base_branch} updated to project version ${tag_early_version}"

        local commit_to_tag=$(get_head_commit "${base_branch}")
        git tag "${tag_early_version}" "${commit_to_tag}"
        log_info "New tag ${tag_early_version} created refs to ${commit_to_tag}"

        set_version "${base_branch}" "${current_version}"
        log_info "Branch ${base_branch} updated to project version ${current_version}"
    uncheckout

    git push -q origin "${base_branch}"
    git push -q origin "${tag_early_version}"

    log_success "${tag_early_version} early version released"
}

function create_release {
    local base_branch="${1}"
    local hotfix_branch="releases_$(determine_hotfix_version_pattern "origin/${base_branch}")"
    local tag_release_version=$(determine_release_version "origin/${base_branch}")

    test_jongo "${base_branch}"

    checkout -b "${hotfix_branch}" "${base_branch}"
        set_version "${hotfix_branch}" "${tag_release_version}"
        log_info "New branch ${hotfix_branch} created"

        local commit_to_tag=$(get_head_commit "${hotfix_branch}")
        git tag "${tag_release_version}" "${commit_to_tag}"
        log_info "New tag ${tag_release_version} created on ${commit_to_tag}"

        bump_to_next_hotfix_snapshot_version "${hotfix_branch}"
        bump_to_next_minor_snapshot_version "${base_branch}"
    uncheckout


    git push -q -u origin "${hotfix_branch}"
    git push -q origin "${tag_release_version}"
    git push -q origin "${base_branch}"

    log_success "${tag_release_version} version released"
}

function create_hotfix_release {
    local base_branch="${1}"
    local tag_release_version=$(determine_release_version "origin/${base_branch}")

    test_jongo "${base_branch}"

    checkout "${base_branch}"
        set_version "${base_branch}" "${tag_release_version}"

        log_info "New branch ${base_branch} updated to project version ${tag_release_version}"

        local commit_to_tag=$(get_head_commit "${base_branch}")
        git tag "${tag_release_version}" "${commit_to_tag}"
        log_info "New tag ${tag_release_version} created refs to ${commit_to_tag}"

        bump_to_next_hotfix_snapshot_version "${base_branch}"
    uncheckout

    git push -q origin "${base_branch}"
    git push -q origin "${tag_release_version}"

    log_success "${tag_release_version} hotfix version released"
}

function deploy {
    local tag="${1}"

    checkout "${tag}"
        _mvn deploy
    uncheckout
}

function test_jongo {
    local base_branch="${1}"

    checkout "${base_branch}"
         log_info "Retrieving all Maven dependencies..."
        _mvn dependency:go-offline

        log_info "Running Jongo tests..."
        _mvn verify
    uncheckout
}
