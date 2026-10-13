package com.kuro.ims.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 园区建筑（地图可编辑资产）
 */
@Data
@TableName("wms_building")
public class Building {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String code;

    /** OFFICE / WORKSHOP / WAREHOUSE / CANTEEN / QC / VISITOR */
    private String model;

    private Double posX;

    private Double posZ;

    private Double sizeX;

    private Double sizeY;

    private Double sizeZ;

    private String manager;

    private Integer capacity;

    private Integer used;

    /** RUNNING / MAINTAIN / STOP */
    private String status;

    private String description;

    private String color;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @JsonIgnore
    @TableLogic
    private Integer deleted;
}
