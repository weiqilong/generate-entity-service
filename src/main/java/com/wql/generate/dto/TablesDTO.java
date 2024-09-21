package com.wql.generate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Desc:
 *
 * @author: weiqi
 * 2024/9/21 09:12
 */
@Data
public class TablesDTO {
    // 数据库主机地址
    @NotBlank
    private String host;
    // 数据库端口号
    @NotNull
    private Integer port = 3306;
    // 数据库用户名
    @NotBlank
    private String username;
    // 数据库密码
    @NotBlank
    private String password;
    // 数据库名称
    @NotBlank
    private String database;
    // 代码生成的输出目录
    @NotBlank
    private String outputDir;
    // 代码生成的包名
    @NotBlank
    private String packageName;
    // 代码作者
    @NotBlank
    private String author;
    // 是否生成删除字段，默认不生成
    @NotNull
    private Boolean generateDeleteField = false;
    // 删除字段的名称
    private String deleteFieldName;
    // 忽略的表前缀
    private String ignorePrefix;
    // 是否生成Mapper和服务接口，默认生成
    @NotNull
    private Boolean generateMapperService = true;

    // 多个，用,分割
    private String generateInsertFields;
    // 多个，用,分割
    private String generateUpdateFields;

}
