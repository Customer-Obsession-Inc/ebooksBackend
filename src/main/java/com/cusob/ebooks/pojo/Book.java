package com.cusob.ebooks.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName("books")
public class Book extends BaseEntity {
    @TableField("name")
    private String name;

    @TableField("price")
    private Integer lastName;


    @TableField("author")
    private String author;

    @TableField("coverurl")
    private String coverurl;

    @TableField("resourceurl")
    private String resourceurl;

    @TableField("type")
    @ApiModelProperty(value = "逻辑type(1:document，0:book)")
    private Integer type;

}
