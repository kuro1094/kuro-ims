package com.kuro.ims.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kuro.ims.common.Result;
import com.kuro.ims.entity.*;
import com.kuro.ims.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final WarehouseMapper warehouseMapper;
    private final ContainerMapper containerMapper;
    private final GoodsMapper goodsMapper;
    private final VehicleMapper vehicleMapper;
    private final ForkliftMapper forkliftMapper;
    private final TaskMapper taskMapper;
    private final DeviceMapper deviceMapper;

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> data = new HashMap<>();
        data.put("warehouseCount", warehouseMapper.selectCount(new LambdaQueryWrapper<>()));
        data.put("containerCount", containerMapper.selectCount(new LambdaQueryWrapper<>()));
        data.put("goodsCount", goodsMapper.selectCount(new LambdaQueryWrapper<>()));
        data.put("vehicleCount", vehicleMapper.selectCount(new LambdaQueryWrapper<>()));
        data.put("forkliftCount", forkliftMapper.selectCount(new LambdaQueryWrapper<>()));
        data.put("pendingTaskCount", taskMapper.selectCount(new LambdaQueryWrapper<Task>().eq(Task::getStatus, "PENDING")));
        data.put("doingTaskCount", taskMapper.selectCount(new LambdaQueryWrapper<Task>().eq(Task::getStatus, "DOING")));
        data.put("alarmDeviceCount", deviceMapper.selectCount(new LambdaQueryWrapper<Device>().eq(Device::getStatus, "ALARM")));
        return Result.success(data);
    }
}
