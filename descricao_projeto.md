# Descrição do Projeto: Sistema de Agendamento de Recursos Tecnológicos (SART)

Este documento detalha a arquitetura, plataforma tecnológica, estrutura, serviços e outras convenções adotadas para o projeto SART, conforme definido em sua documentação principal.

## 1. Arquitetura

O projeto utiliza uma **Arquitetura MVC** (Model-View-Controller) monolítica tradicional, com renderização de páginas no lado do servidor (Server-Side Rendering). Toda a infraestrutura da aplicação e do banco de dados será mantida em uma **estrutura de contêiner Docker**, garantindo isolamento e facilidade de implantação.

## 2. Plataforma Tecnológica

A stack tecnológica principal é composta por:
- **Linguagem:** Java
- **Framework Backend:** Spring Boot (com Spring Security para autenticação e controle de acesso).
- **Motor de Templates (Frontend):** Thymeleaf
- **Estilização:** Bootstrap
- **Banco de Dados:** MySQL (Relacional, com suporte a Soft Delete)
- **Infraestrutura:** Docker (aplicação + banco de dados)

## 3. Estrutura de Diretórios

A organização do projeto segue a estrutura padrão de uma aplicação Maven com Spring Boot:

```text
sart/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── br/com/uab/sart/
│   │   │       ├── config/       # Configurações do Spring, Security, etc.
│   │   │       ├── controllers/  # Endpoints e roteamento web
│   │   │       ├── models/       # Entidades que mapeiam as tabelas do banco
│   │   │       ├── repositories/ # Interfaces de acesso a dados (JPA)
│   │   │       └── services/     # Regras de negócio da aplicação
│   │   └── resources/
│   │       ├── static/           # Arquivos estáticos (CSS, JS, Imagens)
│   │       ├── templates/        # Telas da aplicação (arquivos HTML do Thymeleaf)
│   │       └── application.properties # Configurações de ambiente do Spring
├── docker-compose.yml            # Orquestração dos contêineres da aplicação e banco de dados
└── Dockerfile                    # Receita para geração da imagem da aplicação
```

## 4. Convenções

Para manter a consistência do código e da colaboração, adotam-se as seguintes regras:
- **Nomenclatura:** Padrão `CamelCase` para classes e métodos em Java; `kebab-case` para URLs e nomes de arquivos HTML/CSS.
- **Idioma:** Código em Português ou Inglês (deve ser padronizado pela equipe), com forte ênfase na clareza de variáveis e métodos.
- **Commits:** Mensagens claras e no imperativo (ex: "Adiciona funcionalidade X", "Corrige bug Y").
- **Design de Código (Arquitetura):** Controladores devem ser enxutos (Thin Controllers), delegando toda a lógica de negócio e validação para a camada de Serviços (Thick Services).

## 5. Serviços

A aplicação contará com os seguintes serviços principais:
- **Serviço de Autenticação e Autorização:** Gestão de login, sessões e controle de papéis de usuários (via Spring Security).
- **Serviço de Agendamento:** Validação das regras de negócio para reservas, incluindo checagem de conflitos, aplicação da grade de horários fixa e bloqueio de reservas com menos de 5 minutos de antecedência.
- **Serviço de Status Automático:** Rotina assíncrona/agendada (`@Scheduled`) responsável por alterar o status da reserva para `ATRASADO` caso o equipamento não seja devolvido no prazo.
- **Serviço de Relatórios:** Responsável pela agregação de dados e exportação de relatórios gerenciais no formato PDF.

## 6. Variáveis de Ambiente

O sistema dependerá de variáveis de ambiente para configuração, especialmente úteis no contexto do Docker (arquivos `.env` ou `docker-compose.yml`):

- `DB_HOST`: Host do servidor de banco de dados (ex: `mysql-db`)
- `DB_PORT`: Porta de conexão do banco de dados (ex: `3306`)
- `DB_NAME`: Nome do banco de dados do sistema (ex: `sart_db`)
- `DB_USER`: Usuário do banco
- `DB_PASSWORD`: Senha do banco
- `ADMIN_SEED_EMAIL`: Email para criação automática do administrador inicial.
- `ADMIN_SEED_PASSWORD`: Senha para o administrador inicial.

## 7. Definição de Usuários

O sistema possui dois perfis principais de acesso:

### Professor
Usuário com foco no agendamento e uso dos recursos.
- **Importante:** Os professores **não se autocadastram** no sistema. O registro de qualquer professor deve ser feito exclusivamente por um Administrador.

### Administrador
Servidor ou Gestor responsável pelo controle de equipamentos, relatórios e gestão de usuários.
- **Importante:** O sistema deverá ser inicializado com um **administrador inicial (seed)** previamente configurado pelas variáveis de ambiente. Somente este usuário (e outros administradores criados por ele) terão permissão para cadastrar professores e novos administradores.

#### ⚙️ Mecanismo de Criação do Administrador Inicial (Seed)
Para garantir que o sistema seja acessível logo após a primeira implantação, a aplicação implementará uma rotina de inicialização (utilizando, por exemplo, a interface `CommandLineRunner` ou a anotação `@EventListener(ApplicationReadyEvent.class)` do Spring Boot).

**Comportamento esperado na inicialização:**
1. O Spring Boot verifica no banco de dados se já existe algum usuário cadastrado com a permissão `ROLE_ADMINISTRADOR`.
2. Caso o banco não possua nenhum administrador, o sistema lê as variáveis de ambiente `ADMIN_SEED_EMAIL` e `ADMIN_SEED_PASSWORD`.
3. Um novo usuário administrador é inserido automaticamente no banco de dados com essas credenciais (a senha deve ser codificada com `BCryptPasswordEncoder` antes do salvamento).
4. Caso já exista pelo menos um administrador no banco de dados, a rotina de criação é ignorada, garantindo que o processo ocorra apenas na primeira execução ou em bancos de dados recém-criados.
