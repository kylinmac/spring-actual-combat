package com.mc.spring.actual.combat.utils;

import com.mc.spring.actual.combat.mapper.CreateTableMapper;
import com.mc.spring.actual.combat.model.ColumnEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author macheng
 * @date 2022/7/6 16:26
 */
@Service
public class FlinkSqlUtils {
    @Autowired
    CreateTableMapper createTableMapper;
    public  String getMysqlFlinkType(String sql, Map<String,String> tableMap ,String schema){
        HashMap<String, String> colsType = new HashMap<>();

        tableMap.entrySet().forEach(x->{
            createTableMapper.getColumnInfo(x.getKey(),schema);

        });

        String[] split = sql.trim().replace("\n", "").split(",");
        int i=0;
//        for (String s : split) {
//            String[] st = s.split("\\.");
//            if (st.length==0){
//                continue;
//            }
//            i++;
//            System.out.println(s);
//            switch (st[0].trim()){
//                case "po":
//                    sb.append(t1.get(st[1])).append(",");
//                    if (t1.get(st[1]).equals("timestamp")){
//                        sbTime.append(s).append(",");
//                    }
//                    break;
//                case "poc":
//                    sb.append(t2.get(st[1])).append(",");
//                    if (t2.get(st[1]).equals("timestamp")){
//                        sbTime.append(s).append(",");
//                    }
//                    break;
//                default:
//
//            }
//        }
//        sb.append("Long");
//        String replace = sb.toString().replace("bigint unsigned","BigInteger").replace("bigint", "Long").replace("varchar", "String")
//                .replace("datetime", "Date").replace("smallint", "Integer")
//                .replace("tinyint", "Integer").replace("int", "Integer")
//                .replace("decimal","BigDecimal").replace("timestamp","String");
//        System.out.println(i);
//        System.out.println(replace);
//        System.out.println(sbTime);
return "";
    }
}
