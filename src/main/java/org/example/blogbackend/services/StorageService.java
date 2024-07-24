package org.example.blogbackend.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
@Slf4j
public class StorageService {

    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    public String uploadFile(MultipartFile file) {
        File convertedFile = convertMultipartFileToFile(file);
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        s3Client.putObject(new PutObjectRequest(bucketName, "profileImages/" +fileName, convertedFile));
        convertedFile.delete();
        return fileName;
    }

    private File convertMultipartFileToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try(FileOutputStream fostream = new FileOutputStream(convertedFile)) {
            fostream.write(file.getBytes());
        } catch(IOException ex) {
            log.error("Error converting multipartFile to File", ex);
        }
        return convertedFile;
    }
}
