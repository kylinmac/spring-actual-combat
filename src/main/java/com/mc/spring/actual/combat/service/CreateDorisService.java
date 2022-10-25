package com.mc.spring.actual.combat.service;


import com.mc.spring.actual.combat.mapper.CreateTableMapper;
import com.mc.spring.actual.combat.model.ColumnEntity;
import com.mc.spring.actual.combat.model.CreateTableEntity;
import com.mc.spring.actual.combat.model.MysqlConnection;
import com.mc.spring.actual.combat.utils.DorisSqlUtils;
import com.mc.spring.actual.combat.utils.YmlUtil;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author macheng
 * @date 2022/1/24 14:03
 */
@Service
public class CreateDorisService {
    @Autowired
    private CreateTableMapper createTableMapper;


    public void createOdbcTable(boolean drop, String suffix, String schema, String tables, MysqlConnection mysqlConnection, StringBuilder all) {
        createTableMapper.execute("set query_timeout=3600;");
        String[] split = tables.split(",");
        for (String table : split) {
            StringBuilder sqlBuilder = new StringBuilder();
            try {
                String tableComment = createTableMapper.getTableComment(table, schema);
                sqlBuilder.append("CREATE TABLE IF NOT EXISTS ")
                        .append(table + suffix)
                        .append(" ")
                        .append("(").append("\n");
                ArrayList<String> columnList = new ArrayList<>();
                Map<String, ColumnEntity> columnMap = createTableMapper.getColumnInfo(table, schema).stream().collect(Collectors.toMap(ColumnEntity::getColumnName, x ->
                {
                    columnList.add(x.getColumnName());
                    if (x.getDataType().contains("char")) {
                        String columnType = x.getColumnType();
                        Long length = Long.parseLong(columnType.substring(columnType.indexOf('(') + 1, columnType.length() - 1));
                        x.setColumnType(columnType.replaceAll(length + "", length * 3 + ""));
                    } else if (x.getDataType().equals("timestamp")) {
                        x.setColumnType("datetime");
                        x.setColumnDefault(null);
                    }
                    if (x.getColumnName().equals("tenant_id")) {
                        x.setColumnType("bigint");
                    }
                    x.setColumnType(x.getColumnType().replaceAll("unsigned", ""));
                    x.setColumnType(x.getColumnType().replaceAll("zerofill", ""));
                    x.setColumnType(x.getColumnType().replaceAll("longtext", "String"));
                    return x;
                }));

                CreateTableEntity createTableEntity = CreateTableEntity.builder().build();
                createTableEntity.setKeyColumns("");
                concatColumn(columnList, schema, table, createTableEntity, sqlBuilder, columnMap);

                sqlBuilder.append("ENGINE=ODBC\n");
                sqlBuilder.append("COMMENT \"").append(tableComment).append("\"\n");

//                concatRangePartition(sqlBuilder, uniqueConfig.getPartitionColumns(), columnMap);

                String prop = String.format("\"host\" = \"%s\",\n" +
                                "\"port\" = \"%s\",\n" +
                                "\"user\" = \"%s\",\n" +
                                "\"password\" = \"%s\",\n" +
                                "\"driver\" = \"MySQL ODBC 8.0 Unicode Driver\",\n" +
                                "\"odbc_type\" = \"mysql\",\n" +
                                "\"database\" = \"%s\",\n" +
                                "\"table\" = \"%s\"\n"
                        , mysqlConnection.getHost()
                        , mysqlConnection.getPort()
                        , mysqlConnection.getUser()
                        , mysqlConnection.getPassword()
                        , schema,
                        table);
                sqlBuilder.append("PROPERTIES (\n")
                        .append(prop)
                        .append(");\n");

                String sql = sqlBuilder.toString().replaceAll(",is_delete_doris tinyint\\(4\\) NULL COMMENT 'doris删除标记'", "");

                if (drop) {
                    all.append("drop table if exists " + table + suffix + " ;\n ");
                }
                all.append(sql);
//                createTableMapper.create(sql);
            } catch (Exception e) {
                System.out.println(sqlBuilder);
                throw e;
            }

        }


    }


