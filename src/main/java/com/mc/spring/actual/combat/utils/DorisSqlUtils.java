package com.mc.spring.actual.combat.utils;

import com.alibaba.fastjson.JSONObject;
import com.mc.spring.actual.combat.model.DescEntity;
import com.mc.spring.actual.combat.service.ReadSqlFileCreateDorisSqlResult;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author macheng
 * @date 2022/6/27 14:24
 */
public class DorisSqlUtils {
    static HashMap<String, Long> tenantRange = new HashMap<>();

    public static void main(String[] args) throws IOException {

//        getCreateTableSql();

        //pickSlowLog("slow_0630.log");
        processShowCreateTableFile("new_partition_table.sql");
//        getAlterPartition("tables.txt");

    }

    public static HashMap<String,String> processDesc(String tableName,String descName) throws IOException {
        HashMap<String, String> map = new HashMap<>();
        List<String> tables = FileUtils.getFileString(tableName);
        List<String> descString = FileUtils.getFileString(descName);
        for (int i = 0,j=0; i < tables.size(); i++) {
            StringBuilder sb=new StringBuilder();
            while (++j<descString.size()&&!descString.get(j).equals("Field")){
                sb.append(descString.get(j)).append(",");
            }
            sb.deleteCharAt(sb.length()-1);
            map.put(tables.get(i),sb.toString());
        }
        for (String table : tables) {
            System.out.println(table);
            System.out.println(map.get(table));
        }
        return map;
    }
    public static void processShowCreateTableFile(String fileName) throws IOException {


        HashMap<String, String> map = processDesc("tables.txt", "tablesDesc.txt");

        FileUtils.writeString( FileUtils.getFileString(fileName, x -> !x.startsWith("Table"),
                x ->{
                    String[] split = x.split("CREATE TABLE");
                    String table = split[0].trim().replace("'", "");

                    if(split.length==1){
                        System.out.println(x);
                    }
                    String create = ("CREATE TABLE" + split[1] + "\n")
                            .replace(table,table+"_mp")
                            .replace("OLAP","OLAP ")
                            .replace("\"replication_allocation\" = \"tag.location.default: 3\"","\"replication_allocation\" = \"tag.location.default: 1\"")
                            .replace("PROPERTIES ("," PROPERTIES (")
                            .replace("PARTITION p12","PARTITION p20").replace("[(\"12\"), (\"13\"))","[(\"20\"), (\"21\"))").replace("(\"12\",","(\"20\",")
                            .replace("PARTITION p13","PARTITION p23").replace("[(\"13\"), (\"14\"))","[(\"23\"), (\"24\"))").replace("(\"13\",","(\"23\",")
                            .replace("PARTITION p30","PARTITION p50").replace("[(\"30\"), (\"31\"))","[(\"50\"), (\"51\"))").replace("(\"30\",","(\"50\",")
                            .replace("PARTITION p31","PARTITION p58").replace("[(\"31\"), (\"32\"))","[(\"58\"), (\"59\"))").replace("(\"31\",","(\"58\",");
                    String insert = "insert into " +table+"_mp" + " ("+map.get(table)+") select "+map.get(table)+" from "+table+";\n" ;
                    String select="select count(*) from "+table+"_mp;\n";
                    String selectO="select count(*) from "+table+";\n";

                    String renameOld = "alter table " + table + " rename " +table + "_op;\n";
                    String rename = "alter table " + table+"_mp" + " rename " +table + ";\n";


                    return create+insert+select+selectO+renameOld+rename;
                }),"new_partition.sql");
    }

    public static void pickSlowLog(String fileName) throws IOException {

        List<String> all = FileUtils.getFileString(fileName
                , x -> StringUtils.isNotEmpty(x) && !x.contains("root")&&x.contains("Stmt")&&!x.contains("mp")
                , x -> {
                    String[] split = x.split("[slow_query]");
                    String time = split[0];
                    String[] afterSql = split[1].split("Stmt=")[1].split("\\|CpuTimeMS=0");
                    String s = afterSql[0].stripTrailing();
                    if (s.charAt(s.length() - 1) == ';') {
                        return s + "\n";
                    }
                    return time+"\n"+s.substring(0, s.length() - 1) + ";\n";
                }
        );
        StringBuilder stringBuilder = new StringBuilder();
        for (String a : all) {
            stringBuilder.append(a);
        }
        FileUtils.writeString(stringBuilder.toString(), "slow.out");

    }

