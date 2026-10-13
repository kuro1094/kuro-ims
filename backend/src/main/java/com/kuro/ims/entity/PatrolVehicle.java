package com.kuro.ims.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 园区巡逻车
 */
@Data
@TableName("wms_patrol_vehicle")
public class PatrolVehicle {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    private String driver;

    private String phone;

    /** PATROL / IDLE / CHARGING / MAINTAIN */
    private String status;

    private Integer battery;

    private String route;

    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @JsonIgnore
    @TableLogic
    private Integer deleted;
}
