package com.kuro.ims.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wms_task")
public class Task {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    /** INBOUND / OUTBOUND / TRANSFER / LOADING */
    private String type;

    /** PENDING / DOING / DONE / CANCEL */
    private String status;

    private Integer priority;

    private Long goodsId;

    private String goodsName;

    private Integer quantity;

    private String fromLocation;

    private String toLocation;

    private Long forkliftId;

    private Long vehicleId;

    private Long containerId;

    private String operator;

    private String remark;

    private LocalDateTime startTime;

    private LocalDateTime finishTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @JsonIgnore
    @TableLogic
    private Integer deleted;
}
