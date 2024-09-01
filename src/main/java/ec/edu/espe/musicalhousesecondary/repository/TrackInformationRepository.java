package ec.edu.espe.musicalhousesecondary.repository;

import ec.edu.espe.musicalhousesecondary.model.TrackInformation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrackInformationRepository extends MongoRepository<TrackInformation, Long> {

    Optional<TrackInformation> findByFileIdentifier(String id);

}
