/*
    Copyright (C) 2026 Forrest Guice
    This file is part of SuntimesWidget.

    SuntimesWidget is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SuntimesWidget is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
     along with SuntimesWidget.  If not, see <http://www.gnu.org/licenses/>.
*/

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
