package com.kuro.ims.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kuro.ims.entity.Task;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {
}
