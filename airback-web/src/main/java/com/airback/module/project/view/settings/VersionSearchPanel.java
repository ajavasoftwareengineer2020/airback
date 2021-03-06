/**
 * Copyright © airback
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.airback.module.project.view.settings;

import com.airback.common.i18n.GenericI18Enum;
import com.airback.db.arguments.NumberSearchField;
import com.airback.db.arguments.StringSearchField;
import com.airback.module.project.CurrentProjectVariables;
import com.airback.module.project.ProjectRolePermissionCollections;
import com.airback.module.project.ProjectTypeConstants;
import com.airback.module.project.event.BugVersionEvent;
import com.airback.module.project.i18n.VersionI18nEnum;
import com.airback.module.project.ui.components.ComponentUtils;
import com.airback.module.project.domain.criteria.VersionSearchCriteria;
import com.airback.vaadin.EventBusFactory;
import com.airback.vaadin.UserUIContext;
import com.airback.vaadin.ui.HeaderWithIcon;
import com.airback.vaadin.web.ui.*;
import com.vaadin.event.ShortcutAction;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.*;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.layouts.MHorizontalLayout;

/**
 * @author airback Ltd.
 * @since 1.0
 */
public class VersionSearchPanel extends DefaultGenericSearchPanel<VersionSearchCriteria> {
    private static final long serialVersionUID = 1L;
    protected VersionSearchCriteria searchCriteria;

    protected SearchLayout<VersionSearchCriteria> createBasicSearchLayout() {
        return new VersionBasicSearchLayout();
    }

    @Override
    protected SearchLayout<VersionSearchCriteria> createAdvancedSearchLayout() {
        return null;
    }

    @Override
    protected HeaderWithIcon buildSearchTitle() {
        return ComponentUtils.headerH2(ProjectTypeConstants.VERSION, UserUIContext.getMessage(VersionI18nEnum.LIST));
    }

    @Override
    protected Component buildExtraControls() {
        return new MButton(UserUIContext.getMessage(VersionI18nEnum.NEW),
                clickEvent -> EventBusFactory.getInstance().post(new BugVersionEvent.GotoAdd(this, null)))
                .withIcon(VaadinIcons.PLUS).withStyleName(WebThemes.BUTTON_ACTION)
                .withVisible(CurrentProjectVariables.canWrite(ProjectRolePermissionCollections.VERSIONS));
    }

    private class VersionBasicSearchLayout extends BasicSearchLayout<VersionSearchCriteria> {
        private static final long serialVersionUID = 1L;
        private TextField nameField;

        VersionBasicSearchLayout() {
            super(VersionSearchPanel.this);
        }

        @Override
        public ComponentContainer constructBody() {
            MHorizontalLayout basicSearchBody = new MHorizontalLayout().withMargin(true);
            basicSearchBody.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);

            Label nameLbl = new Label("Name:");
            basicSearchBody.with(nameLbl);

            nameField = new MTextField().withPlaceholder(UserUIContext.getMessage(GenericI18Enum.ACTION_QUERY_BY_TEXT))
                    .withWidth(WebUIConstants.DEFAULT_CONTROL_WIDTH);
            basicSearchBody.with(nameField);

            MButton searchBtn = new MButton(UserUIContext.getMessage(GenericI18Enum.BUTTON_SEARCH), clickEvent -> callSearchAction())
                    .withIcon(VaadinIcons.SEARCH).withStyleName(WebThemes.BUTTON_ACTION)
                    .withClickShortcut(ShortcutAction.KeyCode.ENTER);
            basicSearchBody.with(searchBtn);

            MButton cancelBtn = new MButton(UserUIContext.getMessage(GenericI18Enum.BUTTON_CLEAR), clickEvent -> nameField.setValue(""))
                    .withStyleName(WebThemes.BUTTON_OPTION);
            basicSearchBody.with(cancelBtn);

            return basicSearchBody;
        }

        @Override
        protected VersionSearchCriteria fillUpSearchCriteria() {
            searchCriteria = new VersionSearchCriteria();
            searchCriteria.setProjectId(new NumberSearchField(CurrentProjectVariables.getProjectId()));
            searchCriteria.setVersionname(StringSearchField.and(nameField.getValue().trim()));
            return searchCriteria;
        }
    }
}