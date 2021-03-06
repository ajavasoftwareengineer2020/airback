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
package com.airback.module.project.view.milestone;

import com.hp.gagawa.java.elements.*;
import com.airback.common.i18n.DayI18nEnum;
import com.airback.core.utils.StringUtils;
import com.airback.db.arguments.BasicSearchRequest;
import com.airback.db.arguments.SearchCriteria;
import com.airback.db.arguments.SetSearchField;
import com.airback.module.project.ProjectLinkGenerator;
import com.airback.module.project.ProjectTypeConstants;
import com.airback.module.project.domain.Milestone;
import com.airback.module.project.domain.SimpleMilestone;
import com.airback.module.project.domain.criteria.MilestoneSearchCriteria;
import com.airback.module.project.i18n.MilestoneI18nEnum;
import com.airback.module.project.i18n.OptionI18nEnum.MilestoneStatus;
import com.airback.module.project.i18n.ProjectCommonI18nEnum;
import com.airback.module.project.service.MilestoneService;
import com.airback.module.project.ui.ProjectAssetsManager;
import com.airback.spring.AppContextUtil;
import com.airback.vaadin.TooltipHelper;
import com.airback.vaadin.UserUIContext;
import com.airback.vaadin.ui.ELabel;
import com.airback.vaadin.web.ui.Depot;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.CheckBox;
import org.vaadin.viritin.layouts.MCssLayout;

import java.util.Collections;
import java.util.List;

/**
 * @author airback Ltd
 * @since 5.2.4
 */
public class AllMilestoneTimelineWidget extends Depot {
    private List<SimpleMilestone> milestones;

    public AllMilestoneTimelineWidget() {
        super(UserUIContext.getMessage(MilestoneI18nEnum.OPT_TIMELINE), new MCssLayout());
        this.setWidth("100%");
        this.addStyleName("tm-container");
    }

    public void display(List<Integer> projectIds) {
        CheckBox includeNoDateSet = new CheckBox(UserUIContext.getMessage(DayI18nEnum.OPT_NO_DATE_SET));
        includeNoDateSet.setValue(false);

        CheckBox includeClosedMilestone = new CheckBox(UserUIContext.getMessage(MilestoneStatus.Closed));
        includeClosedMilestone.setValue(false);

        includeNoDateSet.addValueChangeListener(valueChangeEvent -> displayTimelines(includeNoDateSet.getValue(), includeClosedMilestone.getValue()));
        includeClosedMilestone.addValueChangeListener(valueChangeEvent -> displayTimelines(includeNoDateSet.getValue(), includeClosedMilestone.getValue()));

        addHeaderElement(includeNoDateSet);
        addHeaderElement(includeClosedMilestone);

        MilestoneSearchCriteria searchCriteria = new MilestoneSearchCriteria();
        searchCriteria.setProjectIds(new SetSearchField<>(projectIds));
        searchCriteria.setOrderFields(Collections.singletonList(new SearchCriteria.OrderField(Milestone.Field.enddate.name(), "ASC")));
        MilestoneService milestoneService = AppContextUtil.getSpringBean(MilestoneService.class);
        milestones = (List<SimpleMilestone>) milestoneService.findPageableListByCriteria(new BasicSearchRequest<>(searchCriteria));

        bodyContent.addStyleName("tm-wrapper");
        displayTimelines(false, false);
    }

    private void displayTimelines(boolean includeNoDateSet, boolean includeClosedMilestone) {
        bodyContent.removeAllComponents();
        Ul ul = new Ul().setCSSClass("timeline");

        for (SimpleMilestone milestone : milestones) {
            if (!includeClosedMilestone) {
                if (MilestoneStatus.Closed.name().equals(milestone.getStatus())) {
                    continue;
                }
            }
            if (!includeNoDateSet) {
                if (milestone.getEnddate() == null) {
                    continue;
                }
            }
            Li li = new Li();
            if (MilestoneStatus.Closed.name().equals(milestone.getStatus())) {
                li.setCSSClass("li closed");
            } else if (MilestoneStatus.InProgress.name().equals(milestone.getStatus())) {
                li.setCSSClass("li inprogress");
            } else if (MilestoneStatus.Future.name().equals(milestone.getStatus())) {
                li.setCSSClass("li future");
            }

            Div timestampDiv = new Div().setCSSClass("timestamp");

            int openAssignments = milestone.getNumOpenBugs() + milestone.getNumOpenTasks() + milestone.getNumOpenRisks();
            int totalAssignments = milestone.getNumBugs() + milestone.getNumTasks() + milestone.getNumRisks();
            if (totalAssignments > 0) {
                timestampDiv.appendChild(new Span().appendText((totalAssignments -
                        openAssignments) * 100 / totalAssignments + "%"));
            } else {
                timestampDiv.appendChild(new Span().appendText("100%"));
            }

            if (milestone.getEnddate() == null) {
                timestampDiv.appendChild(new Span().setCSSClass("date").appendText(UserUIContext.getMessage(DayI18nEnum.OPT_NO_DATE_SET)));
            } else {
                if (milestone.isOverdue()) {
                    timestampDiv.appendChild(new Span().setCSSClass("date overdue").appendText(UserUIContext.formatDate(milestone.getEnddate()) +
                            " (" + UserUIContext.getMessage(ProjectCommonI18nEnum.OPT_DUE_IN, UserUIContext.formatDuration(milestone.getEnddate())) + ")"));
                } else {
                    timestampDiv.appendChild(new Span().setCSSClass("date").appendText(UserUIContext.formatDate(milestone.getEnddate())));
                }
            }
            li.appendChild(timestampDiv);

            A projectDiv = new A(ProjectLinkGenerator.generateProjectLink(milestone.getProjectid())).appendText
                    (VaadinIcons.BUILDING_O.getHtml() + " " + StringUtils.trim(milestone.getProjectName(), 30, true))
                    .setId("tag" + TooltipHelper.TOOLTIP_ID);
            projectDiv.setAttribute("onmouseover", TooltipHelper.projectHoverJsFunction(ProjectTypeConstants.PROJECT,
                    milestone.getProjectid() + ""));
            projectDiv.setAttribute("onmouseleave", TooltipHelper.itemMouseLeaveJsFunction());

            A milestoneDiv = new A(ProjectLinkGenerator.generateMilestonePreviewLink
                    (milestone.getProjectid(), milestone.getId())).appendText(ProjectAssetsManager.getAsset
                    (ProjectTypeConstants.MILESTONE).getHtml() + " " + StringUtils.trim(milestone.getName(), 30, true))
                    .setId("tag" + TooltipHelper.TOOLTIP_ID);
            milestoneDiv.setAttribute("onmouseover", TooltipHelper.projectHoverJsFunction(ProjectTypeConstants.MILESTONE,
                    milestone.getId() + ""));
            milestoneDiv.setAttribute("onmouseleave", TooltipHelper.itemMouseLeaveJsFunction());

            Div statusDiv = new Div().setCSSClass("status").appendChild(projectDiv, milestoneDiv);
            li.appendChild(statusDiv);
            ul.appendChild(li);
        }

        bodyContent.addComponent(ELabel.html(ul.write()).withUndefinedWidth());
    }
}
