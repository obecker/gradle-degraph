package de.obqo.gradle.degraph;

import java.io.Serializable;

/**
 * Serializable helper class representing a Degraph {@link de.schauderhaft.degraph.configuration.NamedPattern}
 *
 * @author Oliver Becker
 */
class NamedPatternConfig implements Serializable {

    private final String name;
    private final String pattern;

    NamedPatternConfig(final String name, final String pattern) {
        this.name = name;
        this.pattern = pattern;
    }

    String getName() {
        return this.name;
    }

    String getPattern() {
        return this.pattern;
    }
}
