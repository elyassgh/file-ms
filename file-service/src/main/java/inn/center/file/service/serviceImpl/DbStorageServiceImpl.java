package inn.center.file.service.serviceImpl;

import inn.center.file.service.model.File;
import inn.center.file.service.repository.FileRepository;
import inn.center.file.service.service.DbStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class DbStorageServiceImpl implements DbStorageService {

    @Autowired
    public FileRepository repository;

    @Override
    public File insertFile(MultipartFile file) throws IOException {
        File f = File.builder().fileName(file.getOriginalFilename()).fileType(file.getContentType()).data(file.getBytes()).build();
        return repository.save(f);
    }

    @Override
    public Optional<File> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public Optional<File> findByFileName(String filename) {
        return repository.findByFileName(filename);
    }

    @Override
    public Stream<File> findAll() {
        return repository.findAll().stream();
    }

    @Override
    public void deleteFile(String filename) {
        Optional<File> file = findByFileName(filename);
        if(file.isEmpty()) {
            throw new RuntimeException("File not found !");
        } else {
            repository.delete(file.get());
        }
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }
}
