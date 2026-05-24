# Especificação do Sistema de Agendamento de Recursos Tecnológicos (SART)

## 1. Infraestrutura e Contêineres

    /docker-compose.yml
    - ação: criar
    - descrição: Orquestração do contêiner da aplicação Spring Boot e do banco de dados MySQL.
    - pseudocódigo:
        definir serviço 'db' usando imagem mysql:8
        configurar variáveis de ambiente do 'db' (DB_DATABASE, DB_USER, DB_PASSWORD, DB_ROOT_PASSWORD)
        mapear porta 3306 do 'db'
        definir serviço 'app' efetuando build do Dockerfile local
        configurar variáveis de ambiente do 'app' (DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD, ADMIN_SEED_EMAIL, ADMIN_SEED_PASSWORD)
        mapear porta 8080 do 'app'
        configurar dependência ('depends_on') do 'app' para o 'db'

    /Dockerfile
    - ação: criar
    - descrição: Receita para construir a imagem Docker da aplicação Java com Spring Boot.
    - pseudocódigo:
        definir etapa de build: imagem base maven:3-openjdk-17
        copiar arquivo pom.xml e diretório src/
        executar comando `mvn clean package -DskipTests`
        definir etapa runtime: imagem base openjdk:17-jdk-alpine
        copiar pacote .jar gerado na etapa de build
        expor porta 8080
        definir entrypoint para executar o jar (`java -jar app.jar`)

## 2. Configurações da Aplicação

    /src/main/resources/application.properties
    - ação: criar
    - descrição: Configurações principais do Spring Boot, JPA, Hibernate e conexão com banco de dados.
    - pseudocódigo:
        configurar `server.port` para 8080
        configurar `spring.datasource.url` interpolando DB_HOST, DB_PORT e DB_NAME
        configurar `spring.datasource.username` usando DB_USER
        configurar `spring.datasource.password` usando DB_PASSWORD
        configurar `spring.jpa.hibernate.ddl-auto` como update
        configurar `app.seed.admin.email` lendo ADMIN_SEED_EMAIL
        configurar `app.seed.admin.password` lendo ADMIN_SEED_PASSWORD

## 3. Entidades do Domínio (Models)

    /src/main/java/br/com/uab/sart/models/Usuario.java
    - ação: criar
    - descrição: Entidade persistente representando Professores e Administradores.
    - pseudocódigo:
        anotar classe com @Entity e @Table("usuarios")
        definir atributo id (Long, Primary Key, Auto Increment)
        definir atributo nome (String, não nulo)
        definir atributo email (String, não nulo, Unique)
        definir atributo senha (String, não nulo)
        definir atributo perfil (Enum: ROLE_PROFESSOR, ROLE_ADMINISTRADOR, não nulo)
        definir atributo ativo (Boolean, default true, usado para soft delete)

    /src/main/java/br/com/uab/sart/models/Equipamento.java
    - ação: criar
    - descrição: Entidade persistente representando os recursos tecnológicos.
    - pseudocódigo:
        anotar classe com @Entity e @Table("equipamentos")
        definir atributo id (Long, Primary Key, Auto Increment)
        definir atributo nome (String, não nulo)
        definir atributo descricao (String)
        definir atributo status (Enum: DISPONIVEL, RESERVADO, EM_USO, MANUTENCAO, ATRASADO, não nulo)
        definir atributo ativo (Boolean, default true)

    /src/main/java/br/com/uab/sart/models/Turma.java
    - ação: criar
    - descrição: Entidade persistente representando as turmas da escola.
    - pseudocódigo:
        anotar classe com @Entity e @Table("turmas")
        definir atributo id (Long, Primary Key, Auto Increment)
        definir atributo nome (String, não nulo)
        definir atributo ativo (Boolean, default true)

    /src/main/java/br/com/uab/sart/models/Reserva.java
    - ação: criar
    - descrição: Entidade persistente que mapeia a relação de agendamento.
    - pseudocódigo:
        anotar classe com @Entity e @Table("reservas")
        definir atributo id (Long, Primary Key, Auto Increment)
        definir relação professor (@ManyToOne -> Usuario)
        definir relação equipamento (@ManyToOne -> Equipamento)
        definir relação turma (@ManyToOne -> Turma)
        definir atributo data (LocalDate, não nulo)
        definir atributo horarioAula (Integer de 1 a 8, não nulo)
        definir atributo status (Enum: ATIVA, CANCELADA, CONCLUIDA, ATRASADA, não nulo)
        definir atributo ativo (Boolean, default true)

## 4. Repositórios (Repositories)

    /src/main/java/br/com/uab/sart/repositories/UsuarioRepository.java
    - ação: criar
    - descrição: Interface JPA para persistência de usuários.
    - pseudocódigo:
        interface UsuarioRepository herda JpaRepository<Usuario, Long>
        declarar método buscarPorEmail(String email)
        declarar método buscarPorPerfilEAtivo(Enum perfil, Boolean ativo)

    /src/main/java/br/com/uab/sart/repositories/ReservaRepository.java
    - ação: criar
    - descrição: Interface JPA para persistência de reservas e consultas de conflito.
    - pseudocódigo:
        interface ReservaRepository herda JpaRepository<Reserva, Long>
        declarar método buscarPorEquipamentoEDataEHorarioEAtivo(Equipamento eq, LocalDate dt, Integer hr, Boolean atv)
        declarar método buscarPorStatusIn(List<Status> statusList)

