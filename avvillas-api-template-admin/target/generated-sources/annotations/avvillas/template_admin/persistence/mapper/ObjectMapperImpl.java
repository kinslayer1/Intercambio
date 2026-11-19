package avvillas.template_admin.persistence.mapper;

import avvillas.template_admin.persistence.entity.CellEntity;
import avvillas.template_admin.persistence.entity.ObjectEntity;
import avvillas.template_admin.persistence.entity.ObjectImageEntity;
import avvillas.template_admin.persistence.entity.ObjectTableEntity;
import avvillas.template_admin.service.dto.object.TableBodyConfig;
import avvillas.template_admin.service.dto.object.request.CellDTO;
import avvillas.template_admin.service.dto.object.request.ObjectGenericDTO;
import avvillas.template_admin.service.dto.object.request.ObjectImageDTO;
import avvillas.template_admin.service.dto.object.request.ObjectTableDTO;
import avvillas.template_admin.service.dto.object.response.CellResponseDTO;
import avvillas.template_admin.service.dto.object.response.ObjectGenericResponseDTO;
import avvillas.template_admin.service.dto.object.response.ObjectImageResponseDTO;
import avvillas.template_admin.service.dto.object.response.ObjectResponseDTO;
import avvillas.template_admin.service.dto.object.response.ObjectTableResponseDTO;
import avvillas.template_admin.service.dto.object.response.TableBodyConfigResponse;
import avvillas.template_admin.util.ConversionUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-11-19T17:02:28-0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.12.1 (Oracle Corporation)"
)
@Component
public class ObjectMapperImpl implements ObjectMapper {

    @Override
    public ObjectTableEntity toTablaEntity(ObjectTableDTO dto) {
        if ( dto == null ) {
            return null;
        }

        ObjectTableEntity objectTableEntity = new ObjectTableEntity();

        Map<String, Object> map = dto.getStyle();
        if ( map != null ) {
            objectTableEntity.setStyleProperties( new LinkedHashMap<String, Object>( map ) );
        }
        objectTableEntity.setEsCierreTabla( dto.getEsCierreTabla() );
        objectTableEntity.setHeader( dto.getHeader() );
        objectTableEntity.setBody( dto.getBody() );
        objectTableEntity.setProductId( dto.getProductId() );
        objectTableEntity.setName( dto.getName() );
        objectTableEntity.setIdentifier( dto.getIdentifier() );
        objectTableEntity.setType( dto.getType() );
        objectTableEntity.setRenderCondicionalHabilitado( dto.getRenderCondicionalHabilitado() );
        objectTableEntity.setRenderCondicionalCampoId( dto.getRenderCondicionalCampoId() );
        objectTableEntity.setRenderCondicionalOperador( dto.getRenderCondicionalOperador() );
        objectTableEntity.setRenderCondicionalValorEsperado( dto.getRenderCondicionalValorEsperado() );

        objectTableEntity.setFixed( ConversionUtil.booleanToInteger(dto.getFixed()) );

        return objectTableEntity;
    }

    @Override
    public ObjectEntity genericToEntity(ObjectGenericDTO dto) {
        if ( dto == null ) {
            return null;
        }

        ObjectEntity objectEntity = new ObjectEntity();

        Map<String, Object> map = dto.getStyle();
        if ( map != null ) {
            objectEntity.setStyleProperties( new LinkedHashMap<String, Object>( map ) );
        }
        objectEntity.setEsCierreTabla( dto.getEsCierreTabla() );
        objectEntity.setProductId( dto.getProductId() );
        objectEntity.setName( dto.getName() );
        objectEntity.setIdentifier( dto.getIdentifier() );
        objectEntity.setType( dto.getType() );
        objectEntity.setContent( dto.getContent() );
        objectEntity.setRenderCondicionalHabilitado( dto.getRenderCondicionalHabilitado() );
        objectEntity.setRenderCondicionalCampoId( dto.getRenderCondicionalCampoId() );
        objectEntity.setRenderCondicionalOperador( dto.getRenderCondicionalOperador() );
        objectEntity.setRenderCondicionalValorEsperado( dto.getRenderCondicionalValorEsperado() );

        return objectEntity;
    }

    @Override
    public ObjectImageEntity imageToEntity(ObjectImageDTO dto) {
        if ( dto == null ) {
            return null;
        }

        ObjectImageEntity objectImageEntity = new ObjectImageEntity();

        byte[] image = dto.getDecodedImage();
        if ( image != null ) {
            objectImageEntity.setImage( Arrays.copyOf( image, image.length ) );
        }
        objectImageEntity.setMimeType( dto.getMimeType() );
        Map<String, Object> map = dto.getStyle();
        if ( map != null ) {
            objectImageEntity.setStyleProperties( new LinkedHashMap<String, Object>( map ) );
        }
        objectImageEntity.setEsCierreTabla( dto.getEsCierreTabla() );
        objectImageEntity.setProductId( dto.getProductId() );
        objectImageEntity.setName( dto.getName() );
        objectImageEntity.setIdentifier( dto.getIdentifier() );
        objectImageEntity.setType( dto.getType() );
        objectImageEntity.setRenderCondicionalHabilitado( dto.getRenderCondicionalHabilitado() );
        objectImageEntity.setRenderCondicionalCampoId( dto.getRenderCondicionalCampoId() );
        objectImageEntity.setRenderCondicionalOperador( dto.getRenderCondicionalOperador() );
        objectImageEntity.setRenderCondicionalValorEsperado( dto.getRenderCondicionalValorEsperado() );

        return objectImageEntity;
    }

