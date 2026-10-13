package com.kuro.ims.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kuro.ims.common.Result;
import com.kuro.ims.entity.Forklift;
import com.kuro.ims.mapper.ForkliftMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/forklifts")
@RequiredArgsConstructor
public class ForkliftController {

    private final ForkliftMapper forkliftMapper;

    @GetMapping
    public Result<Page<Forklift>> page(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(required = false) String keyword) {
        Page<Forklift> p = new Page<>(page, size);
        LambdaQueryWrapper<Forklift> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Forklift::getCode, keyword).or().like(Forklift::getName, keyword);
        }
        return Result.success(forkliftMapper.selectPage(p, wrapper));
    }

    @GetMapping("/list")
    public Result<List<Forklift>> list() {
        return Result.success(forkliftMapper.selectList(new LambdaQueryWrapper<>()));
    }

    @GetMapping("/{id}")
    public Result<Forklift> get(@PathVariable Long id) {
        return Result.success(forkliftMapper.selectById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody Forklift forklift) {
        forkliftMapper.insert(forklift);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Forklift forklift) {
        forklift.setId(id);
        forkliftMapper.updateById(forklift);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        forkliftMapper.deleteById(id);
        return Result.success();
    }
}
