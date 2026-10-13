package com.kuro.ims.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wms_forklift")
public class Forklift {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    private String driver;

    /** IDLE / WORKING / CHARGING / MAINTAIN */
    private String status;

    private Integer battery;

    private String currentTask;

    private Double locationX;

    private Double locationZ;

    private Long warehouseId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @JsonIgnore
    @TableLogic
    private Integer deleted;
}
