package ch.hftm.relationaldatabases.transferdemo.web;

import ch.hftm.relationaldatabases.transferdemo.dtos.NoteForm;
import ch.hftm.relationaldatabases.transferdemo.mappers.NoteFormMapper;
import ch.hftm.relationaldatabases.transferdemo.services.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class NoteController {
  private final NoteService service;
  private final NoteFormMapper mapper;

  @GetMapping("/")
  public String listNotes(Model model) {
    var notes = service.getAll();
    model.addAttribute("notes", notes);
    return "notes/list";
  }

  @GetMapping("/notes/new")
  public String showCreateForm(Model model) {
    model.addAttribute("noteForm", new NoteForm());
    model.addAttribute("isEdit", false);
    return "notes/form";
  }

  @GetMapping("/notes/{uuid}/edit")
  public String showEditForm(@PathVariable UUID uuid, Model model, RedirectAttributes redirectAttributes) {
    var noteOpt = service.findById(uuid);
    if (noteOpt.isEmpty()) {
      redirectAttributes.addFlashAttribute("error", "Note not found");
      return "redirect:/";
    }

    var noteForm = mapper.toForm(noteOpt.get());
    model.addAttribute("noteForm", noteForm);
    model.addAttribute("isEdit", true);
    return "notes/form";
  }

  @PostMapping("/notes")
  public String saveNote(@ModelAttribute NoteForm noteForm, RedirectAttributes redirectAttributes) {
    try {
      var note = mapper.toDto(noteForm);
      service.upsert(note);

      var action = noteForm.getUuid() == null ? "created" : "updated";
      redirectAttributes.addFlashAttribute("success", String.format("Note %s successfully", action));
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", String.format("Error saving note: %s", e.getMessage()));
    }

    return "redirect:/";
  }

  @PostMapping("/notes/{uuid}/delete")
  public String deleteNote(@PathVariable UUID uuid, RedirectAttributes redirectAttributes) {
    try {
      service.deleteById(uuid);
      redirectAttributes.addFlashAttribute("success", "Note deleted successfully");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", String.format("Error deleting note: %s", e.getMessage()));
    }

    return "redirect:/";
  }
}