    public void createDorisTableByMysql(String schema, String tables, boolean needDrop) {
        createTableMapper.execute("set query_timeout=3600;");
        String[] split = tables.split(",");
        for (String table : split) {
            StringBuilder sqlBuilder = new StringBuilder();
            try {
                CreateTableEntity uniqueConfig = CreateTableEntity.builder()
                        .keyColumns("")
                        .dorisTableName(table)
                        .build();
                String tableComment = createTableMapper.getTableComment(table, schema);
                uniqueConfig.setKeyColumns(uniqueConfig.getKeyColumns().replaceAll("`", "").replaceAll(" ", ""));
                sqlBuilder.append("CREATE TABLE IF NOT EXISTS ")
                        .append(table)
                        .append(" ")
                        .append("(").append("\n");
                ArrayList<String> columnList = new ArrayList<>();
                Map<String, ColumnEntity> columnMap = createTableMapper.getColumnInfo(table, schema).stream().collect(Collectors.toMap(ColumnEntity::getColumnName, x ->
                {
                    columnList.add(x.getColumnName());
                    if (x.getDataType().contains("char")) {
                        String columnType = x.getColumnType();
                        Long length = Long.parseLong(columnType.substring(columnType.indexOf('(') + 1, columnType.length() - 1));
                        x.setColumnType(columnType.replaceAll(length + "", length * 3 + ""));
                    } else if (x.getDataType().equals("timestamp")) {
                        x.setColumnType("datetime");
                        x.setColumnDefault(null);
                    }
                    if (x.getColumnName().equals("tenant_id")) {
                        x.setColumnType("bigint");
                    }
                    x.setColumnType(x.getColumnType().replaceAll("unsigned", ""));
                    x.setColumnType(x.getColumnType().replaceAll("zerofill", ""));
                    x.setColumnType(x.getColumnType().replaceAll("longtext", "String"));
                    return x;
                }));

                String[] keyColumns = new String[]{"tenant_id", "created"};
                StringBuilder sbk = new StringBuilder();
                for (String keyColumn : keyColumns) {
                    if (columnMap.containsKey(keyColumn)) {
                        sbk.append(keyColumn).append(",");
                    }
                }
                sbk.deleteCharAt(sbk.length() - 1);
                uniqueConfig.setKeyColumns(sbk.toString());

//                createTableMapper.updateById(uniqueConfig);
                concatColumn(columnList, schema, table, uniqueConfig, sqlBuilder, columnMap);

                sqlBuilder.append("DUPLICATE  KEY(").append(uniqueConfig.getKeyColumns()).append(")\n");
                sqlBuilder.append("COMMENT \"").append(tableComment).append("\"\n");

                //concatRangePartition(sqlBuilder, uniqueConfig.getPartitionColumns(), columnMap);
                sqlBuilder.append("PARTITION BY RANGE(tenant_id,create_time)" +
                        "\n(" +
                        ")\n");
                sqlBuilder.append("DISTRIBUTED BY HASH(");
                sqlBuilder.append("id" + ",");
                sqlBuilder.delete(sqlBuilder.length() - 1, sqlBuilder.length());
                sqlBuilder.append(")").append(" ").append("BUCKETS 1").append("\n").append("PROPERTIES(\n");
                sqlBuilder.append("\"replication_num\" = \"1\"");
//                if (!StringUtils.isEmpty(uniqueConfig.getBloomFilterColumns())) {
//                    sqlBuilder.append(",\n\"bloom_filter_columns\" = \"" + uniqueConfig.getBloomFilterColumns() + "\"");
//                }
//                sqlBuilder.append(",\n\"colocate_with\" = \"tenant_id\"");

                sqlBuilder.append(")");

                if (needDrop) {
//                    createTableMapper.create("ALTER TABLE " + table + " RENAME " + table + "_old_cdc;");
                }


                System.out.println(sqlBuilder);
//                createTableMapper.create(sqlBuilder.toString());
            } catch (Exception e) {
                System.out.println(sqlBuilder);
                throw e;
            }

        }


    }

