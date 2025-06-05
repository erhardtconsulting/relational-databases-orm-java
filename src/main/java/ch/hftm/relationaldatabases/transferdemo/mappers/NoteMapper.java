package ch.hftm.relationaldatabases.transferdemo.mappers;

import ch.hftm.relationaldatabases.transferdemo.dtos.Note;
import ch.hftm.relationaldatabases.transferdemo.jpa.entities.NoteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NoteMapper {
  @Mapping(target = "uuid", source = "uuid")
  @Mapping(target = "note", source = "note")
  @Mapping(target = "createdAt", source = "createdAt")
  @Mapping(target = "updatedAt", source = "updatedAt")
  Note toDto(NoteEntity entity);

  @Mapping(target = "uuid", source = "uuid")
  @Mapping(target = "note", source = "note")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  NoteEntity toEntity(Note dto);

  @Mapping(target = "uuid", ignore = true)
  @Mapping(target = "note", source = "note")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntity(Note dto, @MappingTarget NoteEntity entity);
}
