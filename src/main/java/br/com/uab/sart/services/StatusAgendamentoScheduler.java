package br.com.uab.sart.services;

import br.com.uab.sart.models.Reserva;
import br.com.uab.sart.models.StatusEquipamento;
import br.com.uab.sart.models.StatusReserva;
import br.com.uab.sart.repositories.EquipamentoRepository;
import br.com.uab.sart.repositories.ReservaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
@SuppressWarnings("null")
public class StatusAgendamentoScheduler {

    private final ReservaRepository reservaRepository;
    private final EquipamentoRepository equipamentoRepository;
    private Clock clock;

    private static final Map<Integer, LocalTime> HORARIOS_FIM = Map.of(
            1, LocalTime.of(9, 0),
            2, LocalTime.of(10, 0),
            3, LocalTime.of(11, 0),
            4, LocalTime.of(12, 0),
            5, LocalTime.of(14, 0),
            6, LocalTime.of(15, 0),
            7, LocalTime.of(16, 0),
            8, LocalTime.of(17, 0)
    );

    public StatusAgendamentoScheduler(ReservaRepository reservaRepository, EquipamentoRepository equipamentoRepository) {
        this.reservaRepository = reservaRepository;
        this.equipamentoRepository = equipamentoRepository;
        this.clock = Clock.systemDefaultZone();
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Scheduled(fixedRate = 60000)
    public void verificarAtrasos() {
        LocalDateTime agora = LocalDateTime.now(clock);
        
        List<Reserva> reservasAtivas = reservaRepository.findByStatusIn(List.of(StatusReserva.ATIVA));

        for (Reserva reserva : reservasAtivas) {
            LocalTime horaFimAula = HORARIOS_FIM.get(reserva.getHorarioAula());
            if (horaFimAula == null) continue;
            
            LocalDateTime limite = LocalDateTime.of(reserva.getData(), horaFimAula).plusMinutes(5);

            if (agora.isAfter(limite)) {
                reserva.setStatus(StatusReserva.ATRASADA);
                reservaRepository.save(reserva);

                if (reserva.getEquipamento() != null) {
                    reserva.getEquipamento().setStatus(StatusEquipamento.ATRASADO);
                    equipamentoRepository.save(reserva.getEquipamento());
                }
            }
        }
    }
}
