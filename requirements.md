# Dependências de Teste (Requirements)

Para suportar a estratégia de **TDD First** e garantir testes determinísticos e isolados na plataforma Spring Boot, o projeto deverá incluir as seguintes dependências no seu arquivo `pom.xml` (escopo de teste):

## 1. Dependências Principais (Spring Boot Starter Test)
A dependência agregadora padrão do Spring Boot que já traz a maioria das ferramentas necessárias.
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```
**O que está incluso nesta dependência:**
- **JUnit 5 (Jupiter):** Motor padrão para execução dos testes unitários e de integração.
- **Mockito:** Framework para criação de mocks estruturados (essencial para isolar testes de Serviço).
- **AssertJ:** Biblioteca de asserções fluentes (ex: `assertThat(resultado).isEqualTo(esperado)`).
- **Spring TestContext Framework:** Para subir contextos do Spring, simular injeção de dependências e testar slices da aplicação (como `@WebMvcTest` e `@DataJpaTest`).

## 2. Dependência de Testes de Segurança (Spring Security Test)
Necessária para testar as regras de acesso e os controladores sem a necessidade de passar pelo fluxo real de login.
```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```
**Motivo:** Permite o uso da anotação `@WithMockUser(roles = "ADMINISTRADOR")` e o teste de rotas protegidas usando `MockMvc`.

## 3. Banco de Dados em Memória (H2 Database)
Para testes de integração nos Repositórios (JPA), garantindo execução super rápida sem depender de contêineres ou do MySQL de produção.
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```
**Motivo:** Executar testes da camada `@DataJpaTest` com isolamento completo e sem efeitos colaterais.

## 4. Testcontainers (MySQL) - (Opcional, porém Recomendado)
Se for necessário realizar testes de integração E2E (End-to-End) garantindo 100% de paridade com o banco em produção.
```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mysql</artifactId>
    <scope>test</scope>
</dependency>
```
**Motivo:** Levanta um contêiner Docker real do MySQL estritamente durante o fluxo de testes.
