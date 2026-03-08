# ADR 0018 – Ícones Lucide nas ações das tabelas admin

## Status

Aceito.

## Contexto

As tabelas do painel admin (Clientes, Contatos, Serviços, Staff, Assinaturas) exibem ações em texto
(Ver, Editar, Excluir) e um botão "Novo X" no topo. Para interface mais limpa e consistente, deseja-se
representar essas ações por ícones, com uma fonte de ícones única e leve no frontend.

## Decisão

- **Biblioteca:** `lucide-react` como fonte de ícones no projeto. Ícones tree-shakeable; uso de
  `Eye` (Ver), `Pencil` (Editar), `Trash2` (Excluir), `Plus` (Novo).
- **Ações na coluna "Ações":** Botões apenas com ícone (sem texto), tamanho `size-4`, com
  `title` e `aria-label` iguais ao texto da ação (ex.: "Ver", "Editar", "Excluir") para
  acessibilidade e tooltip.
- **Botão "Novo X":** Ícone `Plus` à esquerda do texto ("Novo cliente", "Nova assinatura", etc.)
  para manter clareza.
- **Estilo:** Mantidos os estilos por página (zinc para a maioria; âmbar para Assinaturas). Botões
  de ação com `rounded p-1.5` e cores existentes (bg-zinc-600 para Ver/Editar, bg-red-800 para Excluir;
  ou texto âmbar/vermelho com hover em Assinaturas).

## Consequências

- Páginas AdminClientesPage, AdminContatosPage, AdminServicosPage, AdminStaffPage e
  AdminAssinaturasPage passam a importar e usar os ícones de `lucide-react`. Novas tabelas ou
  ações podem reutilizar o mesmo conjunto de ícones. Dependência adicional no bundle do frontend;
  Lucide é leve e tree-shakeable.
