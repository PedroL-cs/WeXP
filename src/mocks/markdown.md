# Banco de Dados Orientado a Grafos

> "Grafo é uma estrutura de dados que conecta um conjunto de vértices através de
> um conjunto de arestas. Os bancos de dados de grafo modernos suportam
> estruturas de grafo multi-relacionais, onde existem tipos diferentes de
> vértices (representando pessoas, lugares, itens) e diferentes tipos de arestas
> (como por exemplo amigo de, mora em, comprado por) [...]"

**Marko Rodriguez** _Arquiteto de Sistemas de Grafo da AT&T Interactive_

---

## Introdução

Os bancos de dados orientados a grafos são sistemas de armazenamento projetados
para representar dados através de entidades e seus relacionamentos.
Diferentemente dos bancos relacionais tradicionais, que organizam informações em
tabelas, os bancos de grafos armazenam os dados como uma rede de conexões.

Esse modelo é frequentemente chamado de **whiteboard friendly**, pois permite
modelar informações da mesma forma que desenhamos elementos e suas conexões em
um quadro branco.

Quando o foco principal da aplicação está nas relações entre os dados, os bancos
de grafos costumam oferecer uma solução mais natural e eficiente.

---

# Características

- Os dados são organizados em estruturas de grafo compostas por nós e
  relacionamentos.
- Utilizam propriedades no formato chave-valor para armazenar atributos.
- Permitem representar relações complexas de forma explícita.
- Possuem esquemas flexíveis ou até mesmo sem esquema definido (_schema-less_).
- São especialmente eficientes para consultas baseadas em conexões entre
  entidades.
- Podem suportar relacionamentos estáticos ou dinâmicos.
- Facilitam a descoberta de padrões e conexões ocultas entre os dados.
- São amplamente utilizados em sistemas que exigem navegação entre múltiplos
  níveis de relacionamento.

---

# Estrutura de um Grafo

Um banco de dados orientado a grafos é composto por três elementos principais.

## Nós (Nodes)

Representam entidades do mundo real.

Exemplos:

```text
(Pessoa)
(Produto)
(Cidade)
(Empresa)
```

Exemplo real:

```text
(João)
(Notebook)
(Fortaleza)
```

---

## Relacionamentos (Edges)

Representam as conexões entre os nós.

Exemplo:

```text
(João) --[COMPROU]--> (Notebook)

(João) --[MORA_EM]--> (Fortaleza)
```

Os relacionamentos possuem algumas características importantes:

- Possuem direção.
- Possuem tipo.
- Podem armazenar propriedades próprias.
- São armazenados diretamente no banco.

Exemplo de propriedade em um relacionamento:

```json
{
  "dataCompra": "2026-06-01",
  "valor": 4500
}
```

---

## Propriedades (Properties)

São atributos associados aos nós ou aos relacionamentos.

Exemplo de nó:

```json
{
  "nome": "João",
  "idade": 20,
  "cidade": "Fortaleza"
}
```

Exemplo de relacionamento:

```json
{
  "dataCompra": "2026-06-01"
}
```

---

# Como as Consultas Funcionam

Uma das maiores vantagens dos bancos de grafos é a forma como realizam
consultas.

Nos bancos relacionais, relacionamentos entre entidades geralmente exigem
múltiplas operações de **JOIN**.

Exemplo de pergunta:

> Quais amigos dos meus amigos trabalham na mesma empresa que eu?

Em um banco relacional essa consulta pode envolver diversas tabelas e vários
JOINs.

Em um banco de grafos, a consulta consiste basicamente em percorrer os
relacionamentos existentes:

```text
Pessoa
   |
AMIGO_DE
   |
Pessoa
   |
TRABALHA_EM
   |
Empresa
```

Por esse motivo, consultas relacionais profundas costumam apresentar desempenho
superior em bancos de grafos.

---

# Comparação com Bancos Relacionais

| Banco Relacional                        | Banco de Grafos                              |
| --------------------------------------- | -------------------------------------------- |
| Dados organizados em tabelas            | Dados organizados em nós e relacionamentos   |
| Relacionamentos por chaves estrangeiras | Relacionamentos armazenados diretamente      |
| Excelente para transações estruturadas  | Excelente para navegação entre conexões      |
| JOINs podem ser custosos                | Percorrer relacionamentos costuma ser rápido |
| Esquema mais rígido                     | Esquema mais flexível                        |
| SQL como padrão dominante               | Linguagens específicas para grafos           |

---

# Linguagens de Consulta

Os bancos de grafos geralmente utilizam linguagens próprias para consulta e
manipulação dos dados.

