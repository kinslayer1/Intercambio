package avvillas.template_admin.service;

import avvillas.template_admin.service.dto.PatchUpdateResponse;
import avvillas.template_admin.service.dto.object.patch.PatchObjectBaseDTO;
import avvillas.template_admin.service.dto.object.request.ObjectBaseDTO;
import avvillas.template_admin.service.dto.object.response.ObjectPageResponseDTO;
import avvillas.template_admin.service.dto.object.response.ObjectResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ObjectService {

    ObjectResponseDTO create(ObjectBaseDTO product);

    PatchUpdateResponse<ObjectResponseDTO> update(String id, PatchObjectBaseDTO dto);

    ObjectResponseDTO getById(String id);

    void deleteById(String id);

    Page<ObjectPageResponseDTO> searchGlobal(String search, Pageable pageable);

    List<ObjectResponseDTO> getByProductId(String productId);
}
