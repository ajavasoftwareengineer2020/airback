package com.airback.common.dao;

import com.airback.common.domain.Comment;
import com.airback.common.domain.CommentExample;
import com.airback.db.persistence.ICrudGenericDAO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@SuppressWarnings({ "ucd", "rawtypes" })
@Mapper
public interface CommentMapper extends ICrudGenericDAO {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_comment
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    long countByExample(CommentExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_comment
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    int deleteByExample(CommentExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_comment
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_comment
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    int insert(Comment record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_comment
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    int insertSelective(Comment record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_comment
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    List<Comment> selectByExampleWithBLOBs(CommentExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_comment
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    List<Comment> selectByExample(CommentExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_comment
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    Comment selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_comment
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    int updateByExampleSelective(@Param("record") Comment record, @Param("example") CommentExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_comment
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    int updateByExampleWithBLOBs(@Param("record") Comment record, @Param("example") CommentExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_comment
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    int updateByExample(@Param("record") Comment record, @Param("example") CommentExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_comment
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    int updateByPrimaryKeySelective(Comment record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_comment
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    int updateByPrimaryKeyWithBLOBs(Comment record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_comment
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    int updateByPrimaryKey(Comment record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_comment
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    Integer insertAndReturnKey(Comment value);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_comment
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    void removeKeysWithSession(List primaryKeys);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table m_comment
     *
     * @mbg.generated Sat Feb 09 11:42:26 CST 2019
     */
    void massUpdateWithSession(@Param("record") Comment record, @Param("primaryKeys") List primaryKeys);
}