package com.mc.spring.actual.combat;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;

/**
 * @author macheng
 * @date 2022/2/19 9:30
 */
public class TestJoin {


    public static void main(String[] args) throws InterruptedException, IOException {
        final Properties p = new Properties();
        p.put("avatica_user", "root");
        p.put("avatica_password", "password5");
        p.put("serialization", "protobuf");
        try (Connection conn = DriverManager.getConnection("jdbc:avatica:remote:url=http://localhost:8765;" + "authentication=DIGEST", p)) {
            final Statement statement = conn.createStatement();
            final ResultSet rs = statement.executeQuery("SHOW tables");
            rs.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
