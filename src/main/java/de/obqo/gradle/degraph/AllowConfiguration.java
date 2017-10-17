package de.obqo.gradle.degraph;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Configuration model for {@code allow} constraints
 *
 * @author Oliver Becker
 */
class AllowConfiguration implements Serializable {

    private final boolean direct;
    private transient final Object[] slices;

    // slices are not necessary serializable (especially Degraph's Layer), so create a substitute to support incremental builds.
    // Using strings is safe because the concrete layers are case classes with a proper toString() method
    private final List<String> serializableSlices;

    AllowConfiguration(final boolean direct, final Object[] slices) {
        this.direct = direct;
        this.slices = slices;
        this.serializableSlices = Arrays.stream(slices).map(Object::toString).collect(Collectors.toList());
    }

    boolean isDirect() {
        return this.direct;
    }

    Object[] getSlices() {
        return this.slices;
    }
}
