package com.cusob.ebooks.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cusob.ebooks.auth.AuthContext;
import com.cusob.ebooks.mapper.BCMapper;
import com.cusob.ebooks.mapper.CategoryMapper;
import com.cusob.ebooks.pojo.Book;
import com.cusob.ebooks.pojo.Category;
import com.cusob.ebooks.pojo.DTO.BookDto;
import com.cusob.ebooks.pojo.Minio;
import com.cusob.ebooks.pojo.vo.BookDetailVo;
import com.cusob.ebooks.result.Result;
import com.cusob.ebooks.service.BookService;
import com.cusob.ebooks.service.CategoryService;
import com.cusob.ebooks.service.MinioService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/book")
public class BookController {

    @Autowired
    private MinioService minioService;

    @Autowired
    private Minio minio;

    @Autowired
    private BookService bookService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BCMapper bcMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private RedisTemplate redisTemplate;



    @ApiOperation("seaching")
    @GetMapping("search")
    public List<BookDetailVo> search(@RequestParam String name) {

        List<BookDetailVo> bookDetailVos = bookService.searchBook(name);

        return bookDetailVos;
    }

    @ApiOperation("advance seaching")
    @GetMapping("searchAdvance")
    public BookDetailVo searchAdvance(Long userId){

        return null;
    }

    @ApiOperation("upload book")
    @PostMapping("/uploadDocumentInfo")
    public Result uploadDocumentInfo(@RequestBody BookDto bookDto){
        Long bookid = bookService.updateDocumentInfo(bookDto);
        Long userId = AuthContext.getUserId();
        String[] categories = bookDto.getCategory();

        for(String categoryName: categories){
            Long categoryid = categoryMapper.selectByName(categoryName);
            bcMapper.insertBooksCategory(bookid, categoryid);
        }

//        redisTemplate.opsForValue().set("book:" + userId, bookid, 10, TimeUnit.MINUTES);


        return Result.ok(bookid);
    }

    @ApiOperation("upload media to minio")
    @PostMapping(value = "/uploadDocumentFile")
    public Result uploadDocumentFile(@RequestParam("file") MultipartFile file ,@RequestParam(name = "bookId", required = false) Long bookId){
//        Long userId = AuthContext.getUserId();
//        Long bookId = (Long) redisTemplate.opsForValue().get("book:" + userId);
        // 确保文件类型为 PDF
        if (!"application/pdf".equals(file.getContentType())) {
            System.out.println("wrong!");
            return Result.fail("dw");
        }
        boolean res = minioService.uploadFile("books", file, bookId);
        return Result.ok(res);
    }
    @ApiOperation("upload media to minio")
    @GetMapping(value = "/getUploadFiles")
    public Result<List<String>> getUploadFiles(){
        Long userId = AuthContext.getUserId();
        List<String> res = bookService.getUploadFiles(userId);
        return Result.ok(res);
    }

}
