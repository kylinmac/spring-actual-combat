package com.mc.spring.actual.combat;

import org.apache.calcite.avatica.jdbc.JdbcMeta;
import org.apache.calcite.avatica.remote.Driver;
import org.apache.calcite.avatica.remote.LocalService;
import org.apache.calcite.avatica.server.HttpServer;
import org.apache.calcite.avatica.util.Sources;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringActualCombatApplication {

    public static void main(String[] args) {

        SpringApplication.run(SpringActualCombatApplication.class, args);

//        final String userPropertiesFile = Sources.of(MetaDataApplication.class
//                .getResource("/auth-users.properties")).file().getAbsolutePath();
//        int port = 8765;
//        String url = "jdbc:mysql://39.98.118.218:30003/hjy";
//        final JdbcMeta meta = new JdbcMeta(url, "hjy_admin", "123456");
//        final LocalService service = new LocalService(meta);
//        System.out.println(userPropertiesFile);
//        final HttpServer server = new HttpServer.Builder<>().withPort(port).withHandler(service, Driver.Serialization.PROTOBUF).withDigestAuthentication(userPropertiesFile, new String[]{"users", "admins"}).build();
//        server.start();
//        server.join();
    }

}
