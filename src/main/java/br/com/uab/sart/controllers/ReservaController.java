package br.com.uab.sart.controllers;

import br.com.uab.sart.exceptions.EquipamentoIndisponivelException;
import br.com.uab.sart.exceptions.TempoAntecedenciaInvalidoException;
import br.com.uab.sart.models.Usuario;
import br.com.uab.sart.repositories.EquipamentoRepository;
import br.com.uab.sart.repositories.TurmaRepository;
import br.com.uab.sart.repositories.UsuarioRepository;
import br.com.uab.sart.services.AgendamentoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/reservas")
public class ReservaController {

    private final AgendamentoService agendamentoService;
    private final EquipamentoRepository equipamentoRepository;
    private final TurmaRepository turmaRepository;
    private final UsuarioRepository usuarioRepository;

    public ReservaController(AgendamentoService agendamentoService, EquipamentoRepository equipamentoRepository, TurmaRepository turmaRepository, UsuarioRepository usuarioRepository) {
        this.agendamentoService = agendamentoService;
        this.equipamentoRepository = equipamentoRepository;
        this.turmaRepository = turmaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/nova")
    public String novaReserva(Model model) {
        model.addAttribute("equipamentos", equipamentoRepository.findByAtivoTrue());
        model.addAttribute("turmas", turmaRepository.findByAtivoTrue());
        // Defaults to today
        model.addAttribute("dataAtual", LocalDate.now());
        return "reserva-form";
    }

    @PostMapping("/salvar")
    public String salvar(
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam("horariosAula") List<Integer> horariosAula,
            @RequestParam("equipamentosIds") List<Long> equipamentosIds,
            @RequestParam("turmaId") Long turmaId,
            Authentication authentication,
            Model model) {

        String email = authentication.getName();
        Usuario professor = usuarioRepository.findByEmail(email).orElseThrow();

        try {
            agendamentoService.criarReserva(professor.getId(), equipamentosIds, turmaId, data, horariosAula);
            return "redirect:/";
        } catch (EquipamentoIndisponivelException | TempoAntecedenciaInvalidoException | IllegalArgumentException e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("equipamentos", equipamentoRepository.findByAtivoTrue());
            model.addAttribute("turmas", turmaRepository.findByAtivoTrue());
            
            // Retain previous selections
            model.addAttribute("dataSelecionada", data);
            model.addAttribute("horariosSelecionados", horariosAula);
            model.addAttribute("equipamentosSelecionados", equipamentosIds);
            model.addAttribute("turmaSelecionada", turmaId);
            
            return "reserva-form";
        }
    }
}
