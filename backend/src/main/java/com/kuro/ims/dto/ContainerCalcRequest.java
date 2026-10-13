package com.kuro.ims.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ContainerCalcRequest {

    @NotBlank(message = "货柜编码不能为空")
    private String containerCode;

    @NotBlank(message = "货柜名称不能为空")
    private String containerName;

    /** 20GP / 40GP / 40HQ / 45HQ / CUSTOM */
    @NotBlank(message = "柜型不能为空")
    private String type;

    /** 仅当 type=CUSTOM 时生效 */
    private Double length;

    private Double width;

    private Double height;

    @NotNull(message = "最大载重不能为空")
    private Double maxWeight;

    @NotNull(message = "待装箱货物不能为空")
    private List<GoodsItem> goodsList;

    @Data
    public static class GoodsItem {
        @NotBlank(message = "货物名称不能为空")
        private String name;

        @NotNull(message = "数量不能为空")
        private Integer qty;

        @NotNull(message = "长不能为空")
        private Double length;

        @NotNull(message = "宽不能为空")
        private Double width;

        @NotNull(message = "高不能为空")
        private Double height;

        @NotNull(message = "重量不能为空")
        private Double weight;
    }
}
