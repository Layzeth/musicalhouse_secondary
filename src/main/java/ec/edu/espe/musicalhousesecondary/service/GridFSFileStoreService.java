package ec.edu.espe.musicalhousesecondary.service;

import com.mongodb.BasicDBObject;
import ec.edu.espe.musicalhousesecondary.schemas.DownloadedFile;
import ec.edu.espe.musicalhousesecondary.schemas.FileInfo;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servicio para operaciones relacionadas con archivos.
 */
@Service
@Log4j2
public class GridFSFileStoreService implements FileStoreService {

    private final GridFsTemplate template;
    private final GridFsOperations operations;
    private static final String FILE_SIZE = "fileSize";
    private static final String EPHIMERAL = "ephimeral";

    @Autowired
    public GridFSFileStoreService(GridFsTemplate template, GridFsOperations operations) {
        this.template = template;
        this.operations = operations;
        removeEmphimeralFiles();
    }

    private void removeEmphimeralFiles() {
        var query = new Query(Criteria.where("metadata.ephimeral").is(true));
        template.delete(query);
        log.info("Deleting ephimeral files in collections ps.files and ps.chunks");
    }

    public String store(InputStream upload, String filename) throws IOException {
        return internalAddFile(upload, filename, false);
    }

    public void store(InputStream upload, String filename, String identifier) throws IOException {
        var contentType = guessContentType(filename);
        var fileBytes = upload.readAllBytes();

        var metadata = new BasicDBObject();
        metadata.put(FILE_SIZE, fileBytes.length);
        metadata.put("dateUpload", LocalDateTime.now());
        metadata.put("identifier", identifier);
        ObjectId fileID = template.store(new ByteArrayInputStream(fileBytes), filename, contentType, metadata);
    }

    public String storeEphimeral(InputStream upload, String filename) throws IOException {
        return internalAddFile(upload, filename, true);
    }

    private String internalAddFile(InputStream upload, String filename, boolean ephimeral) throws IOException {

        var contentType = guessContentType(filename);
        var fileBytes = upload.readAllBytes();

        var metadata = new BasicDBObject();
        metadata.put(FILE_SIZE, fileBytes.length);
        metadata.put("dateUpload", LocalDateTime.now());
        if (ephimeral) metadata.put(EPHIMERAL, true);
        ObjectId fileID = template.store(new ByteArrayInputStream(fileBytes), filename, contentType, metadata);
        return fileID.toString();
    }


    public Optional<DownloadedFile> download(String id) throws IOException {

        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(
                Criteria.where("_id").is(id),
                Criteria.where("metadata.identifier").is(id)
        ));

        var gridFSFile = template.findOne(query);
        var loadFileBuilder = DownloadedFile.builder();

        if (gridFSFile == null) return Optional.empty();

        if (gridFSFile.getMetadata() != null) {

            var metadata = gridFSFile.getMetadata();

            var fileInfo = FileInfo.builder()
                    .filename(gridFSFile.getFilename())
                    .fileType(metadata.get("_contentType").toString())
                    .fileSize(Long.parseLong(metadata.get(FILE_SIZE).toString()))
                    .build();

            loadFileBuilder.file(IOUtils.toByteArray(operations.getResource(gridFSFile).getInputStream()));
            loadFileBuilder.info(fileInfo);

            if (metadata.containsKey(EPHIMERAL) && Boolean.TRUE.equals(metadata.getBoolean(EPHIMERAL))) remove(id);
        }

        return Optional.of(loadFileBuilder.build());
    }

    @Override
    public Optional<FileInfo> info(String id) {

        var gridFSFile = template.findOne(new Query(Criteria.where("_id").is(id)));
        var builder = FileInfo.builder();

        if (gridFSFile == null) return Optional.empty();

        if (gridFSFile.getMetadata() != null) {

            var metadata = gridFSFile.getMetadata();

            builder
                    .filename(gridFSFile.getFilename())
                    .fileType(metadata.get("_contentType").toString())
                    .fileSize(Long.parseLong(metadata.get(FILE_SIZE).toString()));

        }

        return Optional.of(builder.build());

    }

    @Override
    public boolean exists(String id) {
        return template.findOne(new Query(Criteria.where("_id").is(id))) != null;
    }

    @Override
    public boolean existsByFilename(String filename) {
        return template.findOne(new Query(Criteria.where("filename").is(filename))) != null;
    }

    public void remove(String id) {
        template.delete(new Query(Criteria.where("_id").is(id)));
    }

}