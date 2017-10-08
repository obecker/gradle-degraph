package de.obqo.gradle.degraph;

import java.io.File;
import java.util.List;

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

    private DegraphExtension extension;

    @InputFiles
    private FileCollection classpath;

    void setExtension(final DegraphExtension extension) {
        this.extension = extension;
    }

    void setClasspath(final FileCollection classpath) {
        this.classpath = classpath;
    }

    @TaskAction
    public void runConstraintCheck() {
        ConstraintBuilder constraint = customClasspath(this.classpath.getAsPath());

        if (this.extension.isNoJars()) {
            constraint = constraint.noJars();
        }

        if (this.extension.getIncluding() != null) {
            constraint = constraint.including(this.extension.getIncluding());
        }

        if (this.extension.getExcluding() != null) {
            constraint = constraint.excluding(this.extension.getExcluding());
        }

        final File printTo = this.extension.getPrintTo();
        if (printTo != null) {
            final File parent = printTo.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            constraint = constraint.printTo(printTo.getPath());
        }

        final File printOnFailure = this.extension.getPrintOnFailure();
        if (printOnFailure != null) {
            final File parent = printOnFailure.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            constraint = constraint.printOnFailure(printOnFailure.getPath());
        }

        for (SlicingExtension slicing : this.extension.getSlicings()) {
            constraint = constraint.withSlicing(slicing.getName(), slicing.getPatterns().toArray());
            final AllowExtension allow = slicing.getAllow();
            final List<Object> slices = allow.getSlices();
            if (!slices.isEmpty()) {
                final Object[] slicesArray = slices.toArray();
                constraint = allow.isDirect() ? constraint.allowDirect(slicesArray) : constraint.allow(slicesArray);
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
