<div align="center">

# ProJson

**Criação de JSON com referências para Kotlin**

Permite serializar objetos Kotlin para JSON com suporte a referências (`$id` / `$ref`), de forma transparente e sem complexidade para o utilizador.

---

<div align="center">

<img src="https://img.shields.io/badge/Kotlin-2.3-purple?style=for-the-badge"/>
<img src="https://img.shields.io/badge/JVM-17-blue?style=for-the-badge"/>
<img src="https://img.shields.io/badge/build-passing-brightgreen?style=for-the-badge"/>
<img src="https://img.shields.io/badge/tests-covered-success?style=for-the-badge"/>


[![Last commit](https://img.shields.io/github/last-commit/leticiacascais/ProJson?style=for-the-badge)](https://github.com/leticiacascais/ProJson/commits)
[![Stars](https://img.shields.io/github/stars/leticiacascais/ProJson?style=for-the-badge)](https://github.com/leticiacascais/ProJson/stargazers)
[![Issues](https://img.shields.io/github/issues/leticiacascais/ProJson?style=for-the-badge)](https://github.com/leticiacascais/ProJson/issues)
[![Release](https://img.shields.io/github/v/release/leticiacascais/ProJson?style=for-the-badge)](https://github.com/leticiacascais/ProJson/releases)


</div>

</div>

## Funcionalidades

* Gestão automática de referências entre objetos (`$id` / `$ref`) — o código cliente **não gere UUIDs**
* API simples centrada em `ProJson().toJson(valor)` com modelo JSON manipulável em memória
* Suporte para primitivos, `null`, objetos Kotlin, `Map` e coleções (`Iterable`)
* Objetos Kotlin serializados com propriedade `$type`; mapas serializados **sem** `$type`
* Valores (`data class` sem referências) serializados inline, sem `$id`
* Anotações personalizadas:
    * `@Reference` — grafos com `$ref` em vez de duplicar objetos
    * `@JsonProperty` — nome alternativo da propriedade no JSON
    * `@JsonIgnore` — excluir propriedades da saída
    * `@JsonString` — plugin de serialização para string JSON
* Modelo em memória (`JsonObject`, `JsonArray`, `JsonPrimitive`, `JsonReference`) com leitura, escrita, remoção e travessia da árvore
* Sem utilização de bibliotecas externas (apenas Kotlin stdlib + reflect)
* Cobertura por testes unitários (JUnit) 

---

## Exemplo de utilização

O caso de uso principal do enunciado: tarefas com dependências entre instâncias partilhadas em memória.

```kotlin
import projson.core.ProJson
import projson.core.Reference

data class Date(val day: Int, val month: Int, val year: Int)

class Task(
    val description: String,
    val deadline: Date?,
    @Reference
    val dependencies: List<Task>
)

val t1 = Task("T1", Date(30, 2, 2026), emptyList())
val t2 = Task("T2", Date(31, 4, 2026), emptyList())
val t3 = Task("T3", null, listOf(t1, t2))

val json = ProJson().toJson(listOf(t1, t2, t3))
println(json)
```

### Output

```json
[
  {
    "$id": "9e2e6c64-3236-45b7-8b8a-11271c69e4df",
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
  {
    "$id": "11fb194e-b75c-4f73-9c10-65df91b81352",
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
  {
    "$id": "d388f116-826f-4751-bdad-fb8cc152b968",
    "$type": "Task",
    "description": "T3",
    "deadline": null,
    "dependencies": [
      { "$ref": "9e2e6c64-3236-45b7-8b8a-11271c69e4df" },
      { "$ref": "11fb194e-b75c-4f73-9c10-65df91b81352" }
    ]
  }
]
```

> Os UUID em `$id` são gerados automaticamente em cada execução; a forma do JSON mantém-se.

---

## Como funciona

O ProJson converte valores Kotlin numa árvore JSON em memória (`JsonValue`) e produz texto JSON válido através de `toString()` em cada nó.

### Fluxo geral

1. **`ProJson().toJson(valor)`** — ponto de entrada único para o cliente.
2. **Pré-análise** — percorre o grafo de objetos e marca os alvos de propriedades `@Reference` para receberem `$id`.
3. **Serialização** — constrói `JsonObject`, `JsonArray`, `JsonPrimitive` ou `JsonReference` conforme o tipo em memória.
4. **Referências** — onde `@Reference` aponta para um objeto já marcado, emite `{ "$ref": "<uuid>" }` em vez do objeto completo.
5. **Texto** — `println(json)` ou `json.toString()` devolve JSON válido (pretty-print opcional).

### Regras importantes

| Entrada em memória | Resultado no modelo |
|--------------------|---------------------|
| Objeto Kotlin (classe) | `JsonObject` com `$type` e propriedades |
| `Map<String, *>` | `JsonObject` **sem** `$type` |
| `Iterable` (lista, etc.) | `JsonArray` |
| Primitivo / `null` | `JsonPrimitive` |
| Alvo de `@Reference` | `$id` no objeto completo; `$ref` onde é referenciado |
| Classe com `@JsonString` | `JsonPrimitive` (string) via plugin |

A atribuição de UUIDs, a deteção de partilha de instâncias e a substituição por `$ref` são **transparentes**: o utilizador apenas anota as propriedades que representam ligações no grafo.

### Fase 1 — JSON standard

Depois de `toJson`, o modelo pode ser lido e alterado antes de imprimir:

```kotlin
val d = Date(31, 4, 2026)
val json = ProJson().toJson(d) as JsonObject
json.setProperty("year", 2027)
println(json)
// {"$type": "Date", "day": 31, "month": 4, "year": 2027}
```

```kotlin
val list = listOf("a", null, "b")
val json = ProJson().toJson(list) as JsonArray
json.add("c")
println(json)
// ["a", null, "b", "c"]
```

**API de manipulação**

* `JsonObject`: `setProperty`, `getProperty`, `removeProperty`, `keys`
* `JsonArray`: `add`, `get`, `remove`, `size`
* Travessia: `json.forEachDepthFirst { nó -> ... }` (ordem em profundidade)

### Fase 2 — Referências e personalização

* `@Reference` em propriedades que apontam para outros objetos do grafo.
* `@JsonProperty("nome")` e `@JsonIgnore` para controlar o objeto emitido.
* `@JsonString(Serializer::class)` com implementação de `JsonStringSerializer<T>` para formatos customizados.

---

## Anotações

### Referências entre objetos

Marque propriedades que representam **ligações** no grafo (não valores embutidos). Objetos referenciados recebem `$id` na primeira ocorrência completa; ocorrências seguintes usam `$ref`.

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
    val description: String,
    @JsonIgnore
    val deadline: Date?,
    @JsonProperty("deps")
    val dependencies: List<Task>
)

// {"$type": "Task", "desc": "T1", "deps": []}
```

---

### Ignorar propriedades

```kotlin
@JsonIgnore
val deadline: Date?
```

Propriedades anotadas não aparecem no JSON gerado.

---

### Serialização personalizada (plugin)

Implemente `JsonStringSerializer` e associe-o à classe com `@JsonString`:

```kotlin
import projson.core.JsonString
import projson.core.JsonStringSerializer

object DateAsText : JsonStringSerializer<Date> {
    override fun serialize(value: Date): String =
        "%02d/%02d/%04d".format(value.day, value.month, value.year)
}

@JsonString(DateAsText::class)
data class Date(val day: Int, val month: Int, val year: Int)

val json = ProJson().toJson(listOf(Date(30, 2, 2026), Date(31, 4, 2026)))
println(json)
// ["30/02/2026", "31/04/2026"]
```

O serializer pode ser `object` (singleton) ou classe com construtor sem argumentos.

---

## Estrutura do projeto

```
ProJson/
├── build.gradle.kts
├── settings.gradle.kts
├── src/
│   ├── main/kotlin/projson/
│   │   ├── core/
│   │   │   ├── ProJson.kt           # Entrada: toJson()
│   │   │   ├── Annotations.kt       # @Reference, @JsonProperty, ...
│   │   │   └── JsonStringSerializer.kt
│   │   └── model/
│   │       ├── JsonValue.kt         # Interface raiz (Composite)
│   │       ├── JsonObject.kt
│   │       ├── JsonArray.kt
│   │       ├── JsonPrimitive.kt
│   │       ├── JsonReference.kt
│   │       ├── JsonVisitor.kt
│   │       ├── JsonTraversal.kt     # forEachDepthFirst
│   │       └── JsonConversions.kt
│   └── test/kotlin/projson/
│       ├── ProJsonTest.kt           # Integração fases 1 e 2
│       ├── JsonObjectTest.kt
│       ├── JsonArrayTest.kt
│       ├── JsonPrimitiveTest.kt
│       ├── JsonReferenceTest.kt
│       └── JsonTraversalTest.kt
└── README.md
```

A documentação da API pública segue estilo Javadoc/KDoc nos ficheiros `core` e `model`.

---

## Testes

Todas as funcionalidades principais estão cobertas por testes unitários, de forma a garantir a corretude da serialização, da manipulação do modelo, das referências e das anotações.

```bash
./gradlew test
```

**Pacotes de testes**

| Ficheiro | O que valida |
|----------|----------------|
| `ProJsonTest` | Objetos, arrays, `@Reference`, `@JsonProperty`, `@JsonIgnore`, `@JsonString` |
| `JsonObjectTest` / `JsonArrayTest` | CRUD no modelo em memória |
| `JsonPrimitiveTest` | Valores escalares e `null` |
| `JsonReferenceTest` | Formato `$ref` |
| `JsonTraversalTest` | Percursos em profundidade |

---

## Começar

### Requisitos

* JDK **17** ou superior
* Gradle (wrapper incluído no repositório)

### Obter o código

```bash
git clone https://github.com/leticiacascais/ProJson.git
cd ProJson
```

### Compilar e testar

```bash
./gradlew build
./gradlew test
```

### Gerar o JAR

```bash
./gradlew jar
```

O artefacto fica em `build/libs/ProJson-<versão>.jar`. Para uso em outro projeto Gradle/Maven, pode adicionar o JAR como dependência local ou descarregar a **release** publicada em [GitHub Releases](https://github.com/leticiacascais/ProJson/releases).

### Tutorial rápido — três passos

**1. Criar o serializador**

```kotlin
val proJson = ProJson()
```

**2. Converter para modelo JSON**

```kotlin
val modelo: JsonValue = proJson.toJson(meuValor)
// Faça cast se souber o tipo: as JsonObject ou as JsonArray
```

**3. Obter texto JSON**

```kotlin
println(modelo)   // JSON válido
```

Opcionalmente, altere o modelo antes do passo 3 (`setProperty`, `add`, `removeProperty`, etc.).

### Dependência no projeto

Sem publicação em repositório Maven Central, use o JAR da release ou o output de `./gradlew jar`:

```kotlin
// build.gradle.kts (exemplo)
dependencies {
    implementation(files("libs/ProJson-1.0-SNAPSHOT.jar"))
}
```

Certifique-se de que o módulo que consome a biblioteca também inclui `kotlin("reflect")` se serializar classes Kotlin por reflexão.

### API pública (resumo)

| Classe / função | Descrição |
|-----------------|-----------|
| `ProJson.toJson(Any?)` | Converte qualquer valor suportado em `JsonValue` |
| `JsonObject` | Objeto JSON mutável |
| `JsonArray` | Array JSON mutável |
| `JsonPrimitive` | String, número, booleano ou `null` |
| `JsonReference` | Nó `{ "$ref": "uuid" }` |
| `JsonValue.forEachDepthFirst` | Travessia da árvore |
| `@Reference`, `@JsonProperty`, `@JsonIgnore`, `@JsonString` | Personalização (fase 2) |
| `JsonStringSerializer<T>` | Plugin para `@JsonString` |

### Problema que a biblioteca resolve

JSON é uma árvore; estruturas em grafo (vários nós a apontar para o mesmo objeto) exigem identificadores. O ProJson adota o padrão `$id` / `$ref` do enunciado do projeto **Advanced Programming 2025/2026**, escondendo a gestão de UUIDs por detrás de uma API Kotlin idiomática.

---

**Disciplina:** Advanced Programming — Projeto 2025/2026  
**Repositório:** [github.com/leticiacascais/ProJson](https://github.com/leticiacascais/ProJson)
