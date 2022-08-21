package com.mc.spring.actual.combat.model;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author macheng
 * @date 2022/6/21 14:53
 */
@Data
@Builder
@Accessors(chain = true)
public class MysqlConnection {
    private String host;
    private String port;
    private String user;
    private String password;
    private String database;
    private String table;

}
