#!/usr/bin/env bash

function checkout() {
   git checkout -q $@
}

function uncheckout() {
   git checkout -q -
}

function git_commit() {
    git -c user.name="Jongo Script" -c user.email="contact@jongo.org" commit -q -a -m "${1}"
}

function get_head_commit {
    local base_commit="${1}"
    echo $(git rev-parse "${base_commit}")
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
    local remote_url="${1}"
    local repo_dir=$(mktemp -d -t "jongo-release-repo-XXXXX")

    git clone "${remote_url}" "${repo_dir}"

    echo "${repo_dir}"
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
