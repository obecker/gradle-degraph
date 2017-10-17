package de.obqo.gradle.degraph;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Oliver Becker
 */
public class DegraphPluginFunctionalTest {

    private List<File> pluginClasspath;

    @Before
    public void setUp() throws Exception {
        final URL pluginClasspathResource = getClass().getClassLoader().getResource("plugin-classpath.txt");
        if (pluginClasspathResource == null) {
            throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.");
        }

        try (final InputStream is = pluginClasspathResource.openStream()) {
            this.pluginClasspath = IOUtils.readLines(is, Charset.defaultCharset())
                    .stream()
                    .map(File::new)
                    .collect(Collectors.toList());
        }
    }

    @Test
    public void shouldSucceed() throws Exception {
        BuildResult result = build("success.gradle");
        assertBuildResult(result, TaskOutcome.SUCCESS, "degraphMain");
        assertBuildResult(result, TaskOutcome.SUCCESS, "degraphTest");
    }

    @Test
    public void shouldFailBecauseOfCycles() throws Exception {
        BuildResult result = buildAndFail("cycle.gradle");
        assertBuildResult(result, TaskOutcome.FAILED, "degraphMain");
    }

    @Test
    public void shouldFailBecauseOfCyclesWithDefaultConfiguration() throws Exception {
        BuildResult result = buildAndFail("default.gradle");
        assertBuildResult(result, TaskOutcome.FAILED, "degraphMain");
    }

    @Test
    public void shouldSucceedWithSlicings() throws Exception {
        BuildResult result = build("allowed.gradle");
        assertBuildResult(result, TaskOutcome.SUCCESS, "degraphMain");
    }

    @Test
    public void shouldFailBecauseOfDisallowedSlices() throws Exception {
        BuildResult result = buildAndFail("disallowed.gradle");
        assertBuildResult(result, TaskOutcome.FAILED, "degraphMain");
    }

    @Test
    public void shouldSucceedOnTestSources() throws Exception {
        BuildResult result = build("test-sources.gradle");
        assertBuildResult(result, TaskOutcome.SUCCESS, "degraphTest");

        assertThat(result.task(":degraphMain"), is(nullValue()));
    }

    @Test
    public void shouldSucceedWithAdditionalSourceSets() throws Exception {
        BuildResult result = build("source-sets.gradle");
        assertBuildResult(result, TaskOutcome.SUCCESS, "degraphMain");
        assertBuildResult(result, TaskOutcome.SUCCESS, "degraphTest");
        assertBuildResult(result, TaskOutcome.SUCCESS, "degraphShared");
    }

    private BuildResult build(final String buildFile) {
        return execute(buildFile, true);
    }

    private BuildResult buildAndFail(final String buildFile) {
        return execute(buildFile, false);
    }

    private BuildResult execute(final String buildFile, final boolean expectSuccess) {
        final GradleRunner gradleRunner = buildGradleRunner()
                .withArguments("-b", buildFile, "degraph", "--info", "--rerun-tasks")
                .withDebug(true);
        return expectSuccess ? gradleRunner.build() : gradleRunner.buildAndFail();
    }

    private GradleRunner buildGradleRunner() {
        return GradleRunner.create().withProjectDir(new File("demo")).withPluginClasspath(this.pluginClasspath);
    }

    private void assertBuildResult(final BuildResult buildResult, final TaskOutcome expectedOutcome, final String taskName) {
        System.out.println(buildResult.getOutput());

        final BuildTask degraphTask = buildResult.task(":" + taskName);
        assertThat(degraphTask, is(notNullValue()));
        assertThat(degraphTask.getOutcome(), is(expectedOutcome));
    }

    @Test
    public void shouldCachePreviousRun() throws Exception {
        // given
        final String buildFile = "allowed.gradle";

        // when first run
        final BuildResult successResult = buildGradleRunner()
                .withArguments("-b", buildFile, "degraph", "--info", "--rerun-tasks")
                .withDebug(true)
                .build();
        // then
        assertBuildResult(successResult, TaskOutcome.SUCCESS, "degraphMain");

        // when second run
        final BuildResult upToDateResult = buildGradleRunner()
                .withArguments("-b", buildFile, "degraph", "--info")
                .withDebug(true)
                .build();
        // then
        assertBuildResult(upToDateResult, TaskOutcome.UP_TO_DATE, "degraphMain");
    }
}
