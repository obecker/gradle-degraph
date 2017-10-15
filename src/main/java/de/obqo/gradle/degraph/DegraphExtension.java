package de.obqo.gradle.degraph;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;

/**
 * Extension class for the configuration of degraph
 *
 * @author Oliver Becker
 */
public class DegraphExtension {

    private final NamedDomainObjectContainer<SlicingExtension> slicings;
    private final DegraphConfiguration configuration;

    public DegraphExtension(final Project project, final DegraphConfiguration configuration) {
        this.slicings = project.container(
                SlicingExtension.class,
                sliceType -> new SlicingExtension(configuration.addSlicing(sliceType)));
        this.configuration = configuration;
    }

    public void sourceSets(final SourceSet... sourceSets) {
        for (SourceSet sourceSet : sourceSets) {
            this.configuration.addSourceSet(sourceSet);
        }
    }

    public void including(final String... includings) {
        for (String including : includings) {
            this.configuration.addIncluding(including);
        }
    }

    public void excluding(final String... excludings) {
        for (String excluding : excludings) {
            this.configuration.addExcluding(excluding);
        }
    }

    public void slicings(final Action<NamedDomainObjectContainer<SlicingExtension>> action) {
        action.execute(this.slicings);
    }
}
