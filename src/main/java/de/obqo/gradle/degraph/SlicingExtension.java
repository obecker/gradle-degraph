package de.obqo.gradle.degraph;

import java.util.ArrayList;
import java.util.List;

import de.schauderhaft.degraph.configuration.NamedPattern;
import groovy.lang.Closure;

/**
 * @author Oliver Becker
 */
public class SlicingExtension {

    private final String name;
    private List<Object> patterns;
    private final AllowExtension allow;

    public SlicingExtension(final String name) {
        this.name = name;
        this.allow = new AllowExtension();
    }

    public String getName() {
        return this.name;
    }

    public List<Object> getPatterns() {
        if (this.patterns == null) {
            this.patterns = new ArrayList<>();
        }
        return this.patterns;
    }

    public void setPatterns(final List<Object> patterns) {
        this.patterns = patterns;
    }

    public void allow(final Closure<AllowExtension> closure) {
        closure.setDelegate(this.allow);
        closure.call();
    }

    public AllowExtension getAllow() {
        return this.allow;
    }

    public Object namedPattern(final String name, final String pattern) {
        // Note: in the helper function the name is the first argument, while in the NamedPattern constructor it is the second
        return new NamedPattern(pattern, name);
    }
}