## 5. Serviços (Services e Regras de Negócio)

    /src/main/java/br/com/uab/sart/config/AdminSeedConfig.java
    - ação: criar
    - descrição: EventListener responsável pela criação do administrador inicial (seed).
    - pseudocódigo:
        anotar método `inicializarAdmin` com @EventListener(ApplicationReadyEvent.class)
        se chamada `UsuarioRepository.buscarPorPerfilEAtivo(ROLE_ADMINISTRADOR, true)` retornar lista vazia:
            ler `app.seed.admin.email` das configurações
            ler `app.seed.admin.password` das configurações
            instanciar novo Usuario admin
            atribuir admin.nome = "Administrador Sistema"
            atribuir admin.email = email
            atribuir admin.senha = chamar encoder.encode(password)
            atribuir admin.perfil = ROLE_ADMINISTRADOR
            chamar `UsuarioRepository.save(admin)`

    /src/main/java/br/com/uab/sart/services/AgendamentoService.java
    - ação: criar
    - descrição: Gerencia a criação de reservas validando conflitos e a regra dos 5 minutos. Agora suporta listas para múltiplos itens.
    - pseudocódigo:
        método `criarReserva(idProf, idsEq, idTurma, data, horariosAula)` anotado com @Transactional:
            validar se listas idsEq e horariosAula não estão vazias
            ordenar horariosAula e validar matematicamente se são consecutivos
            se data == hoje:
                iterar sobre `horariosAula` e aplicar regra de antecedência de 5 minutos
            buscar Turma e Professor no banco
            para cada `idEq` em `idsEq`:
                buscar Equipamento no banco
                se `equipamento.status == MANUTENCAO`:
                    lançar erro "Equipamento em manutenção."
                para cada `horarioAula` em `horariosAula`:
                    se `ReservaRepository.buscarPorEquipamentoEDataEHorarioEAtivo()` encontrar reserva:
                        lançar erro "Conflito: Equipamento já reservado neste horário."
                    instanciar Reserva e associar entidades (Professor, Equipamento iterado, Turma, Data, horarioAula iterado)
                    definir `reserva.status = ATIVA`
                    salvar Reserva no repositório
            retornar Lista de Reservas Criadas

    /src/main/java/br/com/uab/sart/services/StatusAgendamentoScheduler.java
    - ação: criar
    - descrição: Job assíncrono para identificar reservas não devolvidas (Atrasadas).
    - pseudocódigo:
        anotar classe com @EnableScheduling e método com @Scheduled(fixedRate = 60000)
        método `verificarAtrasos`:
            obter momento atual (data/hora)
            obter lista de todas reservas ativas do banco
            iterar sobre lista:
                se o (fim do horário da aula da reserva + 5 minutos) já passou do momento atual:
                    atualizar `reserva.status = ATRASADA`
                    atualizar `equipamento.status = ATRASADO`
                    salvar Reserva
                    salvar Equipamento

## 6. Controladores Web (Controllers)

    /src/main/java/br/com/uab/sart/controllers/AuthController.java
    - ação: criar
    - descrição: Responsável pelo mapeamento das rotas públicas (Login).
    - pseudocódigo:
        mapear GET "/login"
        retornar string "login" (template do Thymeleaf)

    /src/main/java/br/com/uab/sart/controllers/DashboardController.java
    - ação: criar
    - descrição: Redireciona o usuário recém-logado para seu respectivo painel (Admin ou Prof).
    - pseudocódigo:
        mapear GET "/dashboard"
        obter contexto de autenticação do usuário
        se usuário logado possuir `ROLE_ADMINISTRADOR`:
            buscar fila de reservas de hoje no serviço
            adicionar modelo "fila"
            retornar template "dashboard-admin"
        se usuário logado possuir `ROLE_PROFESSOR`:
            buscar histórico pessoal no serviço
            adicionar modelo "historico"
            retornar template "dashboard-prof"

## 7. Interfaces Gráficas (Views - Thymeleaf)

    /src/main/resources/templates/login.html
    - ação: criar
    - descrição: Template da página de login integrada ao Spring Security.
    - pseudocódigo:
        definir layout HTML5 importando Bootstrap CSS
        criar formulário POST apontando para "/login"
        criar input de e-mail (name="username")
        criar input de senha (name="password")
        criar botão de submissão
        exibir mensagem condicional se URL contiver "?error"

    /src/main/resources/templates/dashboard-admin.html
    - ação: criar
    - descrição: Painel do Administrador focando no controle de entregas/devoluções do dia.
    - pseudocódigo:
        definir layout HTML5 com Bootstrap CSS
        incluir menu de navegação (Links: Início, Professores, Turmas, Equipamentos, Relatórios)
        iterar sobre lista de modelo `fila`:
            renderizar linha de tabela com: Equipamento, Professor, Turma, Horário, Status
            se status for ATIVA: exibir botão "Confirmar Entrega" (POST)
            se status for EM_USO ou ATRASADA: exibir botão "Confirmar Devolução" (POST)

    /src/main/resources/templates/reserva-form.html
    - ação: criar
    - descrição: Formulário para o professor criar reservas.
    - pseudocódigo:
        definir layout HTML5 com Bootstrap CSS
        criar formulário POST para "/reservas/salvar"
        incluir campo de data (type="date")
        incluir select múltiplo para 'horariosAula'
        incluir select múltiplo para 'equipamentosIds'
        incluir select para 'turmaId'
        incluir botão "Agendar Reserva"
