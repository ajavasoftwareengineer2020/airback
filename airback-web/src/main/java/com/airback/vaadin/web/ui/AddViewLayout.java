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
package com.airback.vaadin.web.ui;

import com.airback.vaadin.ui.ELabel;
import com.airback.web.CustomLayoutExt;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import org.vaadin.viritin.layouts.MCssLayout;
import org.vaadin.viritin.layouts.MHorizontalLayout;

/**
 * @author airback Ltd.
 * @since 2.0
 */
public class AddViewLayout extends CustomLayoutExt {
    private static final long serialVersionUID = 1L;

    private VaadinIcons viewIcon;
    private Label titleLbl;
    private final MHorizontalLayout header;

    public AddViewLayout(String viewTitle, VaadinIcons viewIcon) {
        super("addView");

        this.viewIcon = viewIcon;

        header = new MHorizontalLayout().withFullWidth().withMargin(new MarginInfo(true, false, true, false));
        header.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);

        titleLbl = ELabel.h2("");
        header.with(titleLbl).expand(titleLbl);
        setHeader(viewTitle);
        addComponent(header, "addViewHeader");
    }

    public AddViewLayout(MHorizontalLayout header) {
        super("addView");
        this.header = header;
        addComponent(header, "addViewHeader");
    }

    public void addBody(Component body) {
        addComponent(body, "addViewBody");
    }

    public void addBottom(Component bottomControls) {
        this.addComponent(bottomControls, "addViewBottomControls");
    }

    public void addHeaderTitle(Component headerContainer) {
        header.addComponent(headerContainer, 0);
        header.withAlign(headerContainer, Alignment.TOP_LEFT).expand(headerContainer);
    }

    public void addHeaderRight(Component headerRight) {
        header.with(headerRight).withAlign(headerRight, Alignment.TOP_RIGHT);
    }

    public void setHeader(String viewTitle) {
        String title = viewIcon.getHtml() + " " + viewTitle;
        titleLbl.setValue(title);
    }

    public void setTitle(final String title) {
        if (title != null) {
            MCssLayout titleWrap = new MCssLayout().withFullWidth();
            titleWrap.addComponent(ELabel.h3(title).withFullWidth());
            addComponent(titleWrap, "addViewTitle");
        } else {
            removeComponent("addViewTitle");
        }
    }
}