    private static void getAlterPartition(String fileName) throws IOException {

        Set<Integer> year = Set.of(41, 18, 2, 30, 12, 35, 1, 38);
        Set<Integer> halfYear = Set.of(11, 34, 40);
        Set<Integer> quarter = Set.of(31, 13, 36, 16, 37, 39);
        Set<Integer> month = Set.of(15);

        String[] tenantIds = "12,39,37,30,15,38,36,40,11,18,16,34,31,35,13,2,41,1".split(",");

        year.forEach(x -> tenantRange.put(x + "", 12L));
        halfYear.forEach(x -> tenantRange.put(x + "", 6L));
        quarter.forEach(x -> tenantRange.put(x + "", 3L));
        month.forEach(x -> tenantRange.put(x + "", 1L));

        List<String> tables = FileUtils.getFileString(fileName);
        ArrayList<String> all = new ArrayList<>();
        for (String tableName : tables) {
            String partitionCol = ((String) JSONObject.parseObject(YmlUtil.get(tableName)).getOrDefault("partitionCol", "")).trim();
            String partitionType = ((String) JSONObject.parseObject(YmlUtil.get(tableName)).getOrDefault("partitionType", "")).trim();
            String partitionRange = ((String) JSONObject.parseObject(YmlUtil.get(tableName)).getOrDefault("partitionRange", "")).trim();
            if (partitionCol.isBlank()) {
                all.addAll(getPartitionByTenantIdAndTable(tenantIds, tableName));
            } else {
                HashMap<String, String> partitionTypeMap = new HashMap<>();
                HashMap<String, String> partitionRangeMap = new HashMap<>();
                if (!partitionType.isBlank()) {
                    splitParam(partitionType, partitionTypeMap);
                } else {
                    partitionTypeMap.put("default", "月");
                }

                if (!partitionRange.isBlank()) {
                    splitParam(partitionRange, partitionRangeMap);
                } else {
                    partitionRangeMap.put("default", "2021-03-01,2023-01-01");
                }
                for (String tenantId : tenantIds) {
                    StringBuilder stringBuilder = new StringBuilder();
                    String pt = partitionTypeMap.getOrDefault(tenantId, partitionTypeMap.get("default"));
                    String pr = partitionRangeMap.getOrDefault(tenantId, partitionRangeMap.get("default"));
                    stringBuilder
                            .append(getPartition(pt, pr, partitionCol, tenantId));
                    String[] split = stringBuilder.toString().split("\n");
                    for (String s : split) {
                        all.add("ALTER TABLE " + tableName + " ADD "+s.substring(0,s.length()-1)+";\n");
                    }
                }

            }


        }
        FileUtils.writeString(all,"addPar.sql");

    }
    private static void getCreateTableSql() throws IOException {
        HashMap<String, String> createSql = getCreateSql("showcrt.out");
        HashMap<String, String> finalSql = new HashMap<>();

        StringBuilder total = new StringBuilder();
        StringBuilder totalInsert = new StringBuilder();
        Set<Integer> year = Set.of(41, 18, 2, 30, 12, 35, 1, 38);
        Set<Integer> halfYear = Set.of(11, 34, 40);
        Set<Integer> quarter = Set.of(31, 13, 36, 16, 37, 39);
        Set<Integer> month = Set.of(15);

        String[] tenantIds = "12,39,37,30,15,38,36,40,11,18,16,34,31,35,13,2,41,1".split(",");

        year.forEach(x -> tenantRange.put(x + "", 12L));
        halfYear.forEach(x -> tenantRange.put(x + "", 6L));
        quarter.forEach(x -> tenantRange.put(x + "", 3L));
        month.forEach(x -> tenantRange.put(x + "", 1L));


        for (Map.Entry<String, String> entry : createSql.entrySet()) {
            String tableName = entry.getKey();
            if (YmlUtil.get(tableName) == null) {
                continue;
            }
            System.out.println(tableName);
            String partitionCol = ((String) JSONObject.parseObject(YmlUtil.get(tableName)).getOrDefault("partitionCol", "")).trim();
            String partitionType = ((String) JSONObject.parseObject(YmlUtil.get(tableName)).getOrDefault("partitionType", "")).trim();
            String partitionRange = ((String) JSONObject.parseObject(YmlUtil.get(tableName)).getOrDefault("partitionRange", "")).trim();
            String bucketCol = ((String) JSONObject.parseObject(YmlUtil.get(tableName)).getOrDefault("bucketCol", "")).trim();
            String sql = entry.getValue();
            String oldPartition = "";
            String[] split = sql.split("PARTITION BY");
            StringBuilder stringBuilder = new StringBuilder();
            if (split.length == 1) {
                split = sql.split("DISTRIBUTED");
            } else {
                StringBuilder sbop = new StringBuilder();

                boolean flag = false;
                for (int i = 0; i < split[1].length(); i++) {
                    if (')' == split[1].charAt(i)) {
                        break;
                    }

                    if (flag) {
                        sbop.append(split[1].charAt(i));
                    }

                    if ('(' == split[1].charAt(i)) {
                        flag = true;
                    }

                }
                oldPartition = sbop.toString();
            }
            String tableNamePartition = tableName + "_mp";

            stringBuilder.append(split[0].replace(tableName, tableNamePartition)
                    .replace("   ", "\n")
                    .replace("ENGINE", "\nENGINE")
                    .replace("OLAP", "OLAP\n"));
            if (partitionCol.isBlank()) {
                bucketCol = "tenant_id";
                stringBuilder.append("\n PARTITION BY RANGE(tenant_id)(\n");
                getPartitionBytenantId(tenantIds, stringBuilder);
                stringBuilder.deleteCharAt(stringBuilder.length() - 2);
                stringBuilder.append(")\n");
                stringBuilder.append("DISTRIBUTED BY HASH(" + bucketCol + ") BUCKETS 4\n");
            } else {
                HashMap<String, String> partitionTypeMap = new HashMap<>();
                HashMap<String, String> partitionRangeMap = new HashMap<>();
                if (!partitionType.isBlank()) {
                    splitParam(partitionType, partitionTypeMap);
                } else {
                    partitionTypeMap.put("default", "月");
                }

                if (!partitionRange.isBlank()) {
                    splitParam(partitionRange, partitionRangeMap);
                } else {
                    partitionRangeMap.put("default", "2021-01-01,2023-01-01");
                }
                stringBuilder.append("\n PARTITION BY RANGE(tenant_id,").append(partitionCol).append(")(\n");
                for (String tenantId : tenantIds) {
                    String pt = partitionTypeMap.getOrDefault(tenantId, partitionTypeMap.get("default"));
                    String pr = partitionRangeMap.getOrDefault(tenantId, partitionRangeMap.get("default"));
                    stringBuilder
                            .append(getPartition(pt, pr, partitionCol, tenantId));
                }
                stringBuilder.deleteCharAt(stringBuilder.length() - 2);
                stringBuilder.append(")\n");
                stringBuilder.append("DISTRIBUTED BY HASH(" + bucketCol + ") BUCKETS 4\n");
            }
            String[] prop = split[1].split("PROPERTIES");
            String properties = prop[1].replace("\"colocate_with\" = \"tenant_id\"", "\"colocate_with\" = \"" + bucketCol + "\"")
                    .replaceAll("\",", "\",\n").replace(" \"colocate_with\" = \"tenant_id\",", "");
            stringBuilder.append(" PROPERTIES \n").append(properties);
            stringBuilder.append("\n");
//            FileUtils.writeString(stringBuilder.toString(),"C:\\Data\\IdeaProjects\\spring-actual-combat\\createSql\\"+tableName+".sql");
            StringBuilder insertSql = new StringBuilder();
            getInsertSql(tenantIds, tableName, tableNamePartition, oldPartition, insertSql);
            total.append(stringBuilder);
            if (insertSql.length() > 0) {
//                FileUtils.writeString(insertSql.toString(),"C:\\Data\\IdeaProjects\\spring-actual-combat\\insertSql\\"+tableName+".sql");
            }


            finalSql.put(tableName, stringBuilder.toString());

//            if (tables.contains(tableName)) {
//                totalInsert.append(insertSql);
//                String sqlFile = "insert into hjy_dw." + tableNamePartition + " select * from " + tableName + "_bak where tenant_id={tenant_id} and " + partitionCol + " between '{process_date}' and '{process_date}'";
//                FileUtils.writeString(sqlFile, "C:\\Data\\IdeaProjects\\spring-actual-combat\\insertSql\\" + tableName + ".sql");
//
//            }

        }


        FileUtils.writeString(total.toString(), "C:\\Data\\IdeaProjects\\spring-actual-combat\\createSql\\total.sql");
        FileUtils.writeString(totalInsert.toString(), "C:\\Data\\IdeaProjects\\spring-actual-combat\\insertSql\\totalInsert.sql");
    }

