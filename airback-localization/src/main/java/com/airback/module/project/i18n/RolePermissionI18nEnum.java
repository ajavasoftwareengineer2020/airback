/**
 * Copyright © airback
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.airback.module.project.i18n;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;

@BaseName("project-role-permission")
@LocaleData(value = {@Locale("en-US")}, defaultCharset = "UTF-8")
public enum RolePermissionI18nEnum {
    LIST,

    Message,
    Milestone,
    Task,
    Bug,
    Version,
    Component,
    File,
    Page,
    Risk,
    Time,
    Finance,
    Invoice,
    User,
    Role,
    Project,
    Approve_Timesheet
}
