package ch.hftm.relationaldatabases.transferdemo.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NoteForm {
  private UUID uuid;
  private String note;
}
