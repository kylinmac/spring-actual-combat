package com.mc.spring.actual.combat.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author macheng
 * @date 2021-12-03
 */

@Data
@TableName("canal_test")
@Accessors(chain = true)
public class TableNineEntity implements Serializable {

    private static final long serialVersionUID = 1043548877155608549L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String cKey;
    private String cValue;


	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;
//
//	private String rowKey;
//
//	private String rowValue;
//
//	private Integer isDel;
//
//	private String rowLeftIndex;
//
//	private String rowRightIndex;
//
//	private Long rowVersion;

}
