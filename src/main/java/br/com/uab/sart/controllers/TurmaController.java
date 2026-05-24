package br.com.uab.sart.controllers;

import br.com.uab.sart.models.Turma;
import br.com.uab.sart.repositories.TurmaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/turmas")
public class TurmaController {

    private final TurmaRepository turmaRepository;

    public TurmaController(TurmaRepository turmaRepository) {
        this.turmaRepository = turmaRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("turmas", turmaRepository.findAll());
        return "turmas";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("turma", new Turma());
        return "turma-form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Turma turma) {
        if (turma.getId() == null) {
            turma.setAtivo(true);
        } else {
            Turma existente = turmaRepository.findById(turma.getId()).orElseThrow();
            existente.setNome(turma.getNome());
            turma = existente;
        }
        turmaRepository.save(turma);
        return "redirect:/turmas";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Turma turma = turmaRepository.findById(id).orElseThrow();
        model.addAttribute("turma", turma);
        return "turma-form";
    }

    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        Turma turma = turmaRepository.findById(id).orElseThrow();
        turma.setAtivo(false);
        turmaRepository.save(turma);
        return "redirect:/turmas";
    }
}
