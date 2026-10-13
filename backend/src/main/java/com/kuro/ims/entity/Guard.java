package com.kuro.ims.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 巡检保安（携带定位器）
 */
@Data
@TableName("wms_guard")
public class Guard {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    private String phone;

    /** ON_PATROL / IDLE / OFF */
    private String status;

    private String area;

    private String locatorNo;

    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @JsonIgnore
    @TableLogic
    private Integer deleted;
}
