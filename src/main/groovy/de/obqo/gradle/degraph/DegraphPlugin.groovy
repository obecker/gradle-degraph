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

        final DegraphTask degraphTask = project.tasks.create(TASK_NAME, DegraphTask)
        degraphTask.description = "Checks the java sources for package cycles and other custom constraints"
        degraphTask.group = JavaBasePlugin.VERIFICATION_GROUP

        final DegraphConfiguration configuration = new DegraphConfiguration()
        project.extensions.create(TASK_NAME, DegraphExtension, project, configuration)
        degraphTask.configuration = configuration

        project.afterEvaluate {
            degraphTask.reportFile = new File(project.buildDir, "reports/degraph/result.graphml")

            // configure classpath for degraph (default: output of all source sets)
            def sources = configuration.sourceSets
            if (sources.empty) {
                sources = project.sourceSets.asMap.values()
            }

            degraphTask.classpath = sources
                    .collect { it.output }
                    .inject { classpath, output -> classpath + output }

            // add a dependency to each sourceSet's 'classes' task
            sources.forEach {
                degraphTask.dependsOn project.tasks[it.classesTaskName]
            }

            // finally make task 'check' depend on degraph
            project.tasks[JavaBasePlugin.CHECK_TASK_NAME].dependsOn degraphTask
        }
    }
}
