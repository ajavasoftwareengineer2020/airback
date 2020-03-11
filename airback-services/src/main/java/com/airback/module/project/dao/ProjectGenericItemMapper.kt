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
package com.airback.module.project.dao

import com.airback.db.persistence.ISearchableDAO
import com.airback.module.project.domain.criteria.ProjectGenericItemSearchCriteria
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

/**
 * @author airback Ltd.
 * @since 5.0.3
 */
@Mapper
interface ProjectGenericItemMapper : ISearchableDAO<ProjectGenericItemSearchCriteria> {

    fun getTotalCountFromRisk(@Param("searchCriteria") criteria: ProjectGenericItemSearchCriteria): Int

    fun getTotalCountFromBug(@Param("searchCriteria") criteria: ProjectGenericItemSearchCriteria): Int

    fun getTotalCountFromVersion(@Param("searchCriteria") criteria: ProjectGenericItemSearchCriteria): Int

    fun getTotalCountFromComponent(@Param("searchCriteria") criteria: ProjectGenericItemSearchCriteria): Int

    fun getTotalCountFromTask(@Param("searchCriteria") criteria: ProjectGenericItemSearchCriteria): Int

    fun getTotalCountFromMessage(@Param("searchCriteria") criteria: ProjectGenericItemSearchCriteria): Int

    fun getTotalCountFromMilestone(@Param("searchCriteria") criteria: ProjectGenericItemSearchCriteria): Int
}