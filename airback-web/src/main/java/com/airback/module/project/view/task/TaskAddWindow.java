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
package com.airback.module.project.view.task;

import com.airback.module.project.domain.SimpleTask;
import com.airback.module.project.i18n.TaskI18nEnum;
import com.airback.vaadin.UserUIContext;
import com.airback.vaadin.web.ui.WebThemes;
import org.vaadin.viritin.layouts.MVerticalLayout;
import org.vaadin.viritin.layouts.MWindow;

/**
 * @author airback Ltd
 * @since 5.1.1
 */
public class TaskAddWindow extends MWindow {
    public TaskAddWindow(SimpleTask task) {
        setCaption(task.getId() == null ? UserUIContext.getMessage(TaskI18nEnum.NEW) : UserUIContext.getMessage(TaskI18nEnum.DETAIL));

        TaskEditForm editForm = new TaskEditForm() {
            @Override
            protected void postExecution() {
                close();
            }
        };
        editForm.setBean(task);
        this.withWidth(WebThemes.WINDOW_FORM_WIDTH).withModal(true).withResizable(false).withContent(new MVerticalLayout(editForm)).withCenter();
    }
}
