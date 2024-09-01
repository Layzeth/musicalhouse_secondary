package ec.edu.espe.musicalhousesecondary.controller;

import ec.edu.espe.musicalhousesecondary.model.TrackInformation;
import ec.edu.espe.musicalhousesecondary.model.TrackMetadata;
import ec.edu.espe.musicalhousesecondary.service.TrackInformationService;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.Normalizer;
import java.util.List;
import java.util.function.Consumer;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;


@RestController
@RequestMapping("/tracksInformation")
public class TrackInformationController {

    private final TrackInformationService trackService;

    public TrackInformationController(TrackInformationService trackService) {
        this.trackService = trackService;

    }


    @PostMapping
    public ResponseEntity<String> save(
            @RequestParam List<MultipartFile> tracks,
            @RequestPart List<TrackMetadata> metadatas
    ) throws IOException {

        trackService.save(tracks, metadatas);
        return ResponseEntity.ok("Saved");
    }


    @GetMapping(value = "/download/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<ByteArrayResource> download(@PathVariable String id) throws IOException {

        var loadFile = trackService.downloadTrackFile(id);

        var fileInfo = loadFile.getInfo();
        var filename = normalice(fileInfo.getFilename());

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.parseMediaType(fileInfo.getFileType()))
                .headers(getHeadersForFile(filename, false))
                .body(new ByteArrayResource(loadFile.getFile()));
    }

    @GetMapping(value = "/reproduce/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<ByteArrayResource> reproduce(@PathVariable String id) throws IOException {

        var loadFile = trackService.reproduceTrackFile(id);

        var fileInfo = loadFile.getInfo();
        var filename = normalice(fileInfo.getFilename());

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.parseMediaType(fileInfo.getFileType()))
                .headers(getHeadersForFile(filename, true))
                .body(new ByteArrayResource(loadFile.getFile()));
    }

    @PutMapping("/like/{id}")
    public ResponseEntity<String> like(@PathVariable String id) {
        trackService.likeTrack(id);
        return ResponseEntity.ok("Liked");
    }

    @PutMapping("/dislike/{id}")
    public ResponseEntity<String> dislike(@PathVariable String id) {
        trackService.dislikeTrack(id);
        return ResponseEntity.ok("Disliked");
    }

    @GetMapping("/info")
    public ResponseEntity<Iterable<TrackInformation>> info() {
        return ResponseEntity.ok(trackService.findAll());
    }

    @GetMapping("/info/{id}")
    public ResponseEntity<TrackInformation> info(@PathVariable String id) {
        return ResponseEntity.ok(trackService.findTrackMetadata(id));
    }


    public static Consumer<HttpHeaders> getHeadersForFile(@NotNull String filename, boolean inline) {
        var headerValue = (inline ? "inline" : "attachment") + "; filename=\"" + filename + "\"";
        return headers -> {
            headers.set(CONTENT_DISPOSITION, headerValue);
            headers.set("X-Suggested-Filename", filename);
            headers.setAccessControlExposeHeaders(List.of(CONTENT_DISPOSITION, "X-Suggested-Filename"));
        };
    }

    public static @NotNull String normalice(@NotNull String string) {
        String normalizedString = Normalizer.normalize(string, Normalizer.Form.NFD);
        return normalizedString.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

}
