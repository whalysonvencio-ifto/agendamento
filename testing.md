# Plano de Testes (TDD First)

Este plano define a abordagem de desenvolvimento orientado a testes (TDD) para os requisitos e serviços críticos descritos no `especificacao.md`. A estratégia garante cobertura determinística, priorizando regras de negócio rigorosas e evitando regressões.

## 1. Estratégia Geral
- **Camada de Serviço (Regras de Negócio):** Testes unitários puros usando **JUnit 5** e **Mockito** (`@ExtendWith(MockitoExtension.class)`). Dependências (como Repositórios) devem ser estritamente *mockadas*.
- **Camada de Repositório:** Testes de integração de fatia (slice) usando `@DataJpaTest` e banco em memória (H2), validando queries personalizadas.
- **Camada Web (Controllers):** Testes usando `@WebMvcTest` e `MockMvc` com serviços mockados, focando em segurança e roteamento correto.

---

## 2. Especificação dos Testes Críticos por Funcionalidade

### 2.1. Serviço: `AdminSeedConfig`
**Objetivo:** Garantir a criação condicional do administrador primário (Seed).

*   **Teste 1.A (Cenário Crítico - Banco Vazio):**
    *   **Configuração (Given):** `UsuarioRepository.buscarPorPerfilEAtivo(ROLE_ADMINISTRADOR)` retorna uma lista vazia. Variáveis de ambiente configuradas no contexto de teste.
    *   **Ação (When):** Disparar manualmente ou simular a inicialização do `ApplicationReadyEvent`.
    *   **Verificação (Then):** Garantir (Verify) que `UsuarioRepository.save()` foi chamado exatamente 1 vez com um `Usuario` possuindo o email das propriedades e a senha encriptada.

*   **Teste 1.B (Cenário Crítico - Banco já populado):**
    *   **Configuração (Given):** O mock de `UsuarioRepository` retorna uma lista com 1 administrador.
    *   **Ação (When):** Disparar o `ApplicationReadyEvent`.
    *   **Verificação (Then):** Garantir que `UsuarioRepository.save()` **NUNCA** seja chamado.

---

### 2.2. Serviço: `AgendamentoService` (Core Business)
**Objetivo:** Validar motor de regras anti-conflitos e limite de antecedência.

*   **Teste 2.A (Cenário Crítico - Regra dos 5 Minutos):**
    *   **Configuração (Given):** Simular um relógio (`Clock`) travado em "07:56". Professor tenta criar reserva para a data de hoje, 1º horário (que começa às 08:00).
    *   **Ação (When):** Chamar `agendamentoService.criarReserva(...)`.
    *   **Verificação (Then):** O método deve lançar `TempoAntecedenciaInvalidoException`. Nenhuma reserva é salva.

*   **Teste 2.B (Cenário Crítico - Conflito de Equipamento):**
    *   **Configuração (Given):** Relógio em "07:00". Mock do `ReservaRepository` programado para retornar *uma reserva existente* ao consultar o equipamento 'Projetor X' no 2º horário da data atual.
    *   **Ação (When):** Chamar `agendamentoService.criarReserva(...)` para o mesmo projetor, no mesmo horário.
    *   **Verificação (Then):** O método deve lançar `EquipamentoIndisponivelException` indicando conflito. Nenhuma reserva é salva.

*   **Teste 2.C (Cenário Crítico - Equipamento em Manutenção):**
    *   **Configuração (Given):** Mock do `EquipamentoRepository` retorna um equipamento cujo status é `MANUTENCAO`.
    *   **Ação (When):** Tentar criar a reserva.
    *   **Verificação (Then):** O método deve lançar `EquipamentoIndisponivelException`.

*   **Teste 2.D (Cenário Feliz):**
    *   **Configuração (Given):** Equipamento `DISPONIVEL`, horário distante (>5 min), sem conflitos.
    *   **Ação (When):** Tentar criar a reserva.
    *   **Verificação (Then):** O método não lança exceção, cria um objeto `Reserva` com status `ATIVA` e invoca `ReservaRepository.save()`.

---

### 2.3. Serviço: `StatusAgendamentoScheduler`
**Objetivo:** Validar a marcação correta do atraso.

*   **Teste 3.A (Cenário Crítico - Equipamento Não Devolvido no Prazo):**
    *   **Configuração (Given):** O relógio de teste aponta para "09:06". O mock do `ReservaRepository` retorna uma reserva de 1º Horário (fim às 09:00) ainda como `ATIVA` ou `EM_USO`.
    *   **Ação (When):** Executar o método `verificarAtrasos()`.
    *   **Verificação (Then):** Verificar se o status da reserva mudou para `ATRASADA` e se o `save()` foi chamado.