    private static void getPartitionBytenantId(String[] tenantIds, StringBuilder stringBuilder) {
        for (String tenantId : tenantIds) {

            stringBuilder.append("PARTITION p").append(tenantId)
                    .append(" VALUES [(\"").append(tenantId)
                    .append("\"),(\"").append(Integer.parseInt(tenantId) + 1)
                    .append("\")),\n");
        }
    }


    private static List<String> getPartitionByTenantIdAndTable(String[] tenantIds, String tableNam) {
        ArrayList<String> objects = new ArrayList<>();
        for (String tenantId : tenantIds) {

            objects.add("ALTER TABLE" + tableNam + " ADD PARTITION p" + tenantId +
                    " VALUES [(\"" + tenantId +
                    "\"),(\"" + (Integer.parseInt(tenantId) + 1) +
                    "\"));\n");

        }
        return objects;
    }

    private static void getInsertSql(String[] tenantIds, String tableName, String tableNamePartition, String oldPartition, StringBuilder insertSql) {

        insertSql.append("select count(*),tenant_id from " + tableName + "_bak group by tenant_id ;\n");
//        insertSql.append("ALTER TABLE " + tableName + " RENAME " + tableName + "_bak;\n");


        for (String tenantId : tenantIds) {
            if (oldPartition.isBlank()) {
//                insertSql.append(" insert into ").append(tableNamePartition)
                // .append(" select * from ").append(tableName).append(" where tenant_id=").append(tenantId)
                //  .append(";\n");
            } else {
//                insertSql.append(" insert into ").append(tableNamePartition)
                //   .append(" select * from ").append(tableName).append(" where tenant_id=").append(tenantId)
                //  .append(";\n");
            }
            //   insertSql.append("select count(*) from " + tableNamePartition + " where tenant_id = "+tenantId+" ;\n");
        }
        insertSql.append(" insert into ").append(tableNamePartition)
                .append(" select * from ").append(tableName).append("_bak limit 100;\n");
        insertSql.append("select count(*) from " + tableNamePartition + ";\n");
//        insertSql.append("ALTER TABLE " + tableNamePartition + " RENAME " + tableName + ";\n");
        insertSql.append("select count(*),tenant_id from " + tableName + "_bak group by tenant_id ;\n\n");
//        insertSql.append("DROP TABLE " + tableName + "_bak; \n");
    }

