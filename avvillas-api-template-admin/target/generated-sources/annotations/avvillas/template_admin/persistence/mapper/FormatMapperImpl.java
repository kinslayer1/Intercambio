package avvillas.template_admin.persistence.mapper;

import avvillas.template_admin.persistence.entity.FormatEntity;
import avvillas.template_admin.persistence.entity.FormatObjectsEntity;
import avvillas.template_admin.persistence.entity.FormatPageEntity;
import avvillas.template_admin.service.dto.format.FormatDto;
import avvillas.template_admin.util.ConversionUtil;
import java.math.BigDecimal;
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
public class FormatMapperImpl implements FormatMapper {

    @Override
    public FormatEntity toEntity(FormatDto dto) {
        if ( dto == null ) {
            return null;
        }

        FormatEntity formatEntity = new FormatEntity();

        formatEntity.setTopMargin( dtoPdfConfigMarginsTop( dto ) );
        formatEntity.setBottomMargin( dtoPdfConfigMarginsBottom( dto ) );
        formatEntity.setLeftMargin( dtoPdfConfigMarginsLeft( dto ) );
        formatEntity.setRightMargin( dtoPdfConfigMarginsRight( dto ) );
        formatEntity.setPaperType( dtoPdfConfigPaperType( dto ) );
        formatEntity.setContinues( dtoPdfConfigContinues( dto ) );
        formatEntity.setFontFamily( dtoPdfConfigFontFamily( dto ) );
        formatEntity.setPages( pageDtoListToFormatPageEntityList( dto.getPages() ) );
        formatEntity.setCreatedAt( dto.getCreatedAt() );
        formatEntity.setUpdateAt( dto.getUpdateAt() );
        formatEntity.setId( dto.getId() );
        formatEntity.setProductId( dto.getProductId() );

        formatEntity.setActive( ConversionUtil.booleanToInteger(dto.getActive()) );

        return formatEntity;
    }

    @Override
    public FormatPageEntity toPageEntity(FormatDto.PageDto pageDto) {
        if ( pageDto == null ) {
            return null;
        }

        FormatPageEntity formatPageEntity = new FormatPageEntity();

        if ( pageDto.getPageNumber() != null ) {
            formatPageEntity.setPageNumber( pageDto.getPageNumber() );
        }

        formatPageEntity.setObjects( combineElementsAndFields(pageDto.getElements(), pageDto.getFields()) );

        return formatPageEntity;
    }

    @Override
    public List<FormatObjectsEntity> toEntitiesFromElements(List<FormatDto.ElementDto> elements) {
        if ( elements == null ) {
            return null;
        }

        List<FormatObjectsEntity> list = new ArrayList<FormatObjectsEntity>( elements.size() );
        for ( FormatDto.ElementDto elementDto : elements ) {
            list.add( toEntityFromElement( elementDto ) );
        }

        return list;
    }

    @Override
    public List<FormatObjectsEntity> toEntitiesFromFields(List<FormatDto.FieldDto> fields) {
        if ( fields == null ) {
            return null;
        }

        List<FormatObjectsEntity> list = new ArrayList<FormatObjectsEntity>( fields.size() );
        for ( FormatDto.FieldDto fieldDto : fields ) {
            list.add( toEntityFromField( fieldDto ) );
        }

        return list;
    }

    @Override
    public FormatObjectsEntity toEntityFromElement(FormatDto.ElementDto element) {
        if ( element == null ) {
            return null;
        }

        FormatObjectsEntity formatObjectsEntity = new FormatObjectsEntity();

        formatObjectsEntity.setPositionY( element.getPositionY() );
        formatObjectsEntity.setPositionX( element.getPositionX() );

        formatObjectsEntity.setObject( mapToObjectEntity(element.getObjectId()) );

        return formatObjectsEntity;
    }

    @Override
    public FormatObjectsEntity toEntityFromField(FormatDto.FieldDto field) {
        if ( field == null ) {
            return null;
        }

        FormatObjectsEntity formatObjectsEntity = new FormatObjectsEntity();

        formatObjectsEntity.setFieldId( field.getFieldId() );
        formatObjectsEntity.setPositionY( field.getPositionY() );
        formatObjectsEntity.setPositionX( field.getPositionX() );

        return formatObjectsEntity;
    }