*   **Teste 3.B (Cenário Crítico - Dentro da Margem de Tolerância):**
    *   **Configuração (Given):** Relógio aponta para "09:04" (antes dos 5 min de tolerância). O mock retorna uma reserva do 1º Horário (fim às 09:00).
    *   **Ação (When):** Executar o método `verificarAtrasos()`.
    *   **Verificação (Then):** A reserva permanece inalterada e o método `save()` **NÃO** é acionado.

---

### 2.4. Controladores: `DashboardController` e `AuthController`
**Objetivo:** Validar proteção de rotas (Security) e roteamento de visão corretos.

*   **Teste 4.A (Segurança - Acesso Negado):**
    *   **Ação (When):** Requisição GET via `MockMvc` para `/dashboard` sem usuário logado.
    *   **Verificação (Then):** Resposta HTTP 302 redirecionando para `/login`.

*   **Teste 4.B (Roteamento - Admin):**
    *   **Configuração (Given):** Requisição feita sob `@WithMockUser(roles = "ADMINISTRADOR")`. O mock de `AgendamentoService.buscarFilaTrabalho()` retorna uma lista mockada.
    *   **Ação (When):** Requisição GET para `/dashboard`.
    *   **Verificação (Then):** Resposta HTTP 200, a *view* retornada deve ser `dashboard-admin`, e o modelo (Model) deve conter o atributo `fila`.

*   **Teste 4.C (Roteamento - Professor):**
    *   **Configuração (Given):** Requisição feita sob `@WithMockUser(roles = "PROFESSOR")`.
    *   **Ação (When):** Requisição GET para `/dashboard`.
    *   **Verificação (Then):** Resposta HTTP 200, a *view* retornada deve ser `dashboard-prof`, com os dados pessoais do professor no modelo.

---

## 3. Estratégia de Testes de Frontend

Como o sistema renderiza as páginas no lado do servidor (SSR) com Thymeleaf, a estratégia primária de testes de frontend será conduzida na camada web através de testes de integração com a UI e validações de marcação usando o `MockMvc`, além de testes locais de usabilidade.

### 3.1. Testes de Integração com a UI e Renderização Condicional
**Objetivo:** Garantir que o Thymeleaf processe os templates corretamente baseando-se nas regras de negócio e autorização, injetando alertas e desativando ações indesejadas.

*   **Teste (Renderização Condicional de Componentes):**
    *   **Ação:** Requisitar uma view logado como `ROLE_PROFESSOR` onde um elemento (ex: botão de gerenciar turmas) seja exclusivo para `ROLE_ADMINISTRADOR`.
    *   **Verificação:** Validar via `MockMvc` através de XPath ou extração de conteúdo que a string HTML ou o identificador do botão NÃO está presente na resposta (`content().string(not(containsString("id=\"btn-gerenciar\""))`).

*   **Teste (Feedback de Erro de Formulário - Fallback):**
    *   **Ação:** Disparar um POST de formulário simulando falha de validação ou erro de negócio retornado pelo serviço.
    *   **Verificação:** Confirmar o redirecionamento ou renderização com o modelo contendo a string de erro e garantir que a classe visual `.alert-danger` é renderizada no HTML final.

*   **Teste (Empty States):**
    *   **Ação:** Requisitar o painel administrativo quando o repositório retornar uma lista vazia.
    *   **Verificação:** Validar que a tabela contém um texto ou classe específica de ausência de dados, garantindo que `colspan` está cobrindo a tabela e não há rendering indesejado da estrutura do laço (`th:each`).

### 3.2. Validações Locais e Testes de Acessibilidade (a11y)
Como as interações de script (como carregamento em formulários) e o DOM nativo ocorrem na janela do navegador, as validações visuais não-unitárias seguem o fluxo de auditoria de desenvolvimento:

*   **Testes de Loading (Double Submit Prevention):** Execução do fluxo de ponta-a-ponta em ambiente local submetendo um formulário para garantir que o botão ganha o estado `disabled` e o `spinner-border` se torna visível imediatamente.
*   **Acessibilidade e Navegação por Teclado:** Verificações obrigatórias utilizando a tecla `Tab` e atalhos de navegador para atestar o fluxo sequencial da UI.
*   **Testes Responsivos:** Utilização do inspetor de elementos do navegador (DevTools) reduzindo a janela de exibição para tamanhos Mobile (`320px` a `768px`) certificando que as classes `.table-responsive` mitigam "quebras" horizontais (overflow) nas listagens.
