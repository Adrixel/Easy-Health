# Easy Health — Documentação das APIs para o Frontend

**Branch:** `feature/dono-clinicas`  
**Commit de referência:** `3c62a22`  
**Backend:** Spring Boot  
**Objetivo:** orientar a implementação das telas de agendamentos e histórico de exames.

---

# 1. Autenticação

As rotas abaixo exigem autenticação, exceto quando indicado.

O backend identifica o paciente pelo usuário autenticado. O frontend não deve enviar um `userId` arbitrário para definir a quem pertence um agendamento ou exame.

Nos arquivos de teste HTTP do projeto, a autenticação é feita com `accessToken`:

```http
?accessToken={{login.response.body.$.accessToken}}
```

O frontend deve utilizar o mecanismo de autenticação já adotado pelo aplicativo e não armazenar tokens em locais públicos.

# 2. Agendamentos

## 2.1. Consultar horários disponíveis

**GET**

```http
/api/estabelecimentos/{id}/horarios-disponiveis?data=2026-11-28
```

Exemplo:

```http
/api/estabelecimentos/parceiro-hospital-exemplo/horarios-disponiveis?data=2026-11-28
```

Resposta:

```json
[
  "08:00",
  "09:30",
  "10:00",
  "11:30",
  "14:00",
  "15:30",
  "16:00"
]
```

**Importante:** a disponibilidade é calculada por instituição e data, não individualmente por médico.

## 2.2. Criar agendamento

**POST**

```http
/api/agendamentos
```

JSON:

```json
{
  "establishmentId": "parceiro-hospital-exemplo",
  "especialidade": "Cardiologia",
  "medicoId": "demo-medico",
  "data": "2026-11-28",
  "horario": "10:00"
}
```

O `medicoId` deve identificar um médico vinculado à instituição selecionada.

Resposta de exemplo:

```json
{
  "id": "uuid-do-agendamento",
  "userId": "demo-user",
  "establishmentId": "parceiro-hospital-exemplo",
  "establishmentNome": "Hospital Horizonte (simulação)",
  "especialidade": "Cardiologia",
  "local": "Hospital Horizonte (simulação)",
  "data": "2026-11-28",
  "horario": "10:00:00",
  "status": "agendado",
  "medicoId": "demo-medico",
  "medico": "Dra. Ana Médica"
}
```

O identificador `id` é gerado pelo backend.

## 2.3. Listar agendamentos

**GET**

```http
/api/agendamentos
```

Retorna os agendamentos do usuário autenticado.

Cada item contém:

| Campo | Descrição |
|---|---|
| id | Identificador do agendamento |
| userId | Identificador do paciente |
| establishmentId | Identificador da instituição |
| establishmentNome | Nome da instituição |
| especialidade | Especialidade médica |
| local | Local do atendimento |
| data | Data da consulta |
| horario | Horário da consulta |
| status | Situação do agendamento |
| medicoId | Identificador do médico |
| medico | Nome do médico |

## 2.4. Consultar um agendamento

**GET**

```http
/api/agendamentos/{id}
```

Retorna os dados do agendamento solicitado, desde que pertença ao usuário autenticado.

## 2.5. Reagendar

Utilizar a rota de atualização do agendamento:

```http
PUT /api/agendamentos/{id}
```

O corpo deve informar os novos dados de data e horário conforme o contrato de criação.

O backend verifica novamente a disponibilidade.

O médico associado é preservado no reagendamento atual.

## 2.6. Confirmar agendamento

```http
POST /api/agendamentos/{id}/confirmar
```

A confirmação altera o status do agendamento.

## 2.7. Cancelar agendamento

```http
DELETE /api/agendamentos/{id}
```

Após o cancelamento, o agendamento permanece no histórico com status `cancelado`.

## 2.8. Histórico de consultas

**GET**

```http
/api/historico/consultas
```

Retorna as consultas do paciente autenticado, incluindo consultas canceladas e os dados do médico associado.

---

# 3. Histórico de exames e registros médicos

O módulo utiliza a tabela `medical_history_records`.

Ele permite registrar exames, vacinas, medicamentos e documentos.

## 3.1. Tipos aceitos

| Valor da API | Utilização |
|---|---|
| `exame` | Exames médicos |
| `vacina` | Vacinas |
| `medicamento` | Medicamentos |
| `documento` | Documentos médicos |

Os valores devem ser enviados exatamente como aparecem na tabela.

## 3.2. Cadastrar exame

**POST**

```http
/api/historico/registros
```

JSON:

```json
{
  "tipo": "exame",
  "titulo": "Hemograma completo",
  "data": "2026-11-21",
  "descricao": "Exame de rotina",
  "profissional": "Dra. Ana Médica",
  "local": "Hospital Horizonte",
  "medicoId": "demo-medico",
  "establishmentId": "parceiro-hospital-exemplo",
  "resultado": "Resultado demonstrativo",
  "laudoUrl": null
}
```

Campos:

| Campo | Obrigatório | Descrição |
|---|---|---|
| tipo | Sim | exame, vacina, medicamento ou documento |
| titulo | Sim | Nome do registro |
| data | Sim | Data no formato AAAA-MM-DD |
| descricao | Não | Descrição do registro |
| profissional | Não | Nome do profissional |
| local | Não | Local do atendimento |
| medicoId | Não | ID do médico |
| establishmentId | Não | ID da instituição |
| resultado | Não | Resultado do exame |
| laudoUrl | Não | Endereço do laudo |

