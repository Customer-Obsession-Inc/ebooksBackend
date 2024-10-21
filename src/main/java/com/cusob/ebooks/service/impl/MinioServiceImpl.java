package com.cusob.ebooks.service.impl;


import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cusob.ebooks.auth.AuthContext;
import com.cusob.ebooks.pojo.Minio;
import com.cusob.ebooks.pojo.User;
import com.cusob.ebooks.service.MinioService;
import io.minio.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class MinioServiceImpl implements MinioService {

    @Autowired
    private Minio minio;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private BaseMapper baseMapper;

    /**
     * upload File
     * @param bucketName
     * @param file
     * @return
     */
    @Override
    public String uploadFile(String bucketName, MultipartFile file) {
        try {
            // 判断桶是否存在
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists){
            // 如果不存在，就创建桶
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
            // 本地时间，具体到年、月、日
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            // String uuid= UUID.randomUUID().toString(); todo 文件名覆盖
            Long userId = AuthContext.getUserId();
            String filename = userId.toString() + "/" + timestamp + "/" + file.getOriginalFilename();
            // 加一个/表示创建一个文件夹
            minioClient.putObject(PutObjectArgs.builder().
                    bucket(bucketName).
                    object(filename).
                    stream(file.getInputStream(), file.getSize(), -1).
                    contentType(file.getContentType()).build()); // 文件上传的类型，如果不指定，那么每次访问时都要先下载文件
            String url= minio.getUrl()+"/"+minio.getBucketName()+"/"+filename;
            return url;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("文件上传失败");
        }

    }

    @Override
    public URL getPresignedUrl(String bucketName, String objectName, int expiryInSeconds) {
        return null;
    }

    public InputStream readObject(String bucketName, String objectName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }

    /**
     * batch Remove files
     * @param urlList
     */
    @Override
    public void batchRemove(List<String> urlList) {
        for (String url : urlList) {
            String str = url.substring(StringUtils.ordinalIndexOf(url, "/", 3) + 1);
            String bucketName = str.substring(0, str.indexOf('/'));
            String fileName = str.substring(str.indexOf('/') + 1);
            try {
                // todo 待优化 批量删除minio中文件
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build());
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(url+"文件删除失败");
            }
        }
    }

    /**
     * upload Avatar
     * @param bucketName
     * @param file
     * @return
     */
    @Override
    public String uploadAvatar(String bucketName, MultipartFile file) {
        try {
            // 判断桶是否存在
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists){
                // 如果不存在，就创建桶
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

             String uuid= UUID.randomUUID().toString();
            Long userId = AuthContext.getUserId();
            String filename = userId.toString() + "/avatars/" + uuid + file.getOriginalFilename();
            // 加一个/表示创建一个文件夹
            minioClient.putObject(PutObjectArgs.builder().
                    bucket(bucketName).
                    object(filename).
                    stream(file.getInputStream(), file.getSize(), -1).
                     contentType(file.getContentType()).build()); // 文件上传的类型，如果不指定，那么每次访问时都要先下载文件
            String url= minio.getUrl()+"/"+minio.getBucketName()+"/"+filename;
            // 创建一个 UpdateWrapper 对象
            UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", userId); // 指定要更新的用户 ID
            updateWrapper.set("avatar", url); // 设置要更新的字段及其值
            baseMapper.update(null, updateWrapper);
            return url;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("文件上传失败");
        }
    }

    /**
     *  upload Dkim secret key
     * @return
     */
    @Override
    public String uploadDkim(String bucketName, String filePath, InputStream inputStream) {
        try {
            // 判断桶是否存在
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists){
                // 如果不存在，就创建桶
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            // 加一个/表示创建一个文件夹
            minioClient.putObject(PutObjectArgs.builder().
                    bucket(bucketName).
                    object(filePath).
                    stream(inputStream, inputStream.available(), -1).
                    build());

            String url= minio.getUrl()+"/"+minio.getBucketName()+"/"+filePath;
            return url;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("文件上传失败");
        }
    }

    /**
     * 根据文件 URL 下载文件并写入 HttpServletResponse
     *
     * @param fileUrl 文件 URL
     * @param response HttpServletResponse
     */
    public void downloadFile(String fileUrl, HttpServletResponse response) {
        try {
            // 从 URL 中提取文件名和对象路径
            String objectName = extractObjectNameFromUrl(fileUrl);
            if (objectName == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid file URL");
                return;
            }

            // 获取文件输入流
            InputStream fileStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minio.getBucketName())
                    .object(objectName)
                    .build());

            // 设置响应头
            response.reset();
            response.setHeader("Content-Disposition", "attachment;filename=" +
                    URLEncoder.encode(objectName, "UTF-8"));
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");

            // 获取输出流并写入数据
            ServletOutputStream outputStream = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fileStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
            fileStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "File download failed");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 从 URL 中提取对象名称
     *
     * @param fileUrl 文件 URL
     * @return 对象名称
     */
    private String extractObjectNameFromUrl(String fileUrl) {
        // 假设 URL 的格式为：http://127.0.0.1:9000/your-bucket-name/path/to/file
        String[] parts = fileUrl.split("9000/");
        if (parts.length < 2) {
            return null; // URL 格式不正确
        }
        return parts[1].substring(1); // 返回对象路径
    }
}


