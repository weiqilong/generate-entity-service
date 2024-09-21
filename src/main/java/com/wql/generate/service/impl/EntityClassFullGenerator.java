package com.wql.generate.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 生成DO和mapper、service
 */
public class EntityClassFullGenerator {

    // todo 数据库配置
    public static String HOST = "127.0.0.1";
    public static String DATABASE = "dev";
    public static String USER = "root";
    public static String PASSWORD = "123456";
    public static int PORT = 3306;
    // todo 类生成目录
    public static String OUTPUTDIR = "C:\\Users\\weiqi\\Desktop\\xxx";
    // todo 包名
    public static String PACKAGE_NAME = "com.example";
    // todo 作者
    public static String AUTHOR = "作者";
    // todo 是否生成mybatisplus注解
    public static Boolean IS_ADD_MYBATISPLUS_ANNOTATION = true;
    // todo 是否生成逻辑删除注解
    public static Boolean IS_LOGIC_DELETE = true;
    // todo 逻辑删除字段
    public static String LOGIC_DELETE_COLUMN_NAME = "delete_flag";
    // todo 忽略表前缀
    public static String IGNORE_TABLE_PREFIX = "t_";
    // todo 是否生成service和mapper
    public static Boolean IS_GENERATE_SERVICE_AND_MAPPER = true;

    public static List<String> INSERT_FIELD_FILL_COLUMN_NAMES = new ArrayList<>();
    public static List<String> INSERT_UPDATE_FIELD_FILL_COLUMN_NAMES = new ArrayList<>();
    private static String CLASS_COMMENT = null;

