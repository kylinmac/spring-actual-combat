package com.mc.spring.actual.combat.mybatis.handler;

import com.mc.spring.actual.combat.constant.enums.BaseEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**mybatis 把value转换成具体枚举的处理器*/
@MappedTypes(value = BaseEnum.class)
public class EnumTypeHandler<T extends BaseEnum> extends BaseTypeHandler<BaseEnum> {
    private Class<T> type;
 
    public EnumTypeHandler(Class<T> type) {
        this.type = type;
    }
 
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, BaseEnum parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getValue());
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return convert(rs.getInt(columnName));
    }
 
    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return convert(rs.getInt(columnIndex));
    }
 
    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return convert(cs.getInt(columnIndex));
    }
 
    private T convert(int value) {
        return BaseEnum.getEnum(type, value);
    }
}