# Sistema de Agendamento de Recursos Tecnológicos (SART)

O SART é um sistema web projetado para simplificar, centralizar e organizar o agendamento de equipamentos tecnológicos (projetores, caixas de som, notebooks, etc.) para uso em aulas, eventos e outras atividades escolares.

## 📍 Contexto e Visão Geral

A escola alvo é a **Escola de Tempo Integral Vinícius de Moraes**, localizada em Palmas - TO. 

O projeto nasce para resolver um problema grave de logística interna: o atual agendamento manual (em papel) de recursos tecnológicos causa conflitos de horários, perda de rastreabilidade física dos equipamentos e inviabiliza a criação de um histórico de uso ou controle de inventário. 

A solução proposta substitui o caderno físico por uma plataforma web centralizada. O sistema é moldado especificamente para a rotina da escola, utilizando os **8 horários de aula** (4 pela manhã, 4 pela tarde) como base de tempo. Isso garante reservas sem conflitos e uma gestão eficiente.

### 🕒 Grade de Horários
- **Manhã:**
  - 1ª Aula: 08:00 - 09:00
  - 2ª Aula: 09:00 - 10:00
  - 3ª Aula: 10:00 - 11:00
  - 4ª Aula: 11:00 - 12:00
- **Tarde:**
  - 1ª Aula: 13:00 - 14:00
  - 2ª Aula: 14:00 - 15:00 
  - 3ª Aula: 15:00 - 16:00
  - 4ª Aula: 16:00 - 17:00

## ⚠️ O Problema
Atualmente, o processo manual resulta em:
- **Conflitos de horário:** Duas ou mais reservas para o mesmo equipamento no mesmo momento.
- **Extravios temporários:** Equipamentos "perdidos" na escola devido à falta de rastreamento de quem está com o aparelho.
- **Falta de gestão:** Inexistência de histórico de agendamentos e dificuldades no controle do estado de conservação e manutenção dos itens.

## 🎯 O Diferencial
Diferente de uma agenda comum baseada em horas ou minutos (que permite reservas em horários "quebrados"), o SART é **estritamente moldado na rotina da escola de tempo integral**. O sistema opera exclusivamente com os horários fixos das aulas (1º ao 4º horário). Isso simplifica a interface, agiliza a reserva e elimina completamente a possibilidade de sobreposição de horários.

## 📈 Métricas de Sucesso
- Redução a zero dos conflitos de reserva.
- Rastreabilidade total dos equipamentos.
- Geração de relatórios precisos de utilização (por professor e por equipamento).

## 👥 Perfis de Usuário e Casos de Uso

### 👨‍🏫 Professor
Usuário focado na utilização dos recursos. Precisa de agilidade e zero burocracia.
- Fazer login no sistema.
- Visualizar um dashboard com seu histórico pessoal e reservas futuras.
- Visualizar o catálogo de equipamentos disponíveis na escola.
- Realizar e cancelar suas reservas.

### 👨‍💻 Administrador (Servidor / Gestor)
Servidor focado no controle, manutenção e auditoria.
- Fazer login no sistema.
- Visualizar dashboard com a fila de trabalho diária (entregas e recolhas previstas para o dia).
- Gerenciar (CRUD) Usuários (Professores e outros Administradores).
- Gerenciar (CRUD) Turmas.
- Gerenciar (CRUD) Equipamentos.
- Gerar e exportar relatórios gerenciais em PDF.
- Confirmar entrega e devolução de equipamentos.

## ⚙️ Regras de Negócio e Sistema

- **Regra da Grade (Horários Rígidos):** Zero flexibilidade de horários. Agendamentos são atrelados OBRIGATORIAMENTE à data, equipamento, turma e a 1 dos 8 horários de aula.
- **Prevenção de Conflitos:** Bloqueio sistêmico rigoroso de duplicidade de reserva (mesmo `id_equipamento`, mesma `data`, mesmo `horario_aula`).
- **Regra dos 5 Minutos (Bloqueio):** Bloqueio sistêmico de novas reservas caso faltem 5 minutos ou menos para o início do horário de aula, forçando o hábito de planejamento prévio.
- **Máquina de Estados de Equipamento:** O sistema trabalhará com os seguintes status: `DISPONIVEL`, `RESERVADO`, `EM_USO`, `ATRASADO`, `MANUTENCAO`.
- **Soft Delete:** Nenhum registro é deletado fisicamente do banco de dados para garantir a integridade do histórico (obrigatoriedade de soft delete).
- **Atraso Automático:** Uma rotina em background (`@Scheduled`) muda automaticamente o status da reserva para `ATRASADO` (com destaque visual, ex: vermelho) se o horário de aula acabou e o administrador não confirmou a devolução em até 5 minutos após o término da aula.
- **Manutenção Súbita:** Se um equipamento for marcado como `MANUTENCAO`, todas as reservas futuras (daquele dia em diante) são automaticamente canceladas e os envolvidos são notificados.

## 🛠️ Requisitos e Tecnologias Previstas

*Com base nas definições preliminares do documento original:*
- **Autenticação e Perfis:** Login nativo via Spring Security com papéis `ROLE_PROFESSOR` e `ROLE_SERVIDOR`.
- **Backend:** Java com Spring Boot (inferido pelo uso de `@Scheduled` e `Spring Security`).
- **Banco de Dados:** Relacional, com suporte a Soft Delete em todas as entidades principais.
- **Relatórios:** Geração de relatórios em formato PDF.
