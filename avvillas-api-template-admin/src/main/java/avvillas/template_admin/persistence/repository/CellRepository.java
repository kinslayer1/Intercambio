package avvillas.template_admin.persistence.repository;

import avvillas.template_admin.persistence.entity.CellEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CellRepository extends JpaRepository<CellEntity, String>,
        JpaSpecificationExecutor<CellEntity> {
    Optional<CellEntity> findByIdAndTableObjectId(String cellId, String tableId);

    Optional<List<CellEntity>> findByTableObjectId(String tableId);

}
