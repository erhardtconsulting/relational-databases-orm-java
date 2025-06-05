package ch.hftm.relationaldatabases.transferdemo.dtos;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Builder
@Value
public class Note {
  UUID uuid;
  String note;
  Instant createdAt;
  Instant updatedAt;
}
