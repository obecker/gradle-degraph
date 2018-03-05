package de.obqo.gradle.degraph;

import java.io.File;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.IsolationMode;
import org.gradle.workers.WorkerExecutor;

/**
 * @author Oliver Becker
 */
@CacheableTask
public class DegraphTask extends DefaultTask {

    private final WorkerExecutor workerExecutor;

    private DegraphConfiguration configuration;

    private FileCollection classpath;

    private FileCollection workerClasspath;

    private File reportFile;

    @Inject
    public DegraphTask(final WorkerExecutor workerExecutor) {
        this.workerExecutor = workerExecutor;
    }

    void setConfiguration(final DegraphConfiguration configuration) {
        this.configuration = configuration;
    }

    @Input
    public DegraphConfiguration getConfiguration() {
        return this.configuration;
    }

    void setClasspath(final FileCollection classpath) {
        this.classpath = classpath;
    }

    void setWorkerClasspath(final FileCollection workerClasspath) {
        this.workerClasspath = workerClasspath;
    }

    @SkipWhenEmpty
    @Classpath
    public FileCollection getClasspath() {
        return this.classpath;
    }

    void setReportFile(final File reportFile) {
        this.reportFile = reportFile;
    }

    @OutputFile
    public File getReportFile() {
        return this.reportFile;
    }

    @TaskAction
    public void runConstraintCheck() {
        final DegraphConfiguration degraphConfiguration = this.configuration;
        final String classpath = this.classpath.getAsPath();
        final File reportFile = this.reportFile;
        final FileCollection workerClasspath = this.workerClasspath;

        this.workerExecutor.submit(DegraphWorker.class, workerConfiguration -> {
            workerConfiguration.setIsolationMode(IsolationMode.CLASSLOADER);
            workerConfiguration.classpath(workerClasspath);
            workerConfiguration.params(degraphConfiguration, classpath, reportFile);
        });
    }
}
