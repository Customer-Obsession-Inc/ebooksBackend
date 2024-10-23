package com.cusob.ebooks.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("categories")
public class Category extends BaseEntity {

    @TableField("name")
    private String name;
}
