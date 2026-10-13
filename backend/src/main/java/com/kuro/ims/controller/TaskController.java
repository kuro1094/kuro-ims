package com.kuro.ims.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kuro.ims.common.Result;
import com.kuro.ims.entity.Task;
import com.kuro.ims.mapper.TaskMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskMapper taskMapper;

    @GetMapping
    public Result<Page<Task>> page(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer size,
                                   @RequestParam(required = false) String keyword) {
        Page<Task> p = new Page<>(page, size);
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Task::getCode, keyword).or().like(Task::getGoodsName, keyword);
        }
        return Result.success(taskMapper.selectPage(p, wrapper));
    }

    @GetMapping("/list")
    public Result<List<Task>> list() {
        return Result.success(taskMapper.selectList(new LambdaQueryWrapper<>()));
    }

    @GetMapping("/{id}")
    public Result<Task> get(@PathVariable Long id) {
        return Result.success(taskMapper.selectById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody Task task) {
        taskMapper.insert(task);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Task task) {
        task.setId(id);
        taskMapper.updateById(task);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskMapper.deleteById(id);
        return Result.success();
    }
}
