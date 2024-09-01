package ec.edu.espe.musicalhousesecondary.service;

import ec.edu.espe.musicalhousesecondary.exceptions.ClientException;
import ec.edu.espe.musicalhousesecondary.model.TrackInformation;
import ec.edu.espe.musicalhousesecondary.model.TrackMetadata;
import ec.edu.espe.musicalhousesecondary.repository.TrackInformationRepository;
import ec.edu.espe.musicalhousesecondary.repository.TrackMetadataRepository;
import ec.edu.espe.musicalhousesecondary.schemas.DownloadedFile;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Validated
@Service
@RequiredArgsConstructor
public class TrackInformationService {

    private static final Logger log = LoggerFactory.getLogger(TrackInformationService.class);
    private final TrackInformationRepository repository;
    private final GridFSFileStoreService fileStoreService;
    private final TrackMetadataRepository metadataRepository;
    private final TrackMetadataRepository trackMetadataRepository;

    public void save(
            @NotNull @NotEmpty List<MultipartFile> tracks,
            @NotNull @NotEmpty List<TrackMetadata> metadatas
    ) throws IOException {

        if (tracks.size() != metadatas.size()) {
            throw ClientException.error("The number of tracks and metadata must be the same");
        }

        for (var track : tracks) {
            if (track != null && track.getOriginalFilename() != null && !track.getOriginalFilename().endsWith(".mp3")) {
                throw ClientException.error("Invalid file format, required .mp3");
            }
        }

        for (int i = 0; i < tracks.size(); i++) {
            var track = tracks.get(i);
            var metadata = metadatas.get(i);
            trackMetadataRepository.save(metadata);
            log.info("Saving track with metadata: {}", metadata);
            fileStoreService.store(track.getInputStream(), track.getOriginalFilename(), metadata.getFileId());

            TrackInformation trackInfo = TrackInformation.builder()
                    .fileIdentifier(metadata.getFileId())
                    .reproductionCount(0L)
                    .likeCount(0L)
                    .dislikeCount(0L)
                    .downloadCount(0L)
                    .metadata(metadata)
                    .build();
            repository.save(trackInfo);
        }
    }

    public DownloadedFile downloadTrackFile(@NotNull String id) throws IOException {
        var file = findTrackFile(id);
        TrackInformation trackInfo = findTrackById(id);
        trackInfo.setDownloadCount(trackInfo.getDownloadCount() + 1);
        repository.save(trackInfo);
        return file;
    }

    public DownloadedFile reproduceTrackFile(@NotNull String id) throws IOException {
        DownloadedFile file = findTrackFile(id);
        if (file != null) {
            incrementReproductionCount(id);
        }
        return file;
    }

    @Transactional
    public void incrementReproductionCount(@NotNull String id) {
        TrackInformation trackInfo = findTrackById(id);
        trackInfo.setReproductionCount(trackInfo.getReproductionCount() + 1);
        repository.save(trackInfo);
    }

    public void likeTrack(@NotNull String id) {
        TrackInformation trackInfo = findTrackById(id);
        trackInfo.setLikeCount(trackInfo.getLikeCount() + 1);
        repository.save(trackInfo);
    }

    public void dislikeTrack(@NotNull String id) {
        TrackInformation trackInfo = findTrackById(id);
        trackInfo.setDislikeCount(trackInfo.getDislikeCount() + 1);
        repository.save(trackInfo);
    }

    private DownloadedFile findTrackFile(@NotNull String id) throws IOException {
        return fileStoreService.download(id)
                .orElseThrow(() -> ClientException.status(HttpStatus.NOT_FOUND).error("Song not found"));
    }

    private TrackInformation findTrackById(@NotNull String id) {
        return repository.findByFileIdentifier(id)
                .orElseThrow(() -> ClientException.status(HttpStatus.NOT_FOUND).error("Track not found"));
    }

    public TrackInformation findTrackMetadata(String id) {
        return findTrackById(id);
    }

    public Iterable<TrackInformation> findAll() {
        return repository.findAll();
    }
}