package de.obqo.gradle.degraph;

import static de.schauderhaft.degraph.check.Check.customClasspath;
import static de.schauderhaft.degraph.check.Check.violationFree;

import java.io.File;
import java.util.stream.Stream;

import org.gradle.api.GradleException;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.workers.WorkAction;
import org.scalatest.matchers.MatchResult;

import de.schauderhaft.degraph.check.ConstraintBuilder;
import de.schauderhaft.degraph.check.JLayer;
import de.schauderhaft.degraph.configuration.NamedPattern;

/**
 * @author Oliver Becker
 */
public abstract class DegraphWorker implements WorkAction<DegraphWorkerParameters> {

    private static final Logger logger = Logging.getLogger(Task.class);

    @Override
    public void execute() {
        DegraphConfiguration configuration = getParameters().getConfiguration().get();
        String classpath = getParameters().getClasspath().get();
        File reportFile = getParameters().getReportFile().getAsFile().get();

        ConstraintBuilder constraint = customClasspath(classpath);

        for (String including : configuration.getIncludings()) {
            constraint = constraint.including(including);
        }

        for (String excluding : configuration.getExcludings()) {
            constraint = constraint.excluding(excluding);
        }

        for (SlicingConfiguration slicing : configuration.getSlicings()) {
            constraint = constraint.withSlicing(slicing.getSliceType(), getPatterns(slicing));
            for (AllowConfiguration allow : slicing.getAllows()) {
                constraint = allow.isDirect() ? constraint.allowDirect(getSlices(allow)) : constraint.allow(getSlices(allow));
            }
        }

        reportFile.getParentFile().mkdirs();
        constraint = constraint.printTo(reportFile.getPath());

        logger.info("degraph constraints: {}", constraint);

        final MatchResult result = violationFree().apply(constraint);

        logger.debug("degraph result: {}", result);

        if (!result.matches()) {
            throw new GradleException(String.format("%s\n\nSee the report at: %s", result.rawFailureMessage(), reportFile));
        }
    }

    // Helper methods for converting the plugin's configuration (or extension) instances into degraph objects.
    // Note: the configuration/extension classes of the plugin must not depend on degraph directly, since the
    // degraph classes exist only on the classpath of this worker, but not on the runtime classpath of the plugin.

    private Object[] getSlices(AllowConfiguration allow) {
        return Stream.of(allow.getSlices()).map(slice -> {
            // take care to map each LayerConfig to a Degraph Layer
            if (slice instanceof LayerConfig) {
                final LayerConfig layer = (LayerConfig) slice;
                return layer.isStrict() ? JLayer.oneOf(layer.getSlices()) : JLayer.anyOf(layer.getSlices());
            } else {
                return slice;
            }
        }).toArray();
    }

    private Object[] getPatterns(SlicingConfiguration slicing) {
        return slicing.getPatterns().stream().map(pattern -> {
            // take care to map each NamedPatternConfig to a Degraph NamedPattern
            if (pattern instanceof NamedPatternConfig) {
                final NamedPatternConfig namedPattern = (NamedPatternConfig) pattern;
                return new NamedPattern(namedPattern.getPattern(), namedPattern.getName());
            } else
                return pattern;
        }).toArray();
    }
}
