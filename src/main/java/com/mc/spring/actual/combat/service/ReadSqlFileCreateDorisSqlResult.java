package com.mc.spring.actual.combat.service;

import com.mc.spring.actual.combat.utils.YmlUtil;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author macheng
 * @date 2022/1/24 14:03
 */
public class ReadSqlFileCreateDorisSqlResult {
    public static void main(String[] args) throws JSQLParserException {


        try {
            List<String> list = ReadSqlFileCreateDorisSqlResult.readFile("tpcds.sql");
//            List<String> list = SqlFileUtil2.readFile("q2.sql");
            ArrayList<String> tables=new ArrayList<>();
            ArrayList<String> dbtables=new ArrayList<>();

            //todo 最后输出格式一
            List<String> createSqlList = new ArrayList<>();

            //todo 最后输出格式二，字符串
            StringBuilder finalSqlBuilder = new StringBuilder();

            for (String sql : list) {
                //todo 字段到结尾
                StringBuilder sqlBuilder = new StringBuilder();

                //todo 建表语句+主键
                StringBuilder prefixBuilder = new StringBuilder();

                CCJSqlParserManager parser = new CCJSqlParserManager();
                CopyOnWriteArrayList<ColumnDefinition> columnList = null; //todo 列 字段名称跟信息。
                String tableName = ""; //todo 表名字
                List<String> pkColumnsList = new ArrayList<>(); //todo 主键名称

                if (sql.contains("AUTO_INCREMENT")) {
                    sql = sql.replaceAll("AUTO_INCREMENT", "");
                }
                Statement stmt = parser.parse(new StringReader(sql));
                if (stmt instanceof CreateTable) {
                    tableName = ((CreateTable) stmt).getTable().getName();
                    List<Index> indexes = ((CreateTable) stmt).getIndexes();
                    if (indexes != null && !indexes.isEmpty()) {
                        pkColumnsList = ((CreateTable) stmt).getIndexes().get(0).getColumnsNames();
                    }

                    columnList = new CopyOnWriteArrayList<> (((CreateTable) stmt).getColumnDefinitions());
                }
                prefixBuilder.append("drop table if exists "+tableName+" ;\n");

                //todo 创建语句
                prefixBuilder.append("CREATE TABLE IF NOT EXISTS ")
                        .append(tableName)
                        .append(" ")
                        .append("(").append("\n");

                if (columnList == null) {
                    continue;
                }
                String uniqKey = YmlUtil.get(tableName + ".key");
                String partition = YmlUtil.getStr(tableName + ".partition");
                String[] splitKey = uniqKey.split(",");
                tables.add(tableName);
                dbtables.add("hjy.*."+tableName);
                System.out.println(tableName);
                //todo 先将主键字段放在第一位
                CopyOnWriteArrayList<ColumnDefinition> arrayList = new CopyOnWriteArrayList<>();


                A:
                for (int i = 0; i < splitKey.length; i++) {
                    for (int j = 0; j < columnList.size(); j++) {
                        ColumnDefinition next = columnList.get(j);
                        if (next.getColumnName()!=null && next.getColumnName().equals(splitKey[i])) {
                            arrayList.add(next);
                            columnList.remove(next);
                            continue A;
                        }
                    }

                }
                arrayList.addAll(columnList);
                columnList = arrayList;

                //todo 全部变成varchar()
                HashMap<String,String> columnTypes=new HashMap<>();
                for (int i = 0; i < columnList.size(); i++) {
                    String columnString = columnList.get(i).toString();
                    String columnName = columnList.get(i).getColumnName();
                    ColDataType columnDataType = columnList.get(i).getColDataType();
                    //todo 字段的类型
                    String dataType = columnDataType.getDataType();
                    columnTypes.put(columnName,dataType);

                    //todo  是主键的情况下 主键放最前面。

                    //todo 如果是varchar 类型就放大三倍
                    if ("varchar".equals(dataType)||"char".equals(dataType)) {
                        List<String> argumentsStringList = columnDataType.getArgumentsStringList();
                        Integer typeValueTriple = Integer.valueOf(argumentsStringList.get(0)) * 3;

                        sqlBuilder.append(columnName).append(" ").append("varchar").append("(").append(typeValueTriple).append(")");

                    } else if ("decimal".equals(dataType)) {
                        List<String> argumentsStringList = columnDataType.getArgumentsStringList();

                        sqlBuilder.append(columnName).append(" ").append(dataType).append("(").append(argumentsStringList.get(0) + "," + argumentsStringList.get(1)).append(")");

                    }else{
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

                        if ("timestamp".equals(dataType)) {
                            dataType = "datetime";
                        }
                        sqlBuilder.append(columnName).append(" ").append(dataType);
                    }


                    if (columnString.contains("not null")) {
                        sqlBuilder.append(" not null");
                    }
                    String[] s = columnString.split(" ");
                    if (columnString.contains("default")) {
                        StringBuilder def= new StringBuilder(" default ");
                        for (int j = 0; j < s.length; j++) {
                            if ("default".equals(s[j])) {
                                def.append(s[j + 1]);
                                break;
                            }
                        }
                        try {
                            Integer integer = Integer.valueOf(def.toString());
                            def =new StringBuilder(" default '"+integer+"'");
                        }catch (Exception e){
                            if (def.charAt(def.length()-1)!='\''){
                                def=new StringBuilder("");
                            }
                        }
                        sqlBuilder.append(def);
                    }

                    if (columnString.contains("comment")) {
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


                    sqlBuilder.append(",").append("\n");


                }

                sqlBuilder.append("is_delete_doris tinyint(4) NULL COMMENT \"doris删除标记\")\n");

                sqlBuilder.append("UNIQUE KEY(" + uniqKey);

                sqlBuilder.append(")").append("\n");
                if (partition!="null"){
                    appendPartition(sqlBuilder, partition, columnTypes);
                }

                sqlBuilder.append("DISTRIBUTED BY HASH(");
                sqlBuilder.append("tenant_id" + ",");
                sqlBuilder.delete(sqlBuilder.length() - 1, sqlBuilder.length());
                sqlBuilder.append(")").append(" ").append("BUCKETS 32").append("\n").append("PROPERTIES(\n");
                sqlBuilder.append( "\"replication_num\" = \"3\"");
                    sqlBuilder.append( ",\n\"colocate_with\" = \"tenant_id\"");
                if("null"!=YmlUtil.get(tableName + ".bloom_filter_columns")&&null != YmlUtil.get(tableName + ".bloom_filter_columns")){
                    sqlBuilder.append( ",\n\"bloom_filter_columns\" = \""+YmlUtil.get(tableName + ".bloom_filter_columns")+"\"");
                }
                sqlBuilder.append(")");
                prefixBuilder.append(sqlBuilder);
                createSqlList.add(prefixBuilder.toString());
                finalSqlBuilder.append(prefixBuilder + ";\n\n");

            }

//            System.out.println("最后的SQL = " + createSqlList.toString());
            for (int i = 0; i < tables.size(); i++) {
                if (i<tables.size()-1){
                    System.out.print(tables.get(i)+",");
                }else {
                    System.out.print(tables.get(i));
                }

            }
            System.out.println();
            for (int i = 0; i < dbtables.size(); i++) {
                if (i<dbtables.size()-1){
                    System.out.print(dbtables.get(i)+",");
                }else {
                    System.out.print(dbtables.get(i));
                }
            }
            System.out.println();
            System.out.println("============================================");
            System.out.println(finalSqlBuilder);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void appendPartition(StringBuilder sqlBuilder, String partition, HashMap<String, String> columnTypes) {
        sqlBuilder.append("PARTITION BY RANGE(`" + partition + "`)\n");
        sqlBuilder.append("(");
        if(columnTypes.get(partition).contains("int")){
            if(partition.contains("year")){
                int first=2020;
                for (int i = 0; i < 20; i++) {
                    sqlBuilder.append("PARTITION p" + first + " VALUES  LESS THAN (\"" + first + "\"),\n");
                    first+=1;
                }
            }else {
                int first=1024*8;
                for (int i = 0; i < 20; i++) {
                    sqlBuilder.append("PARTITION p" + first + " VALUES  LESS THAN (\"" + first + "\"),\n");
                    first+=1024*8;
                }
            }

        }else if (columnTypes.get(partition).contains("time")){
            LocalDateTime start = LocalDateTime.of(2021, 1, 1, 0, 0, 0);

            for (int i = 0; i < 37; i++) {
                String s = start.format(DateTimeFormatter.ISO_LOCAL_DATE).replaceAll("-", "_");
                sqlBuilder.append("PARTITION p" + s + " VALUES  LESS THAN (\"" + start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\"),\n");
                start = start.plusMonths(1);
            }
        }else if(columnTypes.get(partition).contains("date")){
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
            System.out.println("XXXX = " + ReadSqlFileCreateDorisSqlResult.class.getClassLoader().getResource("").getPath());
            fileName = ReadSqlFileCreateDorisSqlResult.class.getClassLoader().getResource("").getPath() + fileName;
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
}