    private static void splitParam(String partitionRange, HashMap<String, String> partitionRangeMap) {
        String[] spt = partitionRange.split(";");
        for (String s : spt) {
            String[] st = s.split(":");
            if (st.length > 1) {
                String[] tenants = st[0].split(",");
                for (String tenant : tenants) {
                    partitionRangeMap.put(tenant, st[1]);
                }
            } else {
                partitionRangeMap.put("default", st[0]);
            }
        }
    }

    public static String getPartition(String pt, String pr, String col, String tenantId) {
        String[] ts = pt.split(",");
        int length = 1;
        String type;
        if (ts.length > 1) {
            length = Integer.parseInt(ts[0]);
            type = ts[1];
        } else {
            type = ts[0];
        }
        String[] rs = pr.split(",");
        StringBuilder ps = new StringBuilder();

        switch (type) {
            case "年" -> getByYear(col, tenantId, length, rs, ps, dateTimeFormatter);

            case "月" -> getByMonth(col, tenantId, length, rs, ps, dateTimeFormatter);

            case "天" -> getByDay(col, tenantId, length, rs, ps, dateTimeFormatter);

            case "年-月" -> getByYearAndMonthNumber(tenantId, pr, ps);
            default -> {
            }
        }

        return ps.toString();

    }

    static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00");

