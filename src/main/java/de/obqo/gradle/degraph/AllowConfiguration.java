package de.obqo.gradle.degraph;

import java.io.Serializable;

/**
 * Configuration model for {@code allow} constraints
 *
 * @author Oliver Becker
 */
class AllowConfiguration implements Serializable {

    private final boolean direct;
    private final Object[] slices;

    AllowConfiguration(final boolean direct, final Object[] slices) {
        this.direct = direct;
        this.slices = slices;
    }

    boolean isDirect() {
        return this.direct;
    }

    Object[] getSlices() {
        return this.slices;
    }
}
