package ch.hftm.relationaldatabases.transferdemo.services;

import ch.hftm.relationaldatabases.transferdemo.dtos.Note;
import ch.hftm.relationaldatabases.transferdemo.jpa.entities.NoteEntity;
import ch.hftm.relationaldatabases.transferdemo.jpa.repositories.NoteRepository;
import ch.hftm.relationaldatabases.transferdemo.mappers.NoteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoteService {
  private final NoteRepository repository;
  private final NoteMapper mapper;

  @Transactional(readOnly = true)
  public List<Note> getAll() {
    return repository.findAll().stream().map(mapper::toDto).toList();
  }

  @Transactional(readOnly = true)
  public Optional<Note> findById(UUID uuid) {
    return repository.findById(uuid).map(mapper::toDto);
  }

  @Transactional
  public Note upsert(Note note) {
    NoteEntity entity;

    // Check if this is an update (UUID exists) or create (UUID is null)
    if (note.getUuid() != null) {
      var entityOpt = repository.findById(note.getUuid());
      if (entityOpt.isPresent()) {
        entity = entityOpt.get();
        // update entity
        mapper.updateEntity(note, entity);
      } else {
        // UUID provided but entity not found - create new
        entity = mapper.toEntity(note);
      }
    } else {
      // No UUID provided - create new
      entity = mapper.toEntity(note);
    }

    // save updated note
    return mapper.toDto(repository.save(entity));
  }

  @Transactional
  public void deleteById(UUID uuid) {
    repository.deleteById(uuid);
  }
}
