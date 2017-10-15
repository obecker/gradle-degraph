package de.obqo.gradle.degraph;

import java.io.File;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;
import org.scalatest.matchers.MatchResult;

import de.schauderhaft.degraph.check.ConstraintBuilder;

import static de.schauderhaft.degraph.check.Check.customClasspath;
import static de.schauderhaft.degraph.check.Check.violationFree;

/**
 * @author Oliver Becker
 */
@CacheableTask
public class DegraphTask extends DefaultTask {

    private DegraphConfiguration configuration;

    private FileCollection classpath;

    private File reportFile;

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

    @SkipWhenEmpty
    @Classpath
    @InputFiles // for pre 3.2 gradle versions
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
        ConstraintBuilder constraint = customClasspath(this.classpath.getAsPath());

        for (String including : this.configuration.getIncludings()) {
            constraint = constraint.including(including);
        }

        for (String excluding : this.configuration.getExcludings()) {
            constraint = constraint.excluding(excluding);
        }

        for (SlicingConfiguration slicing : this.configuration.getSlicings()) {
            constraint = constraint.withSlicing(slicing.getSliceType(), slicing.getPatterns().toArray());
            for (AllowConfiguration allow : slicing.getAllows()) {
                constraint = allow.isDirect() ? constraint.allowDirect(allow.getSlices()) : constraint.allow(allow.getSlices());
            }
        }

        this.reportFile.getParentFile().mkdirs();
        constraint = constraint.printTo(this.reportFile.getPath());

        getLogger().info("degraph constraints: {}", constraint);

        final MatchResult result = violationFree().apply(constraint);

        getLogger().debug("degraph result: {}", result);

        if (!result.matches()) {
            throw new GradleException(
                    String.format("%s\n\nSee the report at: %s", result.rawFailureMessage(), this.reportFile));
        }
    }
}
