package com.wql.generate.controller;

import com.wql.generate.dto.TablesDTO;
import com.wql.generate.service.GenerateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Desc:
 *
 * @author: weiqi
 * 2024/9/21 09:10
 */
@Tag(name = "生成代码")
@RestController
@RequestMapping("/generate")
public class GenerateController {

    @Autowired
    private GenerateService generateService;


    @Operation(summary = "获取数据库表")
    @PostMapping("/tables")
    public List<String> tables(@Validated @RequestBody TablesDTO dto){
        return generateService.tables(dto);
    }


    @Operation(summary = "生成代码")
    @PostMapping("/createClass")
    public void createClass(@RequestBody List<String> tableNames){
        generateService.createClass(tableNames);
    }

}
