package br.com.uab.sart.controllers;

import br.com.uab.sart.config.SecurityConfig;
import br.com.uab.sart.models.Usuario;
import br.com.uab.sart.repositories.UsuarioRepository;
import br.com.uab.sart.security.UserDetailsServiceImpl;
import br.com.uab.sart.services.AgendamentoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@Import(SecurityConfig.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @MockBean
    private AgendamentoService agendamentoService;

    @Test
    @WithMockUser(username = "admin@sart.com", roles = {"ADMINISTRADOR"})
    void deveAcessarDashboardAdmin() throws Exception {
        Usuario admin = new Usuario();
        admin.setNome("Admin Name");
        when(usuarioRepository.findByEmail("admin@sart.com")).thenReturn(Optional.of(admin));
        when(agendamentoService.buscarFilaDoDia(any(LocalDate.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard-admin"))
                .andExpect(model().attributeExists("fila"));
    }

    @Test
    @WithMockUser(username = "prof@sart.com", roles = {"PROFESSOR"})
    void deveAcessarDashboardProf() throws Exception {
        Usuario prof = new Usuario();
        prof.setNome("Prof Name");
        when(usuarioRepository.findByEmail("prof@sart.com")).thenReturn(Optional.of(prof));
        when(agendamentoService.buscarHistoricoDoProfessor(prof)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard-prof"))
                .andExpect(model().attributeExists("historico"));
    }
}
