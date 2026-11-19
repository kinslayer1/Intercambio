package avvillas.template_admin.persistence.repository;

import avvillas.template_admin.persistence.entity.FormatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormatRepository extends JpaRepository<FormatEntity, String>, JpaSpecificationExecutor<FormatEntity> {

    FormatEntity findFirstByProductIdOrderByVersionDesc(String productId);

    List<FormatEntity> findByProductId(String productId);

    FormatEntity findByProductIdAndActive(String productId, Number active);
    

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN TRUE ELSE FALSE END FROM FormatEntity r WHERE r.productId = :productId")
    boolean existsByProductId(@Param("productId") String productId);
}