package com.cusob.ebooks.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cusob.ebooks.pojo.Book;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BCMapper {
    @Insert("INSERT INTO books_category (book_id, category_id) VALUES (#{bookId}, #{categoryId})")
    void insertBooksCategory(Long bookId, Long categoryId);

}
