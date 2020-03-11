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
 * along with this program.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
package com.airback.module.user.service

import com.airback.cache.IgnoreCacheClass
import com.airback.core.cache.CacheKey
import com.airback.core.cache.Cacheable
import com.airback.db.persistence.service.ICrudService
import com.airback.module.user.domain.BillingAccount
import com.airback.module.user.domain.SimpleBillingAccount

/**
 * @author airback Ltd.
 * @since 1.0
 */
@IgnoreCacheClass
interface BillingAccountService : ICrudService<Int, BillingAccount> {

    @Cacheable
    fun getBillingAccountById(@CacheKey accountId: Int): SimpleBillingAccount?

    fun getAccountByDomain(domain: String): SimpleBillingAccount?

    @Cacheable
    fun getAccountById(@CacheKey accountId: Int): BillingAccount?

    @Cacheable
    fun getTotalActiveUsersInAccount(@CacheKey accountId: Int): Int

    fun createDefaultAccountData(username: String, password: String, timezoneId: String, language: String, isEmailVerified: Boolean,
                                 isCreatedDefaultData: Boolean, sAccountId: Int)
}
