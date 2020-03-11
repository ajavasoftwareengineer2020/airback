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

import com.airback.db.arguments.SearchCriteria;
import com.airback.vaadin.event.HasMassItemActionHandler;
import com.airback.vaadin.event.HasSearchHandlers;
import com.airback.vaadin.event.HasSelectableItemHandlers;
import com.airback.vaadin.event.HasSelectionOptionHandlers;
import com.airback.vaadin.mvp.PageView;
import com.airback.vaadin.web.ui.table.IPagedTable;

/**
 * @param <S>
 * @param <B>
 * @author airback Ltd.
 * @since 2.0
 */
public interface IListView<S extends SearchCriteria, B> extends PageView {

    default void enableActionControls(int numOfSelectedItem) {
    }

    default void disableActionControls() {
    }

    HasSearchHandlers<S> getSearchHandlers();

    default HasSelectionOptionHandlers getOptionSelectionHandlers() {
        return null;
    }

    default HasMassItemActionHandler getPopupActionHandlers() {
        return null;
    }

    default HasSelectableItemHandlers<B> getSelectableItemHandlers() {
        return null;
    }

    default IPagedTable<S, B> getPagedBeanGrid() {
        return null;
    }

    default void showNoItemView() {
    }
}