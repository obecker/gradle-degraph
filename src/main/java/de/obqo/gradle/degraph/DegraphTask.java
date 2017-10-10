package de.obqo.gradle.degraph;

import java.io.File;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;
import org.scalatest.matchers.MatchResult;

import de.schauderhaft.degraph.check.ConstraintBuilder;

import static de.schauderhaft.degraph.check.Check.customClasspath;
import static de.schauderhaft.degraph.check.Check.violationFree;

/**
 * @author Oliver Becker
 */
public class DegraphTask extends DefaultTask {

    private DegraphConfiguration configuration;

    @InputFiles
    private FileCollection classpath;

    void setConfiguration(final DegraphConfiguration configuration) {
        this.configuration = configuration;
    }

    void setClasspath(final FileCollection classpath) {
        this.classpath = classpath;
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

        final File printTo = this.configuration.getPrintTo();
        if (printTo != null) {
            final File parent = printTo.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            constraint = constraint.printTo(printTo.getPath());
        }

        final File printOnFailure = this.configuration.getPrintOnFailure();
        if (printOnFailure != null) {
            final File parent = printOnFailure.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            constraint = constraint.printOnFailure(printOnFailure.getPath());
        }

        for (SlicingConfiguration slicing : this.configuration.getSlicings()) {
            constraint = constraint.withSlicing(slicing.getSliceType(), slicing.getPatterns().toArray());
            for (AllowConfiguration allow : slicing.getAllows()) {
                constraint = allow.isDirect() ? constraint.allowDirect(allow.getSlices()) : constraint.allow(allow.getSlices());
            }
        }

        getLogger().info("degraph constraints: {}", constraint);

        final MatchResult result = violationFree().apply(constraint);

        getLogger().info("degraph result: {}", result);

        if (!result.matches()) {
            throw new GradleException(result.rawFailureMessage());
        }
    }
}
