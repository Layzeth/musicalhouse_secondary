package ec.edu.espe.musicalhousesecondary.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "track_metadata")
public class TrackMetadata {
    @Id
    private String id;

    private String name;

    private Long genreId;

    private String fileId;

    private List<Long> contributorsId;
}