package inn.center.file.service.wsRest;

import com.google.common.net.HttpHeaders;
import inn.center.file.service.service.HardStorageService;
import inn.center.file.service.util.FileResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class HardStorageController {

    @Autowired
    HardStorageService hardStorageService;

    @GetMapping("/")
    public List<String> listAllFiles() {
        log.info("inside listAllFiles");
        return hardStorageService.loadAll().map(
                path -> ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/uca-inn-center-cdn/")
                        .path(path.getFileName().toString())
                        .toUriString())
                .collect(Collectors.toList());
    }

    @GetMapping("/info/{filename:.+}")
    public ResponseEntity fileInfo(@PathVariable String filename) {
        log.info("inside fileInfo");
        try {
            File file = hardStorageService.loadAsResource(filename).getFile();
            String uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uca-inn-center-cdn/")
                    .path(file.getName())
                    .toUriString();
            return ResponseEntity.ok(new FileResponse(file.getName(), uri, Files.probeContentType(file.toPath()), file.length() + "B"));
        } catch (Exception e) {
            return ResponseEntity.status(404).body("File not found !");
        }

    }

    @GetMapping(value = {"/download/{filename:.+}" , "/uca-inn-center-cdn/{filename:.+}" })
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) throws IOException {
        log.info("inside downloadFile");
        Resource resource = hardStorageService.loadAsResource(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.valueOf(Files.probeContentType(Path.of(resource.getFile().getPath()))))
                .body(resource);
    }

    @PostMapping("/upload-file")
    @ResponseBody
    public ResponseEntity uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("uploadFile called");
        String name;
        try {
            name = hardStorageService.store(file);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to store file.");
        }
        String uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uca-inn-center-cdn/")
                .path(name)
                .toUriString();

        return ResponseEntity.ok(new FileResponse(name, uri, file.getContentType(), file.getSize() + "B"));
    }

    @PostMapping("/upload-multiple-files")
    @ResponseBody
    public List<FileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        log.info("uploadMultipleFiles called");
        return Arrays.stream(files)
                .map(this::uploadFile)
                .map(responseEntity -> (FileResponse) responseEntity.getBody())
                .collect(Collectors.toList());
    }

    @DeleteMapping("/delete/{filename:.+}")
    public ResponseEntity<String> delete(@PathVariable String filename) {
        log.warn("inside delete");
        try {
            String result = hardStorageService.delete(filename);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("File does not exist", HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete-all")
    public void deleteAll() {
        log.warn("inside deleteAll");
        hardStorageService.deleteAll();
    }

}
