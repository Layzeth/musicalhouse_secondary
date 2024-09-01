package ec.edu.espe.musicalhousesecondary.repository;

import ec.edu.espe.musicalhousesecondary.model.TrackMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackMetadataRepository extends MongoRepository<TrackMetadata, String> {

}
