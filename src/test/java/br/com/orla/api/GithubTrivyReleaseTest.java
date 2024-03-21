package br.com.orla.api;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GithubTrivyReleaseTest {

    private GithubTrivyRelease githubTrivyRelease;

    @BeforeEach
    public void setUp() {
        githubTrivyRelease = new GithubTrivyRelease();
    }

    @Test
    public void shouldReturnAssets() {
        var release = githubTrivyRelease.releaseByTag("v0.49.1");
        assertTrue(release.getAssets().size() > 1);
    }

    @Test
    public void shouldReturnName() {
        var release = githubTrivyRelease.releaseByTag("v0.49.1");
        assertNotNull(release.getAssets().get(0).getName());
    }

    @Test
    public void shouldReturnDownloadUrlj() {
        var release = githubTrivyRelease.releaseByTag("v0.49.1");
        assertNotNull(release.getAssets().get(0).getBrowserDownloadUrl());
    }
}
