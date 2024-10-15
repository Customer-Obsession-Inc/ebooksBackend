package com.cusob.ebooks.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cusob.ebooks.pojo.Book;
import com.cusob.ebooks.pojo.DTO.BookDto;
import com.cusob.ebooks.pojo.vo.BookDetailVo;

import java.util.List;

public interface BookService extends IService<Book> {
    List<BookDetailVo> searchBook(String name);



}
