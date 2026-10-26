# Pendura Aí — seu corre merece crédito

Controle de vendas, fiado ("pendura"), Pix e campanhas pelo WhatsApp. Funciona no celular, sem internet, e guarda tudo na planilha do Google de cada empresa.

## Pastas

| Pasta | O que é |
|---|---|
| `site/` | **O que vai para o ar** (Cloudflare Pages ou outro site): `index.html` (o app), `treino.html` (treino guiado), ícones, manifest e modo offline |
| `src/` | A fonte. **Edite só aqui** e rode `python3 build.py` |
| `src/app.html` | O app (uma fonte só para o app e para o treino) |
| `src/treino.css`, `src/treino.js` | O que só o treino tem (mãozinha, lições, voz) |
| `src/pwa/` | Manifest, modo offline (`sw.js`), ícones e `_headers` |
| `original/` | Os arquivos como chegaram (site e treino), para comparar |
| `marca/` | Logo e ícone enviados |

```
python3 build.py      # gera site/index.html e site/treino.html
```

O `index.html` e o `treino.html` saem da mesma fonte, então nunca mais ficam com versões diferentes. O treino usa dados separados (`penduraTreino_v1`) e nada sai do celular. O app de verdade continua com a mesma chave de antes (`controleVendas_v1`): **quem já usa não perde nada**.

## O que mudou no redesenho (26/10/2026)

**Telas**
- **Menu embaixo da tela**, perto do dedão: 🧾 Vender · 🪝 Fiado · 👥 Clientes · 📦 Produtos · ☰ Mais.
- **Vender com "toca e soma"**: cada produto é um botão grande. Cada toque soma 1. Embaixo aparece o pedido com − e +, e o total. Dá para vender vários produtos de uma vez. O ⭐ produto do dia vem primeiro, e com mais de 8 produtos aparece uma busca.
- **Como pagou**: 💠 Pix · 💵 Dinheiro · 💳 Cartão · 🪝 Pendura. O botão de registrar mostra o total e se a venda é paga ou pendurada.
- **Na hora da venda**, ao digitar um cliente que já existe, aparece quanto ele deve e o que costuma levar.
- **Receber depois**: ao tocar em "Pendurado", o app pergunta **como** a pessoa pagou. "Recebi tudo" também pergunta.
- **Clientes (nova aba)**: busca, filtros (🪝 Devendo, ⭐ Frequentes, 💤 Sumidos) e ordem (quem deve mais, comprou por último, quem mais compra, A-Z).
- **Ficha do cliente**:
  - quanto deve e desde quando, com Recebi tudo, QR Pix e cobrança no WhatsApp;
  - quanto já comprou, última compra e de quantos em quantos dias volta;
  - **o que mais leva** e como costuma pagar;
  - todas as compras.
- **Mais**: Campanha, Resumo do dia, Aparência, Pix, Planilha, Backup, Segurança e Treino. As configurações continuam pedindo usuário e senha quando a planilha está conectada.
- **Resumo do dia** mostra o recebido separado por forma de pagamento.
- O cadastro de clientes saiu do Admin e foi para a aba Clientes.
- Botões de no mínimo 44 px, foco visível para teclado e menos animação para quem pede isso no celular.
- Logo novo no rodapé e na tela de boas-vindas.

**Planilha: código versão 7**
- Nova coluna **`forma`** no fim da aba Vendas (Pix, Dinheiro ou Cartão).
- **Compatível com o que já existe**:
  - a planilha versão 6 ganha a coluna sozinha na primeira venda depois de atualizar o código;
  - nenhuma linha antiga muda;
  - enquanto a empresa não atualiza, o app continua funcionando normalmente, só sem guardar a forma na planilha (ela fica guardada no celular), e o botão "Testar conexão" avisa que dá para atualizar.
- O código que leva os dados pelo WhatsApp também leva a forma de pagamento.

**Correção**
- Modo offline (`sw.js`): antes, qualquer página aberta (inclusive o treino) era guardada no lugar do app. Sem internet, o app podia abrir o treino. Agora cada página fica guardada no próprio endereço.

**Treino guiado**
- Todas as lições foram refeitas para as telas novas.
- Lição nova: **A ficha do cliente**.
- "Fazer uma venda" ensina o toca e soma e o "Como pagou".
- "Receber o fiado" ensina a dizer como a pessoa pagou.

## Como cada empresa atualiza a planilha para a versão 7 (uma vez só)

1. No app: **Mais › Planilha › 📋 Copiar código da planilha**.
2. No computador, na planilha da empresa: **Extensões › Apps Script**. Apague tudo, cole e clique em 💾 **Salvar**.
3. **Implantar › Gerenciar implantações › ✏ (editar) › Versão: Nova versão › Implantar**. Assim o link continua o mesmo e os celulares não precisam mudar nada.
4. No app: **Mais › Planilha › Testar conexão**. Na próxima sincronização, o app reenvia tudo uma vez para preencher a coluna nova.

## Testes feitos (navegador automático, tela de celular de 390 px)

- **Venda com toca e soma**:
  - vários produtos, Pix e Pendura;
  - receber depois, escolhendo a forma;
  - "Recebi tudo" na ficha;
  - voltar para pendurado.
- **Ficha do cliente e filtros.** Resumo do dia por forma. Código do WhatsApp levando e trazendo a forma.
- **Planilha (o código versão 7 rodando numa simulação do Google Sheets)**:
  - uma planilha versão 6 com venda antiga ganhou a coluna `forma` sem perder a venda;
  - dois celulares: um vende, o outro recebe no cartão, e o primeiro vê;
  - o app novo numa planilha que ainda está na versão 6 funciona e avisa para atualizar.
- **As 16 lições do treino**, do começo ao fim, tocando onde a mãozinha mostra.
- **Modo offline** num servidor local: sem internet, o app abre o app e o treino abre o treino.
- **Visual**: sem rolagem para os lados e modo escuro sem erros.