    private BigDecimal dtoPdfConfigMarginsTop(FormatDto formatDto) {
        if ( formatDto == null ) {
            return null;
        }
        FormatDto.PdfConfigDto pdfConfig = formatDto.getPdfConfig();
        if ( pdfConfig == null ) {
            return null;
        }
        FormatDto.MarginDto margins = pdfConfig.getMargins();
        if ( margins == null ) {
            return null;
        }
        BigDecimal top = margins.getTop();
        if ( top == null ) {
            return null;
        }
        return top;
    }

    private BigDecimal dtoPdfConfigMarginsBottom(FormatDto formatDto) {
        if ( formatDto == null ) {
            return null;
        }
        FormatDto.PdfConfigDto pdfConfig = formatDto.getPdfConfig();
        if ( pdfConfig == null ) {
            return null;
        }
        FormatDto.MarginDto margins = pdfConfig.getMargins();
        if ( margins == null ) {
            return null;
        }
        BigDecimal bottom = margins.getBottom();
        if ( bottom == null ) {
            return null;
        }
        return bottom;
    }

    private BigDecimal dtoPdfConfigMarginsLeft(FormatDto formatDto) {
        if ( formatDto == null ) {
            return null;
        }
        FormatDto.PdfConfigDto pdfConfig = formatDto.getPdfConfig();
        if ( pdfConfig == null ) {
            return null;
        }
        FormatDto.MarginDto margins = pdfConfig.getMargins();
        if ( margins == null ) {
            return null;
        }
        BigDecimal left = margins.getLeft();
        if ( left == null ) {
            return null;
        }
        return left;
    }

    private BigDecimal dtoPdfConfigMarginsRight(FormatDto formatDto) {
        if ( formatDto == null ) {
            return null;
        }
        FormatDto.PdfConfigDto pdfConfig = formatDto.getPdfConfig();
        if ( pdfConfig == null ) {
            return null;
        }
        FormatDto.MarginDto margins = pdfConfig.getMargins();
        if ( margins == null ) {
            return null;
        }
        BigDecimal right = margins.getRight();
        if ( right == null ) {
            return null;
        }
        return right;
    }

    private String dtoPdfConfigPaperType(FormatDto formatDto) {
        if ( formatDto == null ) {
            return null;
        }
        FormatDto.PdfConfigDto pdfConfig = formatDto.getPdfConfig();
        if ( pdfConfig == null ) {
            return null;
        }
        String paperType = pdfConfig.getPaperType();
        if ( paperType == null ) {
            return null;
        }
        return paperType;
    }

    private Boolean dtoPdfConfigContinues(FormatDto formatDto) {
        if ( formatDto == null ) {
            return null;
        }
        FormatDto.PdfConfigDto pdfConfig = formatDto.getPdfConfig();
        if ( pdfConfig == null ) {
            return null;
        }
        Boolean continues = pdfConfig.getContinues();
        if ( continues == null ) {
            return null;
        }
        return continues;
    }

    private String dtoPdfConfigFontFamily(FormatDto formatDto) {
        if ( formatDto == null ) {
            return null;
        }
        FormatDto.PdfConfigDto pdfConfig = formatDto.getPdfConfig();
        if ( pdfConfig == null ) {
            return null;
        }
        String fontFamily = pdfConfig.getFontFamily();
        if ( fontFamily == null ) {
            return null;
        }
        return fontFamily;
    }

    protected List<FormatPageEntity> pageDtoListToFormatPageEntityList(List<FormatDto.PageDto> list) {
        if ( list == null ) {
            return null;
        }

        List<FormatPageEntity> list1 = new ArrayList<FormatPageEntity>( list.size() );
        for ( FormatDto.PageDto pageDto : list ) {
            list1.add( toPageEntity( pageDto ) );
        }

        return list1;
    }
}
