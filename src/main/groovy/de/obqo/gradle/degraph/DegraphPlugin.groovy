package de.obqo.gradle.degraph

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin

/**
 * @author Oliver Becker
 */
class DegraphPlugin implements Plugin<Project> {

    private static final String TASK_NAME = "degraph"

    @Override
    void apply(final Project project) {
        project.apply plugin: 'java'

        // create a runner task that runs all single degraph tasks
        final Task degraphRunnerTask = project.tasks.create(TASK_NAME)
        degraphRunnerTask.description = "Runs all degraph checks"
        degraphRunnerTask.group = JavaBasePlugin.VERIFICATION_GROUP

        final DegraphConfiguration configuration = new DegraphConfiguration()
        project.extensions.create(TASK_NAME, DegraphExtension, project, configuration)

        project.afterEvaluate {
            def sources = configuration.sourceSets
            if (sources.empty) {
                sources = project.sourceSets.asMap.values()
            }

            // create degraph work tasks, one for each source set
            sources.forEach { source ->
                def name = source.name

                DegraphTask degraphWorkTask = project.tasks.create(TASK_NAME + name.capitalize(), DegraphTask)
                degraphWorkTask.description = "Checks the ${name} sources for package cycles and other custom constraints"
                degraphWorkTask.group = JavaBasePlugin.VERIFICATION_GROUP
                degraphWorkTask.configuration = configuration
                degraphWorkTask.reportFile = new File(project.buildDir, "reports/degraph/${name}.graphml")
                degraphWorkTask.classpath = source.output

                // set task dependencies, e.g. degraph -> degraphTest -> testClasses
                degraphRunnerTask.dependsOn(degraphWorkTask.dependsOn(project.tasks[source.classesTaskName]))
            }

            // finally make task 'check' depend on the runner task
            project.tasks[JavaBasePlugin.CHECK_TASK_NAME].dependsOn degraphRunnerTask
        }
    }
}
