package com.kuro.ims.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wms_goods")
public class Goods {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    private String category;

    private String spec;

    private String unit;

    private Double length;

    private Double width;

    private Double height;

    private Double weight;

    private Integer quantity;

    private String batchNo;

    private Long containerId;

    private Long warehouseId;

    /** IN_STOCK / LOADED / SHIPPED */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @JsonIgnore
    @TableLogic
    private Integer deleted;
}