    private static void getByYearAndMonthNumber(String tenantId, String pr, StringBuilder ps) {
        String[] rs = pr.replace("(", "").replace(")", "").split("-");
        String[] left = rs[0].split(",");
        String[] right = rs[1].split(",");
        int lengthR = tenantRange.get(tenantId).intValue();
        for (String s : tenantId.split(",")) {
            A:
            for (int i = Integer.parseInt(left[0]); i <= Integer.parseInt(right[0]); i++) {
                B:
                for (int j = 1; j < 13; j += lengthR) {
                    if (i == Integer.parseInt(left[0]) && j < ((Integer.parseInt(left[1]) + lengthR - 1) / lengthR - 1) * lengthR + 1) {
                        continue B;
                    }
                    ps.append("PARTITION p" + s + "_" + i + "_" + j + " VALUES   [(\"" + s + "\",\"")
                            .append(i + "\",\"").append(j + "\"),(\"")
                            .append(s).append("\",\"")
                            .append(i + "\",\"").append((j + lengthR) + "\")")
                            .append("),\n");
                    if (i == Integer.parseInt(right[0]) && j >= Integer.parseInt(right[1])) {
                        break A;
                    }
                }

            }
        }

    }


    private static void getByDay(String col, String tenantId, int length, String[] rs, StringBuilder ps, DateTimeFormatter datef) {
        if (col.contains("time")) {
            LocalDateTime start = LocalDateTime.parse(rs[0] + " 00:00:00", timeFormatter);
            LocalDateTime end = LocalDateTime.parse(rs[1] + " 00:00:00", timeFormatter);
            while (!start.isAfter(end)) {

                String s = start.format(DateTimeFormatter.ISO_LOCAL_DATE).replaceAll("-", "_");
                ps.append("PARTITION p" + tenantId + "_" + s + " VALUES   [(\"" + tenantId + "\",\"")
                        .append(start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\"")
                        .append("),(\"").append(tenantId).append("\",\"")
                        .append(start.plusDays(length).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\")")
                        .append("),\n");
                start = start.plusDays(length);
            }
        } else {
            LocalDate start = LocalDate.parse(rs[0], datef);
            LocalDate end = LocalDate.parse(rs[1], datef);
            while (!start.isAfter(end)) {
                String s = start.format(DateTimeFormatter.ISO_LOCAL_DATE).replaceAll("-", "_");
                ps.append("PARTITION p" + tenantId + "_" + s + " VALUES   [(\"" + tenantId + "\",\"")
                        .append(start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\"")
                        .append("),(\"").append(tenantId).append("\",\"")
                        .append(start.plusDays(length).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\")")
                        .append("),\n");
                start = start.plusDays(length);
            }
        }
    }

    private static void getByMonth(String col, String tenantId, int length, String[] rs, StringBuilder ps, DateTimeFormatter datef) {

        int lengthR = tenantRange.get(tenantId).intValue();
        if (col.contains("time")) {
            LocalDateTime start = LocalDateTime.of(LocalDate.parse(rs[0], datef).getYear(), ((LocalDate.parse(rs[0], datef).getMonth().getValue() + lengthR - 1) / lengthR - 1) * lengthR + 1, 1, 0, 0, 0);
            LocalDateTime end = LocalDateTime.of(LocalDate.parse(rs[1], datef).getYear(), LocalDate.parse(rs[1], datef).getMonth(), 1, 0, 0, 0, 0);

            while (!start.isAfter(end)) {

                String s = start.format(DateTimeFormatter.ISO_LOCAL_DATE).replaceAll("-", "_");
                ps.append("PARTITION p" + tenantId + "_" + s + " VALUES   [(\"" + tenantId + "\",\"")
                        .append(start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\"")
                        .append("),(\"").append(tenantId).append("\",\"")
                        .append(start.plusMonths(tenantRange.get(tenantId)).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\")")
                        .append("),\n");
                start = start.plusMonths(tenantRange.get(tenantId));

            }
        } else {
            LocalDate start = LocalDate.of(LocalDate.parse(rs[0], datef).getYear(), ((LocalDate.parse(rs[0], datef).getMonth().getValue() + lengthR - 1) / lengthR - 1) * lengthR + 1, 1);
            LocalDate end = LocalDate.of(LocalDate.parse(rs[1], datef).getYear(), LocalDate.parse(rs[1], datef).getMonth(), 1);
            while (!start.isAfter(end)) {

                String s = start.format(DateTimeFormatter.ISO_LOCAL_DATE).replaceAll("-", "_");
                ps.append("PARTITION p" + tenantId + "_" + s + " VALUES   [(\"" + tenantId + "\",\"")
                        .append(start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\"")
                        .append("),(\"").append(tenantId).append("\",\"")
                        .append(start.plusMonths(tenantRange.get(tenantId)).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\")")
                        .append("),\n");
                start = start.plusMonths(tenantRange.get(tenantId));
            }
        }
    }

