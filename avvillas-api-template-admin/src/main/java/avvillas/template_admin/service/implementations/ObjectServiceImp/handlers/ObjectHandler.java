package avvillas.template_admin.service.implementations.ObjectServiceImp.handlers;

import avvillas.template_admin.persistence.entity.ObjectEntity;
import avvillas.template_admin.service.dto.object.patch.PatchObjectBaseDTO;
import avvillas.template_admin.service.dto.object.request.ObjectBaseDTO;
import avvillas.template_admin.service.dto.object.response.ObjectResponseDTO;

public interface ObjectHandler {
    ObjectResponseDTO create(ObjectBaseDTO dto);

    ObjectResponseDTO update(ObjectEntity entity, PatchObjectBaseDTO dto);

    void deleteWithDependencies(ObjectEntity entity);
}
