package de.obqo.gradle.degraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gradle.api.GradleException;

import de.schauderhaft.degraph.check.JLayer;
import de.schauderhaft.degraph.configuration.NamedPattern;

/**
 * @author Oliver Becker
 */
public class SlicingExtension {

    // NamedDomainObjectContainer must have a name property - here the name represents the sliceType
    private final String name;

    private final List<Object> patterns;
    private final List<Allow> allows;

    public SlicingExtension(final String name) {
        this.name = name;
        this.patterns = new ArrayList<>();
        this.allows = new ArrayList<>();
    }

    // note: don't use getters here since they would be accessible from the gradle build file
    String sliceType() {
        return this.name;
    }

    List<Object> patterns() {
        return this.patterns;
    }

    public void patterns(Object... patterns) {
        for (Object pattern : patterns) {
            if (!(pattern instanceof String || pattern instanceof NamedPattern)) {
                throw new GradleException(
                        String.format("degraph: patterns must be strings or namedPattern(string), found '%s'", pattern));
            }
        }
        patterns().addAll(Arrays.asList(patterns));
    }

    List<Allow> allows() {
        return this.allows;
    }

    public void allow(final Object... slices) {
        if (slices == null) {
            throw new GradleException("degraph: Missing slices after allow");
        }
        allows().add(new Allow(false, slices));
    }

    public void allowDirect(final Object... slices) {
        if (slices == null) {
            throw new GradleException("degraph: Missing slices after allowDirect");
        }
        allows().add(new Allow(true, slices));
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
