package de.obqo.gradle.degraph;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

/**
 * @author Oliver Becker
 */
public class DegraphExtension {

    private final List<String> including;
    private final List<String> excluding;
    private File printTo;
    private File printOnFailure;
    private final NamedDomainObjectContainer<SlicingExtension> slicings;

    public DegraphExtension(Project project) {
        this.including = new ArrayList<>();
        this.excluding = new ArrayList<>();
        this.slicings = project.container(SlicingExtension.class);
    }

    // note: don't use getters here since they would be accessible from the gradle build file
    List<String> including() {
        return this.including;
    }

    public void including(final String... includings) {
        for (String including : includings) {
            including().add(including);
        }
    }

    List<String> excluding() {
        return this.excluding;
    }

    public void excluding(final String... excludings) {
        for (String excluding : excludings) {
            excluding().add(excluding);
        }
    }

    File printTo() {
        return this.printTo;
    }

    public void printTo(final File printTo) {
        this.printTo = printTo;
    }

    File printOnFailure() {
        return this.printOnFailure;
    }

    public void printOnFailure(final File printOnFailure) {
        this.printOnFailure = printOnFailure;
    }

    NamedDomainObjectContainer<SlicingExtension> slicings() {
        return this.slicings;
    }

    public void slicings(final Action<NamedDomainObjectContainer<SlicingExtension>> action) {
        action.execute(this.slicings);
    }
}
