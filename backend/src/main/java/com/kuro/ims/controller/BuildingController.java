package com.kuro.ims.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kuro.ims.common.Result;
import com.kuro.ims.entity.Building;
import com.kuro.ims.mapper.BuildingMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingMapper buildingMapper;

    @GetMapping
    public Result<Page<Building>> page(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(required = false) String keyword) {
        Page<Building> p = new Page<>(page, size);
        LambdaQueryWrapper<Building> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Building::getName, keyword).or().like(Building::getModel, keyword);
        }
        return Result.success(buildingMapper.selectPage(p, wrapper));
    }

    @GetMapping("/list")
    public Result<List<Building>> list() {
        return Result.success(buildingMapper.selectList(new LambdaQueryWrapper<>()));
    }

    @GetMapping("/{id}")
    public Result<Building> get(@PathVariable Long id) {
        return Result.success(buildingMapper.selectById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody Building building) {
        buildingMapper.insert(building);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Building building) {
        building.setId(id);
        buildingMapper.updateById(building);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        buildingMapper.deleteById(id);
        return Result.success();
    }
}
