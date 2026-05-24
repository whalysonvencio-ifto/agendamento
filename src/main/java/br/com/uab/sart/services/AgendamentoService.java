package br.com.uab.sart.services;

import br.com.uab.sart.exceptions.EquipamentoIndisponivelException;
import br.com.uab.sart.exceptions.TempoAntecedenciaInvalidoException;
import br.com.uab.sart.models.*;
import br.com.uab.sart.repositories.EquipamentoRepository;
import br.com.uab.sart.repositories.ReservaRepository;
import br.com.uab.sart.repositories.TurmaRepository;
import br.com.uab.sart.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("null")
public class AgendamentoService {

    private final ReservaRepository reservaRepository;
    private final EquipamentoRepository equipamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TurmaRepository turmaRepository;
    private Clock clock;

    private static final Map<Integer, LocalTime> HORARIOS_INICIO = Map.of(
            1, LocalTime.of(8, 0),
            2, LocalTime.of(9, 0),
            3, LocalTime.of(10, 0),
            4, LocalTime.of(11, 0),
            5, LocalTime.of(13, 0),
            6, LocalTime.of(14, 0),
            7, LocalTime.of(15, 0),
            8, LocalTime.of(16, 0)
    );

    public AgendamentoService(ReservaRepository reservaRepository, EquipamentoRepository equipamentoRepository,
                              UsuarioRepository usuarioRepository, TurmaRepository turmaRepository) {
        this.reservaRepository = reservaRepository;
        this.equipamentoRepository = equipamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.turmaRepository = turmaRepository;
        this.clock = Clock.systemDefaultZone();
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Transactional
    public List<Reserva> criarReserva(Long idProf, List<Long> idsEq, Long idTurma, LocalDate data, List<Integer> horariosAula) {
        if (idsEq == null || idsEq.isEmpty()) {
            throw new IllegalArgumentException("Nenhum equipamento selecionado.");
        }
        if (horariosAula == null || horariosAula.isEmpty()) {
            throw new IllegalArgumentException("Nenhum horário selecionado.");
        }

        List<Integer> horariosOrdenados = new ArrayList<>(horariosAula);
        Collections.sort(horariosOrdenados);
        for (int i = 0; i < horariosOrdenados.size() - 1; i++) {
            if (horariosOrdenados.get(i + 1) != horariosOrdenados.get(i) + 1) {
                throw new IllegalArgumentException("Os horários selecionados devem ser consecutivos.");
            }
        }

        LocalDateTime agora = LocalDateTime.now(clock);
        LocalDate hoje = agora.toLocalDate();
        LocalTime horaAtual = agora.toLocalTime();

        if (data.isBefore(hoje)) {
            throw new TempoAntecedenciaInvalidoException("Não é possível reservar para datas passadas.");
        }

        Usuario professor = usuarioRepository.findById(idProf)
                .orElseThrow(() -> new IllegalArgumentException("Professor não encontrado"));

        Turma turma = turmaRepository.findById(idTurma)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada"));

        List<Reserva> reservasCriadas = new ArrayList<>();

        for (Long idEq : idsEq) {
            Equipamento equipamento = equipamentoRepository.findById(idEq)
                    .orElseThrow(() -> new IllegalArgumentException("Equipamento não encontrado"));

            if (equipamento.getStatus() == StatusEquipamento.MANUTENCAO) {
                throw new EquipamentoIndisponivelException("Equipamento '" + equipamento.getNome() + "' em manutenção.");
            }

            for (Integer horarioAula : horariosOrdenados) {
                if (!HORARIOS_INICIO.containsKey(horarioAula)) {
                    throw new IllegalArgumentException("Horário de aula inválido.");
                }

                LocalTime horarioInicioAula = HORARIOS_INICIO.get(horarioAula);

                if (data.equals(hoje)) {
                    if (horaAtual.isAfter(horarioInicioAula.minusMinutes(5))) {
                        throw new TempoAntecedenciaInvalidoException("Tempo mínimo de antecedência não respeitado (Regra 5 minutos) para o horário " + horarioAula);
                    }
                }

                Optional<Reserva> conflito = reservaRepository.findByEquipamentoAndDataAndHorarioAulaAndAtivoTrueAndStatusNot(
                        equipamento, data, horarioAula, StatusReserva.CANCELADA);

                if (conflito.isPresent()) {
                    throw new EquipamentoIndisponivelException("Conflito: Equipamento '" + equipamento.getNome() + "' já reservado no horário " + horarioAula);
                }

                Reserva reserva = new Reserva();
                reserva.setProfessor(professor);
                reserva.setEquipamento(equipamento);
                reserva.setTurma(turma);
                reserva.setData(data);
                reserva.setHorarioAula(horarioAula);
                reserva.setStatus(StatusReserva.ATIVA);

                reservasCriadas.add(reservaRepository.save(reserva));
            }
        }

        return reservasCriadas;
    }

    public java.util.List<Reserva> buscarFilaDoDia(LocalDate data) {
        return reservaRepository.findByAtivoTrueAndDataOrderByHorarioAulaAsc(data);
    }

    public java.util.List<Reserva> buscarHistoricoDoProfessor(Usuario professor) {
        return reservaRepository.findByAtivoTrueAndProfessorOrderByDataDesc(professor);
    }

    public void confirmarEntrega(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva não encontrada"));
        
        if (reserva.getStatus() == StatusReserva.ATIVA) {
            reserva.setStatus(StatusReserva.EM_USO);
            Equipamento eq = reserva.getEquipamento();
            eq.setStatus(StatusEquipamento.EM_USO);
            equipamentoRepository.save(eq);
            reservaRepository.save(reserva);
        }
    }

    public void confirmarDevolucao(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva não encontrada"));
        
        if (reserva.getStatus() == StatusReserva.EM_USO || reserva.getStatus() == StatusReserva.ATRASADA) {
            reserva.setStatus(StatusReserva.CONCLUIDA);
            Equipamento eq = reserva.getEquipamento();
            eq.setStatus(StatusEquipamento.DISPONIVEL);
            equipamentoRepository.save(eq);
            reservaRepository.save(reserva);
        }
    }
}
