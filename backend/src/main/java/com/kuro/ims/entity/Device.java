package com.kuro.ims.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("mes_device")
public class Device {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    private String type;

    private String workshop;

    /** RUNNING / IDLE / ALARM / STOP */
    private String status;

    private Double temperature;

    private Double humidity;

    private Double runtime;

    private Double oee;

    private Double locationX;

    private Double locationZ;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @JsonIgnore
    @TableLogic
    private Integer deleted;
}
