package com.kuro.ims.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wms_container")
public class Container {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    /** 20GP / 40GP / 40HQ / 45HQ */
    private String type;

    /** 内长 cm */
    private Double length;

    /** 内宽 cm */
    private Double width;

    /** 内高 cm */
    private Double height;

    /** 最大载重 kg */
    private Double maxWeight;

    private Double usedVolume;

    private Double usedWeight;

    /** EMPTY / LOADING / FULL / SHIPPED */
    private String status;

    private Long warehouseId;

    private String location;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @JsonIgnore
    @TableLogic
    private Integer deleted;
}
