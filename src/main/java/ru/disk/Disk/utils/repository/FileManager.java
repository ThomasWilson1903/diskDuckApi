package ru.disk.Disk.utils.repository;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileManager {

    ResponseEntity<Resource> get(String filePath) throws IOException;

    String upload(MultipartFile file, String folderName) throws IOException;

    List<String> upload(List<MultipartFile> files, String folderName) throws IOException;

    Boolean delete(String filePath);

    Boolean rename(String oldFilePath, String newFilePath);

    Boolean moveFolder(String oldFilePath, String newFilePath);

    Long getSizeFolder(String path);

    void deleteFolder(String path);
}
