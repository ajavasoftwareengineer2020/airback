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
package com.airback.module.project.esb

import com.google.common.eventbus.AllowConcurrentEvents
import com.google.common.eventbus.Subscribe
import com.airback.common.dao.CommentMapper
import com.airback.common.domain.CommentExample
import com.airback.module.ecm.service.ResourceService
import com.airback.module.esb.GenericCommand
import com.airback.module.file.AttachmentUtils
import com.airback.module.project.ProjectTypeConstants
import org.springframework.stereotype.Component

/**
 * @author airback Ltd
 * @since 6.0.0
 */
@Component
class DeleteProjectComponentCommand(private val resourceService: ResourceService,
                                    private val commentMapper: CommentMapper) : GenericCommand() {

    @AllowConcurrentEvents
    @Subscribe
    fun removedComponent(event: DeleteProjectComponentEvent) {
        removeRelatedFiles(event.accountId, event.projectId, event.componentId)
        removeRelatedComments(event.componentId)
    }

    private fun removeRelatedFiles(accountId: Int, projectId: Int, componentId: Int) {
        val attachmentPath = AttachmentUtils.getProjectEntityAttachmentPath(accountId, projectId,
                ProjectTypeConstants.COMPONENT, "$componentId")
        resourceService.removeResource(attachmentPath, "", true, accountId)
    }

    private fun removeRelatedComments(bugId: Int) {
        val ex = CommentExample()
        ex.createCriteria().andTypeEqualTo(ProjectTypeConstants.COMPONENT).andExtratypeidEqualTo(bugId)
        commentMapper.deleteByExample(ex)
    }
}