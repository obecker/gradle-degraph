package de.obqo.gradle.degraph;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Oliver Becker
 */
public class DegraphPluginUnitTest {

    @Test
    public void pluginShouldAddTaskAndExtension() throws Exception {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("de.obqo.gradle.degraph");

        assertThat(project.getTasks().getByName("degraph"), is(instanceOf(DegraphTask.class)));
        assertThat(project.getExtensions().getByName("degraph"), is(instanceOf(DegraphExtension.class)));
    }
}
