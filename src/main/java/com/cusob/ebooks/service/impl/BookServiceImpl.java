package com.cusob.ebooks.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cusob.ebooks.auth.AuthContext;
import com.cusob.ebooks.common.Exception.EbooksException;
import com.cusob.ebooks.mapper.BookMapper;
import com.cusob.ebooks.mapper.UserMapper;
import com.cusob.ebooks.pojo.Book;
import com.cusob.ebooks.pojo.DTO.BookDto;
import com.cusob.ebooks.pojo.User;
import com.cusob.ebooks.pojo.vo.BookDetailVo;
import com.cusob.ebooks.result.ResultCodeEnum;
import com.cusob.ebooks.service.BookService;
import com.cusob.ebooks.service.CategoryService;
import com.cusob.ebooks.service.MinioService;
import io.minio.MinioClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {
    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioService minioService;


    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CategoryService categoryService;

    @Override
    public List<BookDetailVo> searchBook(String name) {
        BaseMapper<Book> baseMapper = getBaseMapper();

        // 使用 MyBatis-Plus 的查询方法
        QueryWrapper<Book> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);

        List<Book> books = baseMapper.selectList(queryWrapper);

        //todo minio获取文件url

        List<BookDetailVo> bookDetailVoList = new ArrayList<>();

        if(!books.isEmpty()){
            throw new EbooksException(ResultCodeEnum.BOOK_NOT_FOUND);
        }

        // 将查询结果转换为 BookDetailVo
        for (Book book : books) {
            BookDetailVo bookDetailVo = new BookDetailVo();
            try {
                BeanUtils.copyProperties(bookDetailVo, book);
            } catch (Exception e) {
                e.printStackTrace(); // 处理异常
            }
            bookDetailVoList.add(bookDetailVo);
        }
        return bookDetailVoList; // 返回转换后的 List<BookDetailVo>
    }

    @Override
    public Long updateDocumentInfo(BookDto bookDto) {
        Long userId = AuthContext.getUserId(); // 获取当前用户 ID
        // 查询用户信息
        User user = userMapper.selectById(userId);

        Book book = new Book();
        book.setUpdater(user.getNickname());
        BeanUtils.copyProperties(bookDto, book);

        this.save(book);
        return book.getId();
    }

    @Override
    public List<String> getUploadFiles(Long userId) {
        User user = userMapper.selectById(userId);
        String nickname = user.getNickname();
        // 使用 MyBatis-Plus 查询资源 URL
        return baseMapper.selectList(new QueryWrapper<Book>()
                        .select("resourceurl").eq("updater", nickname))
                        .stream()
                        .map(Book::getResourceurl)
                        .collect(Collectors.toList());

    }



    private String getCoverUrlFromMinio(String coverFileName) {
        String bucketName = "your-bucket-name"; // 替换为实际的桶名
//        return minioService.getPresignedUrl(bucketName, coverFileName,60 * 60); // 获取封面 URL
        return null;
    }

    private String getResourceUrlFromMinio(String resourceFileName) {
        String bucketName = "your-bucket-name"; // 替换为实际的桶名
//        return minioService.getPresignedUrl(bucketName, resourceFileName,60 * 60); // 获取资源 URL
            return null;
    }

}
