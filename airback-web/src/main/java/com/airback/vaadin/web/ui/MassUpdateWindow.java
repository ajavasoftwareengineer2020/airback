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
package com.airback.vaadin.web.ui;

import com.airback.common.i18n.GenericI18Enum;
import com.airback.vaadin.UserUIContext;
import com.airback.vaadin.mvp.MassUpdateCommand;
import com.airback.vaadin.ui.AbstractBeanFieldGroupEditFieldFactory;
import com.airback.vaadin.ui.AbstractFormLayoutFactory;
import com.airback.vaadin.ui.AdvancedEditBeanForm;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Resource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Label;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MWindow;

/**
 * Mass update
 *
 * @param <B>
 * @author airback Ltd.
 * @since 1.0
 */
public abstract class MassUpdateWindow<B> extends MWindow {
    private static final long serialVersionUID = 1L;

    protected B beanItem;
    protected AdvancedEditBeanForm<B> updateForm;
    protected MassUpdateLayout contentLayout;
    private MButton updateBtn, closeBtn;

    private MassUpdateCommand<B> massUpdateCommand;

    public MassUpdateWindow(String title, Resource iconResource, B initialValue, MassUpdateCommand<B> massUpdatePresenter) {
        super(title);
        this.withWidth("1000px").withResizable(false).withModal(true).withIcon(iconResource).withCenter();
        this.massUpdateCommand = massUpdatePresenter;
        this.beanItem = initialValue;

        this.contentLayout = new MassUpdateLayout();
        this.updateForm = new AdvancedEditBeanForm<>();

        this.contentLayout.addBody(this.updateForm);

        this.setContent(this.contentLayout);

        updateForm.setFormLayoutFactory(buildFormLayoutFactory());
        updateForm.setBeanFormFieldFactory(buildBeanFormFieldFactory());
        updateForm.setBean(beanItem);
    }

    abstract protected AbstractFormLayoutFactory buildFormLayoutFactory();

    abstract protected AbstractBeanFieldGroupEditFieldFactory<B> buildBeanFormFieldFactory();

    protected ComponentContainer buildButtonControls() {
        MHorizontalLayout controlsLayout = new MHorizontalLayout().withMargin(true).withFullWidth();

        updateBtn = new MButton(UserUIContext.getMessage(GenericI18Enum.BUTTON_UPDATE_LABEL), clickEvent -> {
            updateForm.commit();
            massUpdateCommand.massUpdate(beanItem);
            close();
        }).withStyleName(WebThemes.BUTTON_ACTION).withIcon(VaadinIcons.CLIPBOARD);

        closeBtn = new MButton(UserUIContext.getMessage(GenericI18Enum.BUTTON_CLOSE), clickEvent -> close())
                .withStyleName(WebThemes.BUTTON_OPTION);

        Label spacing = new Label();
        controlsLayout.with(spacing, closeBtn, updateBtn).alignAll(Alignment.MIDDLE_RIGHT).expand(spacing);
        return controlsLayout;
    }

}
