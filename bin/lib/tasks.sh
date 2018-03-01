
source "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/release-tools.sh"

function create_early_release {
    local target_branch="${1}"
    local current_version=$(get_current_version "${target_branch}")
    local tag_early_version=$(determine_early_release_version "${target_branch}")

    log_info "***************************************************************************************"
    log_info "* Releasing early ${tag_early_version} from branch ${target_branch}"
    log_info "***************************************************************************************"

    checkout "${target_branch}"
        set_version "${target_branch}" "${tag_early_version}"
        log_info "Branch ${target_branch} updated to project version ${tag_early_version}"

        local commit_to_tag=$(get_head_commit "${target_branch}")
        git tag "${tag_early_version}" "${commit_to_tag}"
        log_info "New tag ${tag_early_version} created refs to ${commit_to_tag}"

        set_version "${target_branch}" "${current_version}"
        log_info "Branch ${target_branch} updated to project version ${current_version}"
    uncheckout

    git push -q origin "${target_branch}"

    log_info "SUCCESS "${tag_early_version}" early version released"
}

function create_release {
    local target_branch="${1}"
    local hotfix_branch="releases_$(determine_hotfix_version_pattern "${target_branch}")"
    local tag_release_version=$(determine_release_version "${target_branch}")

    log_info "***************************************************************************************"
    log_info "* Releasing ${tag_release_version} from branch ${target_branch}"
    log_info "***************************************************************************************"

    checkout -b "${hotfix_branch}" "${target_branch}"
        set_version "${hotfix_branch}" "${tag_release_version}"
        log_info "New branch ${hotfix_branch} created"

        local commit_to_tag=$(get_head_commit "${hotfix_branch}")
        git tag "${tag_release_version}" "${commit_to_tag}"
        log_info "New tag ${tag_release_version} created on ${commit_to_tag}"

        bump_to_next_hotfix_snapshot_version "${hotfix_branch}"
        bump_to_next_minor_snapshot_version "${target_branch}"
    uncheckout


    git push -q -u origin "${hotfix_branch}"
    git push -q origin "${tag_release_version}"
    git push -q origin "${target_branch}"

    log_info "SUCCESS "${tag_release_version}" version released"
}

function create_hotfix_release {
    local hotfix_branch="${1}"
    local tag_release_version=$(determine_release_version "${hotfix_branch}")

    log_info "***************************************************************************************"
    log_info "* Releasing hotfix ${tag_release_version} from branch ${hotfix_branch}"
    log_info "***************************************************************************************"

    checkout "${hotfix_branch}"
        set_version "${hotfix_branch}" "${tag_release_version}"

        log_info "New branch ${hotfix_branch} updated to project version ${tag_release_version}"

        local commit_to_tag=$(get_head_commit "${hotfix_branch}")
        git tag "${tag_release_version}" "${commit_to_tag}"
        log_info "New tag ${tag_release_version} created refs to ${commit_to_tag}"

        bump_to_next_hotfix_snapshot_version "${hotfix_branch}"
    uncheckout

    git push -q origin "${hotfix_branch}"

    log_info "SUCCESS "${tag_release_version}" hotfix version released"
}

function deploy {
    local tag="${1}"
    local keyname="${2}"

    log_info "***************************************************************************************"
    log_info "* Deploying ${tag} into repository with maven options"
    log_info "***************************************************************************************"

    git checkout -q "${tag}"
    _mvn deploy -Dgpg.keyname="${keyname}"
}