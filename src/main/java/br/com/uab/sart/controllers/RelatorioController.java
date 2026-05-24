package br.com.uab.sart.controllers;

import br.com.uab.sart.repositories.ReservaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/relatorios")
public class RelatorioController {

    private final ReservaRepository reservaRepository;

    public RelatorioController(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    @GetMapping
    public String relatorios(Model model) {
        model.addAttribute("reservas", reservaRepository.findAll(Sort.by(Sort.Direction.DESC, "data")));
        return "relatorios";
    }
}
