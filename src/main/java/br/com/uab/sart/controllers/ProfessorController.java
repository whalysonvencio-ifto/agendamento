package br.com.uab.sart.controllers;

import br.com.uab.sart.models.Role;
import br.com.uab.sart.models.Usuario;
import br.com.uab.sart.repositories.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/professores")
@SuppressWarnings("null")
public class ProfessorController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfessorController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String listar(Model model) {
        List<Usuario> professores = usuarioRepository.findAll().stream()
                .filter(u -> u.getPerfil() == Role.ROLE_PROFESSOR).toList();
        model.addAttribute("professores", professores);
        return "professores";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("professor", new Usuario());
        return "professor-form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Usuario professor) {
        if (professor.getId() == null) {
            professor.setPerfil(Role.ROLE_PROFESSOR);
            professor.setSenha(passwordEncoder.encode(professor.getSenha()));
            professor.setAtivo(true);
        } else {
            Usuario existente = usuarioRepository.findById(professor.getId()).orElseThrow();
            existente.setNome(professor.getNome());
            existente.setEmail(professor.getEmail());
            professor = existente;
        }
        usuarioRepository.save(professor);
        return "redirect:/professores";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Usuario professor = usuarioRepository.findById(id).orElseThrow();
        model.addAttribute("professor", professor);
        return "professor-form";
    }

    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        Usuario professor = usuarioRepository.findById(id).orElseThrow();
        professor.setAtivo(false);
        usuarioRepository.save(professor);
        return "redirect:/professores";
    }
}
