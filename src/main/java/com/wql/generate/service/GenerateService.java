package com.wql.generate.service;

import com.wql.generate.dto.TablesDTO;

import java.util.List;

/**
 * Desc:
 *
 * @author: weiqi
 * 2024/9/21 09:09
 */
public interface GenerateService {


    List<String> tables(TablesDTO dto);

    void createClass(List<String> tableNames);
}
