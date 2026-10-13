package com.kuro.ims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContainerCalcResponse {

    private String containerCode;

    private String containerName;

    private String type;

    private Double volumeM3;

    private Double maxWeight;

    private Double usedVolumeM3;

    private Double usedWeight;

    private Double volumeUtilization;

    private Double weightUtilization;

    private Boolean feasible;

    private String reason;

    private List<PlacedGoods> placedList;

    private List<LeftGoods> leftList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlacedGoods {
        private String name;
        private Integer qty;
        private Double volumeM3;
        private Double weight;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeftGoods {
        private String name;
        private Integer leftQty;
        private String reason;
    }
}
