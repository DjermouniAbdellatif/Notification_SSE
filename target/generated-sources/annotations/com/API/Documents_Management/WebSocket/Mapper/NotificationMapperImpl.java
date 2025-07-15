package com.API.Documents_Management.WebSocket.Mapper;

import com.API.Documents_Management.WebSocket.Dto.NotificationDTO;
import com.API.Documents_Management.WebSocket.Entities.NotificationEntity;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-14T15:40:20+0200",
    comments = "version: 1.5.3.Final, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class NotificationMapperImpl extends NotificationMapper {

    @Override
    public NotificationDTO toDto(NotificationEntity entity) {
        if ( entity == null ) {
            return null;
        }

        NotificationDTO.NotificationDTOBuilder notificationDTO = NotificationDTO.builder();

        notificationDTO.DivisionName( mapDivisionIdToName( entity.getDivisionId() ) );
        notificationDTO.DirectionName( mapDirectionIdToName( entity.getDirectionId() ) );
        notificationDTO.SousDirectionName( mapSousDirectionIdToName( entity.getSousDirectionId() ) );
        notificationDTO.time( entity.getTime() );
        notificationDTO.id( entity.getId() );
        notificationDTO.email( entity.getEmail() );
        notificationDTO.message( entity.getMessage() );
        notificationDTO.courrielNumber( entity.getCourrielNumber() );
        Set<String> set = entity.getFilesNames();
        if ( set != null ) {
            notificationDTO.filesNames( new LinkedHashSet<String>( set ) );
        }
        notificationDTO.operation( entity.getOperation() );

        return notificationDTO.build();
    }

    @Override
    public NotificationEntity toEntity(NotificationDTO dto) {
        if ( dto == null ) {
            return null;
        }

        NotificationEntity.NotificationEntityBuilder notificationEntity = NotificationEntity.builder();

        notificationEntity.divisionId( mapDivisionNameToId( dto.getDivisionName() ) );
        notificationEntity.directionId( mapDirectionNameToId( dto.getDirectionName() ) );
        notificationEntity.sousDirectionId( mapSousDirectionNameToId( dto.getSousDirectionName() ) );
        notificationEntity.id( dto.getId() );
        notificationEntity.email( dto.getEmail() );
        notificationEntity.message( dto.getMessage() );
        notificationEntity.courrielNumber( dto.getCourrielNumber() );
        Set<String> set = dto.getFilesNames();
        if ( set != null ) {
            notificationEntity.filesNames( new LinkedHashSet<String>( set ) );
        }
        notificationEntity.operation( dto.getOperation() );
        notificationEntity.time( dto.getTime() );

        return notificationEntity.build();
    }
}
