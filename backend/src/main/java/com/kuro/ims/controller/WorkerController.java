package com.kuro.ims.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kuro.ims.common.Result;
import com.kuro.ims.entity.Worker;
import com.kuro.ims.mapper.WorkerMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workers")
@RequiredArgsConstructor
public class WorkerController {

    private final WorkerMapper workerMapper;

    @GetMapping
    public Result<Page<Worker>> page(@RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer size,
                                     @RequestParam(required = false) String keyword) {
        Page<Worker> p = new Page<>(page, size);
        LambdaQueryWrapper<Worker> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Worker::getName, keyword).or().like(Worker::getCode, keyword);
        }
        return Result.success(workerMapper.selectPage(p, wrapper));
    }

    @GetMapping("/list")
    public Result<List<Worker>> list() {
        return Result.success(workerMapper.selectList(new LambdaQueryWrapper<>()));
    }

    @GetMapping("/{id}")
    public Result<Worker> get(@PathVariable Long id) {
        return Result.success(workerMapper.selectById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody Worker worker) {
        workerMapper.insert(worker);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Worker worker) {
        worker.setId(id);
        workerMapper.updateById(worker);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        workerMapper.deleteById(id);
        return Result.success();
    }
}
