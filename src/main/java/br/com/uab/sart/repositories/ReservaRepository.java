package br.com.uab.sart.repositories;

import br.com.uab.sart.models.Equipamento;
import br.com.uab.sart.models.Reserva;
import br.com.uab.sart.models.StatusReserva;
import br.com.uab.sart.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    
    Optional<Reserva> findByEquipamentoAndDataAndHorarioAulaAndAtivoTrueAndStatusNot(
            Equipamento equipamento, LocalDate data, Integer horarioAula, StatusReserva statusIgnorado);

    List<Reserva> findByAtivoTrueAndProfessorOrderByDataDesc(Usuario professor);

    List<Reserva> findByAtivoTrueAndDataOrderByHorarioAulaAsc(LocalDate data);

    List<Reserva> findByStatusIn(List<StatusReserva> statusList);
}
