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
package com.airback.module.user.service.mybatis

import com.google.common.eventbus.AsyncEventBus
import com.airback.configuration.EnDecryptHelper
import com.airback.configuration.IDeploymentMode
import com.airback.core.Tuple2
import com.airback.core.UserInvalidInputException
import com.airback.core.utils.StringUtils
import com.airback.db.arguments.BasicSearchRequest
import com.airback.db.arguments.NumberSearchField
import com.airback.db.arguments.SetSearchField
import com.airback.db.arguments.StringSearchField
import com.airback.db.persistence.ICrudGenericDAO
import com.airback.db.persistence.ISearchableDAO
import com.airback.db.persistence.service.DefaultService
import com.airback.module.billing.RegisterStatusConstants
import com.airback.module.billing.UserStatusConstants
import com.airback.module.billing.service.BillingPlanCheckerService
import com.airback.module.file.service.UserAvatarService
import com.airback.module.user.dao.RolePermissionMapper
import com.airback.module.user.dao.UserAccountMapper
import com.airback.module.user.dao.UserMapper
import com.airback.module.user.dao.UserMapperExt
import com.airback.module.user.domain.*
import com.airback.module.user.domain.criteria.UserSearchCriteria
import com.airback.module.user.esb.DeleteUserEvent
import com.airback.module.user.esb.NewUserJoinEvent
import com.airback.module.user.esb.RequestToResetPasswordEvent
import com.airback.module.user.esb.SendUserInvitationEvent
import com.airback.module.user.service.UserService
import com.airback.security.PermissionMap
import org.apache.commons.collections.CollectionUtils
import org.apache.ibatis.session.RowBounds
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * @author airback Ltd.
 * @since 1.0
 */
