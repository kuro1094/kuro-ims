package com.kuro.ims.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wms_vehicle")
public class Vehicle {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String plateNo;

    private String driver;

    private String phone;

    /** TRUCK / VAN / CONTAINER */
    private String type;

    /** IDLE / LOADING / TRANSIT / UNLOADING */
    private String status;

    private String origin;

    private String destination;

    /** 0-100 */
    private Integer progress;

    private Double locationX;

    private Double locationZ;

    private Long containerId;

    private String cargoDesc;

    private LocalDateTime eta;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @JsonIgnore
    @TableLogic
    private Integer deleted;
}
