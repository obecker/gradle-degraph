package de.obqo.gradle.degraph;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * Configuration model for {@code degraph}
 *
 * @author Oliver Becker
 */
class DegraphConfiguration {

    private final List<String> includings = new ArrayList<>();
    private final List<String> excludings = new ArrayList<>();
    private File printTo;
    private File printOnFailure;

    private Supplier<Collection<SlicingConfiguration>> slicingsSupplier;

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

    File getPrintTo() {
        return this.printTo;
    }

    void setPrintTo(final File printTo) {
        this.printTo = printTo;
    }

    File getPrintOnFailure() {
        return this.printOnFailure;
    }

    void setPrintOnFailure(final File printOnFailure) {
        this.printOnFailure = printOnFailure;
    }

    void setSlicingsSupplier(final Supplier<Collection<SlicingConfiguration>> slicingsSupplier) {
        this.slicingsSupplier = slicingsSupplier;
    }

    Collection<SlicingConfiguration> getSlicings() {
        return this.slicingsSupplier.get();
    }
}
