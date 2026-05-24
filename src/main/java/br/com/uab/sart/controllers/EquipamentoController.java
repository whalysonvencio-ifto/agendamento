package br.com.uab.sart.controllers;

import br.com.uab.sart.models.Equipamento;
import br.com.uab.sart.models.StatusEquipamento;
import br.com.uab.sart.repositories.EquipamentoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/equipamentos")
public class EquipamentoController {

    private final EquipamentoRepository equipamentoRepository;

    public EquipamentoController(EquipamentoRepository equipamentoRepository) {
        this.equipamentoRepository = equipamentoRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("equipamentos", equipamentoRepository.findAll());
        return "equipamentos";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("equipamento", new Equipamento());
        return "equipamento-form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Equipamento equipamento) {
        if (equipamento.getId() == null) {
            equipamento.setStatus(StatusEquipamento.DISPONIVEL);
            equipamento.setAtivo(true);
        } else {
            Equipamento existente = equipamentoRepository.findById(equipamento.getId()).orElseThrow();
            existente.setNome(equipamento.getNome());
            existente.setDescricao(equipamento.getDescricao());
            equipamento = existente;
        }
        equipamentoRepository.save(equipamento);
        return "redirect:/equipamentos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Equipamento equipamento = equipamentoRepository.findById(id).orElseThrow();
        model.addAttribute("equipamento", equipamento);
        return "equipamento-form";
    }

    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        Equipamento equipamento = equipamentoRepository.findById(id).orElseThrow();
        equipamento.setAtivo(false);
        equipamentoRepository.save(equipamento);
        return "redirect:/equipamentos";
    }
}