@Service
class UserServiceDBImpl(private val userMapper: UserMapper,
                        private val userMapperExt: UserMapperExt,
                        private val userAccountMapper: UserAccountMapper,
                        private val rolePermissionMapper: RolePermissionMapper,
                        private val userAvatarService: UserAvatarService,
                        private val billingPlanCheckerService: BillingPlanCheckerService,
                        private val asyncEventBus: AsyncEventBus,
                        private val deploymentMode: IDeploymentMode) : DefaultService<String, User, UserSearchCriteria>(), UserService {

    override val crudMapper: ICrudGenericDAO<String, User>
        get() = userMapper as ICrudGenericDAO<String, User>

    override val searchMapper: ISearchableDAO<UserSearchCriteria>
        get() = userMapperExt

    @Transactional
    override fun saveUserAccount(record: User, roleId: Int?, subDomain: String, sAccountId: Int, inviteUser: String, isSendInvitationEmail: Boolean) {
        billingPlanCheckerService.validateAccountCanCreateNewUser(sAccountId)

        // check if user email has already in this account yet
        var userAccountEx = UserAccountExample()

        if (StringUtils.isBlank(record.email)) {
            record.email = record.username
        }

        if(StringUtils.isBlank(record.username)) {
            record.username = record.email
        }

        if (deploymentMode.isDemandEdition) {
            userAccountEx.createCriteria().andUsernameEqualTo(record.username).andAccountidEqualTo(sAccountId)
                    .andRegisterstatusEqualTo(RegisterStatusConstants.ACTIVE)
        } else {
            userAccountEx.createCriteria().andUsernameEqualTo(record.username).andRegisterstatusEqualTo(RegisterStatusConstants.ACTIVE)
        }

        if (userAccountMapper.countByExample(userAccountEx) > 0) {
            throw UserInvalidInputException("There is already user has email ${record.email} in your account")
        }

        val password = record.password
        if (password != null) {
            record.password = EnDecryptHelper.encryptSaltPassword(password)
        }

        if (StringUtils.isBlank(record.lastname)) {
            val userEmail = record.email
            val index = userEmail.lastIndexOf("@")
            if (index > 0) {
                record.lastname = userEmail.substring(0, index)
            } else {
                record.lastname = userEmail
            }
        }

        if (record.firstname == null) {
            record.firstname = ""
        }

        // Check if user has already account in system, if not we will create new user
        val userEx = UserExample()
        userEx.createCriteria().andUsernameEqualTo(record.username)
        if (userMapper.countByExample(userEx) == 0L) {
            record.registeredtime = LocalDateTime.now()
            record.status = UserStatusConstants.EMAIL_VERIFIED_REQUEST
            userMapper.insert(record)
            userAvatarService.uploadDefaultAvatar(record.username)
        }

        // save record in s_user_account table
        val userAccount = UserAccount()
        userAccount.accountid = sAccountId

        if (roleId != null && roleId > 0) {
            userAccount.roleid = roleId
            userAccount.isaccountowner = false
        } else {
            userAccount.roleid = null
            userAccount.isaccountowner = true
        }

        userAccount.username = record.username
        userAccount.registeredtime = LocalDateTime.now()
        userAccount.lastaccessedtime = LocalDateTime.now()
        userAccount.registerstatus = RegisterStatusConstants.NOT_LOG_IN_YET
        userAccount.inviteuser = inviteUser

        userAccountEx = UserAccountExample()
        if (deploymentMode.isDemandEdition) {
            userAccountEx.createCriteria().andUsernameEqualTo(record.username).andAccountidEqualTo(sAccountId)
        } else {
            userAccountEx.createCriteria().andUsernameEqualTo(record.username)
        }

        when {
            userAccountMapper.countByExample(userAccountEx) > 0 -> userAccountMapper.updateByExampleSelective(userAccount, userAccountEx)
            else -> userAccountMapper.insert(userAccount)
        }

        if (isSendInvitationEmail) {
            val invitationEvent = SendUserInvitationEvent(record.username, password, inviteUser, subDomain, sAccountId)
            asyncEventBus.post(invitationEvent)
        }
    }

    override fun bulkInviteUsers(users: List<Tuple2<String, String>>, roleId: Int?, subDomain: String, sAccountId: Int, inviteUser: String, isSendInvitationEmail: Boolean) {
        val usersList = users.map {
            val user = User()
            user.email = it.item1
            user.password = it.item2
            user
        }.toCollection(mutableListOf())
        usersList.forEach {
            try {
                saveUserAccount(it, roleId, subDomain, sAccountId, inviteUser, isSendInvitationEmail)
            } catch (e: Exception) {
                LOG.debug("Error in save user $e")
            }
        }
    }

    override fun updateWithSession(record: User, username: String?): Int {
        LOG.debug("Check whether there is exist email in system before")
        if (record.email != null && record.username != record.email) {
            val ex = UserExample()
            ex.createCriteria().andUsernameEqualTo(record.email)
            val numUsers = userMapper.countByExample(ex)
            if (numUsers > 0) {
                throw UserInvalidInputException("Email ${record.email} is already existed in system. Please choose another email.")
            }
        }

        // now we keep username similar than email
        val ex = UserExample()
        ex.createCriteria().andUsernameEqualTo(record.username)
        record.username = record.email
        return userMapper.updateByExampleSelective(record, ex)
    }

    @Transactional
    override fun updateUserAccount(record: SimpleUser, sAccountId: Int) {
        val oldUser = findUserByUserNameInAccount(record.username, sAccountId)
        if (oldUser != null) {
            if (java.lang.Boolean.TRUE == oldUser.isAccountOwner && java.lang.Boolean.FALSE == record.isAccountOwner) {
                val userAccountEx = UserAccountExample()
                userAccountEx.createCriteria().andAccountidEqualTo(sAccountId).andIsaccountownerEqualTo(java.lang.Boolean.TRUE)
                        .andRegisterstatusEqualTo(RegisterStatusConstants.ACTIVE)
                if (userAccountMapper.countByExample(userAccountEx) <= 1) {
                    throw UserInvalidInputException("Can not change role of user ${record.username}. The reason is ${record.username} is the unique account owner of the current account.")
                }
            }
        }

        if (StringUtils.isBlank(record.email)) {
            record.email = record.username
            val ex = UserExample()
            ex.createCriteria().andUsernameEqualTo(record.email)
            val numUsers = userMapper.countByExample(ex)
            if (numUsers > 1) {
                throw UserInvalidInputException("Email %s is already existed in system. Please choose another email ${record.email}")
            }
        }

        val ex = UserExample()
        ex.createCriteria().andUsernameEqualTo(record.username)
        userMapper.updateByExampleSelective(record, ex)

        // now we keep username similar than email
        if (record.email != record.username) {
            record.username = record.email
            val ex1 = UserExample()
            ex1.createCriteria().andEmailEqualTo(record.email)
            userMapper.updateByExample(record, ex1)
        }

        val userAccountEx = UserAccountExample()
        userAccountEx.createCriteria().andUsernameEqualTo(record.username).andAccountidEqualTo(sAccountId)
        val userAccounts = userAccountMapper.selectByExample(userAccountEx)
        if (userAccounts.size > 0) {
            val userAccount = userAccounts[0]
            if (record.roleId == -1) {
                userAccount.roleid = null
                userAccount.isaccountowner = true
            } else {
                userAccount.roleid = record.roleId
                userAccount.isaccountowner = false
            }

            userAccount.registerstatus = record.registerstatus
            userAccount.lastaccessedtime = LocalDateTime.now()
            userAccountMapper.updateByPrimaryKey(userAccount)
        }
    }

    override fun massRemoveWithSession(items: List<User>, username: String?, sAccountId: Int) {
        val keys = items.map { it.username }
        userMapperExt.removeKeysWithSession(keys)
    }

    override fun authentication(username: String, password: String, subDomain: String, isPasswordEncrypt: Boolean): SimpleUser {
        LOG.info("Authenticate user $username in sub-domain $subDomain")
        val criteria = UserSearchCriteria()
        criteria.username = StringSearchField.and(username)
        criteria.registerStatuses = SetSearchField(RegisterStatusConstants.ACTIVE, RegisterStatusConstants.NOT_LOG_IN_YET)
        criteria.saccountid = null

        if (deploymentMode.isDemandEdition) {
            criteria.subDomain = StringSearchField.and(subDomain)
        }

        val users = findPageableListByCriteria(BasicSearchRequest(criteria)) as List<SimpleUser>
        if (users.isEmpty()) {
            throw UserInvalidInputException("User $username is not existed in this domain $subDomain")
        } else {
            var user: SimpleUser? = null
            if (deploymentMode.isDemandEdition) {
                for (testUser in users) {
                    if (subDomain == testUser.subDomain) {
                        user = testUser
                        break
                    }
                }
                if (user == null) {
                    throw UserInvalidInputException("Invalid username or password")
                }
            } else {
                user = users[0]
            }

            if (StringUtils.isBlank(user.password) || !EnDecryptHelper.checkPassword(password, user.password, isPasswordEncrypt)) {
                throw UserInvalidInputException("Invalid username or password")
            }

            if (RegisterStatusConstants.NOT_LOG_IN_YET == user.registerstatus) {
                updateUserAccountStatus(user.username, user.accountId!!, RegisterStatusConstants.ACTIVE)
                asyncEventBus.post(NewUserJoinEvent(user.username, user.accountId!!))
            }

            LOG.debug("User $username login to system successfully!")

            if (user.isAccountOwner == null || (user.isAccountOwner != null && !user.isAccountOwner!!)) {
                if (user.roleId != null) {
                    val ex = RolePermissionExample()
                    ex.createCriteria().andRoleidEqualTo(user.roleId)
                    val roles = rolePermissionMapper.selectByExampleWithBLOBs(ex)
                    if (CollectionUtils.isNotEmpty(roles)) {
                        val rolePer = roles[0] as RolePermission
                        val permissionMap = PermissionMap.fromJsonString(rolePer.roleval)
                        user.permissionMaps = permissionMap
                    } else {
                        LOG.error("We can not find any role associate to user $username")
                    }
                } else {
                    LOG.error("User %s has no any role $username")
                }
            }
            user.password = null
            return user
        }
    }

    override fun findUserByUserNameInAccount(username: String, accountId: Int): SimpleUser? =
            findUserInAccount(username, accountId)

    override fun findUserInAccount(username: String, accountId: Int): SimpleUser? {
        val criteria = UserSearchCriteria()
        criteria.username = StringSearchField.and(username)
        criteria.saccountid = NumberSearchField(accountId)

        val users = userMapperExt.findPageableListByCriteria(criteria, RowBounds(0, 1)) as List<SimpleUser>
        return if (CollectionUtils.isEmpty(users)) null else users[0]
    }

    override fun pendingUserAccount(username: String, accountId: Int) {
        pendingUserAccounts(listOf(username), accountId)
    }

    override fun pendingUserAccounts(usernames: List<String>, accountId: Int) {
        // check if current user is the unique account owner, then reject deletion
        var userAccountEx = UserAccountExample()
        userAccountEx.createCriteria().andUsernameNotIn(usernames).andAccountidEqualTo(accountId)
                .andIsaccountownerEqualTo(true).andRegisterstatusEqualTo(RegisterStatusConstants.ACTIVE)
        if (userAccountMapper.countByExample(userAccountEx) == 0L) {
            throw UserInvalidInputException("Can not delete users. The reason is there is no account owner in the rest users")
        }

        userAccountEx = UserAccountExample()
        userAccountEx.createCriteria().andUsernameIn(usernames).andAccountidEqualTo(accountId)
        val userAccount = UserAccount()
        userAccount.registerstatus = RegisterStatusConstants.DELETE
        userAccountMapper.updateByExampleSelective(userAccount, userAccountEx)

        // notify users are "deleted"
        usernames.forEach { asyncEventBus.post(DeleteUserEvent(it, accountId)) }
    }

    override fun findUserByUserName(username: String): User? {
        val ex = UserExample()
        ex.createCriteria().andUsernameEqualTo(username)
        val users = userMapper.selectByExample(ex)
        return if (CollectionUtils.isEmpty(users)) null else users[0]
    }

    override fun updateUserAccountStatus(username: String, sAccountId: Int, registerStatus: String) {
        // Update status of user account
        val userAccount = UserAccount()
        userAccount.accountid = sAccountId
        userAccount.username = username
        userAccount.registerstatus = registerStatus

        val ex = UserAccountExample()
        ex.createCriteria().andAccountidEqualTo(sAccountId).andUsernameEqualTo(username)
        userAccountMapper.updateByExampleSelective(userAccount, ex)
    }

    override fun requestToResetPassword(username: String) {
        asyncEventBus.post(RequestToResetPasswordEvent(username))
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(UserServiceDBImpl::class.java)
    }
}
