package ec.edu.espe.musicalhousesecondary.schemas;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileInfo {
    private String filename;
    private String fileType;
    private Long fileSize;
}