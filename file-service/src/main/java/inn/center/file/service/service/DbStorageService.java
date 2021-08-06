package inn.center.file.service.service;

import inn.center.file.service.model.File;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

public interface DbStorageService {

    public File insertFile(MultipartFile file) throws IOException;

    public Optional<File> findById(String id);

    public Optional<File> findByFileName(String filename);

    public Stream<File> findAll();

    public void deleteFile(String filename);

    public void deleteAll();

}
