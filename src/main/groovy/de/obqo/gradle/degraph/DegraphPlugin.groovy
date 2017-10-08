package de.obqo.gradle.degraph

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin

/**
 * @author Oliver Becker
 */
class DegraphPlugin implements Plugin<Project> {

    private static final String TASK_NAME = "degraph"

    @Override
    void apply(final Project project) {
        project.apply plugin: 'java'

        final DegraphTask degraphTask = project.tasks.create(TASK_NAME, DegraphTask);
        degraphTask.description = "Checks the java sources for package cycles and other custom constraints"
        degraphTask.group = JavaBasePlugin.VERIFICATION_GROUP

        degraphTask.extension = project.extensions.create(TASK_NAME, DegraphExtension, project)

        project.afterEvaluate {
            // make task check depend on degraph
            project.tasks
                    .matching { task -> task.name.equals(JavaBasePlugin.CHECK_TASK_NAME) }
                    .all { task -> task.dependsOn degraphTask }
            // make task degraph depend on compileJava (because it uses the classpath, see below)
            project.tasks
                    .matching { task -> task.name.equals("compileJava") }
                    .all { task -> degraphTask.dependsOn task }

            degraphTask.classpath = project.sourceSets.main.output
        }
    }
}
