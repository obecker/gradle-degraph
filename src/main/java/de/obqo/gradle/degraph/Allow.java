package de.obqo.gradle.degraph;

/**
 * @author Oliver Becker
 */
class Allow {

    private final boolean direct;
    private final Object[] slices;

    Allow(final boolean direct, final Object[] slices) {
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
