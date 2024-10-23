package com.cusob.ebooks.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cusob.ebooks.pojo.Book;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookMapper extends BaseMapper<Book> {

}
