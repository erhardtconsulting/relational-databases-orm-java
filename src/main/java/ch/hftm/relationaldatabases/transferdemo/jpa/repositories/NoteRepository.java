package ch.hftm.relationaldatabases.transferdemo.jpa.repositories;

import ch.hftm.relationaldatabases.transferdemo.jpa.entities.NoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface NoteRepository extends JpaRepository<NoteEntity, UUID> {
  @Query("SELECT n FROM NoteEntity n")
  Stream<NoteEntity> streamAllNotes();
}
