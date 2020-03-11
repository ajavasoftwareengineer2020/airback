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
package com.airback.module.user.accountsettings.team.view;

import com.airback.common.i18n.GenericI18Enum;
import com.airback.db.arguments.StringSearchField;
import com.airback.vaadin.EventBusFactory;
import com.airback.module.user.accountsettings.localization.RoleI18nEnum;
import com.airback.module.user.domain.criteria.RoleSearchCriteria;
import com.airback.module.user.event.RoleEvent;
import com.airback.security.RolePermissionCollections;
import com.airback.vaadin.UserUIContext;
import com.airback.vaadin.ui.HeaderWithIcon;
import com.airback.vaadin.web.ui.*;
import com.vaadin.event.ShortcutAction;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.*;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.layouts.MHorizontalLayout;

/**
 * @author airback Ltd.
 * @since 1.0
 */
public class RoleSearchPanel extends DefaultGenericSearchPanel<RoleSearchCriteria> {
    private static final long serialVersionUID = 1L;

    @Override
    protected SearchLayout<RoleSearchCriteria> createBasicSearchLayout() {
        return new RoleBasicSearchLayout();
    }

    @Override
    protected SearchLayout<RoleSearchCriteria> createAdvancedSearchLayout() {
        return null;
    }

    @Override
    protected HeaderWithIcon buildSearchTitle() {
        return HeaderWithIcon.h2(VaadinIcons.USERS, UserUIContext.getMessage(RoleI18nEnum.LIST));
    }

    @Override
    protected Component buildExtraControls() {
        return new MButton(UserUIContext.getMessage(RoleI18nEnum.NEW),
                clickEvent -> EventBusFactory.getInstance().post(new RoleEvent.GotoAdd(this, null)))
                .withIcon(VaadinIcons.PLUS).withStyleName(WebThemes.BUTTON_ACTION)
                .withVisible(UserUIContext.canWrite(RolePermissionCollections.ACCOUNT_ROLE));
    }

    private class RoleBasicSearchLayout extends BasicSearchLayout<RoleSearchCriteria> {
        private static final long serialVersionUID = 1L;

        private TextField nameField;

        private RoleBasicSearchLayout() {
            super(RoleSearchPanel.this);
        }

        @Override
        public ComponentContainer constructBody() {
            MHorizontalLayout basicSearchBody = new MHorizontalLayout().withMargin(true)
                    .with(new Label(UserUIContext.getMessage(GenericI18Enum.FORM_NAME) + ":"));

            nameField = new MTextField().withPlaceholder(UserUIContext.getMessage(GenericI18Enum.ACTION_QUERY_BY_TEXT))
                    .withWidth(WebUIConstants.DEFAULT_CONTROL_WIDTH);
            basicSearchBody.addComponent(nameField);

            MButton searchBtn = new MButton(UserUIContext.getMessage(GenericI18Enum.BUTTON_SEARCH), clickEvent -> callSearchAction())
                    .withIcon(VaadinIcons.SEARCH).withStyleName(WebThemes.BUTTON_ACTION)
                    .withClickShortcut(ShortcutAction.KeyCode.ENTER);
            basicSearchBody.addComponent(searchBtn);

            MButton clearBtn = new MButton(UserUIContext.getMessage(GenericI18Enum.BUTTON_CLEAR), clickEvent -> nameField.setValue(""))
                    .withStyleName(WebThemes.BUTTON_OPTION);
            basicSearchBody.addComponent(clearBtn);
            basicSearchBody.setComponentAlignment(clearBtn, Alignment.MIDDLE_LEFT);
            return basicSearchBody;
        }

        @Override
        protected RoleSearchCriteria fillUpSearchCriteria() {
            RoleSearchCriteria searchCriteria = new RoleSearchCriteria();
            if (StringUtils.isNotBlank(nameField.getValue())) {
                searchCriteria.setRoleName(StringSearchField.and(this.nameField.getValue()));
            }
            return searchCriteria;
        }
    }
}