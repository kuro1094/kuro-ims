package com.kuro.ims.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 园区作业人员
 */
@Data
@TableName("wms_worker")
public class Worker {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    private String phone;

    /** 岗位：叉车司机 / 仓管员 / 调度员 ... */
    private String role;

    private String department;

    /** 负责区域 */
    private String area;

    /** ON 在职 / OFF 休假 / LEAVE 离职 */
    private String status;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @JsonIgnore
    @TableLogic
    private Integer deleted;
}
