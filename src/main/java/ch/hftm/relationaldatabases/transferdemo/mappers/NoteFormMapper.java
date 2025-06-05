package ch.hftm.relationaldatabases.transferdemo.mappers;

import ch.hftm.relationaldatabases.transferdemo.dtos.Note;
import ch.hftm.relationaldatabases.transferdemo.dtos.NoteForm;
import ch.hftm.relationaldatabases.transferdemo.jpa.entities.NoteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NoteFormMapper {
  @Mapping(target = "uuid", source = "uuid")
  @Mapping(target = "note", source = "note")
  NoteForm toForm(Note entity);

  @Mapping(target = "uuid", source = "uuid")
  @Mapping(target = "note", source = "note")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Note toDto(NoteForm dto);
}
