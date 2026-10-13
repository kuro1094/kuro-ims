package com.kuro.ims.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kuro.ims.common.Result;
import com.kuro.ims.entity.Warehouse;
import com.kuro.ims.mapper.WarehouseMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseMapper warehouseMapper;

    @GetMapping
    public Result<Page<Warehouse>> page(@RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer size,
                                        @RequestParam(required = false) String keyword) {
        Page<Warehouse> p = new Page<>(page, size);
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Warehouse::getName, keyword).or().like(Warehouse::getCode, keyword);
        }
        return Result.success(warehouseMapper.selectPage(p, wrapper));
    }

    @GetMapping("/list")
    public Result<List<Warehouse>> list() {
        return Result.success(warehouseMapper.selectList(new LambdaQueryWrapper<>()));
    }

    @GetMapping("/{id}")
    public Result<Warehouse> get(@PathVariable Long id) {
        return Result.success(warehouseMapper.selectById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody Warehouse warehouse) {
        warehouseMapper.insert(warehouse);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Warehouse warehouse) {
        warehouse.setId(id);
        warehouseMapper.updateById(warehouse);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        warehouseMapper.deleteById(id);
        return Result.success();
    }
}
