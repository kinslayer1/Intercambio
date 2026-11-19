package avvillas.template_admin.persistence.repository;

import avvillas.template_admin.persistence.entity.ObjectTableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ObjectTableRepository extends JpaRepository<ObjectTableEntity, String>,
        JpaSpecificationExecutor<ObjectTableEntity> {

}