## Cypher (Neo4j)

Cypher é a linguagem de consulta do Neo4j e possui sintaxe visual semelhante à
estrutura dos grafos.

Exemplo:

```cypher
MATCH (p:Pessoa)-[:AMIGO_DE]->(amigo)
RETURN amigo
```

Essa consulta retorna todos os amigos de uma pessoa.

---

## Gremlin

Gremlin é uma linguagem utilizada em diversos bancos de grafos distribuídos.

Exemplo:

```gremlin
g.V().hasLabel('Pessoa')
```

A consulta retorna todos os vértices rotulados como "Pessoa".

---

# Exemplo Prático de Modelagem

Imagine uma rede social:

```text
(João) ---- SEGUE ----> (Maria)

(Maria) ---- CURTIU ----> (Postagem)

(João) ---- AMIGO_DE ----> (Carlos)
```

Esse tipo de estrutura pode ser consultado rapidamente para encontrar:

- Amigos em comum.
- Recomendações de amizade.
- Conteúdos relacionados.
- Influenciadores.
- Comunidades de interesse.

---

# Casos de Uso

## Redes Sociais

Representação de amizades, seguidores, grupos e interações.

## Sistemas de Recomendação

Recomendação de produtos, filmes, músicas ou conteúdos baseada em padrões de
comportamento.

Exemplos:

- Netflix
- Spotify
- Amazon

## Detecção de Fraudes

Identificação de padrões suspeitos entre:

- Contas bancárias
- Cartões de crédito
- Endereços IP
- Dispositivos compartilhados

## Sistemas de Rotas

Utilizados em:

- GPS
- Logística
- Planejamento de transporte

Permitem encontrar caminhos mínimos e rotas ótimas.

## Grafos de Conhecimento

Representação de conceitos e relações semânticas.

Exemplos:

- Mecanismos de busca
- Assistentes virtuais
- Sistemas de IA

## Redes de Telecomunicações

Mapeamento de conexões entre dispositivos, servidores e usuários.

---

# Escalabilidade

Os bancos de grafos podem crescer de duas formas:

## Escalabilidade Vertical

Consiste em aumentar os recursos de uma única máquina:

- Mais memória RAM
- Mais processadores
- Mais armazenamento

## Escalabilidade Horizontal

Consiste em adicionar novos servidores ao sistema.

Apesar de possível, a escalabilidade horizontal em bancos de grafos costuma ser
mais complexa do que em bancos relacionais, pois os relacionamentos
frequentemente atravessam diferentes conjuntos de dados.

---

# Vantagens

- Modelagem intuitiva e próxima do mundo real.
- Excelente desempenho para consultas relacionais complexas.
- Menor necessidade de JOINs.
- Flexibilidade de esquema.
- Facilidade para identificar padrões e conexões.
- Ótima escolha para dados altamente conectados.
- Facilita sistemas de recomendação e análise de relacionamentos.
- Permite navegação eficiente em grandes redes de dados.

---

# Desvantagens

- Menos eficiente para dados tabulares simples.
- Curva de aprendizado maior.
- Menor padronização em comparação ao SQL.
- Escalabilidade horizontal pode ser complexa.
- Menor disponibilidade de profissionais especializados.
- Ecossistema menor quando comparado aos bancos relacionais.
- Pode consumir mais armazenamento devido aos relacionamentos explícitos.
- Nem sempre é a melhor opção para relatórios analíticos e agregações massivas.

---

# Principais Bancos de Dados Orientados a Grafos

| Banco          | Características                                |
| -------------- | ---------------------------------------------- |
| Neo4j          | Banco de grafos mais popular do mercado        |
| JanusGraph     | Distribuído e escalável                        |
| ArangoDB       | Multimodelo (documentos, grafos e chave-valor) |
| TigerGraph     | Focado em análise de grafos em larga escala    |
| Amazon Neptune | Serviço gerenciado da AWS para grafos          |
| OrientDB       | Banco multimodelo com suporte a grafos         |

---

# Conclusão

Os bancos de dados orientados a grafos oferecem uma forma natural de representar
entidades e seus relacionamentos. Eles são especialmente indicados para
aplicações em que as conexões entre os dados são tão importantes quanto os
próprios dados.

Embora não substituam os bancos relacionais em todos os cenários, destacam-se em
problemas envolvendo redes complexas, recomendações, detecção de fraudes,
sistemas de rotas e grafos de conhecimento, tornando-se uma ferramenta cada vez
mais relevante no desenvolvimento de sistemas modernos.
