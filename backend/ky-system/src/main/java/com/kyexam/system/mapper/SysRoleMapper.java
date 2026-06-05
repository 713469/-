package com.kyexam.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kyexam.system.entity.SysRole;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SysRoleMapper extends BaseMapper<SysRole> {
    @Select("select r.* from sys_role r join sys_user_role ur on ur.role_id = r.id where ur.user_id = #{userId}")
    List<SysRole> selectByUserId(@Param("userId") Long userId);
}
