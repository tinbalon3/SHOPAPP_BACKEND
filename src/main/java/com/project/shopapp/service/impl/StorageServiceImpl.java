package com.project.shopapp.service.impl;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.service.IStorageService;
import com.project.shopapp.untils.MessageKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class StorageServiceImpl implements IStorageService {
    @Value("${application.bucket.name}")
    private String bucketName;


    private final AmazonS3 s3Client;

    private final String folderName = "uploads_fruit_image/";
    private final LocalizationUtils localizationUtils;



    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        File fileObj = convertMultiPartFileToFile(file);
        String fileName =  storeFile(file);
        s3Client.putObject(new PutObjectRequest(bucketName,folderName + fileName, fileObj));
        fileObj.delete();
        return  fileName;
    }


    @Override
    public byte[] downloadFile(String fileName) {
        S3Object s3Object = s3Client.getObject(bucketName, folderName + fileName);
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        try {
            byte[] content = IOUtils.toByteArray(inputStream);
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public  String deleteFile(String fileName) {
        s3Client.deleteObject(bucketName, folderName + fileName);
        return fileName + " removed ...";
    }
    @Override
    public byte[] viewImage(String fileName) throws IOException {
        S3Object s3Object = s3Client.getObject(bucketName, folderName + fileName);
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        return IOUtils.toByteArray(inputStream);
    }
//    @Override
//    public String viewImage(String fileName) {
//        // Set the presigned URL to expire after one hour.
//        Date expiration = new Date();
//        long expTimeMillis = expiration.getTime();
//        expTimeMillis += 1000 * 60 * 60;
//        expiration.setTime(expTimeMillis);
//
//        // Generate the presigned URL.
//        GeneratePresignedUrlRequest generatePresignedUrlRequest =
//                new GeneratePresignedUrlRequest(bucketName, folderName + fileName)
//                        .withMethod(HttpMethod.GET)
//                        .withExpiration(expiration);
//        URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
//
//        return url.toString();
//    }
    private boolean isImageFile(MultipartFile file){
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
    private String storeFile(MultipartFile file) throws IOException{
        if(!isImageFile(file) || file.getOriginalFilename() == null){
            throw new IOException(localizationUtils.getLocalizeMessage(MessageKeys.UPLOAD_IMAGES_FILE_MUST_BE_IMAGE));
        }
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        //Them UUID vao truoc ten file de dam bao ten file la duy nhat
        String uniqueFilename = UUID.randomUUID().toString() + "_" + filename;
        return uniqueFilename;

    };
    private File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            log.error("Error converting multipartFile to file", e);
        }
        return convertedFile;
    }

}
