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

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public abstract class LocalizationPlugin implements Plugin<Project>
{
    @Override
    public void apply(Project project)
    {
        LocalizationPluginExtension extension = project.getExtensions().create("localization_plugin", LocalizationPluginExtension.class);

        project.getTasks().register("processTranslations", ProcessTranslations.class, task ->
        {
            task.setGroup("Translation");
            task.getBaseNames().set(extension.getBaseNames());
            task.getInputDir().set(extension.getInputDir());
            task.getOutputDir().set(extension.getOutputDir());
        });

        project.getTasks().register("updateTranslations", UpdateTranslations.class, task ->
        {
            task.setGroup("Translation");
            task.getBaseNames().set(extension.getBaseNames());
            task.getExcludeLocales().set(extension.getExcludeLocales());
            task.getInputDir().set(extension.getInputDir());
            task.getOutputDir().set(extension.getOutputDir());
        });
    }

}
