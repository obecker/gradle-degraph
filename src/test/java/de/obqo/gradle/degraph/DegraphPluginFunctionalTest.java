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

    private void execute(final String buildFile, final TaskOutcome taskOutcome) {
        final GradleRunner gradleRunner = GradleRunner.create()
                .withProjectDir(new File("demo"))
                .withPluginClasspath(this.pluginClasspath)
                .withArguments("-b", buildFile, "degraph", "--info")
                .withDebug(true);
        final BuildResult buildResult = taskOutcome == TaskOutcome.SUCCESS ? gradleRunner.build() : gradleRunner.buildAndFail();

        System.out.println(buildResult.getOutput());

        final BuildTask degraphTask = buildResult.task(":degraph");
        assertThat(degraphTask, is(notNullValue()));
        assertThat(degraphTask.getOutcome(), is(taskOutcome));
    }
}
