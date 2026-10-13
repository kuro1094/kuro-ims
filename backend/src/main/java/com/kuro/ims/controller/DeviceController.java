package com.kuro.ims.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kuro.ims.common.Result;
import com.kuro.ims.entity.Device;
import com.kuro.ims.mapper.DeviceMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceMapper deviceMapper;

    @GetMapping
    public Result<Page<Device>> page(@RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer size,
                                     @RequestParam(required = false) String keyword) {
        Page<Device> p = new Page<>(page, size);
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Device::getCode, keyword).or().like(Device::getName, keyword);
        }
        return Result.success(deviceMapper.selectPage(p, wrapper));
    }

    @GetMapping("/list")
    public Result<List<Device>> list() {
        return Result.success(deviceMapper.selectList(new LambdaQueryWrapper<>()));
    }

    @GetMapping("/{id}")
    public Result<Device> get(@PathVariable Long id) {
        return Result.success(deviceMapper.selectById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody Device device) {
        deviceMapper.insert(device);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Device device) {
        device.setId(id);
        deviceMapper.updateById(device);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deviceMapper.deleteById(id);
        return Result.success();
    }
}
