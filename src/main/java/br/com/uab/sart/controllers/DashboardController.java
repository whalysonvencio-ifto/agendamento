package br.com.uab.sart.controllers;

import br.com.uab.sart.models.Usuario;
import br.com.uab.sart.repositories.UsuarioRepository;
import br.com.uab.sart.services.AgendamentoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;

@Controller
public class DashboardController {

    private final AgendamentoService agendamentoService;
    private final UsuarioRepository usuarioRepository;

    public DashboardController(AgendamentoService agendamentoService, UsuarioRepository usuarioRepository) {
        this.agendamentoService = agendamentoService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/")
    public String dashboard(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

        if (usuario != null) {
            model.addAttribute("usuarioLogado", usuario.getNome());
            
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"))) {
                model.addAttribute("fila", agendamentoService.buscarFilaDoDia(LocalDate.now()));
                return "dashboard-admin";
            } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PROFESSOR"))) {
                model.addAttribute("historico", agendamentoService.buscarHistoricoDoProfessor(usuario));
                return "dashboard-prof";
            }
        }
        
        return "redirect:/login";
    }

    @PostMapping("/reserva/{id}/entregar")
    public String confirmarEntrega(@PathVariable Long id) {
        agendamentoService.confirmarEntrega(id);
        return "redirect:/";
    }

    @PostMapping("/reserva/{id}/devolver")
    public String confirmarDevolucao(@PathVariable Long id) {
        agendamentoService.confirmarDevolucao(id);
        return "redirect:/";
    }
}
