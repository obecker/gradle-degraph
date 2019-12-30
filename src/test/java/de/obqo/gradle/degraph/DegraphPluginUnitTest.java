package de.obqo.gradle.degraph;

import static org.assertj.core.api.Assertions.assertThat;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

/**
 * @author Oliver Becker
 */
public class DegraphPluginUnitTest {

    @Test
    void pluginShouldAddTaskAndExtension() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("de.obqo.gradle.degraph");

        assertThat(project.getTasks().getByName("degraph")).isInstanceOf(Task.class);
        assertThat(project.getExtensions().getByName("degraph")).isInstanceOf(DegraphExtension.class);
    }
}
