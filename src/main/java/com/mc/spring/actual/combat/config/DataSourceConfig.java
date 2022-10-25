//package com.mc.spring.actual.combat.config;
//
//import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
//import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
//import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import javax.sql.DataSource;
//
///**
// * @author macheng
// * @date 2022/8/5 14:36
// */
//@Configuration
//public class DataSourceConfig {
//
//    @Autowired
//    private DataSource dataSource;
//    @Autowired
//    private DefaultDataSourceCreator dataSourceCreator;
//    /**
//     *         avatica:
//     *           driver-class-name: org.apache.calcite.avatica.remote.Driver
//     *           url: jdbc:avatica:remote:url=http://localhost:8765;authentication=DIGEST;serialization=PROTOBUF
//     *           avatica_user: root
//     *           avatica_password: password5
//     */
//    @Bean(name = "avaticaDataSource")
//    DataSource avaticaDataSource(){
//        DataSourceProperty dataSourceProperty = new DataSourceProperty();
//        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
//        DataSource dataSource = dataSourceCreator.createDataSource(dataSourceProperty);
//        ds.addDataSource("avatica", dataSource);
//        return null;
//    }
//
//}
