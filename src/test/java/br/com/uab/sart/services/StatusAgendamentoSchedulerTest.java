package br.com.uab.sart.services;

import br.com.uab.sart.models.Equipamento;
import br.com.uab.sart.models.Reserva;
import br.com.uab.sart.models.StatusEquipamento;
import br.com.uab.sart.models.StatusReserva;
import br.com.uab.sart.repositories.EquipamentoRepository;
import br.com.uab.sart.repositories.ReservaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class StatusAgendamentoSchedulerTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private EquipamentoRepository equipamentoRepository;

    @InjectMocks
    private StatusAgendamentoScheduler scheduler;

    @Test
    void testeEquipamentoNaoDevolvidoNoPrazo() {
        // Horario 1 termina 09:00. Limite é 09:05.
        LocalDate hoje = LocalDate.now();
        LocalDateTime agora = LocalDateTime.of(hoje, LocalTime.of(9, 6)); // Já passou do limite
        scheduler.setClock(Clock.fixed(agora.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault()));

        Reserva reserva = new Reserva();
        reserva.setData(hoje);
        reserva.setHorarioAula(1);
        reserva.setStatus(StatusReserva.ATIVA);
        
        Equipamento eq = new Equipamento();
        eq.setStatus(StatusEquipamento.EM_USO);
        reserva.setEquipamento(eq);

        when(reservaRepository.findByStatusIn(List.of(StatusReserva.ATIVA)))
                .thenReturn(List.of(reserva));

        scheduler.verificarAtrasos();

        verify(reservaRepository).save(reserva);
        verify(equipamentoRepository).save(eq);
        assert(reserva.getStatus() == StatusReserva.ATRASADA);
        assert(eq.getStatus() == StatusEquipamento.ATRASADO);
    }

    @Test
    void testeDentroDaMargemDeTolerancia() {
        LocalDate hoje = LocalDate.now();
        LocalDateTime agora = LocalDateTime.of(hoje, LocalTime.of(9, 4)); // Dentro do limite
        scheduler.setClock(Clock.fixed(agora.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault()));

        Reserva reserva = new Reserva();
        reserva.setData(hoje);
        reserva.setHorarioAula(1);
        reserva.setStatus(StatusReserva.ATIVA);

        when(reservaRepository.findByStatusIn(List.of(StatusReserva.ATIVA)))
                .thenReturn(List.of(reserva));

        scheduler.verificarAtrasos();

        verify(reservaRepository, never()).save(any());
        verify(equipamentoRepository, never()).save(any());
    }
}
