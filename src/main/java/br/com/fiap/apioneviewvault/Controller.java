package br.com.fiap.apioneviewvault;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RestController
public class Controller {

    record FileResponse(UUID link) {}

    @Autowired
    FileRepository fileRepository;

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID id) throws MalformedURLException {
        FileStorage fileStorage = fileRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Arquivo não encontrado no bd")
        );

        Path filePath = Paths.get("src/main/resources/static/files/" + fileStorage.getPath());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("Arquivo não encontrado ou não pode ser lido");
        }

        fileRepository.delete(fileStorage);

        String contentType = "application/octet-stream";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileStorage.getPath() + "\"")
                .body(resource);
    }


    @GetMapping("file")
    public List<FileStorage> listFiles() {
        return fileRepository.findAll();
    }

}
