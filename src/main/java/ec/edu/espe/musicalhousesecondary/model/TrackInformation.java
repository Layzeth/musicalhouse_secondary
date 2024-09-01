package ec.edu.espe.musicalhousesecondary.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "track_information")
public class TrackInformation {
    @Id
    private String id;

    private String fileIdentifier;

    private Long reproductionCount;

    private Long likeCount;

    private Long dislikeCount;

    private Long downloadCount;

    @DBRef
    private TrackMetadata metadata;
}
