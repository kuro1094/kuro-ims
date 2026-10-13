package com.kuro.ims.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kuro.ims.common.Result;
import com.kuro.ims.entity.Guard;
import com.kuro.ims.mapper.GuardMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/guards")
@RequiredArgsConstructor
public class GuardController {

    private final GuardMapper guardMapper;

    @GetMapping
    public Result<Page<Guard>> page(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size,
                                    @RequestParam(required = false) String keyword) {
        Page<Guard> p = new Page<>(page, size);
        LambdaQueryWrapper<Guard> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Guard::getName, keyword).or().like(Guard::getCode, keyword);
        }
        return Result.success(guardMapper.selectPage(p, wrapper));
    }

    @GetMapping("/list")
    public Result<List<Guard>> list() {
        return Result.success(guardMapper.selectList(new LambdaQueryWrapper<>()));
    }

    @GetMapping("/{id}")
    public Result<Guard> get(@PathVariable Long id) {
        return Result.success(guardMapper.selectById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody Guard guard) {
        guardMapper.insert(guard);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Guard guard) {
        guard.setId(id);
        guardMapper.updateById(guard);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        guardMapper.deleteById(id);
        return Result.success();
    }
}
