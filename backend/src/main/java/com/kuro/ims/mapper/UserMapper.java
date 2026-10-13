package com.kuro.ims.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kuro.ims.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
