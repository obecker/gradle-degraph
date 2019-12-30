package de.obqo.gradle.degraph;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Oliver Becker
 */
public class DegraphPluginFunctionalTest {

    private List<File> pluginClasspath;

    @BeforeEach
    void setUp() throws Exception {
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
    void shouldSucceed() {
        BuildResult result = build("success.gradle");
        assertBuildResult(result, TaskOutcome.SUCCESS, "degraphMain");
        assertBuildResult(result, TaskOutcome.SUCCESS, "degraphTest");
    }

    @Test
    void shouldFailBecauseOfCycles() {
        BuildResult result = buildAndFail("cycle.gradle");
        assertBuildResult(result, TaskOutcome.FAILED, "degraphMain");
    }

    @Test
    void shouldFailBecauseOfCyclesWithDefaultConfiguration() {
        BuildResult result = buildAndFail("default.gradle");
        assertBuildResult(result, TaskOutcome.FAILED, "degraphMain");
    }

    @Test
    void shouldSucceedWithSlicings() {
        BuildResult result = build("allowed.gradle");
        assertBuildResult(result, TaskOutcome.SUCCESS, "degraphMain");
    }

    @Test
    void shouldFailBecauseOfDisallowedSlices() {
        BuildResult result = buildAndFail("disallowed.gradle");
        assertBuildResult(result, TaskOutcome.FAILED, "degraphMain");
    }

    @Test
    void shouldSucceedOnTestSources() {
        BuildResult result = build("test-sources.gradle");
        assertBuildResult(result, TaskOutcome.SUCCESS, "degraphTest");

        assertThat(result.task(":degraphMain")).isNull();
    }

    @Test
    void shouldSucceedWithAdditionalSourceSets() {
        BuildResult result = build("source-sets.gradle");
        assertBuildResult(result, TaskOutcome.SUCCESS, "degraphMain");
        assertBuildResult(result, TaskOutcome.SUCCESS, "degraphTest");
        assertBuildResult(result, TaskOutcome.SUCCESS, "degraphShared");
    }

    @Test
    void shouldFailBecauseOfCyclesWithDifferentToolVersion() {
        BuildResult result = buildAndFail("toolversion.gradle");
        assertBuildResult(result, TaskOutcome.FAILED, "degraphMain");
    }

    private BuildResult build(final String buildFile) {
        return execute(buildFile, true);
    }

    private BuildResult buildAndFail(final String buildFile) {
        return execute(buildFile, false);
    }

    private BuildResult execute(final String buildFile, final boolean expectSuccess) {
        final GradleRunner gradleRunner = buildGradleRunner()
                .withArguments("-b", buildFile, "degraph", "--info", "--stacktrace", "--rerun-tasks")
                .withDebug(true);
        return expectSuccess ? gradleRunner.build() : gradleRunner.buildAndFail();
    }

    private GradleRunner buildGradleRunner() {
        return GradleRunner.create().withProjectDir(new File("demo")).withPluginClasspath(this.pluginClasspath);
    }

    private void assertBuildResult(final BuildResult buildResult, final TaskOutcome expectedOutcome, final String taskName) {
        System.out.println(buildResult.getOutput());

        final BuildTask degraphTask = buildResult.task(":" + taskName);
        assertThat(degraphTask).isNotNull();
        assertThat(degraphTask.getOutcome()).isEqualTo(expectedOutcome);
    }

    @Test
    void shouldCachePreviousRun() {
        // given
        final String buildFile = "allowed.gradle";

        // when first run
        final BuildResult successResult = buildGradleRunner()
                .withArguments("-b", buildFile, "degraph", "--info", "--stacktrace", "--rerun-tasks")
                .withDebug(true)
                .build();
        // then
        assertBuildResult(successResult, TaskOutcome.SUCCESS, "degraphMain");

        // when second run
        final BuildResult upToDateResult = buildGradleRunner()
                .withArguments("-b", buildFile, "degraph", "--info", "--stacktrace")
                .withDebug(true)
                .build();
        // then
        assertBuildResult(upToDateResult, TaskOutcome.UP_TO_DATE, "degraphMain");
    }
}
