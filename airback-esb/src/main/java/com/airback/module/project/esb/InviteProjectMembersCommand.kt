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
import com.airback.common.GenericLinkUtils
import com.airback.common.domain.MailRecipientField
import com.airback.common.i18n.MailI18nEnum
import com.airback.configuration.ApplicationConfiguration
import com.airback.configuration.IDeploymentMode
import com.airback.core.utils.DateTimeUtils
import com.airback.core.utils.RandomPasswordGenerator
import com.airback.html.LinkUtils
import com.airback.i18n.LocalizationHelper
import com.airback.module.billing.RegisterStatusConstants
import com.airback.module.esb.GenericCommand
import com.airback.module.mail.service.ExtMailService
import com.airback.module.mail.service.IContentGenerator
import com.airback.module.project.ProjectLinkGenerator
import com.airback.module.project.ProjectMemberStatusConstants
import com.airback.module.project.domain.ProjectMember
import com.airback.module.project.i18n.ProjectMemberI18nEnum
import com.airback.module.project.service.ProjectMemberService
import com.airback.module.project.service.ProjectService
import com.airback.module.user.domain.User
import com.airback.module.user.service.RoleService
import com.airback.module.user.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

/**
 * @author airback Ltd
 * @since 6.0.0
 */
@Component
class InviteProjectMembersCommand(private val userService: UserService,
                                  private val roleService: RoleService,
                                  private val deploymentMode: IDeploymentMode,
                                  private val extMailService: ExtMailService,
                                  private val projectService: ProjectService,
                                  private val projectMemberService: ProjectMemberService,
                                  private val contentGenerator: IContentGenerator,
                                  private val applicationConfiguration: ApplicationConfiguration) : GenericCommand() {
    companion object {
        val LOG = LoggerFactory.getLogger(InviteProjectMembersCommand::class.java)
    }

    @AllowConcurrentEvents
    @Subscribe
    fun inviteUsers(event: InviteProjectMembersEvent) {
        val project = projectService.findById(event.projectId, event.sAccountId)
        val user = userService.findUserInAccount(event.inviteUser, event.sAccountId)
        val billingAccount = projectService.getAccountInfoOfProject(event.projectId)

        if (project != null && user != null) {
            contentGenerator.putVariable("inviteUser", user.displayName!!)
            contentGenerator.putVariable("inviteMessage", event.inviteMessage)
            contentGenerator.putVariable("project", project)
            contentGenerator.putVariable("password", "")
            contentGenerator.putVariable("logoPath", LinkUtils.accountLogoPath(billingAccount.id, billingAccount.logopath))

            event.emails.forEach {
                val invitee = userService.findUserInAccount(it, event.sAccountId)
                contentGenerator.putVariable("inviteeEmail", it)
                if (invitee != null) {
                    if (RegisterStatusConstants.ACTIVE != invitee.registerstatus) {
                        userService.updateUserAccountStatus(it, event.sAccountId, RegisterStatusConstants.ACTIVE)
                    }
                } else {
                    val systemGuestRoleId = roleService.getDefaultRoleId(event.sAccountId)
                    if (systemGuestRoleId == null) {
                        LOG.error("Can not find the guess role of account ${event.sAccountId}")
                    }

                    val newUser = User()
                    newUser.username = it
                    newUser.email = it
                    val password = RandomPasswordGenerator.generateRandomPassword()
                    contentGenerator.putVariable("password", password)
                    newUser.password = password
                    userService.saveUserAccount(newUser, systemGuestRoleId, billingAccount.subdomain, event.sAccountId, event.inviteUser, false)
                }
                val projectMember = projectMemberService.findMemberByUsername(it, event.projectId, event.sAccountId)
                if (projectMember != null) {
                    if (ProjectMemberStatusConstants.ACTIVE != projectMember.status) {
                        projectMember.status = ProjectMemberStatusConstants.NOT_ACCESS_YET
                    } else {
                        return
                    }
                    projectMember.projectroleid = event.projectRoleId
                    projectMemberService.updateWithSession(projectMember, "")
                } else {
                    val member = ProjectMember()
                    member.projectid = event.projectId
                    member.username = it
                    member.createdtime = LocalDateTime.now()
                    member.saccountid = event.sAccountId
                    member.billingrate = project.defaultbillingrate
                    member.overtimebillingrate = project.defaultovertimebillingrate
                    member.status = ProjectMemberStatusConstants.NOT_ACCESS_YET
                    member.projectroleid = event.projectRoleId
                    projectMemberService.saveWithSession(member, "")
                }
                contentGenerator.putVariable("copyRight", LocalizationHelper.getMessage(Locale.US, MailI18nEnum.Copyright,
                        DateTimeUtils.getCurrentYear()))
                contentGenerator.putVariable("urlAccept", GenericLinkUtils.generateConfirmEmailLink(deploymentMode.getSiteUrl(billingAccount.subdomain), it))
                val subject = LocalizationHelper.getMessage(Locale.US, ProjectMemberI18nEnum.MAIL_INVITE_USERS_SUBJECT,
                        project.name, applicationConfiguration.siteName)
                val content = contentGenerator.parseFile("mailMemberInvitationNotifier.ftl", Locale.US)
                val toUser = listOf(MailRecipientField(it, it))
                extMailService.sendHTMLMail(applicationConfiguration.notifyEmail, applicationConfiguration.siteName, toUser, subject, content)
            }
        } else {
            LOG.error("Can not find user ${event.inviteUser} in account ${event.sAccountId}")
        }
    }
}