    @Override
    public CellEntity toCellEntity(CellDTO dto) {
        if ( dto == null ) {
            return null;
        }

        CellEntity cellEntity = new CellEntity();

        cellEntity.setRowIndex( dto.getRowIndex() );
        cellEntity.setColumnIndex( dto.getColumnIndex() );
        Map<String, Object> map = dto.getStyle();
        if ( map != null ) {
            cellEntity.setStyleProperties( new LinkedHashMap<String, Object>( map ) );
        }
        cellEntity.setContent( dto.getContent() );
        cellEntity.setId( dto.getId() );
        cellEntity.setColSpan( dto.getColSpan() );
        cellEntity.setRowSpan( dto.getRowSpan() );

        return cellEntity;
    }

    @Override
    public List<ObjectResponseDTO> toResponseListDTO(List<ObjectEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<ObjectResponseDTO> list = new ArrayList<ObjectResponseDTO>( entities.size() );
        for ( ObjectEntity objectEntity : entities ) {
            list.add( toResponseDTO( objectEntity ) );
        }

        return list;
    }

    @Override
    public ObjectTableResponseDTO toTableResponseDTO(ObjectTableEntity entity) {
        if ( entity == null ) {
            return null;
        }

        ObjectTableResponseDTO objectTableResponseDTO = new ObjectTableResponseDTO();

        if ( entity.getBody() != null ) {
            if ( objectTableResponseDTO.getBody() == null ) {
                objectTableResponseDTO.setBody( new TableBodyConfigResponse() );
            }
            tableBodyConfigToTableBodyConfigResponse( entity.getBody(), objectTableResponseDTO.getBody() );
        }
        if ( objectTableResponseDTO.getBody() == null ) {
            objectTableResponseDTO.setBody( new TableBodyConfigResponse() );
        }
        objectTableEntityToTableBodyConfigResponse( entity, objectTableResponseDTO.getBody() );
        objectTableResponseDTO.setHeader( entity.getHeader() );
        objectTableResponseDTO.setId( entity.getId() );
        objectTableResponseDTO.setProductId( entity.getProductId() );
        objectTableResponseDTO.setName( entity.getName() );
        objectTableResponseDTO.setIdentifier( entity.getIdentifier() );
        objectTableResponseDTO.setType( entity.getType() );
        objectTableResponseDTO.setEsCierreTabla( entity.getEsCierreTabla() );
        objectTableResponseDTO.setRenderCondicionalHabilitado( entity.getRenderCondicionalHabilitado() );
        objectTableResponseDTO.setRenderCondicionalCampoId( entity.getRenderCondicionalCampoId() );
        objectTableResponseDTO.setRenderCondicionalOperador( entity.getRenderCondicionalOperador() );
        objectTableResponseDTO.setRenderCondicionalValorEsperado( entity.getRenderCondicionalValorEsperado() );

        objectTableResponseDTO.setFixed( ConversionUtil.integerToBoolean(entity.getFixed()) );

        return objectTableResponseDTO;
    }

    @Override
    public ObjectGenericResponseDTO toGenericResponseDTO(ObjectEntity entity) {
        if ( entity == null ) {
            return null;
        }

        ObjectGenericResponseDTO objectGenericResponseDTO = new ObjectGenericResponseDTO();

        objectGenericResponseDTO.setContent( entity.getContent() );
        Map<String, Object> map = entity.getStyleProperties();
        if ( map != null ) {
            objectGenericResponseDTO.setStyle( new LinkedHashMap<String, Object>( map ) );
        }
        objectGenericResponseDTO.setId( entity.getId() );
        objectGenericResponseDTO.setProductId( entity.getProductId() );
        objectGenericResponseDTO.setName( entity.getName() );
        objectGenericResponseDTO.setIdentifier( entity.getIdentifier() );
        objectGenericResponseDTO.setType( entity.getType() );
        objectGenericResponseDTO.setEsCierreTabla( entity.getEsCierreTabla() );
        objectGenericResponseDTO.setRenderCondicionalHabilitado( entity.getRenderCondicionalHabilitado() );
        objectGenericResponseDTO.setRenderCondicionalCampoId( entity.getRenderCondicionalCampoId() );
        objectGenericResponseDTO.setRenderCondicionalOperador( entity.getRenderCondicionalOperador() );
        objectGenericResponseDTO.setRenderCondicionalValorEsperado( entity.getRenderCondicionalValorEsperado() );

        return objectGenericResponseDTO;
    }

