package avvillas.template_admin.persistence.repository;

import avvillas.template_admin.persistence.entity.FormatObjectsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FormatObjectsRepository extends JpaRepository<FormatObjectsEntity, String>, JpaSpecificationExecutor<FormatObjectsEntity> {

    boolean existsByObjectId(String objectId);
}