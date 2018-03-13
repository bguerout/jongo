
readonly JONGO_TEST_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source "${JONGO_TEST_DIR}/assert.sh"

function run_test_suite {
    readonly JONGO_TEST_TARGET_BRANCH="${1}"

    before_all
        should_validate_tools
        can_create_an_early_release
        can_create_a_new_release
        can_create_an_hotfix_release
        can_deploy_artifacts
    after_all
}

function before_all {
    log_header "Starting testing suite"

    git config user.email "test@jongo.org"
    git config user.name "Robert Hue"

    checkout "${JONGO_TEST_TARGET_BRANCH}"

    bump_to_test_version "${JONGO_TEST_TARGET_BRANCH}"
}

function after_all {
    uncheckout
    echo ""
}

function before_each {
    echo ""
    log_header "${FUNCNAME[1]}"
}

function after_each {
    log_success "${FUNCNAME[1]} passed"
}

function should_validate_tools {
    before_each
        echo "[TEST] --> can get the current project version"
        assert_eq "$(get_current_version "${JONGO_TEST_TARGET_BRANCH}")" "42.0.0-SNAPSHOT" "Current versions mismatched"

        echo "[TEST] --> can determine the release version when project will be released"
        assert_eq "$(determine_release_version "${JONGO_TEST_TARGET_BRANCH}")" "42.0.0" "Release versions mismatched"

        echo "[TEST] --> can determine the early release version when project will be released"
        assert_eq "$(determine_early_release_version "${JONGO_TEST_TARGET_BRANCH}")" "42.0.0-early-$(date +%Y%m%d-%H%M)" "Early release versions mismatched"

        echo "[TEST] --> can determine the hotfix version pattern when project will be released"
        assert_eq "$(determine_hotfix_version_pattern "${JONGO_TEST_TARGET_BRANCH}")" "42.0.x" "Versions mismatched"

        echo "[TEST] --> should update project version to the next hotfix version"
        bump_to_next_hotfix_snapshot_version "${JONGO_TEST_TARGET_BRANCH}"
        assert_eq "$(get_current_version "${JONGO_TEST_TARGET_BRANCH}")" "42.0.1-SNAPSHOT" "Versions mismatched"

        echo "[TEST] --> should update project version in pom.xml to the next minor version"
        bump_to_next_minor_snapshot_version "${JONGO_TEST_TARGET_BRANCH}"
        assert_eq "$(get_current_version "${JONGO_TEST_TARGET_BRANCH}")" "42.1.0-SNAPSHOT" "Versions mismatched"

        git reset -q --hard "origin/${JONGO_TEST_TARGET_BRANCH}"
    after_each
}

function can_create_an_early_release {
    before_each

        local expected_early_tag="42.0.0-early-$(date +%Y%m%d-%H%M)"

        create_early_release "${JONGO_TEST_TARGET_BRANCH}"

        local early_commit="$(get_head_commit ${JONGO_TEST_TARGET_BRANCH}^)"
        assert_eq "$(get_current_version ${JONGO_TEST_TARGET_BRANCH})" "42.0.0-SNAPSHOT" "HEAD version of the base branch should be left intact"
        assert_eq "$(get_current_version ${early_commit})" "${expected_early_tag}" "early version in pom.xml has not been set"
        assert_not_eq "$(git ls-remote origin refs/tags/${expected_early_tag})" "" "Tag does not exist or has not been pushed"
        assert_eq "$(git show-ref -s "${expected_early_tag}")" "${early_commit}" "Tag does not point to the right commit"
    after_each
}

function can_create_a_new_release {
    before_each
        create_release "${JONGO_TEST_TARGET_BRANCH}"

        local release_commit="$(get_head_commit releases_42.0.x^)"
        assert_eq "$(git branch -r | grep releases_42.0.x | sed -e 's/ //g')" "origin/releases_42.0.x" "Hotfixes branch has not been created or pushed"
        assert_eq "$(get_current_version releases_42.0.x)" "42.0.1-SNAPSHOT" "HEAD version of the hotfix branch has not been set to the next hotfix snapshot version"
        assert_not_eq "$(git ls-remote origin refs/tags/42.0.0)" "" "Tag does not exist or has not been pushed"
        assert_eq "$(git show-ref -s 42.0.0)" "${release_commit}" "Tag does not point to the right commit"
        assert_eq "$(get_current_version ${release_commit})" "42.0.0" "Release version in pom.xml has not been set"
        assert_eq "$(get_current_version "${JONGO_TEST_TARGET_BRANCH}")" "42.1.0-SNAPSHOT" "HEAD version of the base branch has not been set to the next version"
    after_each
}

