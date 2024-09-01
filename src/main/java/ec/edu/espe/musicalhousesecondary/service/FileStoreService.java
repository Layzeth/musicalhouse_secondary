package ec.edu.espe.musicalhousesecondary.service;

import ec.edu.espe.musicalhousesecondary.schemas.DownloadedFile;
import ec.edu.espe.musicalhousesecondary.schemas.FileInfo;
import org.apache.tika.Tika;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public interface FileStoreService {


    String store(InputStream upload, String filename) throws IOException;

    String storeEphimeral(InputStream upload, String filename) throws IOException;

    Optional<DownloadedFile> download(String id) throws IOException;

    Optional<FileInfo> info(String id) throws IOException;

    boolean exists(String id);

    boolean existsByFilename(String filename);

    void remove(String id);

    default String guessContentType(String filename) {
        return new Tika().detect(filename);
    }
}