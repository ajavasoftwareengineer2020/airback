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
package com.airback.module.project.ui.components;

import com.airback.common.domain.CommentWithBLOBs;
import com.airback.common.i18n.GenericI18Enum;
import com.airback.common.service.CommentService;
import com.airback.module.file.AttachmentUtils;
import com.airback.module.project.CurrentProjectVariables;
import com.airback.module.user.domain.SimpleUser;
import com.airback.spring.AppContextUtil;
import com.airback.vaadin.AppUI;
import com.airback.vaadin.UserUIContext;
import com.airback.vaadin.ui.ReloadableComponent;
import com.airback.vaadin.web.ui.AttachmentPanel;
import com.airback.vaadin.web.ui.WebThemes;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.RichTextArea;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import java.time.LocalDateTime;

/**
 * @author airback Ltd.
 * @since 1.0
 */
class ProjectCommentInput extends MHorizontalLayout {
    private static final long serialVersionUID = 1L;

    private RichTextArea commentArea;

    private String type;
    private String typeId;
    private Integer extraTypeId;

    ProjectCommentInput(final ReloadableComponent component, final String typeVal, Integer extraTypeIdVal) {
        this.withMargin(new MarginInfo(true, true, false, false)).withFullWidth().withUndefinedHeight();

        SimpleUser currentUser = UserUIContext.getUser();
        ProjectMemberBlock userBlock = new ProjectMemberBlock(currentUser.getUsername(), currentUser.getAvatarid(),
                currentUser.getDisplayName());

        MVerticalLayout textAreaWrap = new MVerticalLayout().withFullWidth().withStyleName(WebThemes.MESSAGE_CONTAINER);
        this.with(userBlock, textAreaWrap).expand(textAreaWrap);

        type = typeVal;
        extraTypeId = extraTypeIdVal;

        commentArea = new RichTextArea();
        commentArea.setWidth("100%");
        commentArea.setHeight("200px");
        commentArea.addStyleName("comment-attachment");

        final AttachmentPanel attachments = new AttachmentPanel();
        attachments.setWidth("100%");

        final MButton newCommentBtn = new MButton(UserUIContext.getMessage(GenericI18Enum.BUTTON_POST), clickEvent -> {
            CommentWithBLOBs comment = new CommentWithBLOBs();
            comment.setComment(Jsoup.clean(commentArea.getValue(), Whitelist.relaxed()));
            comment.setCreatedtime(LocalDateTime.now());
            comment.setCreateduser(UserUIContext.getUsername());
            comment.setSaccountid(AppUI.getAccountId());
            comment.setType(type);
            comment.setTypeid("" + typeId);
            comment.setExtratypeid(extraTypeId);

            final CommentService commentService = AppContextUtil.getSpringBean(CommentService.class);
            int commentId = commentService.saveWithSession(comment, UserUIContext.getUsername());

            String attachmentPath = AttachmentUtils.getCommentAttachmentPath(typeVal, AppUI.getAccountId(),
                    CurrentProjectVariables.getProjectId(), typeId, commentId);

            if (!"".equals(attachmentPath)) {
                attachments.saveContentsToRepo(attachmentPath);
            }

            // save success, clear comment area and load list
            // comments again
            commentArea.setValue("");
            component.reload();
        }).withStyleName(WebThemes.BUTTON_ACTION).withIcon(VaadinIcons.PAPERPLANE);

        textAreaWrap.with(commentArea, attachments, newCommentBtn).withAlign(newCommentBtn, Alignment.TOP_RIGHT);
    }

    void setTypeAndId(final String typeId) {
        this.typeId = typeId;
    }
}