    private void concatColumn(ArrayList<String> columnList, String schema, String table, CreateTableEntity uniqueConfig, StringBuilder sqlBuilder, Map<String, ColumnEntity> columnMap) {

        String[] splitKey = uniqueConfig.getKeyColumns().split(",");


        List<String> orderedList = changeColumnOrderByKey(columnList, splitKey);

        for (String col : orderedList) {
            ColumnEntity columnEntity = columnMap.get(col);
            sqlBuilder.append(columnEntity.getColumnName()).append(" ")
                    .append(columnEntity.getColumnType()).append(" ");
            if ("YES".equals(columnEntity.getIsNullable())) {
                sqlBuilder.append(" null ");
            } else {
                sqlBuilder.append("not null ");
            }

            if (columnEntity.getColumnDefault() != null) {
                sqlBuilder.append("default '").append(columnEntity.getColumnDefault()).append("' ");
            }

            if (columnEntity.getColumnComment() != null) {
                sqlBuilder.append("comment '").append(columnEntity.getColumnComment()).append("' ");
            }

            sqlBuilder.append("\n,");
        }
        sqlBuilder.append("is_delete_doris tinyint(4) NULL COMMENT 'doris删除标记')\n");
    }


    private static void concatRangePartition(StringBuilder sqlBuilder, String partition, Map<String, ColumnEntity> columnTypes) {
        if (StringUtils.isEmpty(partition)) {
            return;
        }

        sqlBuilder.append("PARTITION BY RANGE(`" + partition + "`)\n");
        sqlBuilder.append("(");
        if (columnTypes.get(partition).getColumnType().contains("int")) {
            if (partition.contains("year")) {
                int first = 2020;
                for (int i = 0; i < 20; i++) {
                    sqlBuilder.append("PARTITION p" + first + " VALUES  LESS THAN (\"" + first + "\"),\n");
                    first += 1;
                }
            } else {
                int first = 1024 * 8;
                for (int i = 0; i < 20; i++) {
                    sqlBuilder.append("PARTITION p" + first + " VALUES  LESS THAN (\"" + first + "\"),\n");
                    first += 1024 * 8;
                }
            }

        } else if (columnTypes.get(partition).getColumnType().contains("time")) {
            LocalDateTime start = LocalDateTime.of(2021, 10, 18, 0, 0, 0);

            for (int i = 0; i < 366; i++) {
                String s = start.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE).replaceAll("-", "_");
                sqlBuilder.append("PARTITION p" + s + " VALUES  LESS THAN (\"" + start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\"),\n");
                start = start.plusDays(1);
            }
        } else if (columnTypes.get(partition).getColumnType().contains("date")) {
            LocalDate start = LocalDate.of(2021, 10, 1);

            for (int i = 0; i < 366; i++) {
                String s = start.format(DateTimeFormatter.ISO_LOCAL_DATE).replaceAll("-", "_");
                sqlBuilder.append("PARTITION p" + s + " VALUES  LESS THAN (\"" + start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\"),\n");
                start = start.plusDays(1);
            }

        }

