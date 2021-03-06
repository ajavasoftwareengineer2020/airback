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
package com.airback.schedule.email.format

import com.hp.gagawa.java.elements.Span
import com.airback.core.utils.DateTimeUtils
import com.airback.core.utils.StringUtils
import com.airback.core.utils.TimezoneVal
import com.airback.schedule.email.MailContext
import org.apache.commons.beanutils.PropertyUtils
import org.slf4j.LoggerFactory
import java.time.LocalDate

/**
 * @author airback Ltd
 * @since 6.0.0
 */
class DateFieldFormat(fieldName: String, displayName: Enum<*>) : FieldFormat(fieldName, displayName) {
    companion object {
        val LOG = LoggerFactory.getLogger(DateFieldFormat::class.java)
    }

    override fun formatField(context: MailContext<*>): String {
        val wrappedBean = context.wrappedBean
        return try {
            val value = PropertyUtils.getProperty(wrappedBean, fieldName)
            when (value) {
                null -> Span().write()
                else -> Span().appendText(DateTimeUtils.formatDate(value as LocalDate, context.user.dateFormat,
                        context.locale, TimezoneVal.valueOf(context.user.timezone))).write()
            }
        } catch (e: Exception) {
            LOG.error("Can not generate email field: $fieldName", e)
            Span().write()
        }
    }

    override fun formatField(context: MailContext<*>, value: String): String =
            if (StringUtils.isBlank(value)) Span().write() else
                DateTimeUtils.convertDateToStringWithUserTimeZone(value, context.user.dateFormat, context.locale, context.timeZone)
}