package com.airback.common.dao;

import com.airback.common.domain.AuditLog;
import com.airback.common.domain.AuditLogExample;
import com.airback.db.persistence.ICrudGenericDAO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@SuppressWarnings({ "ucd", "rawtypes" })
@Mapper
public interface AuditLogMapper extends ICrudGenericDAO {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_audit_log
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    long countByExample(AuditLogExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_audit_log
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    int deleteByExample(AuditLogExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_audit_log
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_audit_log
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    int insert(AuditLog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_audit_log
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    int insertSelective(AuditLog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_audit_log
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    List<AuditLog> selectByExampleWithBLOBs(AuditLogExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_audit_log
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    List<AuditLog> selectByExample(AuditLogExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_audit_log
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    AuditLog selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_audit_log
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    int updateByExampleSelective(@Param("record") AuditLog record, @Param("example") AuditLogExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_audit_log
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    int updateByExampleWithBLOBs(@Param("record") AuditLog record, @Param("example") AuditLogExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_audit_log
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    int updateByExample(@Param("record") AuditLog record, @Param("example") AuditLogExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_audit_log
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    int updateByPrimaryKeySelective(AuditLog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_audit_log
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    int updateByPrimaryKeyWithBLOBs(AuditLog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_audit_log
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    int updateByPrimaryKey(AuditLog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_audit_log
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    Integer insertAndReturnKey(AuditLog value);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_audit_log
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    void removeKeysWithSession(List primaryKeys);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_audit_log
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    void massUpdateWithSession(@Param("record") AuditLog record, @Param("primaryKeys") List primaryKeys);
}