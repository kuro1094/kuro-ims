package com.kuro.ims.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kuro.ims.common.Result;
import com.kuro.ims.entity.PatrolVehicle;
import com.kuro.ims.mapper.PatrolVehicleMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patrol-vehicles")
@RequiredArgsConstructor
public class PatrolVehicleController {

    private final PatrolVehicleMapper patrolVehicleMapper;

    @GetMapping
    public Result<Page<PatrolVehicle>> page(@RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer size,
                                            @RequestParam(required = false) String keyword) {
        Page<PatrolVehicle> p = new Page<>(page, size);
        LambdaQueryWrapper<PatrolVehicle> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(PatrolVehicle::getName, keyword).or().like(PatrolVehicle::getCode, keyword);
        }
        return Result.success(patrolVehicleMapper.selectPage(p, wrapper));
    }

    @GetMapping("/list")
    public Result<List<PatrolVehicle>> list() {
        return Result.success(patrolVehicleMapper.selectList(new LambdaQueryWrapper<>()));
    }

    @GetMapping("/{id}")
    public Result<PatrolVehicle> get(@PathVariable Long id) {
        return Result.success(patrolVehicleMapper.selectById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody PatrolVehicle patrolVehicle) {
        patrolVehicleMapper.insert(patrolVehicle);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody PatrolVehicle patrolVehicle) {
        patrolVehicle.setId(id);
        patrolVehicleMapper.updateById(patrolVehicle);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        patrolVehicleMapper.deleteById(id);
        return Result.success();
    }
}
