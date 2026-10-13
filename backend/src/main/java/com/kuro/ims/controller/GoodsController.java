package com.kuro.ims.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kuro.ims.common.Result;
import com.kuro.ims.entity.Goods;
import com.kuro.ims.mapper.GoodsMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsMapper goodsMapper;

    @GetMapping
    public Result<Page<Goods>> page(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size,
                                    @RequestParam(required = false) String keyword) {
        Page<Goods> p = new Page<>(page, size);
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Goods::getName, keyword).or().like(Goods::getCode, keyword);
        }
        return Result.success(goodsMapper.selectPage(p, wrapper));
    }

    @GetMapping("/list")
    public Result<List<Goods>> list() {
        return Result.success(goodsMapper.selectList(new LambdaQueryWrapper<>()));
    }

    @GetMapping("/{id}")
    public Result<Goods> get(@PathVariable Long id) {
        return Result.success(goodsMapper.selectById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody Goods goods) {
        goodsMapper.insert(goods);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Goods goods) {
        goods.setId(id);
        goodsMapper.updateById(goods);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        goodsMapper.deleteById(id);
        return Result.success();
    }
}
