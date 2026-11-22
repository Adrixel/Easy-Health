# Easy Health — Documentação das Funcionalidades do Backend

**Branch:** `feature/dono-clinicas`
**Commit de referência:** `4054df6`
**Backend:** Spring Boot
**Banco de dados:** MySQL
**Objetivo:** documentar o compartilhamento do histórico médico e o sistema de avaliações das instituições de saúde.

---

# 1. Autenticação e permissões

As rotas protegidas utilizam autenticação por token:

```http
Authorization: Bearer <accessToken>
```

O backend identifica o usuário pelo token autenticado.

O frontend não deve enviar um `patientId` ou `medicoId` arbitrário para definir a identidade de quem está realizando uma operação.

Cargos utilizados:

- `paciente`
- `funcionario`
- `medico`
- `dono`

As permissões são verificadas pelo backend.

# 2. Compartilhamento do histórico médico

O compartilhamento permite que o paciente autorize um médico específico a consultar seus registros médicos.

O paciente mantém o controle da autorização e pode revogá-la.

## 2.1. Compartilhar histórico com um médico

**POST**

```http
/api/historico/compartilhamentos
```

Autorização: somente paciente autenticado.

JSON:

```json
{
  "medicoId": "demo-medico"
}
```

O `medicoId` identifica o médico que receberá autorização.

O paciente é identificado pelo token, não pelo corpo da requisição.

## 2.2. Listar compartilhamentos

**GET**

```http
/api/historico/compartilhamentos
```

Autorização: paciente autenticado.

Retorna os compartilhamentos associados ao paciente.

## 2.3. Médico consultar histórico autorizado

**GET**

```http
/api/medicos/pacientes/{patientId}/historico/registros
```

Autorização: médico autenticado.

O backend verifica se existe uma autorização ativa entre o paciente e o médico autenticado.

O médico não pode escolher sua própria identidade enviando um identificador arbitrário: ela é obtida pelo token.

O acesso é somente para consulta.

## 2.4. Revogar compartilhamento

**DELETE**

```http
/api/historico/compartilhamentos/{id}
```

Autorização: paciente autenticado.

O paciente pode revogar uma autorização anteriormente concedida.

Depois da revogação, o médico perde o acesso aos registros daquele paciente por meio dessa autorização.

## 2.5. Auditoria de acesso

Os acessos autorizados são registrados para auditoria.

A tabela de auditoria registra informações como:

- Paciente consultado.
- Médico que realizou o acesso.
- Data e horário.
- Tipo de acesso.
- Quantidade de registros consultados.

## 2.6. Regras de segurança

1. Somente pacientes podem criar compartilhamentos.
2. Somente pacientes podem revogar seus compartilhamentos.
3. Somente médicos autenticados podem consultar históricos compartilhados.
4. O médico precisa possuir autorização ativa do paciente.
5. O acesso do médico é somente leitura.
6. A revogação impede novos acessos por aquela autorização.
7. Os acessos autorizados são registrados para auditoria.

# 3. Avaliações de clínicas

O sistema permite que pacientes avaliem instituições de saúde com uma nota de 1 a 5 estrelas e um comentário opcional.

Cada paciente pode manter uma avaliação por instituição.

## 3.1. Criar avaliação

**POST**

```http
/api/estabelecimentos/{id}/avaliacoes
```

Autorização: somente paciente autenticado.

JSON:

```json
{
  "nota": 5,
  "comentario": "Atendimento muito bom e equipe atenciosa."
}
```

A nota deve ser um número inteiro entre 1 e 5.

O comentário é opcional e aceita até 2000 caracteres.

Resposta de exemplo:

```json
{
  "id": "uuid-da-avaliacao",
  "nota": 5,
  "comentario": "Atendimento muito bom e equipe atenciosa.",
  "createdAt": "2026-11-22T01:50:02.000+00:00",
  "updatedAt": "2026-11-22T01:50:02.000+00:00"
}
```

O identificador do paciente é obtido pelo token de autenticação.

Se o paciente já tiver avaliado a instituição, o backend retorna HTTP `409 Conflict`.

## 3.2. Editar minha avaliação

**PUT**

```http
/api/estabelecimentos/{id}/avaliacoes/minha
```

Autorização: somente paciente autenticado.

JSON:

```json
{
  "nota": 4,
  "comentario": "Atendimento bom, com alguns pontos a melhorar."
}
```

O paciente só pode editar a própria avaliação.

A edição atualiza a nota, o comentário e a data `updatedAt`, preservando o identificador original.

Se ainda não existir avaliação do paciente para aquela instituição, o backend retorna HTTP `404 Not Found`.

## 3.3. Consultar avaliações

**GET**

```http
/api/estabelecimentos/{id}/avaliacoes
```

