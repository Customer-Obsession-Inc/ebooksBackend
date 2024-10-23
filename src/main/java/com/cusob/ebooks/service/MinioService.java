package com.cusob.ebooks.service;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public interface MinioService {

    /**
     * upload File
     *
     * @param bucketName
     * @param fil
     * @param bookid
     * @return
     */
    boolean uploadFile(String bucketName, MultipartFile fil, Long bookid);

    URL getPresignedUrl(String bucketName, String objectName, int expiryInSeconds);

    InputStream readObject(String bucketName, String objectName) throws Exception;

    /**
     * batch Remove files
     * @param urlList
     */
    void batchRemove(List<String> urlList);

    /**
     * upload Avatar
     * @param bucketName
     * @param file
     * @return
     */
    String uploadAvatar(String bucketName, MultipartFile file);

    /**
     * upload Dkim secret key
     */
    String uploadDkim(String bucketName, String filePath, InputStream inputStream);

    /**
     * 下载文件
     *
     * @param fileUrl 文件名
     * @param response HttpServletResponse
     */

    void downloadFile(String fileUrl, HttpServletResponse response);

    void uploadCover(String bucketName, MultipartFile file, Long bookid);

}
