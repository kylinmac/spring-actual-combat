package com.mc.spring.actual.combat.service;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author macheng
 * @date 2022/1/24 14:03
 */
public class ReadSqlFileCreateDorisODBCSqlResult {
    public static void main(String[] args) throws JSQLParserException {


        try {
            List<String> list = ReadSqlFileCreateDorisODBCSqlResult.readFile("tpcds.sql");
//            List<String> list = SqlFileUtil2.readFile("q2.sql");


            //todo 最后输出格式一
            List<String> createSqlList = new ArrayList<>();

            //todo 最后输出格式二，字符串
            StringBuilder finalSqlBuilder = new StringBuilder();
            finalSqlBuilder.hashCode();
            for (String sql : list) {
                //todo 字段到结尾
                StringBuilder sqlBuilder = new StringBuilder();

                //todo 建表语句+主键
                StringBuilder prefixBuilder = new StringBuilder();

                CCJSqlParserManager parser = new CCJSqlParserManager();
                List<ColumnDefinition> columnList = null; //todo 列 字段名称跟信息。
                String tableName = ""; //todo 表名字
                List<String> pkColumnsList = new ArrayList<>() ; //todo 主键名称

                if(sql.contains("AUTO_INCREMENT")){
                    sql = sql.replaceAll("AUTO_INCREMENT","");
                }
                Statement stmt = parser.parse(new StringReader(sql));
                if (stmt instanceof CreateTable) {
                    tableName= ((CreateTable) stmt).getTable().getName();
                    List<Index> indexes = ((CreateTable) stmt).getIndexes();
                    if (indexes!=null&&!indexes.isEmpty()){
                        pkColumnsList = ((CreateTable) stmt).getIndexes().get(0).getColumnsNames();
                    }

                    columnList = ((CreateTable) stmt).getColumnDefinitions();
                }

                //todo 创建语句
                prefixBuilder.append("Drop table if exists ").append(tableName+";\n").append("CREATE TABLE ")
                        .append(tableName)
                        .append(" ")
                        .append("(").append("\n");

                if (columnList==null){
                    continue;
                }
                System.out.println(tableName);
                //todo 先将主键字段放在第一位
                List<ColumnDefinition> arrayList=new ArrayList<>();

                Iterator<ColumnDefinition> iterator = columnList.iterator();
                while (iterator.hasNext()){
                    ColumnDefinition next = iterator.next();
                    if (next.toString().contains("tenant_id")){
                        arrayList.add(next);
                        iterator.remove();
                    }
                }
                iterator = columnList.iterator();
                while (iterator.hasNext()){
                    ColumnDefinition next = iterator.next();
                    if (next.toString().contains("event_day")){
                        arrayList.add(next);
                        iterator.remove();
                    }
                }
                iterator = columnList.iterator();
                while (iterator.hasNext()){
                    ColumnDefinition next = iterator.next();
                    if (next.toString().contains("gather_id")){
                        arrayList.add(next);
                        iterator.remove();
                    }
                }
                iterator = columnList.iterator();
                while (iterator.hasNext()){
                    ColumnDefinition next = iterator.next();
                    if (next.toString().contains("account_type")){
                        arrayList.add(next);
                        iterator.remove();
                    }
                }
                iterator = columnList.iterator();
                while (iterator.hasNext()){
                    ColumnDefinition next = iterator.next();
                    if (next.toString().contains("shop_id")){
                        arrayList.add(next);
                        iterator.remove();
                    }
                }
                iterator = columnList.iterator();
                while (iterator.hasNext()){
                    ColumnDefinition next = iterator.next();
                    if (next.toString().contains("bill_name")){
                        arrayList.add(next);
                        iterator.remove();
                    }
                }
                iterator = columnList.iterator();
                while (iterator.hasNext()){
                    ColumnDefinition next = iterator.next();
                    if (next.toString().contains("shop_name")){
                        arrayList.add(next);
                        iterator.remove();
                    }
                }
                iterator = columnList.iterator();
                while (iterator.hasNext()){
                    ColumnDefinition next = iterator.next();
                    if (next.toString().contains("order_id")){
                        arrayList.add(next);
                        iterator.remove();
                    }
                }
                iterator = columnList.iterator();
                while (iterator.hasNext()){
                    ColumnDefinition next = iterator.next();
                    if (next.toString().contains("trade_order_no")){
                        arrayList.add(next);
                        iterator.remove();
                    }
                }
                iterator = columnList.iterator();
                while (iterator.hasNext()){
                    ColumnDefinition next = iterator.next();
                    if (next.toString().contains("primary key")){
                        arrayList.add(next);
                        iterator.remove();
                    }
                }
                arrayList.addAll(columnList);
                columnList=arrayList;
                //todo 全部变成varchar()
                for (int i = 0; i < columnList.size(); i++) {
                    String columnString = columnList.get(i).toString();
                    String columnName = columnList.get(i).getColumnName();
                    ColDataType columnDataType = columnList.get(i).getColDataType();
                    //todo 字段的类型
                    String dataType = columnDataType.getDataType();

//                    //todo  是主键的情况下 主键放最前面。
//                    if (pkColumnsList.contains(columnName)){
//                        if ("varchar".equals(dataType)){
//                            List<String> argumentsStringList = columnDataType.getArgumentsStringList();
//                            Integer typeValueTriple = Integer.valueOf(argumentsStringList.get(0)) * 5;
//                            prefixBuilder.append(columnName).append(" ").append(dataType).append("(").append(typeValueTriple).append(")");
//
//                        }else if("datetime".equals(dataType)|| "date".equals(dataType)){
//                            prefixBuilder.append(columnName).append(" ").append(dataType);
//                        }else {
//                            prefixBuilder.append(columnString);
//                        }
//                        prefixBuilder.append(",").append("\n");
//                    }else {
                        //todo 如果是varchar 类型就放大三倍
                        if ("varchar".equals(dataType)){
                            List<String> argumentsStringList = columnDataType.getArgumentsStringList();
                            Integer typeValueTriple = Integer.valueOf(argumentsStringList.get(0)) * 3;

                            sqlBuilder.append(columnName).append(" ").append(dataType).append("(").append(typeValueTriple).append(")");

                        }else {
                            if (columnString.contains("primary key")){
                                columnString=columnString.replaceAll("primary key","");
                            }
                            if (columnString.contains("auto_increment")){
                                columnString=columnString.replaceAll("auto_increment","");
                            }
                            if (columnString.contains(" unsigned ")){
                                columnString=columnString.replaceAll(" unsigned "," ");
                            }
                            if (columnString.contains("zerofill")){
                                columnString=columnString.replaceAll("zerofill","");
                            }

                            if ("timestamp".equals(dataType)){
                                dataType="datetime";
                            }
                            sqlBuilder.append(columnName).append(" ").append(dataType);
                            if (columnString.contains("not null")){
                                sqlBuilder.append(" not null");
                            }
                            String[] s = columnString.split(" ");
                            if (columnString.contains("default")){
                                sqlBuilder.append(" default ");
                                for (int j = 0; j < s.length; j++) {
                                    if ("default".equals(s[j])){
                                        sqlBuilder.append(s[j+1]);
                                        break;
                                    }
                                }
                            }

                            if (columnString.contains("comment")){
                                sqlBuilder.append(" comment ");
                                O:
                                for (int j = 0; j < s.length; j++) {
                                    if ("comment".equals(s[j])){
                                        for (int k = j+1; k < s.length; k++) {
                                            sqlBuilder.append(s[k]);
                                            if (s[k].endsWith("'"))
                                            break O;
                                        }

                                    }
                                }
                            }



                        }
                        sqlBuilder.append(",").append("\n");




                }
                if (sqlBuilder.length()>0){
                    sqlBuilder.delete(sqlBuilder.length()-2, sqlBuilder.length());
                    sqlBuilder.append("\n").append(")").append("  ENGINE=ODBC\n");
                }else {
                    //todo 字段全是主键
                    System.out.println("111111 = " + 111111);
                }

            sqlBuilder.append("");


                prefixBuilder.append(sqlBuilder);
                createSqlList.add(prefixBuilder.toString());
                finalSqlBuilder.append(prefixBuilder+";\n\n");
//                finalSqlBuilder.append("ALTER TABLE "+tableName+" ENABLE FEATURE \"BATCH_DELETE\";\n");

            }

//            System.out.println("最后的SQL = " + createSqlList.toString());
            System.out.println("============================================");
            System.out.println( finalSqlBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static List<String> readFile(String fileName) throws IOException {
        // list store return sql
        List<String> sqlList = new ArrayList<String>();
        // add widonw path
        if (File.separator.equals("\\")) {
            System.out.println("XXXX = " + ReadSqlFileCreateDorisODBCSqlResult.class.getClassLoader().getResource("").getPath());
            fileName = ReadSqlFileCreateDorisODBCSqlResult.class.getClassLoader().getResource("").getPath() + fileName;
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
            if (StringUtils.isEmpty(line) || line.trim().startsWith("--")||line.trim().startsWith("DROP")) {
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
