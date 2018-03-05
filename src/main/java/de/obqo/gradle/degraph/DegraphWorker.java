package de.obqo.gradle.degraph;

import java.io.File;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.gradle.api.GradleException;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.scalatest.matchers.MatchResult;

import de.schauderhaft.degraph.check.ConstraintBuilder;
import de.schauderhaft.degraph.check.JLayer;
import de.schauderhaft.degraph.configuration.NamedPattern;

import static de.schauderhaft.degraph.check.Check.customClasspath;
import static de.schauderhaft.degraph.check.Check.violationFree;

/**
 * @author Oliver Becker
 */
public class DegraphWorker implements Runnable {

    private final DegraphConfiguration configuration;

    private final String classpath;

    private final File reportFile;

    private final Logger logger = Logging.getLogger(Task.class);

    @Inject
    public DegraphWorker(final DegraphConfiguration configuration, final String classpath, final File reportFile) {
        this.configuration = configuration;
        this.classpath = classpath;
        this.reportFile = reportFile;
    }

    @Override
    public void run() {
        ConstraintBuilder constraint = customClasspath(this.classpath);

        for (String including : this.configuration.getIncludings()) {
            constraint = constraint.including(including);
        }

        for (String excluding : this.configuration.getExcludings()) {
            constraint = constraint.excluding(excluding);
        }

        for (SlicingConfiguration slicing : this.configuration.getSlicings()) {
            constraint = constraint.withSlicing(slicing.getSliceType(), getPatterns(slicing));
            for (AllowConfiguration allow : slicing.getAllows()) {
                constraint = allow.isDirect() ? constraint.allowDirect(getSlices(allow)) : constraint.allow(getSlices(allow));
            }
        }

        this.reportFile.getParentFile().mkdirs();
        constraint = constraint.printTo(this.reportFile.getPath());

        this.logger.info("degraph constraints: {}", constraint);

        final MatchResult result = violationFree().apply(constraint);

        this.logger.debug("degraph result: {}", result);

        if (!result.matches()) {
            throw new GradleException(String.format("%s\n\nSee the report at: %s", result.rawFailureMessage(), this.reportFile));
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
