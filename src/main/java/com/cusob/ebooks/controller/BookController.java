package com.cusob.ebooks.controller;

import com.cusob.ebooks.pojo.DTO.BookDto;
import com.cusob.ebooks.pojo.Minio;
import com.cusob.ebooks.pojo.vo.BookDetailVo;
import com.cusob.ebooks.result.Result;
import com.cusob.ebooks.service.BookService;
import com.cusob.ebooks.service.MinioService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/book")
public class BookController {

    @Autowired
    private MinioService minioService;

    @Autowired
    private Minio minio;

    @Autowired
    private BookService bookService;

    @ApiOperation("seaching")
    @GetMapping("search")
    public List<BookDetailVo> search(@RequestBody BookDto bookdto){
        String bookName = bookdto.getName();
        List<BookDetailVo> bookDetailVos = bookService.searchBook(bookName);

        return bookDetailVos;
    }

    @ApiOperation("advance seaching")
    @GetMapping("searchAdvance")
    public BookDetailVo searchAdvance(Long userId){

        return null;
    }

    @ApiOperation("upload media to minio")
    @PostMapping("/upload")
    public Result uploadFile(@RequestPart("file") MultipartFile file){
        String url = minioService.uploadFile(minio.getBucketName(), file);
        return Result.ok(url);
    }
}
