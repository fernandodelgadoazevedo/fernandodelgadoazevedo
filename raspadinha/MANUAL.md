# Manual da Recheadinha: como usar o sistema

Este manual é para quem vai usar o sistema no dia a dia, sem precisar de ninguém para ajudar.
O mesmo passo a passo está dentro do sistema, em **⚙️ Mais → Ajuda**.

---

## Antes de começar

- Abra o arquivo `raspadinha.html` no navegador do celular ou do computador (Chrome é o recomendado).
- Em cima tem dois botões: **🏪 Loja** (é onde você trabalha) e **📱 Cliente** (mostra o que o cliente vê).
- Na Loja há 5 botões:

| Botão | Para que serve |
|---|---|
| 🧾 **Vender** | Lançar uma venda |
| 👥 **Clientes** | Ver e cadastrar clientes, histórico e brinde de fidelidade |
| 🍟 **Produtos** | Cadastrar produtos e mudar preços |
| 🎁 **Campanhas** | Raspadinha, QR dos pedidos e Instagram |
| ⚙️ **Mais** | Ajuda, planilha e cópia de segurança, impressora |

**Tudo salva sozinho.** Não existe botão "Salvar" para editar: mudou, está salvo. Aparece um aviso "Salvo ✓" embaixo.

---

## 1. Lançar uma venda

1. Toque em **🧾 Vender**.
2. No campo **Cliente**, digite o WhatsApp com DDD (ex.: 11 99999-0001) **ou** comece a digitar o nome e escolha na lista.
   - Cliente que já existe: aparece em verde quantos pedidos ele tem, o que mais compra e quanto falta para o brinde.
   - Cliente novo: aparece um campo para escrever o **nome**.
3. Toque nos **produtos**. Cada toque soma 1. Errou? Use o **−** na lista que aparece embaixo.
4. Escolha **por onde comprou**: Balcão, WhatsApp, Instagram, iFood, Keeta ou 99Food.
5. Toque em **Salvar venda**.

Pronto. O histórico do cliente e o brinde de fidelidade se atualizam sozinhos.

- **Venda de outro dia?** Toque em "Mais detalhes" e mude a data antes de salvar.
- **Quer imprimir o cupom com a raspadinha?** Marque "Imprimir cupom com raspadinha" antes de salvar.

---

## 2. Clientes

### Cadastrar
- **👥 Clientes → + Cadastrar cliente**: WhatsApp e nome, depois **Salvar cliente**.
- Ou simplesmente lance uma venda para um número novo: ele é cadastrado na hora.

### Achar um cliente
- Digite parte do **nome** ou os **últimos números** do WhatsApp na busca.
- Use os filtros:

| Filtro | Quem aparece |
|---|---|
| ⭐ Frequentes | 3 ou mais pedidos nos últimos 30 dias |
| 🎯 Perto do brinde | Já passou de 60% da meta de fidelidade |
| 💤 Sumidos | Mais de 30 dias sem comprar (bom para mandar um "oi, sentimos sua falta") |
| 🎂 Aniversário no mês | Faz aniversário neste mês |

### A ficha do cliente (toque no nome)
- **Números:** pedidos, quanto já gastou, ticket médio, última compra, de quantos em quantos dias compra.
- **Mais compra:** os produtos que ele mais leva.
- **⭐ Fidelidade:** quanto falta para o próximo brinde.
- **🎁 Brindes:** o que ele tem guardado. Botão **Dar brinde agora** para um presente fora da meta.
- **Dados:** nome, aniversário, bairro, observações. Toque e mude: salva sozinho.
- **Compras:** todas as compras. Lançou errado? Toque em **Apagar** na venda.
- **💬 WhatsApp:** abre a conversa com o cliente.

---

## 3. Brinde de fidelidade (automático)

Em **👥 Clientes**, no cartão **⭐ Brinde de fidelidade**:

1. Deixe a chave **ligada**.
2. Escolha a meta: **"A cada 5 pedidos"** ou **"A cada 200 reais em produtos"**.
3. Escolha **qual brinde** o cliente ganha.

Quando o cliente bate a meta, o sistema guarda o brinde na conta dele e manda mensagem no WhatsApp. Depois a contagem recomeça do zero.

> É uma meta fixa: todo mundo que chega lá ganha. Não tem sorteio, e por isso não precisa de autorização do governo.

---

## 4. Produtos

- **Novo:** em **🍟 Produtos**, preencha nome, preço (ex.: 34,90) e categoria, e toque em **Adicionar produto**.
- **Mudar nome, preço ou categoria:** toque no campo, digite e pronto.
- **Saiu do cardápio:** **desligue a chave** do produto. Não apague, para o histórico das vendas continuar certo.
- Embaixo de cada produto aparece **quantos foram vendidos nos últimos 30 dias**.

---

## 5. Planilha do Google

### Trazer da planilha para o sistema
1. Na planilha, selecione as linhas **junto com a primeira linha** (a dos títulos).
2. Copie.
3. No sistema: **⚙️ Mais → Planilha e backup**.
4. Escolha se é **Clientes**, **Produtos** ou **Vendas**, cole no campo e toque em **Conferir**.
5. Leia o resumo (quantos novos, quantos atualizados, linhas com problema) e toque em **Importar agora**.

Pode importar de novo depois: quem já existe é **atualizado**, não duplicado.

**Títulos das colunas** (acento e maiúscula não importam):

| Planilha de | Colunas |
|---|---|
| Clientes | nome · whatsapp · aniversario · bairro · obs |
| Produtos | nome · preco · categoria · ativo |
| Vendas (uma linha por produto) | data · whatsapp · nome · produto · quantidade · preco · canal |

- "Telefone" e "celular" também valem como WhatsApp.
- Datas no formato **dd/mm/aaaa** (pode ter hora: 01/10/2026 19:30).
- Nas vendas, se a coluna preço estiver vazia, o sistema usa o preço cadastrado do produto.

### Levar do sistema para a planilha
1. **⚙️ Mais → Planilha e backup → Levar para a planilha** e toque em Clientes, Produtos ou Vendas.
2. Baixa um arquivo `.csv`.
3. No Google Sheets: **Arquivo → Importar → Fazer upload** e escolha o arquivo.

---

## 6. Cópia de segurança (toda semana!)

Os dados ficam guardados **neste aparelho**. Se trocar de celular ou limpar o navegador, eles somem.

1. **⚙️ Mais → Planilha e backup → Baixar cópia de segurança**.
2. Guarde o arquivo no **Google Drive**.
3. O sistema avisa na tela de vendas quando passar de 7 dias sem cópia.

**Para voltar uma cópia:** **Restaurar de uma cópia** e escolha o arquivo.

---

## 7. Deu algo errado?

| Problema | O que fazer |
|---|---|
| Sumiram os dados | Restaure a última cópia de segurança (item 6) |
| Não acho o cliente | Busque só pelos últimos 4 números do WhatsApp |
| Cliente cadastrado duas vezes | Cada WhatsApp só existe uma vez; confira se o número foi digitado com DDD |
| Lancei uma venda errada | Abra a ficha do cliente e toque em **Apagar** na venda |
| Não imprime | **⚙️ Mais → Impressora** |
| Cliente pediu para apagar os dados dele | Ficha do cliente → **Excluir cadastro (LGPD)** |
