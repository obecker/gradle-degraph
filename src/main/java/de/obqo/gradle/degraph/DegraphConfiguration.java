package de.obqo.gradle.degraph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.tasks.SourceSet;

/**
 * Configuration model for {@code degraph}
 *
 * @author Oliver Becker
 */
class DegraphConfiguration implements Serializable {

    private transient final List<SourceSet> sourceSets = new ArrayList<>();
    private final List<String> includings = new ArrayList<>();
    private final List<String> excludings = new ArrayList<>();
    private final List<SlicingConfiguration> slicings = new ArrayList<>();

    List<SourceSet> getSourceSets() {
        return this.sourceSets;
    }

    void addSourceSet(final SourceSet sourceSet) {
        this.sourceSets.add(sourceSet);
    }

    List<String> getIncludings() {
        return this.includings;
    }

    void addIncluding(final String including) {
        this.includings.add(including);
    }

    List<String> getExcludings() {
        return this.excludings;
    }

    void addExcluding(final String excluding) {
        this.excludings.add(excluding);
    }

    SlicingConfiguration addSlicing(final String sliceType) {
        final SlicingConfiguration slicing = new SlicingConfiguration(sliceType);
        this.slicings.add(slicing);
        return slicing;
    }

    List<SlicingConfiguration> getSlicings() {
        return this.slicings;
    }
}
