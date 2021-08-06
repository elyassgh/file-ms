package inn.center.file.service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "Files")
public class File {

    @Id
    private String id;

    @Indexed(unique = true)
    private String fileName;
    private String fileType;
    private byte[] data;

}
