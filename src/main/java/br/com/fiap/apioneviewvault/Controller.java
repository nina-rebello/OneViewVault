package br.com.fiap.apioneviewvault;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

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

    @Autowired
    private FileRepository fileRepository;

    @PostMapping("/file")
    public Response uploadFile(@RequestParam("file") MultipartFile file, UriComponentsBuilder uriBuilder) {
        // validar o arquivo
        if(file.isEmpty()){
            throw new RuntimeException("Invalid File");
        }

        // salvar arquivo no disco
        try(InputStream is = file.getInputStream()){
            Path destinationDir = Path.of("src/main/resources/static/files");
            Path destinationFile = destinationDir
                    .resolve(System.currentTimeMillis() + file.getOriginalFilename() )
                    .normalize()
                    .toAbsolutePath();

            Files.copy(is, destinationFile);

            System.out.println("Arquivo salvo com sucesso");


            var fileUrl = destinationFile.getFileName().toString();
            FileStorage fileStorage = new FileStorage();
            fileStorage.setPath(fileUrl);
            fileRepository.save(fileStorage);

            Response response = new Response(fileStorage.getId().toString());
            return response;
        }catch (Exception e){
            System.out.println("Erro ao copiar arquivo. " + e.getCause());
            return null;
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID id) throws MalformedURLException {
        FileStorage fileStorage = fileRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Arquivo não encontrado no banco de dados")
        );

        Path filePath = Paths.get("src/main/resources/static/files/" + fileStorage.getPath());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("Arquivo não encontrado ou não pode ser lido");
        }

        // Se deseja remover o arquivo do banco de dados após o download, mantenha esta linha.
        // fileRepository.delete(fileStorage);

        String contentType = "application/octet-stream";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileStorage.getPath() + "\"")
                .body(resource);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileStorage> getFileById(@PathVariable UUID id) {
        FileStorage fileStorage = fileRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Arquivo não encontrado")
        );
        return ResponseEntity.ok(fileStorage);
    }

    @GetMapping("/file")
    public List<FileStorage> listFiles() {
        return fileRepository.findAll();
    }
}
