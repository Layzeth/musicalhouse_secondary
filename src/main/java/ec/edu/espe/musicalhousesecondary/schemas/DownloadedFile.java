package ec.edu.espe.musicalhousesecondary.schemas;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DownloadedFile {
    private byte[] file;
    private FileInfo info;
}