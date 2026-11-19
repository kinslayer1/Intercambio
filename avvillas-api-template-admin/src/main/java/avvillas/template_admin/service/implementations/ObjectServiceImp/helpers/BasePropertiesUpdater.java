package avvillas.template_admin.service.implementations.ObjectServiceImp.helpers;

import avvillas.template_admin.constant.message.ObjectMessage;
import avvillas.template_admin.persistence.entity.ObjectEntity;
import avvillas.template_admin.service.dto.object.patch.PatchObjectBaseDTO;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BasePropertiesUpdater {

    public void update(ObjectEntity entity, PatchObjectBaseDTO dto) {
        dto.getName().ifPresent(entity::setName);
        dto.getProductId().ifPresent(entity::setProductId);
        dto.getIdentifier().ifPresent(entity::setIdentifier);
        dto.getEsCierreTabla().ifPresent(entity::setEsCierreTabla);
        dto.getRenderCondicionalHabilitado().ifPresent(enabled -> {
            entity.setRenderCondicionalHabilitado(enabled);
            if (!enabled) {
                entity.setRenderCondicionalCampoId(null);
                entity.setRenderCondicionalOperador(ObjectMessage.CONDITIONAL_RENDER_OPERATOR_EQUALS);
                entity.setRenderCondicionalValorEsperado(null);
            }
        });
        updateIfPresent(dto.getRenderCondicionalCampoId(), entity::setRenderCondicionalCampoId);
        updateIfPresent(dto.getRenderCondicionalOperador(), entity::setRenderCondicionalOperador);
        updateIfPresent(dto.getRenderCondicionalValorEsperado(), entity::setRenderCondicionalValorEsperado);
        dto.getStyle().ifPresent(entity::setStyleProperties);
    }

    private void updateIfPresent(Optional<String> value, java.util.function.Consumer<String> setter) {
        value.ifPresent(v -> setter.accept(v == null ? null : v.trim()));
    }

}
