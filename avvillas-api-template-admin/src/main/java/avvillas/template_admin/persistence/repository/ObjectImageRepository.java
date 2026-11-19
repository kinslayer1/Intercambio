package avvillas.template_admin.persistence.repository;

import avvillas.template_admin.persistence.entity.ObjectImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ObjectImageRepository extends JpaRepository<ObjectImageEntity, String>,
        JpaSpecificationExecutor<ObjectImageEntity> {

}
