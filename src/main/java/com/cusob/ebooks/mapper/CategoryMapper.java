package com.cusob.ebooks.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cusob.ebooks.pojo.Category;
import com.cusob.ebooks.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    @Select("SELECT id FROM categories WHERE name = #{name}")
    Long selectByName(String name);

}
