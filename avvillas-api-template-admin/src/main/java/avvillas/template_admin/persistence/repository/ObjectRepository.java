package avvillas.template_admin.persistence.repository;

import avvillas.template_admin.persistence.entity.ObjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObjectRepository extends JpaRepository<ObjectEntity, String>,
        JpaSpecificationExecutor<ObjectEntity> {

    boolean existsByIdentifier(String identifier);

    List<ObjectEntity> findAllByProductId(String productId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN TRUE ELSE FALSE END FROM ObjectEntity r WHERE r.productId = :productId")
    boolean existsByProductId(@Param("productId") String productId);
}
