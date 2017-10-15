package de.obqo.gradle.degraph;

import org.gradle.api.GradleException;

import de.schauderhaft.degraph.check.JLayer;
import de.schauderhaft.degraph.check.Layer;
import de.schauderhaft.degraph.configuration.NamedPattern;

/**
 * Extension class for the configuration of slicings
 *
 * @author Oliver Becker
 */
public class SlicingExtension {

    // a NamedDomainObjectContainer can only contain objects having a name property - the name will be used as sliceType
    private final String name;
    private final SlicingConfiguration configuration;

    SlicingExtension(final SlicingConfiguration configuration) {
        this.name = configuration.getSliceType();
        this.configuration = configuration;
    }

    public void patterns(Object... patterns) {
        for (Object pattern : patterns) {
            if (!(pattern instanceof String || pattern instanceof NamedPattern)) {
                throw new GradleException(String.format(
                        "degraph: patterns must be strings or namedPattern(string), found '%s'",
                        pattern));
            }
            this.configuration.addPattern(pattern);
        }
    }

    public void allow(final Object... slices) {
        checkSlices("allow", slices);
        this.configuration.addAllow(false, slices);
    }

    public void allowDirect(final Object... slices) {
        checkSlices("allowDirect", slices);
        this.configuration.addAllow(true, slices);
    }

    private void checkSlices(final String prop, final Object[] slices) {
        for (Object slice : slices) {
            if (!(slice instanceof String || slice instanceof Layer)) {
                throw new GradleException(String.format(
                        "degraph: slices after %s must be strings, oneOf(strings), or anyOf(strings), found '%s'",
                        prop,
                        slice));
            }
        }
    }

    public Object namedPattern(final String name, final String pattern) {
        if (name.contains("*") || name.contains(".")) {
            throw new GradleException(String.format("degraph: illegal pattern name '%s' - must contain neither * nor .", name));
        }
        // Note: in the helper function the name is the first argument, while in the NamedPattern constructor it is the second
        return new NamedPattern(pattern, name);
    }

    public Object oneOf(final String... slices) {
        return JLayer.oneOf(slices);
    }

    public Object anyOf(final String... slices) {
        return JLayer.anyOf(slices);
    }
}
