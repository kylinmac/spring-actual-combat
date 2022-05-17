package com.mybatis.method;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

/**
 * @author macheng
 * @date 2021/5/19 13:24
 */
public class UpdateByPrimaryKeySelectiveMethod extends AbstractMethod {


    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        final String sql = "<script>update %s  %s  where  %s = %s </script>";
        final String tableName = tableInfo.getTableName();
        final String setStatement = prepareSetStatement(tableInfo);
        final String sqlStatement = String.format(sql, tableName, setStatement, tableInfo.getKeyColumn(), tableInfo.getKeyProperty());
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sqlStatement, modelClass);
        return this.addInsertMappedStatement(mapperClass, modelClass, "updateByPrimaryKeySelective", sqlSource, new NoKeyGenerator(), null, null);
    }


    private String prepareSetStatement(TableInfo tableInfo) {
        StringBuilder setColumn = new StringBuilder();
        setColumn.append(" <set> ");
        tableInfo.getFieldList().forEach(
                field -> {
                    setColumn.append(String.format(" <if test=\"%s != null\"> ", field.getProperty()));
                    setColumn.append(String.format(" %s = %s ,", field.getColumn(), field.getProperty()));
                    setColumn.append(" </if> ");
                }
        );
        setColumn.append(" </set> ");
        return setColumn.toString();
    }


}