    /**
     * 主方法，运行此方法
     */
    public static void generate(List<String> tableNames) {
        String url = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE;
        CLASS_COMMENT = generateClassComment();
        try (Connection conn = DriverManager.getConnection(url, USER, PASSWORD)) {
            try {
                for (String tableName : tableNames) {
                    // 生成实体类
                    generateEntityForTable(conn,tableName, OUTPUTDIR);
                    if (!IS_GENERATE_SERVICE_AND_MAPPER) {
                        return;
                    }
                    // 生成mapper和service
                    generateMapperAndServiceForTable(tableName, OUTPUTDIR);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // 打印结束
            System.out.println("逆向生成类完毕，目录:"+OUTPUTDIR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void generateEntityForTable(Connection conn,String tableName, String outputDir) throws SQLException, IOException {
        System.out.println("=================================================>>表"+tableName);

        String className = toCamelCaseDoTableName(tableName);
        DatabaseMetaData metaData = conn.getMetaData();

        StringBuilder sb = new StringBuilder();
        // 包名
        sb.append(generatePackageComment());
        if (IS_ADD_MYBATISPLUS_ANNOTATION) {
            sb.append("import com.baomidou.mybatisplus.annotation.*;\n");
        }
        sb.append("import com.fasterxml.jackson.annotation.JsonFormat;\n");
        sb.append("import lombok.Data;\n\n");
        sb.append(CLASS_COMMENT);
        sb.append("@Data\n");

        // 判断是否需要添加mybatisplus注解
        if(IS_ADD_MYBATISPLUS_ANNOTATION){
            sb.append("@TableName(\"").append(tableName).append("\")\n");
        }
        sb.append("public class ").append(className).append(" {\n");

        String primaryKeyColumnName = "";
        ResultSet primaryKeys = metaData.getPrimaryKeys(DATABASE, null, tableName);
        if (primaryKeys.next()) {
            primaryKeyColumnName = primaryKeys.getString("COLUMN_NAME");
        }

        ResultSet columns = metaData.getColumns(null, null, tableName, null);

        while (columns.next()) {
            String columnName = columns.getString("COLUMN_NAME");
            String columnType = columns.getString("TYPE_NAME");
            String javaColumnName = toCamelCaseColumnName(columnName);

            System.out.println(columnName+"->DB字段类型:"+columnType);

            String comment = columns.getString("REMARKS");
            sb.append(generateFieldComment(comment));

            // 判断是否添加mybatisplus注解，并且判断是否主键，如果是主键，就添加
            // @TableId(type = IdType.ASSIGN_ID)
            // @JsonFormat(shape = JsonFormat.Shape.STRING)
            if(IS_ADD_MYBATISPLUS_ANNOTATION && primaryKeyColumnName.equalsIgnoreCase(columnName)){
                sb.append("    @TableId(type = IdType.ASSIGN_ID)\n");
                sb.append("    @JsonFormat(shape = JsonFormat.Shape.STRING)\n");
            }

            // 判断是否需要添加insert注解，并且判断是否是insert字段
            if(IS_ADD_MYBATISPLUS_ANNOTATION && INSERT_FIELD_FILL_COLUMN_NAMES.contains(columnName)){
                sb.append("    @TableField(fill = FieldFill.INSERT)\n");
            }
            // 判断是否需要添加update注解，并且判断是否是update字段
            if(IS_ADD_MYBATISPLUS_ANNOTATION && INSERT_UPDATE_FIELD_FILL_COLUMN_NAMES.contains(columnName)){
                sb.append("    @TableField(fill = FieldFill.INSERT_UPDATE)\n");
            }

            // 逻辑删除
            if(IS_ADD_MYBATISPLUS_ANNOTATION && IS_LOGIC_DELETE && LOGIC_DELETE_COLUMN_NAME.equalsIgnoreCase(columnName)){
                sb.append("    @TableLogic(value = \"0\", delval = \"1\")\n");
            }

            sb.append("    private ").append(mapSqlTypeToJava(columnType)).append(" ").append(javaColumnName).append(";\n\n");
        }

        sb.append("}\n");

        // do目录，在原目录基础上增加do目录
        outputDir += File.separator + "do";
        writeToFile(outputDir, className + ".java", sb.toString());

        System.out.println("<<类:"+className+"完毕!");
    }

    // 判断目录是否存在，如果不存在就创建
    private static void checkOrMkdir(String dir) {
        File outputDirFile = new File(dir);
        if (!outputDirFile.exists()) {
            outputDirFile.mkdirs();
        }
    }


    // 生成package代码方法
    private static String generatePackageComment() {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(PACKAGE_NAME).append(";\n\n");
        return sb.toString();
    }

    // 生成类注释
    public static String generateClassComment() {
        List<String> classContents = new ArrayList<>();
        classContents.add("@author: "+AUTHOR);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        classContents.add("@date: "+dateTimeFormatter.format(now));

        StringBuilder sb = new StringBuilder();
        sb.append("/** \n * \n");
        for (String classContent : classContents) {
            sb.append(" * ").append(classContent).append("\n");
        }
        sb.append(" */\n");
        return sb.toString();
    }

    // 生成字段注释
    private static String generateFieldComment(String comment) {
        StringBuilder sb = new StringBuilder();
        sb.append("    /**\n")
                .append("     * ")
                .append(comment)
                .append("\n")
                .append("     */\n");
        return sb.toString();
    }

    private static String mapSqlTypeToJava(String sqlType) {
        switch (sqlType.toUpperCase()) {
            case "DATE":
                return "LocalDate";
            case "DATETIME":
            case "TIMESTAMP":
                return "LocalDateTime";
            case "INT":
            case "BIT":
            case "TINYINT":
                return "Integer";
            case "BIGINT":
            case "BIGINT UNSIGNED":
                return "Long";
            case "VARCHAR":
            case "CHAR":
            case "TEXT":
                return "String";
            case "DECIMAL":
                return "BigDecimal";
            // todo 其他未处理的类型 在此处添加
            default:
                return "Object";
        }
    }


    private static String toCamelCaseDoTableName(String s) {
        return toCamelCaseTableName(s)+"DO";
    }

    private static String toCamelCaseTableName(String tableName) {
        if (tableName == null || tableName.isEmpty()) {
            return tableName;
        }
        // 判断表是否忽略前缀的开头，如果是，删除表前缀
        if(tableName.startsWith(IGNORE_TABLE_PREFIX)){
            tableName = tableName.substring(IGNORE_TABLE_PREFIX.length());
        }
        String[] parts = tableName.split("_");
        StringBuilder camelCase = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                camelCase.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase());
            }
        }

        return camelCase.toString();
    }


    private static String toCamelCaseColumnName(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        String[] parts = s.split("_");
        StringBuilder camelCase = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                if (camelCase.length() == 0) {
                    camelCase.append(part.toLowerCase());
                } else {
                    camelCase.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase());
                }
            }
        }

