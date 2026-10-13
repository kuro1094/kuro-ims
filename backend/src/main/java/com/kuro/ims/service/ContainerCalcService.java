package com.kuro.ims.service;

import com.kuro.ims.dto.ContainerCalcRequest;
import com.kuro.ims.dto.ContainerCalcResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ContainerCalcService {

    /** 标准柜型（内尺寸 cm + 最大载重 kg） */
    private static final double[][] STANDARD = {
            // {length, width, height, maxWeight}
            {589, 235, 239, 21700},   // 20GP
            {1203, 235, 239, 26500},  // 40GP
            {1203, 235, 269, 26500},  // 40HQ
            {1355, 235, 269, 27600}   // 45HQ
    };

    private static final String[] TYPES = {"20GP", "40GP", "40HQ", "45HQ"};

    public ContainerCalcResponse calculate(ContainerCalcRequest req) {
        double length;
        double width;
        double height;
        double maxWeight = req.getMaxWeight();

        if ("CUSTOM".equals(req.getType())) {
            length = req.getLength();
            width = req.getWidth();
            height = req.getHeight();
        } else {
            int idx = indexOfType(req.getType());
            length = STANDARD[idx][0];
            width = STANDARD[idx][1];
            height = STANDARD[idx][2];
            maxWeight = STANDARD[idx][3];
        }

        double containerVolumeM3 = length * width * height / 1_000_000.0;

        List<ContainerCalcResponse.PlacedGoods> placed = new ArrayList<>();
        List<ContainerCalcResponse.LeftGoods> left = new ArrayList<>();

        double usedVolumeM3 = 0;
        double usedWeight = 0;
        boolean feasible = true;

        for (ContainerCalcRequest.GoodsItem item : req.getGoodsList()) {
            double itemVolumeM3 = item.getLength() * item.getWidth() * item.getHeight() / 1_000_000.0;
            double itemWeight = item.getWeight();

            int qty = item.getQty();
            int placedQty = 0;
            int leftQty = 0;

            for (int i = 0; i < qty; i++) {
                if (usedVolumeM3 + itemVolumeM3 > containerVolumeM3) {
                    feasible = false;
                    leftQty = qty - placedQty;
                    break;
                }
                if (usedWeight + itemWeight > maxWeight) {
                    feasible = false;
                    leftQty = qty - placedQty;
                    break;
                }
                usedVolumeM3 += itemVolumeM3;
                usedWeight += itemWeight;
                placedQty++;
            }

            if (placedQty > 0) {
                placed.add(ContainerCalcResponse.PlacedGoods.builder()
                        .name(item.getName())
                        .qty(placedQty)
                        .volumeM3(round(placedQty * itemVolumeM3, 4))
                        .weight(round(placedQty * itemWeight, 2))
                        .build());
            }
            if (leftQty > 0) {
                left.add(ContainerCalcResponse.LeftGoods.builder()
                        .name(item.getName())
                        .leftQty(leftQty)
                        .reason(usedVolumeM3 + itemVolumeM3 > containerVolumeM3 ? "容积不足" : "超重")
                        .build());
            }
        }

        return ContainerCalcResponse.builder()
                .containerCode(req.getContainerCode())
                .containerName(req.getContainerName())
                .type(req.getType())
                .volumeM3(round(containerVolumeM3, 2))
                .maxWeight(round(maxWeight, 2))
                .usedVolumeM3(round(usedVolumeM3, 2))
                .usedWeight(round(usedWeight, 2))
                .volumeUtilization(round(usedVolumeM3 / containerVolumeM3 * 100, 2))
                .weightUtilization(round(usedWeight / maxWeight * 100, 2))
                .feasible(feasible)
                .reason(feasible ? null : "货物超出柜体容积或载重限制")
                .placedList(placed)
                .leftList(left)
                .build();
    }

    private int indexOfType(String type) {
        for (int i = 0; i < TYPES.length; i++) {
            if (TYPES[i].equals(type)) {
                return i;
            }
        }
        throw new IllegalArgumentException("不支持的柜型：" + type);
    }

    private double round(double value, int scale) {
        double factor = Math.pow(10, scale);
        return Math.round(value * factor) / factor;
    }
}
