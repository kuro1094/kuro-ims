package com.kuro.ims.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kuro.ims.common.Result;
import com.kuro.ims.dto.ContainerCalcRequest;
import com.kuro.ims.dto.ContainerCalcResponse;
import com.kuro.ims.entity.Container;
import com.kuro.ims.mapper.ContainerMapper;
import com.kuro.ims.service.ContainerCalcService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/containers")
@RequiredArgsConstructor
public class ContainerController {

    private final ContainerMapper containerMapper;
    private final ContainerCalcService containerCalcService;

    @GetMapping
    public Result<Page<Container>> page(@RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer size,
                                        @RequestParam(required = false) String keyword) {
        Page<Container> p = new Page<>(page, size);
        LambdaQueryWrapper<Container> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Container::getName, keyword).or().like(Container::getCode, keyword);
        }
        return Result.success(containerMapper.selectPage(p, wrapper));
    }

    @GetMapping("/list")
    public Result<List<Container>> list() {
        return Result.success(containerMapper.selectList(new LambdaQueryWrapper<>()));
    }

    @GetMapping("/{id}")
    public Result<Container> get(@PathVariable Long id) {
        return Result.success(containerMapper.selectById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody Container container) {
        containerMapper.insert(container);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Container container) {
        container.setId(id);
        containerMapper.updateById(container);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        containerMapper.deleteById(id);
        return Result.success();
    }

    @PostMapping("/calculate")
    public Result<ContainerCalcResponse> calculate(@Valid @RequestBody ContainerCalcRequest request) {
        return Result.success(containerCalcService.calculate(request));
    }
}