    @Override
    public ObjectImageResponseDTO toImageResponseDTO(ObjectImageEntity entity) {
        if ( entity == null ) {
            return null;
        }

        ObjectImageResponseDTO objectImageResponseDTO = new ObjectImageResponseDTO();

        Map<String, Object> map = entity.getStyleProperties();
        if ( map != null ) {
            objectImageResponseDTO.setStyle( new LinkedHashMap<String, Object>( map ) );
        }
        objectImageResponseDTO.setId( entity.getId() );
        objectImageResponseDTO.setProductId( entity.getProductId() );
        objectImageResponseDTO.setName( entity.getName() );
        objectImageResponseDTO.setIdentifier( entity.getIdentifier() );
        objectImageResponseDTO.setType( entity.getType() );
        objectImageResponseDTO.setEsCierreTabla( entity.getEsCierreTabla() );
        objectImageResponseDTO.setRenderCondicionalHabilitado( entity.getRenderCondicionalHabilitado() );
        objectImageResponseDTO.setRenderCondicionalCampoId( entity.getRenderCondicionalCampoId() );
        objectImageResponseDTO.setRenderCondicionalOperador( entity.getRenderCondicionalOperador() );
        objectImageResponseDTO.setRenderCondicionalValorEsperado( entity.getRenderCondicionalValorEsperado() );

        objectImageResponseDTO.setImage( mapImageToBase64(entity.getImage(), entity.getMimeType()) );

        return objectImageResponseDTO;
    }

    @Override
    public CellResponseDTO toCellResponseDTO(CellEntity cell) {
        if ( cell == null ) {
            return null;
        }

        CellResponseDTO cellResponseDTO = new CellResponseDTO();

        cellResponseDTO.setRowIndex( cell.getRowIndex() );
        cellResponseDTO.setColumnIndex( cell.getColumnIndex() );
        Map<String, Object> map = cell.getStyleProperties();
        if ( map != null ) {
            cellResponseDTO.setStyle( new LinkedHashMap<String, Object>( map ) );
        }
        cellResponseDTO.setContent( cell.getContent() );
        cellResponseDTO.setColSpan( cell.getColSpan() );
        cellResponseDTO.setRowSpan( cell.getRowSpan() );
        cellResponseDTO.setId( cell.getId() );

        return cellResponseDTO;
    }

    @Override
    public List<CellResponseDTO> toCeldaResponseDTOList(List<CellEntity> cells) {
        if ( cells == null ) {
            return null;
        }

        List<CellResponseDTO> list = new ArrayList<CellResponseDTO>( cells.size() );
        for ( CellEntity cellEntity : cells ) {
            list.add( toCellResponseDTO( cellEntity ) );
        }

        return list;
    }

    protected List<CellResponseDTO> cellDTOListToCellResponseDTOList(List<CellDTO> list) {
        if ( list == null ) {
            return null;
        }

        List<CellResponseDTO> list1 = new ArrayList<CellResponseDTO>( list.size() );
        for ( CellDTO cellDTO : list ) {
            list1.add( toCellResponseDTO( toCellEntity( cellDTO ) ) );
        }

        return list1;
    }

    protected void tableBodyConfigToTableBodyConfigResponse(TableBodyConfig tableBodyConfig, TableBodyConfigResponse mappingTarget) {
        if ( tableBodyConfig == null ) {
            return;
        }

        if ( mappingTarget.getGlobalStyles() != null ) {
            Map<String, Object> map = tableBodyConfig.getGlobalStyles();
            if ( map != null ) {
                mappingTarget.getGlobalStyles().clear();
                mappingTarget.getGlobalStyles().putAll( map );
            }
            else {
                mappingTarget.setGlobalStyles( null );
            }
        }
        else {
            Map<String, Object> map = tableBodyConfig.getGlobalStyles();
            if ( map != null ) {
                mappingTarget.setGlobalStyles( new LinkedHashMap<String, Object>( map ) );
            }
        }
        if ( mappingTarget.getCells() != null ) {
            List<CellResponseDTO> list = cellDTOListToCellResponseDTOList( tableBodyConfig.getCells() );
            if ( list != null ) {
                mappingTarget.getCells().clear();
                mappingTarget.getCells().addAll( list );
            }
            else {
                mappingTarget.setCells( null );
            }
        }
        else {
            List<CellResponseDTO> list = cellDTOListToCellResponseDTOList( tableBodyConfig.getCells() );
            if ( list != null ) {
                mappingTarget.setCells( list );
            }
        }
    }

    protected void objectTableEntityToTableBodyConfigResponse(ObjectTableEntity objectTableEntity, TableBodyConfigResponse mappingTarget) {
        if ( objectTableEntity == null ) {
            return;
        }

        if ( mappingTarget.getCells() != null ) {
            List<CellResponseDTO> list = toCeldaResponseDTOList( objectTableEntity.getCells() );
            if ( list != null ) {
                mappingTarget.getCells().clear();
                mappingTarget.getCells().addAll( list );
            }
            else {
                mappingTarget.setCells( null );
            }
        }
        else {
            List<CellResponseDTO> list = toCeldaResponseDTOList( objectTableEntity.getCells() );
            if ( list != null ) {
                mappingTarget.setCells( list );
            }
        }
    }
}
