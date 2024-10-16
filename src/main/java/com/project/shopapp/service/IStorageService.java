package com.project.shopapp.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IStorageService {
    public String uploadFile(MultipartFile file) throws IOException;
    public byte[] downloadFile(String fileName);
    public String deleteFile(String fileName);
    public byte[] viewImage(String fileName) throws IOException;


}
