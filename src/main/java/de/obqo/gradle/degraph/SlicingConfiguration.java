package de.obqo.gradle.degraph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration model for {@code slicings}
 *
 * @author Oliver Becker
 */
class SlicingConfiguration implements Serializable {

    private final String sliceType;
    private transient final List<Object> patterns;
    private final List<AllowConfiguration> allows;

    // patterns are not necessary serializable (especially Degraph's NamedPattern), so create a substitute to support incremental builds.
    // Using strings is safe because NamedPattern is a case class with a proper toString() method
    private final List<String> serializablePatterns;

    SlicingConfiguration(final String sliceType) {
        this.sliceType = sliceType;
        this.patterns = new ArrayList<>();
        this.allows = new ArrayList<>();
        this.serializablePatterns = new ArrayList<>();
    }

    String getSliceType() {
        return this.sliceType;
    }

    List<Object> getPatterns() {
        return this.patterns;
    }

    void addPattern(final Object pattern) {
        this.patterns.add(pattern);
        this.serializablePatterns.add(pattern.toString());
    }

    List<AllowConfiguration> getAllows() {
        return this.allows;
    }

    void addAllow(final boolean direct, final Object[] slices) {
        this.allows.add(new AllowConfiguration(direct, slices));
    }
}
