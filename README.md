<div align="center">

# ProJson

**Criação de JSON com referências para Kotlin**

Permite serializar objetos Kotlin para JSON com suporte a referências (`$id` / `$ref`), de forma transparente e sem complexidade para o utilizador.

---

<div align="center">

<img src="https://img.shields.io/badge/Kotlin-1.9-purple?style=for-the-badge"/>
<img src="https://img.shields.io/badge/build-passing-brightgreen?style=for-the-badge"/>
<img src="https://img.shields.io/badge/tests-covered-success?style=for-the-badge"/>


[![Last commit](https://img.shields.io/github/last-commit/leticiacascais/ProJson?style=for-the-badge)](https://github.com/leticiacascais/ProJson/commits)
[![Stars](https://img.shields.io/github/stars/leticiacascais/ProJson?style=for-the-badge)](https://github.com/leticiacascais/ProJson/stargazers)
[![Issues](https://img.shields.io/github/issues/leticiacascais/ProJson?style=for-the-badge)](https://github.com/leticiacascais/ProJson/issues)


</div>

</div>

## Funcionalidades

*  Gestão automática de referências entre objetos (`$id` / `$ref`)
*  API simples (sem necessidade de lidar com UUIDs)
*  Suporte para objetos e coleções
*  Anotações personalizadas:
    * `@Reference`
    * `@JsonProperty`
    * `@JsonIgnore`
    * `@JsonString`
*  Sem utilização de bibliotecas externas
*  Preparado para testes unitários

---

##  Exemplo de utilização

```kotlin
val t1 = Task("T1", Date(30,2,2026), emptyList())
val t2 = Task("T2", Date(31,4,2026), emptyList())
val t3 = Task("T3", null, listOf(t1, t2))

val json = ProJson().toJson(listOf(t1, t2, t3))
println(json)
```

### Output

```json
[
  {
    "$id": "uuid-1",
    "$type": "Task",
    "description": "T1",
    "deadline": {
      "$type": "Date",
      "day": 30,
      "month": 2,
      "year": 2026
  },
    "dependencies": []
},
  { "$id": "uuid-2",
    "$type": "Task",
    "description": "T2",
    "deadline": {
      "$type": "Date",
      "day": 31,
      "month": 4,
      "year": 2026
},
  "dependencies": []
},
  { "$id": "uuid-3",
    "$type": "Task",
    "description": "T3",
    "deadline": null,
  "dependencies": [
    { "$ref": "uuid-1" },
    { "$ref": "uuid-2" }
  ]
}
]

```

---

##  Como funciona

O ProJson converte objetos em JSON através de:

* Atribuição automática de UUIDs a objetos
* Substituição de referências repetidas por `$ref`
* Preservação da estrutura sem duplicação de dados

---

##  Anotações

### Referências entre objetos

```kotlin
class Task(
    val description: String,
    val deadline: Date?,
    @Reference
    val dependencies: List<Task>
)
```

---

### Nome personalizado de propriedades

```kotlin
class Task(
    @JsonProperty("desc")
    val description: String
)
```

---

### Ignorar propriedades

```kotlin
@JsonIgnore
val deadline: Date?
```

---

### Serialização personalizada

```kotlin
@JsonString(DateAsText::class)
data class Date(...)
```

---

## Estrutura do projeto

```
src/
 ├── main/kotlin/
 └── test/kotlin/
```

---

##  Testes

Todas as funcionalidades encontram-se cobertas por testes unitários, de forma a garantir 

---

##  Começar


---



