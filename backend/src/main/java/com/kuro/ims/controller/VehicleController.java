package com.kuro.ims.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kuro.ims.common.Result;
import com.kuro.ims.entity.Vehicle;
import com.kuro.ims.mapper.VehicleMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleMapper vehicleMapper;

    @GetMapping
    public Result<Page<Vehicle>> page(@RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer size,
                                      @RequestParam(required = false) String keyword) {
        Page<Vehicle> p = new Page<>(page, size);
        LambdaQueryWrapper<Vehicle> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Vehicle::getPlateNo, keyword).or().like(Vehicle::getDriver, keyword);
        }
        return Result.success(vehicleMapper.selectPage(p, wrapper));
    }

    @GetMapping("/list")
    public Result<List<Vehicle>> list() {
        return Result.success(vehicleMapper.selectList(new LambdaQueryWrapper<>()));
    }

    @GetMapping("/{id}")
    public Result<Vehicle> get(@PathVariable Long id) {
        return Result.success(vehicleMapper.selectById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody Vehicle vehicle) {
        vehicleMapper.insert(vehicle);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Vehicle vehicle) {
        vehicle.setId(id);
        vehicleMapper.updateById(vehicle);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        vehicleMapper.deleteById(id);
        return Result.success();
    }
}
