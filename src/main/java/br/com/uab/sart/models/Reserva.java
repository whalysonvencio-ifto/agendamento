package br.com.uab.sart.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "professor_id")
    private Usuario professor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "equipamento_id")
    private Equipamento equipamento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "turma_id")
    private Turma turma;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private Integer horarioAula;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusReserva status = StatusReserva.ATIVA;

    @Column(nullable = false)
    private Boolean ativo = true;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getProfessor() { return professor; }
    public void setProfessor(Usuario professor) { this.professor = professor; }
    public Equipamento getEquipamento() { return equipamento; }
    public void setEquipamento(Equipamento equipamento) { this.equipamento = equipamento; }
    public Turma getTurma() { return turma; }
    public void setTurma(Turma turma) { this.turma = turma; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public Integer getHorarioAula() { return horarioAula; }
    public void setHorarioAula(Integer horarioAula) { this.horarioAula = horarioAula; }
    public StatusReserva getStatus() { return status; }
    public void setStatus(StatusReserva status) { this.status = status; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