        sqlBuilder.delete(sqlBuilder.length() - 1, sqlBuilder.length());
        sqlBuilder.delete(sqlBuilder.length() - 1, sqlBuilder.length());
        sqlBuilder.append(")\n");
    }

    public static String getCreateSql(String sql) throws JSQLParserException {

        //todo 字段到结尾
        StringBuilder sqlBuilder = new StringBuilder();

        CCJSqlParserManager parser = new CCJSqlParserManager();
        CopyOnWriteArrayList<ColumnDefinition> columnList = null; //todo 列 字段名称跟信息。
        String tableName = "";


        Statement stmt = parser.parse(new StringReader(sql));
        if (stmt instanceof CreateTable) {
            //todo 表名字 get by map
            tableName = ((CreateTable) stmt).getTable().getName();
            columnList = new CopyOnWriteArrayList<>(((CreateTable) stmt).getColumnDefinitions());
        }

//            sqlBuilder.append("drop table if exists " + tableName + " ;\n");

        //todo 创建语句
        sqlBuilder.append("CREATE TABLE IF NOT EXISTS ")
                .append(tableName)
                .append(" ")
                .append("(").append("\n");

        if (columnList == null) {
            return "";
        }
        tableName = tableName.replaceAll("`", "");
        String uniqKey = YmlUtil.get(tableName + ".key");
        String partition = YmlUtil.getStr(tableName + ".partition");
        String[] splitKey = uniqKey.split(",");
        System.out.println(tableName);
        //todo 先将主键字段放在第一位
        CopyOnWriteArrayList<ColumnDefinition> arrayList = new CopyOnWriteArrayList<>();


        changeColumnOrderByKey(columnList, splitKey, arrayList);

        columnList = arrayList;

        //todo 全部变成varchar()
        HashMap<String, String> columnTypes = new HashMap<>();
        for (int i = 0; i < columnList.size(); i++) {
            String columnString = columnList.get(i).toString();
            columnString = removeUnsurpportedWord(columnString);
            String columnName = columnList.get(i).getColumnName();
            ColDataType columnDataType = columnList.get(i).getColDataType();
            //todo 字段的类型
            String dataType = columnDataType.getDataType();
            columnTypes.put(columnName, dataType);

            //todo  是主键的情况下 主键放最前面。

            //todo 如果是varchar 类型就放大三倍
            if ("varchar".equals(dataType) || "char".equals(dataType)) {
                List<String> argumentsStringList = columnDataType.getArgumentsStringList();
                Integer typeValueTriple = Integer.valueOf(argumentsStringList.get(0)) * 3;

                sqlBuilder.append(columnName).append(" ").append("varchar").append("(").append(typeValueTriple).append(")");

            } else if ("decimal".equals(dataType)) {
                List<String> argumentsStringList = columnDataType.getArgumentsStringList();

                sqlBuilder.append(columnName).append(" ").append(dataType).append("(").append(argumentsStringList.get(0) + "," + argumentsStringList.get(1)).append(")");

            }
            if ("timestamp".equals(dataType)) {
                dataType = "datetime";
            }

            sqlBuilder.append(columnName).append(" ").append(dataType);


            if (columnString.contains("not null")) {
                sqlBuilder.append(" not null ");
            }
            String[] s = columnString.split(" ");
            if (columnString.contains("default")) {
                getDefaultValue(sqlBuilder, s);
            }

            if (columnString.contains("comment")) {
                getComment(sqlBuilder, s);
            }

            sqlBuilder.append(",").append("\n");


        }

        sqlBuilder.append("is_delete_doris tinyint(4) NULL COMMENT \"doris删除标记\")\n");

        sqlBuilder.append("UNIQUE KEY(" + uniqKey).append(")").append("\n");

        if (partition != "null") {
            appendPartition(sqlBuilder, partition, columnTypes);
        }

        sqlBuilder.append("DISTRIBUTED BY HASH(");
        sqlBuilder.append("id" + ",");
        sqlBuilder.delete(sqlBuilder.length() - 1, sqlBuilder.length());
        sqlBuilder.append(")").append(" ").append("BUCKETS 32").append("\n").append("PROPERTIES(\n");
        sqlBuilder.append("\"replication_num\" = \"1\"");
        sqlBuilder.append(",\n\"colocate_with\" = \"tenant_id\"");

//            if ("null" != YmlUtil.get(tableName + ".bloom_filter_columns") && null != YmlUtil.get(tableName + ".bloom_filter_columns")) {
//                sqlBuilder.append(",\n\"bloom_filter_columns\" = \"" + YmlUtil.get(tableName + ".bloom_filter_columns") + "\"");
//            }
        sqlBuilder.append(")");
//            prefixBuilder.append(sqlBuilder);
//            createSqlList.add(prefixBuilder.toString());
        return sqlBuilder.toString();


//            System.out.println("最后的SQL = " + createSqlList.toString());

    }

    private static void getDefaultValue(StringBuilder sqlBuilder, String[] s) {
        StringBuilder def = new StringBuilder(" default ");
        for (int j = 0; j < s.length; j++) {
            if ("default".equals(s[j])) {
                def.append(s[j + 1]);
                break;
            }
        }
        try {
            Integer integer = Integer.valueOf(def.toString());
            def = new StringBuilder(" default '" + integer + "'");
        } catch (Exception e) {
            if (def.charAt(def.length() - 1) != '\'') {
                def = new StringBuilder("");
            }
        }
        sqlBuilder.append(def);
    }

    private static void getComment(StringBuilder sqlBuilder, String[] s) {
        sqlBuilder.append(" comment ");
        O:
        for (int j = 0; j < s.length; j++) {
            if ("comment".equals(s[j])) {
                for (int k = j + 1; k < s.length; k++) {
                    sqlBuilder.append(s[k]);
                    if (s[k].endsWith("'"))
                        break O;
                }

            }
        }
    }

    private static void changeColumnOrderByKey(CopyOnWriteArrayList<ColumnDefinition> columnList, String[] splitKey, CopyOnWriteArrayList<ColumnDefinition> arrayList) {
        A:
        for (int i = 0; i < splitKey.length; i++) {
            for (int j = 0; j < columnList.size(); j++) {
                ColumnDefinition next = columnList.get(j);
                if (next.getColumnName() != null && next.getColumnName().equals(splitKey[i])) {
                    arrayList.add(next);
                    columnList.remove(next);
                    continue A;
                }
            }

        }
        arrayList.addAll(columnList);
    }


    private List<String> changeColumnOrderByKey(List<String> columnList, String[] splitKey) {
        List<String> arrayList = new ArrayList<>();
        A:
        for (int i = 0; i < splitKey.length; i++) {
            for (int j = 0; j < columnList.size(); j++) {
                String next = columnList.get(j);
                if (next != null && next.equals(splitKey[i].trim())) {
                    arrayList.add(next);
                    columnList.remove(next);
                    continue A;
                }
            }

        }
        arrayList.addAll(columnList);
        return arrayList;
    }

    private static String removeUnsurpportedWord(String columnString) {
        if (columnString.contains("primary key")) {
            columnString = columnString.replaceAll("primary key", "");
        }
        if (columnString.contains("auto_increment")) {
            columnString = columnString.replaceAll("auto_increment", "");
        }
        if (columnString.contains(" unsigned ")) {
            columnString = columnString.replaceAll(" unsigned ", " ");
        }
        if (columnString.contains("zerofill")) {
            columnString = columnString.replaceAll("zerofill", "");
        }
        return columnString;
    }

    private static void appendPartition(StringBuilder sqlBuilder, String partition, HashMap<String, String> columnTypes) {
        sqlBuilder.append("PARTITION BY RANGE(`" + partition + "`)\n");
        sqlBuilder.append("(");
        if (columnTypes.get(partition).contains("int")) {
            if (partition.contains("year")) {
                int first = 2020;
                for (int i = 0; i < 20; i++) {
                    sqlBuilder.append("PARTITION p" + first + " VALUES  LESS THAN (\"" + first + "\"),\n");
                    first += 1;
                }
            } else {
                int first = 1024 * 8;
                for (int i = 0; i < 20; i++) {
                    sqlBuilder.append("PARTITION p" + first + " VALUES  LESS THAN (\"" + first + "\"),\n");
                    first += 1024 * 8;
                }
            }

        } else if (columnTypes.get(partition).contains("time")) {
            LocalDateTime start = LocalDateTime.of(2021, 10, 18, 0, 0, 0);

            for (int i = 0; i < 366; i++) {
                String s = start.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE).replaceAll("-", "_");
                sqlBuilder.append("PARTITION p" + s + " VALUES  LESS THAN (\"" + start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\"),\n");
                start = start.plusDays(1);
            }
        } else if (columnTypes.get(partition).contains("date")) {
            LocalDate start = LocalDate.of(2021, 1, 1);

            for (int i = 0; i < 37; i++) {
                String s = start.format(DateTimeFormatter.ISO_LOCAL_DATE).replaceAll("-", "_");
                sqlBuilder.append("PARTITION p" + s + " VALUES  LESS THAN (\"" + start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\"),\n");
                start = start.plusMonths(1);
            }

        }

        sqlBuilder.delete(sqlBuilder.length() - 1, sqlBuilder.length());
        sqlBuilder.delete(sqlBuilder.length() - 1, sqlBuilder.length());
        sqlBuilder.append(")\n");
    }


    public static List<String> readFile(String fileName) throws IOException {
        // list store return sql
        List<String> sqlList = new ArrayList<String>();
        // add widonw path
        if (File.separator.equals("\\")) {
            System.out.println("XXXX = " + CreateDorisService.class.getClassLoader().getResource("").getPath());
            fileName = CreateDorisService.class.getClassLoader().getResource("").getPath() + fileName;
        }
        File file = new File(fileName);
        // check file exists
        if (!file.exists()) {
            System.out.println("File not found: " + fileName);
            System.exit(-1);
        }
        // read file
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        StringBuilder sqlBuffer = new StringBuilder();
        while ((line = br.readLine()) != null) {
            // ignore empty line and comment line
            if (StringUtils.isEmpty(line) || line.trim().startsWith("--") || line.trim().startsWith("DROP")) {
                continue;
            }

            // remove comment
            if (line.contains("--")) {
                line = line.substring(0, line.indexOf("--"));
            }
            // add current line to sqlBuffer
            sqlBuffer.append(line);
            sqlBuffer.append("\n");
            // check sql end
            if (line.endsWith(";")) {
                // add sql to sqlList
                String tmpSql = sqlBuffer.toString();
                // remove last ";"
                tmpSql = tmpSql.substring(0, tmpSql.lastIndexOf(";"));
                sqlList.add(tmpSql);
                // remove StringBuilder
                sqlBuffer.delete(0, sqlBuffer.length());


            }
        }
        // if last sql sentence not end with ";"
        if (sqlBuffer.length() != 0) {
            sqlList.add(sqlBuffer.toString());
        }
        return sqlList;


    }


    /*public static List<ColumnDefinition> getTableNameBySql(String sql) throws JSQLParserException {
        CCJSqlParserManager parser = new CCJSqlParserManager();
        List<ColumnDefinition> columnList = null;
        Statement stmt = parser.parse(new StringReader(sql));
        if (stmt instanceof CreateTable) {
            String name = ((CreateTable) stmt).getTable().getName();

            List<String> columnsNames = ((CreateTable) stmt).getIndexes().get(0).getColumnsNames();
            columnList = ((CreateTable) stmt).getColumnDefinitions();

        }
        return columnList;
    }*/
    public String getCreateTableSqlFromDb(String tableName) {
        return createTableMapper.getCreateSql(tableName).get("Create Table");

    }

    public void moveDataByRename(String source, String target, String sourceSuffix) {

        String insertSql = DorisSqlUtils.insertSql(target, createTableMapper.getDesc(source), source);
        String renameSource = DorisSqlUtils.renameSql(source, source + sourceSuffix);
        String renameTarget = DorisSqlUtils.renameSql(target, source);
        createTableMapper.execute(insertSql);
        createTableMapper.execute(renameSource);
        createTableMapper.execute(renameTarget);

    }


}
