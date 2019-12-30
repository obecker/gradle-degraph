package de.obqo.gradle.degraph;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkParameters;

/**
 * @author Oliver Becker
 */
public interface DegraphWorkerParameters extends WorkParameters {

    Property<DegraphConfiguration> getConfiguration();

    Property<String> getClasspath();

    RegularFileProperty getReportFile();

}
