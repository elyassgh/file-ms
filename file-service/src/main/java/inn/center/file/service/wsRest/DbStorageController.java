package inn.center.file.service.wsRest;

import com.google.common.net.HttpHeaders;
import inn.center.file.service.model.File;
import inn.center.file.service.service.DbStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/db")
@Slf4j
public class DbStorageController {

    @Autowired
    DbStorageService dbStorageService;

    @GetMapping("/")
    public List<String> listAllFiles() {
        log.info("inside listAllFiles");
        return dbStorageService.findAll().map(
                file -> ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/db/uca-inn-center-cdn/")
                        .path(file.getFileName())
                        .toUriString())
                .collect(Collectors.toList());
    }

    @GetMapping("/get/{filename:.+}")
    public File getFile(@PathVariable String filename) {
        log.info("inside downloadFile");
        Optional<File> file = dbStorageService.findByFileName(filename);
        if (file.isEmpty()) throw new RuntimeException("File not found !");
        return file.get();
    }

    @GetMapping("/uca-inn-center-cdn/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Object> downloadFile(@PathVariable String filename) {
        File file = getFile(filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(file.getFileType()))
                .body(file.getData());
    }

    @PostMapping("/upload-file")
    public ResponseEntity uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("uploadFile called");
        try {
            return ResponseEntity.status(200).body(dbStorageService.insertFile(file));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to store file.");
        }
    }

    @PostMapping("/upload-multiple-files")
    public List<File> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        log.info("uploadMultipleFiles called");
        return Arrays.stream(files)
                .map(this::uploadFile)
                .map(responseEntity -> (File) responseEntity.getBody())
                .collect(Collectors.toList());
    }

    @DeleteMapping("/delete/{filename:.+}")
    public ResponseEntity<String> delete(@PathVariable String filename) {
        log.warn("inside delete");
        try {
            dbStorageService.deleteFile(filename);
            return ResponseEntity.status(200).body("deleted");
        } catch (Exception e) {
            return ResponseEntity.status(404).body("File not found !");
        }
    }

    @DeleteMapping("/delete-all")
    public void deleteAll() {
        log.warn("inside deleteAll");
        dbStorageService.deleteAll();
    }
}