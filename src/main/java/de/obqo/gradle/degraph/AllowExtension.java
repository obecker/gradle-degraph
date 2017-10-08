package de.obqo.gradle.degraph;

import java.util.ArrayList;
import java.util.List;

import de.schauderhaft.degraph.check.JLayer;

/**
 * @author Oliver Becker
 */
public class AllowExtension {

    private boolean direct;
    private List<Object> slices;

    public boolean isDirect() {
        return this.direct;
    }

    public void setDirect(final boolean direct) {
        this.direct = direct;
    }

    public List<Object> getSlices() {
        if (this.slices == null) {
            this.slices = new ArrayList<>();
        }
        return this.slices;
    }

    public void setSlices(final List<Object> slices) {
        this.slices = slices;
    }

    public Object oneOf(final String... slices) {
        return JLayer.oneOf(slices);
    }

    public Object anyOf(final String... slices) {
        return JLayer.anyOf(slices);
    }
}
