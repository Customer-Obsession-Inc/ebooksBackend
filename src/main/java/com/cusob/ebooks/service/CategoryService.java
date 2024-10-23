package com.cusob.ebooks.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cusob.ebooks.mapper.CategoryMapper;
import com.cusob.ebooks.pojo.Book;
import com.cusob.ebooks.pojo.Category;
import com.cusob.ebooks.pojo.DTO.BookDto;
import com.cusob.ebooks.pojo.vo.BookDetailVo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


public interface CategoryService extends IService<Category>{


}
