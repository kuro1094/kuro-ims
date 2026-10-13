package com.kuro.ims.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wms_warehouse")
public class Warehouse {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    /** NORMAL / COLD / HAZARD */
    private String type;

    private Double locationX;

    private Double locationZ;

    private Double sizeX;

    private Double sizeZ;

    private Double sizeY;

    private String color;

    private String manager;

    private Integer capacity;

    private Integer used;

    /** RUNNING / MAINTAIN / STOP */
    private String status;

    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @JsonIgnore
    @TableLogic
    private Integer deleted;
}
