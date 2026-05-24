package br.com.uab.sart.services;

import br.com.uab.sart.exceptions.EquipamentoIndisponivelException;
import br.com.uab.sart.exceptions.TempoAntecedenciaInvalidoException;
import br.com.uab.sart.models.*;
import br.com.uab.sart.repositories.EquipamentoRepository;
import br.com.uab.sart.repositories.ReservaRepository;
import br.com.uab.sart.repositories.TurmaRepository;
import br.com.uab.sart.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private EquipamentoRepository equipamentoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private TurmaRepository turmaRepository;

    @InjectMocks
    private AgendamentoService agendamentoService;

    @BeforeEach
    void setup() {
    }

    @Test
    void testeRegra5MinutosCritico() {
        // Horario aula 1 começa 08:00
        // Set clock to 07:56 (hoje)
        LocalDate hoje = LocalDate.now();
        LocalDateTime tempo = LocalDateTime.of(hoje, LocalTime.of(7, 56));
        agendamentoService.setClock(Clock.fixed(tempo.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault()));

        Usuario prof = new Usuario();
        prof.setId(1L);
        Turma turma = new Turma();
        turma.setId(1L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(prof));
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));

        assertThatThrownBy(() -> agendamentoService.criarReserva(1L, List.of(1L), 1L, hoje, List.of(1)))
                .isInstanceOf(TempoAntecedenciaInvalidoException.class)
                .hasMessageContaining("Tempo mínimo de antecedência não respeitado (Regra 5 minutos)");
    }

    @Test
    void testeConflitoEquipamento() {
        LocalDate hoje = LocalDate.now();
        LocalDateTime tempo = LocalDateTime.of(hoje, LocalTime.of(7, 0));
        agendamentoService.setClock(Clock.fixed(tempo.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault()));

        Equipamento eq = new Equipamento();
        eq.setId(1L);
        eq.setStatus(StatusEquipamento.DISPONIVEL);

        Usuario prof = new Usuario();
        prof.setId(1L);
        Turma turma = new Turma();
        turma.setId(1L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(prof));
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));

        when(equipamentoRepository.findById(1L)).thenReturn(Optional.of(eq));
        
        Reserva existente = new Reserva();
        when(reservaRepository.findByEquipamentoAndDataAndHorarioAulaAndAtivoTrueAndStatusNot(eq, hoje, 2, StatusReserva.CANCELADA))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> agendamentoService.criarReserva(1L, List.of(1L), 1L, hoje, List.of(2)))
                .isInstanceOf(EquipamentoIndisponivelException.class)
                .hasMessageContaining("Conflito: Equipamento");
    }

    @Test
    void testeEquipamentoEmManutencao() {
        LocalDate hoje = LocalDate.now();
        LocalDateTime tempo = LocalDateTime.of(hoje, LocalTime.of(7, 0));
        agendamentoService.setClock(Clock.fixed(tempo.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault()));

        Equipamento eq = new Equipamento();
        eq.setId(1L);
        eq.setStatus(StatusEquipamento.MANUTENCAO);

        when(equipamentoRepository.findById(1L)).thenReturn(Optional.of(eq));

        assertThatThrownBy(() -> agendamentoService.criarReserva(1L, List.of(1L), 1L, hoje, List.of(1)))
                .isInstanceOf(EquipamentoIndisponivelException.class)
                .hasMessageContaining("em manutenção");
    }

    @Test
    void testeCenarioFeliz() {
        LocalDate hoje = LocalDate.now();
        LocalDateTime tempo = LocalDateTime.of(hoje, LocalTime.of(7, 0));
        agendamentoService.setClock(Clock.fixed(tempo.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault()));

        Equipamento eq = new Equipamento();
        eq.setId(1L);
        eq.setStatus(StatusEquipamento.DISPONIVEL);

        Usuario prof = new Usuario();
        prof.setId(1L);

        Turma turma = new Turma();
        turma.setId(1L);

        when(equipamentoRepository.findById(1L)).thenReturn(Optional.of(eq));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(prof));
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
        when(reservaRepository.findByEquipamentoAndDataAndHorarioAulaAndAtivoTrueAndStatusNot(eq, hoje, 1, StatusReserva.CANCELADA))
                .thenReturn(Optional.empty());

        when(reservaRepository.save(any(Reserva.class))).thenAnswer(i -> i.getArguments()[0]);

        List<Reserva> salvas = agendamentoService.criarReserva(1L, List.of(1L), 1L, hoje, List.of(1));

        assertThat(salvas).isNotEmpty();
        assertThat(salvas.get(0).getStatus()).isEqualTo(StatusReserva.ATIVA);
        verify(reservaRepository).save(any(Reserva.class));
    }
}
