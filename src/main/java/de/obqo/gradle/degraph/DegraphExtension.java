package de.obqo.gradle.degraph;

import java.io.File;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

/**
 * @author Oliver Becker
 */
public class DegraphExtension {

    private boolean noJars;
    private String including;
    private String excluding;
    private File printTo;
    private File printOnFailure;
    private final NamedDomainObjectContainer<SlicingExtension> slicings;

    public DegraphExtension(Project project) {
        this.slicings = project.container(SlicingExtension.class);
    }

    public boolean isNoJars() {
        return this.noJars;
    }

    public void setNoJars(final boolean noJars) {
        this.noJars = noJars;
    }

    public String getIncluding() {
        return this.including;
    }

    public void setIncluding(final String including) {
        this.including = including;
    }

    public String getExcluding() {
        return this.excluding;
    }

    public void setExcluding(final String excluding) {
        this.excluding = excluding;
    }

    public File getPrintTo() {
        return this.printTo;
    }

    public void setPrintTo(final File printTo) {
        this.printTo = printTo;
    }

    public File getPrintOnFailure() {
        return this.printOnFailure;
    }

    public void setPrintOnFailure(final File printOnFailure) {
        this.printOnFailure = printOnFailure;
    }

    public NamedDomainObjectContainer<SlicingExtension> getSlicings() {
        return this.slicings;
    }

    public void slicings(final Action<NamedDomainObjectContainer<SlicingExtension>> action) {
        action.execute(this.slicings);
    }
}
