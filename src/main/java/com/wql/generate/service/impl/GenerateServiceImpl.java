package com.wql.generate.service.impl;

import com.wql.generate.dto.TablesDTO;
import com.wql.generate.service.GenerateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Desc:
 *
 * @author: weiqi
 * 2024/9/21 09:09
 */
@Slf4j
@Service
public class GenerateServiceImpl implements GenerateService {


    @Override
    public List<String> tables(TablesDTO dto) {
        // 给EntityClassFullGenerator的字段赋值
        EntityClassFullGenerator.HOST = dto.getHost();
        EntityClassFullGenerator.PORT = dto.getPort();
        EntityClassFullGenerator.USER = dto.getUsername();
        EntityClassFullGenerator.PASSWORD = dto.getPassword();
        EntityClassFullGenerator.DATABASE = dto.getDatabase();
        EntityClassFullGenerator.OUTPUTDIR = dto.getOutputDir();
        EntityClassFullGenerator.PACKAGE_NAME = dto.getPackageName();
        EntityClassFullGenerator.AUTHOR = dto.getAuthor();
        EntityClassFullGenerator.IS_ADD_MYBATISPLUS_ANNOTATION = true;
        EntityClassFullGenerator.IS_LOGIC_DELETE = dto.getGenerateDeleteField();
        EntityClassFullGenerator.LOGIC_DELETE_COLUMN_NAME = dto.getDeleteFieldName();
        EntityClassFullGenerator.IGNORE_TABLE_PREFIX = dto.getIgnorePrefix();
        EntityClassFullGenerator.IS_GENERATE_SERVICE_AND_MAPPER = dto.getGenerateMapperService();


        String generateInsertFields = dto.getGenerateInsertFields();
        if (null != generateInsertFields) {
            EntityClassFullGenerator.INSERT_FIELD_FILL_COLUMN_NAMES = new ArrayList<>();
            String[] split = generateInsertFields.split(",");
            for (String field : split) {
                if (null !=field && field.trim().length()>0) {
                    EntityClassFullGenerator.INSERT_FIELD_FILL_COLUMN_NAMES.add(field.trim());
                }
            }
        }
        String generateUpdateFields = dto.getGenerateUpdateFields();
        if (null != generateUpdateFields) {
            EntityClassFullGenerator.INSERT_UPDATE_FIELD_FILL_COLUMN_NAMES = new ArrayList<>();
            String[] split = generateUpdateFields.split(",");
            for (String field : split) {
                if (null !=field && field.trim().length()>0) {
                    EntityClassFullGenerator.INSERT_UPDATE_FIELD_FILL_COLUMN_NAMES.add(field.trim());
                }
            }
        }


        List<String> tableList = new ArrayList<>();
        // 连接数据库，查询表列表返回
        // String url = "jdbc:mysql://" + EntityClassFullGenerator.HOST + ":" + EntityClassFullGenerator.PORT + "/" + EntityClassFullGenerator.DATABASE;
        String url = "jdbc:mysql://" + EntityClassFullGenerator.HOST + ":" + EntityClassFullGenerator.PORT + "/" + EntityClassFullGenerator.DATABASE + "?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=false";
        log.info("数据url[{}]",url);
        try (Connection connection = DriverManager.getConnection(url, EntityClassFullGenerator.USER, EntityClassFullGenerator.PASSWORD)) {
            DatabaseMetaData metaData = connection.getMetaData();
            // metaData获取所有表
            ResultSet tables = metaData.getTables(null, null, null, new String[]{"TABLE"});
            while (tables.next()) {
                log.info("表名:[{}]", tables.getString("TABLE_NAME"));
                tableList.add(tables.getString("TABLE_NAME"));
            }

        } catch (Exception e) {
            log.error("出错:[{}]",e.getMessage());
            throw new RuntimeException(e);
        }

        return tableList;
    }

    @Override
    public void createClass(List<String> tableNames) {
        if (null == tableNames || tableNames.size() <= 0) {
            return;
        }
        EntityClassFullGenerator.generate(tableNames);
    }
}
