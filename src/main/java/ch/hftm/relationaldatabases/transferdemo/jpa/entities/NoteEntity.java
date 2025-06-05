package ch.hftm.relationaldatabases.transferdemo.jpa.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "notes")
public class NoteEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "uuid", updatable = false, nullable = false)
  private UUID uuid;

  @Column(name = "note", nullable = false)
  private String note;

  @Column(name = "created_at", nullable = false)
  @CreationTimestamp
  protected Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  @UpdateTimestamp
  protected Instant updatedAt;
}