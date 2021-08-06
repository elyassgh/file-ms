package inn.center.file.service.repository;

import inn.center.file.service.model.File;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileRepository extends MongoRepository<File, String> {

    Optional<File> findByFileName(String filename);

}
