# Decisões estruturais

## Organização por feature

As features usam uma estrutura rasa. Classes diretamente relacionadas ao domínio ficam na raiz da feature e `dto/` permanece separado para os contratos da API:

```text
feature/<domínio>/
├── ClasseController.java
├── ClasseService.java
├── ClasseEntity.java
├── IClasseRepository.java
└── dto/
```

Subpastas adicionais só existem quando representam uma subdivisão conceitual real do domínio.

## Configuração compartilhada

As configurações globais permanecem em `shared/config`. Configurações gerais ficam na raiz, enquanto apenas grupos conceitualmente distintos usam subpastas:

- `security/`: regras de autenticação, autorização, filtro JWT e handlers;
- `client/`: criação dos clientes HTTP externos.

`ApplicationConfig` e `OpenApiConfig` ficam diretamente em `shared/config`.

## Integrações de jogos

O serviço, a entidade e o repository específicos da Steam ficam em `feature/game/steam`. Os clientes HTTP reutilizáveis ficam em `shared/client/steam`, enquanto suas configurações permanecem em `shared/config/client`. Essa divisão mantém a integração compartilhada sem misturar os dados específicos de game.

O cliente do The Cat API permanece em `shared/client/thecatapi`, pois não está associado a uma feature de negócio específica.

## Gerenciamento de imagens

`ImageService` concentra as regras simples de validação, extensão e resolução do caminho. O armazenamento permanece separado em `ImageStorage` e `LocalImageStorage`, pois representa uma responsabilidade técnica real e permite trocar o backend sem duplicar a lógica do caso de uso.

O caminho físico agora é baseado no recurso:

```text
user/{userId}/avatar.png
game/{gameId}/cover.jpg
achievement/{achievementId}/icon.png
guide/{guideId}/{imageId}.jpg
```

Imagens conhecidas e únicas usam nomes semânticos. Imagens de recursos que podem possuir várias unidades usam o identificador da imagem. A entidade continua usando `ownerType`, `ownerId` e `variant` para manter a associação genérica e não criar classes específicas como `GameImage` ou `UserImage`.

## Integração com a Steam

`SteamStoreClient` permanece em `shared/client/steam/store` como cliente compartilhado, embora processe dados usados pela feature game. A sincronização dos dados retornados ainda é feita no mesmo fluxo para preservar o comportamento atual; uma divisão adicional exigiria alterar o fluxo de persistência e não é necessária.