Consulta pública, sem necessidade de autenticação.

Resposta de exemplo:

```json
{
  "avaliacoes": [
    {
      "paciente": "Paciente",
      "nota": 4,
      "comentario": "Atendimento bom, com alguns pontos a melhorar.",
      "createdAt": "2026-11-22T01:50:02.000+00:00",
      "updatedAt": "2026-11-22T01:50:49.000+00:00"
    }
  ],
  "avaliacoesCount": 1,
  "avaliacaoMedia": 4.0
}
```

A resposta apresenta:

| Campo | Descrição |
|---|---|
| avaliacoes | Lista de avaliações |
| paciente | Identificação anonimizada |
| nota | Nota atribuída, de 1 a 5 |
| comentario | Comentário opcional |
| createdAt | Data de criação |
| updatedAt | Data da última atualização |
| avaliacoesCount | Quantidade de avaliações |
| avaliacaoMedia | Média das notas |

A média e a quantidade são calculadas a partir das avaliações cadastradas.

O nome completo do paciente não é exposto na consulta pública.

## 3.4. Regras de negócio

1. Somente pacientes autenticados podem criar avaliações.
2. Somente o próprio paciente pode editar sua avaliação.
3. Cada paciente pode avaliar uma instituição apenas uma vez.
4. A nota deve estar entre 1 e 5.
5. O comentário pode ser omitido.
6. O comentário tem limite de 2000 caracteres.
7. A instituição precisa existir no catálogo.
8. A consulta pública não expõe o nome completo do paciente.

# 4. Banco de dados

## 4.1. Avaliações de instituições

Tabela: `establishment_reviews`

A tabela armazena as avaliações dos pacientes.

Principais campos:

| Campo | Descrição |
|---|---|
| id | Identificador da avaliação |
| establishment_id | Identificador da instituição |
| patient_id | Identificador do paciente |
| nota | Nota de 1 a 5 |
| comentario | Comentário opcional |
| created_at | Data de criação |
| updated_at | Data de atualização |

Restrições:

- Chave primária no campo `id`.
- Chave estrangeira de `patient_id` para `users(id)`.
- Restrição de unicidade para `establishment_id` e `patient_id`.
- Restrição para permitir somente notas entre 1 e 5.

O identificador da instituição não possui chave estrangeira porque o catálogo está em outro banco de dados.

## 4.2. Compartilhamento do histórico

O recurso utiliza as tabelas de compartilhamento e auditoria do histórico médico.

A autorização mantém o vínculo entre paciente e médico e registra a situação do compartilhamento.

A auditoria registra os acessos realizados pelos médicos.

# 5. Testes realizados

## 5.1. Compartilhamento do histórico

Foram realizados testes manuais para verificar:

- Criação de compartilhamento pelo paciente.
- Listagem dos compartilhamentos.
- Bloqueio da tentativa de criação por médico.
- Consulta do histórico por médico autorizado.
- Registro de auditoria.
- Revogação pelo paciente.
- Bloqueio de acesso após revogação.

## 5.2. Avaliações de clínicas

Foram realizados testes manuais para verificar:

| Operação | Resultado |
|---|---|
| Login do paciente | HTTP 200 |
| Criar avaliação | HTTP 201 |
| Consultar avaliações | HTTP 200 |
| Editar avaliação | HTTP 200 |
| Recalcular média | Correto |
| Anonimizar paciente | Correto |

Também foram executados os testes Maven:

```text
Tests run: 22
Failures: 0
Errors: 0
Skipped: 1

BUILD SUCCESS
```

Um teste foi ignorado durante a execução.

# 6. Arquivos relacionados

| Arquivo | Responsabilidade |
|---|---|
| `EstablishmentReviewController.java` | Endpoints de avaliações |
| `EstablishmentReviewRequest.java` | Validação dos dados da avaliação |
| `01_easyhealth.sql` | Estrutura da tabela de avaliações |
| `02-avaliacoes.http` | Requisições para testes manuais |
| Controllers do histórico médico | Compartilhamento e consulta dos registros |

# 7. Execução e testes

O projeto utiliza Docker para executar o backend e o banco de dados.

Para iniciar ou reconstruir o backend:

```cmd
docker compose up -d --build backend
```

Para executar os testes Maven:

```cmd
docker run --rm -v "%cd%\backend:/app" -w /app maven:3.9-eclipse-temurin-25 mvn test
```

As requisições manuais estão disponíveis em:

```text
backend/requests/02-avaliacoes.http
```

A URL base utilizada nos testes é:

```text
http://localhost:3333
```

---

**Referência:** branch `feature/dono-clinicas`, commit `4054df6`.

**Observação:** esta documentação descreve as funcionalidades implementadas e testadas no backend. Ela não significa que todas as respectivas telas já estejam integradas ao frontend.