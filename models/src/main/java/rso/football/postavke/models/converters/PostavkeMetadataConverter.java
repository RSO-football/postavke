package rso.football.postavke.models.converters;

import rso.football.postavke.lib.PostavkeMetadata;
import rso.football.postavke.models.entities.PostavkeMetadataEntity;

public class PostavkeMetadataConverter {

    public static PostavkeMetadata toDto(PostavkeMetadataEntity entity) {

        PostavkeMetadata dto = new PostavkeMetadata();
        dto.setPostavkaId(entity.getId());
        dto.setUporabnikID(entity.getUporabnikID());
        dto.setPay(entity.getPay());

        return dto;
    }

    public static PostavkeMetadataEntity toEntity(PostavkeMetadata dto) {

        PostavkeMetadataEntity entity = new PostavkeMetadataEntity();
        entity.setUporabnikID(dto.getUporabnikID());
        entity.setPay(dto.getPay());

        return entity;
    }

}