    private static void getByYear(String col, String tenantId, int length, String[] rs, StringBuilder ps, DateTimeFormatter datef) {
        if (col.contains("time")) {
            LocalDateTime start = LocalDateTime.of(LocalDate.parse(rs[0], datef).getYear(), 1, 1, 0, 0, 0, 0);
            LocalDateTime end = LocalDateTime.of(LocalDate.parse(rs[1], datef).getYear(), 1, 1, 0, 0, 0, 0);
            while (!start.isAfter(end)) {

                String s = start.format(DateTimeFormatter.ISO_LOCAL_DATE).replaceAll("-", "_");
                ps.append("PARTITION p" + tenantId + "_" + s + " VALUES   [(\"" + tenantId + "\",\"")
                        .append(start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\"")
                        .append("),(\"").append(tenantId).append("\",\"")
                        .append(start.plusYears(length).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\")")
                        .append("),\n");
                start = start.plusYears(length);

            }
        } else {
            LocalDate start = LocalDate.of(LocalDate.parse(rs[0], datef).getYear(), 1, 1);
            LocalDate end = LocalDate.of(LocalDate.parse(rs[1], datef).getYear(), 1, 1);
            while (!start.isAfter(end)) {

                String s = start.format(DateTimeFormatter.ISO_LOCAL_DATE).replaceAll("-", "_");
                ps.append("PARTITION p" + tenantId + "_" + s + " VALUES   [(\"" + tenantId + "\",\"")
                        .append(start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\"")
                        .append("),(\"").append(tenantId).append("\",\"")
                        .append(start.plusYears(length).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\")")
                        .append("),\n");
                start = start.plusYears(length);

            }
        }
    }


    public static HashMap<String, String> getCreateSql(String fileName) throws IOException {
        // list store return sql
        HashMap<String, String> map = new HashMap<>();
        // add widonw path
        if (File.separator.equals("\\")) {
            fileName = ReadSqlFileCreateDorisSqlResult.class.getClassLoader().getResource("").getPath() + fileName;
        }
        File file = new File(fileName);
        // check file exists
        if (!file.exists()) {
            System.out.println("File not found: " + fileName);
            System.exit(-1);
        }
        // read file
        try ( BufferedReader br = new BufferedReader(new FileReader(file))){
            String line;
            while ((line = br.readLine()) != null) {
                // ignore empty line and comment line
                if (StringUtils.isEmpty(line) || line.trim().startsWith("--") || line.trim().startsWith("DROP") || line.trim().startsWith("Table")) {
                    continue;
                }

                String[] split = line.split("CREATE TABLE");

                map.put(split[0].trim(), String.join(" ", "CREATE TABLE", split[1].replace("tag.location.default: 1", "tag.location.default: 3")));
            }
            return map;
        }


    }

    public static String renameSql(String source,String target){
        return "alter table "+source+" rename "+target+";";
    }


    public static String insertSql(String source,String cols,String target){
        return "insert into "+source+"("+cols+")"+" select "+cols+target+";";
    }
    public static String insertSql(String source,List<DescEntity> descEntities,String target){
        String cols = flatDesc(descEntities);
        return "insert into "+source+" ("+cols+") "+" select "+cols+" from "+target+";";
    }
    public static String flatDesc(List<DescEntity> descEntities){

        List<String> cols = descEntities.stream().map(DescEntity::getField).toList();
        String[] array=new String[cols.size()];
        cols.toArray(array);
        return String.join(",",array);

    }
}
