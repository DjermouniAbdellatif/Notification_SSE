package com.API.Documents_Management.WebSocket.Mapper;

import com.API.Documents_Management.Direction.Direction;
import com.API.Documents_Management.Direction.DirectionRepo;
import com.API.Documents_Management.Division.Division;
import com.API.Documents_Management.Division.DivisionRepo;
import com.API.Documents_Management.SousDirection.SousDierctionRepo;
import com.API.Documents_Management.SousDirection.SousDirection;
import com.API.Documents_Management.WebSocket.Dto.NotificationDTO;
import com.API.Documents_Management.WebSocket.Entities.NotificationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class NotificationMapper {

    @Autowired
    protected DivisionRepo divisionRepo;

    @Autowired
    protected DirectionRepo directionRepo;

    @Autowired
    protected SousDierctionRepo sousDirectionRepo;

    @Mapping(source = "divisionId", target = "DivisionName", qualifiedByName = "mapDivisionIdToName")
    @Mapping(source = "directionId", target = "DirectionName", qualifiedByName = "mapDirectionIdToName")
    @Mapping(source = "sousDirectionId", target = "SousDirectionName", qualifiedByName = "mapSousDirectionIdToName")
    @Mapping(source = "time", target = "time", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    @Mapping(source = "id", target = "id")
    public abstract NotificationDTO toDto(NotificationEntity entity);

    @Mapping(source = "divisionName", target = "divisionId", qualifiedByName = "mapDivisionNameToId")
    @Mapping(source = "directionName", target = "directionId", qualifiedByName = "mapDirectionNameToId")
    @Mapping(source = "sousDirectionName", target = "sousDirectionId", qualifiedByName = "mapSousDirectionNameToId")
    @Mapping(source = "id", target = "id")
    public abstract NotificationEntity toEntity(NotificationDTO dto);

    @Named("mapDivisionIdToName")
    protected String mapDivisionIdToName(Long id) {
        if (id == null) return null;
        return divisionRepo.findById(id).map(Division::getName).orElse(null);
    }

    @Named("mapDirectionIdToName")
    protected String mapDirectionIdToName(Long id) {
        if (id == null) return null;
        return directionRepo.findById(id).map(Direction::getName).orElse(null);
    }

    @Named("mapSousDirectionIdToName")
    protected String mapSousDirectionIdToName(Long id) {
        if (id == null) return null;
        return sousDirectionRepo.findById(id).map(SousDirection::getName).orElse(null);
    }

    @Named("mapDivisionNameToId")
    protected Long mapDivisionNameToId(String name) {
        if (name == null || name.isBlank()) return null;
        return divisionRepo.findByName(name).map(Division::getId).orElse(null);
    }

    @Named("mapDirectionNameToId")
    protected Long mapDirectionNameToId(String name) {
        if (name == null || name.isBlank()) return null;
        return directionRepo.findByName(name).map(Direction::getId).orElse(null);
    }

    @Named("mapSousDirectionNameToId")
    protected Long mapSousDirectionNameToId(String name) {
        if (name == null || name.isBlank()) return null;
        return sousDirectionRepo.findByName(name).map(SousDirection::getId).orElse(null);
    }
}
