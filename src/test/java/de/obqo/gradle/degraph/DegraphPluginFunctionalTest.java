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
        execute("success.gradle", TaskOutcome.SUCCESS);
    }

    @Test
    public void shouldFailBecauseOfCycles() throws Exception {
        execute("cycle.gradle", TaskOutcome.FAILED);
    }

    @Test
    public void shouldFailBecauseOfCyclesWithDefaultConfiguration() throws Exception {
        execute("default.gradle", TaskOutcome.FAILED);
    }

    @Test
    public void shouldSucceedWithSlicings() throws Exception {
        execute("allowed.gradle", TaskOutcome.SUCCESS);
    }

    @Test
    public void shouldFailBecauseOfDisallowedSlices() throws Exception {
        execute("disallowed.gradle", TaskOutcome.FAILED);
    }

    @Test
    public void shouldSucceedOnTestSources() throws Exception {
        execute("testsources.gradle", TaskOutcome.SUCCESS);
    }

    private void execute(final String buildFile, final TaskOutcome expectedOutcome) {
        final GradleRunner gradleRunner = buildGradleRunner()
                .withArguments("-b", buildFile, "degraph", "--info", "--rerun-tasks")
                .withDebug(true);
        final BuildResult buildResult = expectedOutcome == TaskOutcome.SUCCESS
                ? gradleRunner.build()
                : gradleRunner.buildAndFail();

        assertBuildResult(buildResult, expectedOutcome);
    }

    private GradleRunner buildGradleRunner() {
        return GradleRunner.create()
                .withProjectDir(new File("demo"))
                .withPluginClasspath(this.pluginClasspath);
    }

    private void assertBuildResult(final BuildResult buildResult, final TaskOutcome expectedOutcome) {
        System.out.println(buildResult.getOutput());

        final BuildTask degraphTask = buildResult.task(":degraph");
        assertThat(degraphTask, is(notNullValue()));
        assertThat(degraphTask.getOutcome(), is(expectedOutcome));
    }

    @Test
    public void shouldCachePreviousRun() throws Exception {
        // first run
        final BuildResult successResult = buildGradleRunner()
                .withArguments("-b", "success.gradle", "degraph", "--info", "--rerun-tasks")
                .withDebug(true)
                .build();
        assertBuildResult(successResult, TaskOutcome.SUCCESS);

        // second run
        final BuildResult upToDateResult = buildGradleRunner()
                .withArguments("-b", "success.gradle", "degraph", "--info")
                .withDebug(true)
                .build();
        assertBuildResult(upToDateResult, TaskOutcome.UP_TO_DATE);
    }
}
