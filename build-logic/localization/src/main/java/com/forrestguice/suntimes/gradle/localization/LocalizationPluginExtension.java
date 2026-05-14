package com.forrestguice.suntimes.gradle.localization;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

public abstract class LocalizationPluginExtension
{
    /**
     * @return e.g. en-rUS|en-rCA|...
     */
    @Input
    public abstract Property<String> getExcludeLocales();

    /**
     * @return e.g. strings|help_content
     */
    @Input
    public abstract Property<String> getBaseNames();

    /**
     * Base directory of resources to be scanned.
     *
     * @return e.g. app/src/main/res
     */
    @Input
    public abstract Property<String> getInputDir();

    /**
     * Output directory for generated bundles.
     *
     * @return e.g. ${buildDir}/generated/sources/staticResources
     */
    @Input
    public abstract Property<String> getOutputDir();

}