function can_create_an_hotfix_release {
    before_each
        create_hotfix_release "releases_42.0.x"

        local hotfix_commit="$(get_head_commit releases_42.0.x^)"
        assert_eq "$(get_current_version releases_42.0.x)" "42.0.2-SNAPSHOT" "Version into hotfixes branch has not been set to the next version"
        assert_eq "$(git tag -l 42.0.1)" "42.0.1" "Hotfix tag does not exist"
        assert_eq "$(git show-ref -s 42.0.1)" "${hotfix_commit}" "Tag does not point to the right commit"
        assert_eq "$(get_current_version ${hotfix_commit})" "42.0.1" "Hotfix version in pom.xml has not been set"
        assert_eq "$(get_current_version "${JONGO_TEST_TARGET_BRANCH}")" "42.1.0-SNAPSHOT" "Version into test branch should be left intact"
    after_each
}

function can_deploy_artifacts {
    before_each
        local tag="42.0.0"
        local deploy_dir="$(pwd)/target/deploy/org/jongo/jongo/${tag}"
        local -r gpg_keyname=$(import_gpg "${JONGO_TEST_DIR}/resources/jongo-dummy-key.gpg")
        append_maven_options "-Dgpg.keyname=${gpg_keyname}"

        deploy ${tag}

        assert_file_exists "${deploy_dir}/jongo-42.0.0-javadoc.jar"
        assert_file_exists "${deploy_dir}/jongo-42.0.0-javadoc.jar.asc"
        assert_file_exists "${deploy_dir}/jongo-42.0.0-javadoc.jar.md5"
        assert_file_exists "${deploy_dir}/jongo-42.0.0-javadoc.jar.sha1"
        assert_signature_is_valid "${deploy_dir}/jongo-42.0.0-javadoc.jar"

        assert_file_exists "${deploy_dir}/jongo-42.0.0-sources.jar"
        assert_file_exists "${deploy_dir}/jongo-42.0.0-sources.jar.asc"
        assert_file_exists "${deploy_dir}/jongo-42.0.0-sources.jar.md5"
        assert_file_exists "${deploy_dir}/jongo-42.0.0-sources.jar.sha1"
        assert_signature_is_valid "${deploy_dir}/jongo-42.0.0-sources.jar"

        assert_file_exists "${deploy_dir}/jongo-42.0.0-tests.jar"
        assert_file_exists "${deploy_dir}/jongo-42.0.0-tests.jar.asc"
        assert_file_exists "${deploy_dir}/jongo-42.0.0-tests.jar.md5"
        assert_file_exists "${deploy_dir}/jongo-42.0.0-tests.jar.sha1"
        assert_signature_is_valid "${deploy_dir}/jongo-42.0.0-tests.jar"

        assert_file_exists "${deploy_dir}/jongo-42.0.0.jar"
        assert_file_exists "${deploy_dir}/jongo-42.0.0.jar.asc"
        assert_file_exists "${deploy_dir}/jongo-42.0.0.jar.md5"
        assert_file_exists "${deploy_dir}/jongo-42.0.0.jar.sha1"
        assert_signature_is_valid "${deploy_dir}/jongo-42.0.0.jar"

        assert_file_exists "${deploy_dir}/jongo-42.0.0.pom"
        assert_file_exists "${deploy_dir}/jongo-42.0.0.pom.asc"
        assert_file_exists "${deploy_dir}/jongo-42.0.0.pom.md5"
        assert_file_exists "${deploy_dir}/jongo-42.0.0.pom.sha1"
        assert_signature_is_valid "${deploy_dir}/jongo-42.0.0.pom"

    after_each
}

function assert_file_exists {
    assert_eq "$([ -f "${1}" ] && echo 'pass' || echo 'fail')" \
            "pass" "File is missing"
}

function assert_signature_is_valid {
    local file="${1}"

     assert_eq "$(gpg -v --no-tty --batch --verify "${file}.asc" ${file} && echo 'pass' || echo 'fail')" \
                "pass" "file signature seems invalid"
}

function bump_to_test_version {
    local branch="${1}"

    #Update pom.xml as if we were working on a version 42
    set_version "${branch}" "42.0.0-SNAPSHOT" && git push -q origin "${branch}"
}