**Regra importante:** `medicoId` e `establishmentId` devem ser enviados juntos ou ambos omitidos.

Quando informados, o backend valida se o médico existe, se possui cargo de médico e se está vinculado à instituição.

Resposta de exemplo:

```json
{
  "id": "uuid-do-registro",
  "userId": "demo-user",
  "paciente": "Maria Silva",
  "medicoId": "demo-medico",
  "medico": "Dra. Ana Médica",
  "establishmentId": "parceiro-hospital-exemplo",
  "estabelecimento": "Hospital Horizonte (simulação)",
  "tipo": "exame",
  "titulo": "Hemograma completo",
  "data": "2026-11-21",
  "descricao": "Exame de rotina",
  "profissional": "Dra. Ana Médica",
  "local": "Hospital Horizonte",
  "resultado": "Resultado demonstrativo",
  "laudoUrl": null
}
```

## 3.3. Listar todos os registros

**GET**

```http
/api/historico/registros
```

Também é possível filtrar pelo tipo:

```http
/api/historico/registros?tipo=exame
```

Exemplos:

```http
/api/historico/registros?tipo=vacina
/api/historico/registros?tipo=medicamento
/api/historico/registros?tipo=documento
```

## 3.4. Listar por categoria

Existem rotas específicas para facilitar a implementação das telas:

| Tela | Rota |
|---|---|
| Exames | `GET /api/historico/exames` |
| Vacinas | `GET /api/historico/vacinas` |
| Medicamentos | `GET /api/historico/medicamentos` |
| Documentos | `GET /api/historico/documentos` |

Todas retornam somente registros do usuário autenticado.

## 3.5. Editar registro

**PUT**

```http
/api/historico/registros/{id}
```

Exemplo:

```json
{
  "tipo": "exame",
  "titulo": "Hemograma completo atualizado",
  "data": "2026-11-21",
  "descricao": "Exame de rotina atualizado",
  "profissional": "Dra. Ana Médica",
  "local": "Hospital Horizonte",
  "medicoId": "demo-medico",
  "establishmentId": "parceiro-hospital-exemplo",
  "resultado": "Resultado atualizado",
  "laudoUrl": null
}
```

## 3.6. Excluir registro

**DELETE**

```http
/api/historico/registros/{id}
```

Retorno esperado: HTTP `204 No Content`.

---

# 4. Tratamento de erros

O frontend deve tratar os principais códigos HTTP:

| Código | Significado | Comportamento sugerido |
|---|---|---|
| 200 | Operação realizada | Atualizar a tela |
| 201 | Registro criado | Atualizar lista e mostrar confirmação |
| 204 | Exclusão realizada | Remover item da lista |
| 400 | Dados inválidos | Exibir mensagem de validação |
| 401 | Não autenticado | Solicitar autenticação novamente |
| 403 | Sem permissão | Informar que a operação não é permitida |
| 404 | Registro não encontrado | Atualizar tela ou informar indisponibilidade |
| 409 | Conflito | Informar que o horário ou operação não está disponível |

Não presumir que todos os erros retornam o mesmo formato JSON. Tratar também respostas sem corpo.

# 5. Orientações para implementação das telas

## Tela de agendamento

Fluxo recomendado:

1. Selecionar instituição.
2. Selecionar especialidade.
3. Selecionar médico.
4. Selecionar data.
5. Consultar horários disponíveis.
6. Selecionar horário.
7. Confirmar agendamento.
8. Exibir os dados retornados pela API.

## Tela de consultas

- Carregar os dados de `/api/agendamentos`.
- Mostrar instituição, médico, especialidade, data e horário.
- Permitir confirmação, reagendamento e cancelamento conforme o status.
- Atualizar os dados depois de cada operação.

## Tela de exames

- Carregar `/api/historico/exames`.
- Permitir cadastro e edição.
- Exibir título, data, instituição, médico e resultado.
- Permitir acesso ao laudo quando houver um endereço válido em `laudoUrl`.

## Tela de histórico médico

- Utilizar as rotas específicas de exames, vacinas, medicamentos e documentos.
- Exibir registros em ordem cronológica.
- Não utilizar dados fixos quando a API já fornecer os dados reais.

# 6. Observações importantes

1. O paciente não pode consultar ou modificar registros de outro usuário.
2. O `userId` é definido pela autenticação, não pelo formulário.
3. O médico precisa estar vinculado à instituição.
4. A disponibilidade de horários é por instituição, não por médico.
5. O campo `laudoUrl` é apenas um endereço de referência. O cadastro atual não implementa upload de arquivos.
6. Dados de demonstração não devem ser apresentados como resultados médicos reais.
7. O frontend deve atualizar as listas depois de cadastrar, editar, confirmar ou cancelar registros.

---

**Referência de implementação:** commit `3c62a22` na branch `feature/dono-clinicas`.

**Nota para integração:** antes de finalizar cada chamada no frontend, conferir o caminho exato das operações de reagendamento e confirmação nos arquivos `PersonalDataController.java` e `backend/requests/06-agendamentos.http`, mantendo o contrato do backend como referência.