        return camelCase.toString();
    }


    private static void writeToFile(String dir, String fileName, String content) throws IOException {
        checkOrMkdir(dir);
        FileWriter writer = new FileWriter(dir + "/" + fileName);
        writer.write(content);
        writer.close();
    }


    private static void generateMapperAndServiceForTable(String tableName, String outputDir) throws IOException {
        // 生成mapper方法
        generateMapper(tableName, outputDir);
        // 生成service方法
        generateService(tableName, outputDir);
        // 生成serviceImpl方法
        generateServiceImpl(tableName, outputDir);
    }


    private static void generateServiceImpl(String tableName, String outputDir) throws IOException {
        String entityName = toCamelCaseTableName(tableName);
        String entityDoName = toCamelCaseDoTableName(tableName);
        String serviceClassName = entityName + "Service";
        String serviceImplClassName = entityName + "ServiceImpl";
        StringBuilder serviceImplContent = new StringBuilder();
        serviceImplContent.append(generatePackageComment());
        serviceImplContent.append("import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;\n");
        serviceImplContent.append("import lombok.extern.slf4j.Slf4j;\n");
        serviceImplContent.append("import org.springframework.stereotype.Service;\n");
        serviceImplContent.append(generateClassComment());
        serviceImplContent.append("@Slf4j\n");
        serviceImplContent.append("@Service\n");
        serviceImplContent.append("public class ").append(serviceImplClassName)
                .append(" extends ServiceImpl<").append(entityName).append("Mapper, ")
                .append(entityDoName).append("> implements ").append(serviceClassName)
                .append(" {\n\n}");
        // serviceImpl目录
        outputDir += File.separator + "serviceImpl";
        writeToFile(outputDir, serviceImplClassName + ".java", serviceImplContent.toString());
        System.out.println("<<类:"+serviceImplClassName+"完毕!");
    }

    private static void generateService(String tableName, String outputDir)throws IOException{
        String entityName = toCamelCaseTableName(tableName);
        String entityDoName = toCamelCaseDoTableName(tableName);
        String serviceClassName = entityName + "Service";
        StringBuilder serviceContent = new StringBuilder();
        serviceContent.append(generatePackageComment());
        serviceContent.append("import com.baomidou.mybatisplus.extension.service.IService;\n");
        serviceContent.append(generateClassComment());
        serviceContent.append("public interface ").append(serviceClassName)
                .append(" extends IService<").append(entityDoName)
                .append("> {\n\n}");
        outputDir += File.separator + "service";
        writeToFile(outputDir, serviceClassName + ".java", serviceContent.toString());
        System.out.println("<<类:"+serviceClassName+"完毕!");
    }

    private static void generateMapper(String tableName, String outputDir) throws IOException {
        String entityName = toCamelCaseTableName(tableName);
        String entityDoName = toCamelCaseDoTableName(tableName);
        String mapperClassName = entityName + "Mapper";
        StringBuilder mapperContent = new StringBuilder();
        mapperContent.append(generatePackageComment());
        mapperContent.append("import com.baomidou.mybatisplus.core.mapper.BaseMapper;\n");
        mapperContent.append(generateClassComment());
        mapperContent.append("public interface ").append(mapperClassName)
                .append(" extends BaseMapper<").append(entityDoName)
                .append("> {\n\n}");
        outputDir += File.separator + "mapper";
        writeToFile(outputDir, mapperClassName + ".java", mapperContent.toString());
        System.out.println("<<类:"+mapperClassName+"完毕!");
    }

}
    