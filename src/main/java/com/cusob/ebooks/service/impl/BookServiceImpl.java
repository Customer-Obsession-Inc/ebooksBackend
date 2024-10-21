package com.cusob.ebooks.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cusob.ebooks.common.Exception.EbooksException;
import com.cusob.ebooks.pojo.Book;
import com.cusob.ebooks.pojo.vo.BookDetailVo;
import com.cusob.ebooks.result.ResultCodeEnum;
import com.cusob.ebooks.service.BookService;
import com.cusob.ebooks.service.MinioService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class BookServiceImpl implements BookService {
    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioService minioService;

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
                // 使用 BeanUtils 复制属性

                BeanUtils.copyProperties(bookDetailVo, book);

                // 获取 MinIO 中的封面 URL
//                String coverUrl = getCoverUrlFromMinio(book.getCoverUrl());
//                bookDetailVo.setCoverUrl(coverUrl); // 设置封面 URL

                // 获取 MinIO 中的资源 URL
//                String resourceUrl = getResourceUrlFromMinio(book.getResourceUrl());
//                bookDetailVo.setResourceUrl(resourceUrl); // 设置资源 URL
            } catch (Exception e) {
                e.printStackTrace(); // 处理异常
            }
            bookDetailVoList.add(bookDetailVo);
        }

        return bookDetailVoList; // 返回转换后的 List<BookDetailVo>
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

    @Override
    public boolean saveBatch(Collection<Book> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<Book> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean updateBatchById(Collection<Book> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdate(Book entity) {
        return false;
    }

    @Override
    public Book getOne(Wrapper<Book> queryWrapper, boolean throwEx) {
        return null;
    }

    @Override
    public Map<String, Object> getMap(Wrapper<Book> queryWrapper) {
        return null;
    }

    @Override
    public <V> V getObj(Wrapper<Book> queryWrapper, Function<? super Object, V> mapper) {
        return null;
    }

    @Override
    public BaseMapper<Book> getBaseMapper() {
        return null;
    }

    @Override
    public Class<Book> getEntityClass() {
        return null;
    }
}
