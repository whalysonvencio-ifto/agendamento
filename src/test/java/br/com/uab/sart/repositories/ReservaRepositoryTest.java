package br.com.uab.sart.repositories;

import br.com.uab.sart.models.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReservaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReservaRepository reservaRepository;

    @Test
    void deveEncontrarConflitoDeReserva() {
        Usuario prof = new Usuario();
        prof.setNome("Prof Teste");
        prof.setEmail("prof@teste.com");
        prof.setSenha("123");
        prof.setPerfil(Role.ROLE_PROFESSOR);
        entityManager.persist(prof);

        Equipamento eq = new Equipamento();
        eq.setNome("Projetor");
        entityManager.persist(eq);

        Turma turma = new Turma();
        turma.setNome("1A");
        entityManager.persist(turma);

        Reserva reserva = new Reserva();
        reserva.setProfessor(prof);
        reserva.setEquipamento(eq);
        reserva.setTurma(turma);
        reserva.setData(LocalDate.now());
        reserva.setHorarioAula(1);
        reserva.setStatus(StatusReserva.ATIVA);
        entityManager.persist(reserva);
        entityManager.flush();

        Optional<Reserva> conflito = reservaRepository.findByEquipamentoAndDataAndHorarioAulaAndAtivoTrueAndStatusNot(
                eq, LocalDate.now(), 1, StatusReserva.CANCELADA);

        assertThat(conflito).isPresent();
        assertThat(conflito.get().getEquipamento().getNome()).isEqualTo("Projetor");
    }
}
