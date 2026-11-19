package avvillas.template_admin.persistence.mapper;

import avvillas.template_admin.persistence.entity.FormatObjectsEntity;
import avvillas.template_admin.persistence.entity.ObjectEntity;
import avvillas.template_admin.service.dto.format.FormatResponseDto;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-11-19T17:02:27-0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.12.1 (Oracle Corporation)"
)
@Component
public class ElementMapperImpl implements ElementMapper {

    @Override
    public List<FormatResponseDto.ElementResponse> toElementResponses(List<FormatObjectsEntity> objects) {
        if ( objects == null ) {
            return null;
        }

        List<FormatResponseDto.ElementResponse> list = new ArrayList<FormatResponseDto.ElementResponse>( objects.size() );
        for ( FormatObjectsEntity formatObjectsEntity : objects ) {
            list.add( toElementResponse( formatObjectsEntity ) );
        }

        return list;
    }

    @Override
    public FormatResponseDto.ElementResponse toElementResponse(FormatObjectsEntity object) {
        if ( object == null ) {
            return null;
        }

        FormatResponseDto.ElementResponse elementResponse = new FormatResponseDto.ElementResponse();

        elementResponse.setObjectId( objectObjectId( object ) );
        elementResponse.setFieldId( object.getFieldId() );
        if ( object.getPositionX() != null ) {
            elementResponse.setPositionX( object.getPositionX() );
        }
        if ( object.getPositionY() != null ) {
            elementResponse.setPositionY( object.getPositionY() );
        }
        elementResponse.setEsCierreTabla( objectObjectEsCierreTabla( object ) );
        elementResponse.setRenderCondicionalHabilitado( objectObjectRenderCondicionalHabilitado( object ) );
        elementResponse.setRenderCondicionalCampoId( objectObjectRenderCondicionalCampoId( object ) );
        elementResponse.setRenderCondicionalOperador( objectObjectRenderCondicionalOperador( object ) );
        elementResponse.setRenderCondicionalValorEsperado( objectObjectRenderCondicionalValorEsperado( object ) );

        return elementResponse;
    }

    private String objectObjectId(FormatObjectsEntity formatObjectsEntity) {
        if ( formatObjectsEntity == null ) {
            return null;
        }
        ObjectEntity object = formatObjectsEntity.getObject();
        if ( object == null ) {
            return null;
        }
        String id = object.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Boolean objectObjectEsCierreTabla(FormatObjectsEntity formatObjectsEntity) {
        if ( formatObjectsEntity == null ) {
            return null;
        }
        ObjectEntity object = formatObjectsEntity.getObject();
        if ( object == null ) {
            return null;
        }
        Boolean esCierreTabla = object.getEsCierreTabla();
        if ( esCierreTabla == null ) {
            return null;
        }
        return esCierreTabla;
    }

    private Boolean objectObjectRenderCondicionalHabilitado(FormatObjectsEntity formatObjectsEntity) {
        if ( formatObjectsEntity == null ) {
            return null;
        }
        ObjectEntity object = formatObjectsEntity.getObject();
        if ( object == null ) {
            return null;
        }
        Boolean renderCondicionalHabilitado = object.getRenderCondicionalHabilitado();
        if ( renderCondicionalHabilitado == null ) {
            return null;
        }
        return renderCondicionalHabilitado;
    }

    private String objectObjectRenderCondicionalCampoId(FormatObjectsEntity formatObjectsEntity) {
        if ( formatObjectsEntity == null ) {
            return null;
        }
        ObjectEntity object = formatObjectsEntity.getObject();
        if ( object == null ) {
            return null;
        }
        String renderCondicionalCampoId = object.getRenderCondicionalCampoId();
        if ( renderCondicionalCampoId == null ) {
            return null;
        }
        return renderCondicionalCampoId;
    }

    private String objectObjectRenderCondicionalOperador(FormatObjectsEntity formatObjectsEntity) {
        if ( formatObjectsEntity == null ) {
            return null;
        }
        ObjectEntity object = formatObjectsEntity.getObject();
        if ( object == null ) {
            return null;
        }
        String renderCondicionalOperador = object.getRenderCondicionalOperador();
        if ( renderCondicionalOperador == null ) {
            return null;
        }
        return renderCondicionalOperador;
    }

    private String objectObjectRenderCondicionalValorEsperado(FormatObjectsEntity formatObjectsEntity) {
        if ( formatObjectsEntity == null ) {
            return null;
        }
        ObjectEntity object = formatObjectsEntity.getObject();
        if ( object == null ) {
            return null;
        }
        String renderCondicionalValorEsperado = object.getRenderCondicionalValorEsperado();
        if ( renderCondicionalValorEsperado == null ) {
            return null;
        }
        return renderCondicionalValorEsperado;
    }
}
