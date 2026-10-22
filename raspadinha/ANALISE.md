# Raspadinha saas.delivery: análise, pesquisa e "Poste e ganhe"

Data: 22/10/2026 · Arquivo: `raspadinha.html` (protótipo de 1 arquivo, sem backend; as funções marcadas "SERVIDOR" representam endpoints do FastAPI)

## 1. Análise do protótipo atual

**O que ele faz**

- **Loja:** gera QR avulso (iFood, Keeta, 99Food, WhatsApp, Instagram), simula pedido do iFood com integração e pedido da plataforma própria. Desenha o cupom de 58 mm (384 pontos) e imprime por RawBT, Bluetooth BLE (ESC/POS `GS v 0`), menu do celular ou PNG.
- **Painel:** KPIs, teto de gasto (só alerta), canais, frete grátis por raio, pausas por dia e horário, brindes (estoque, custo, condição), lembrete por WhatsApp, histórico e auditoria.
- **Cliente:** raspagem em canvas, escolha livre do brinde, vínculo do WhatsApp por mensagem pronta (wa.me), cardápio com brinde aplicado a R$ 0,00.
- **Base legal:** a mecânica foi desenhada para não ter sorte (D01 a D19). Todo pedido ganha e o cliente escolhe, o que mantém a campanha fora da Lei 5.768/1971.

**Pontos fortes**

- As decisões de produto estão documentadas (D01 a D19) e citadas no código.
- Idempotência por canal + pedido. O token vem de `crypto` (6 caracteres de 32, sem viés de módulo) e não tem dado pessoal.
- O estoque é reservado no momento da escolha e as condições são revalidadas no "servidor".
- A impressão térmica é bem resolvida: binarização, faixas de 24 linhas e QR desenhado ponto a ponto.
- O texto ao cliente evita termos de sorte (D19).

**Bugs encontrados (corrigidos nesta versão)**

1. **QR expirado aparecia como "já usado".** Ao abrir de novo um QR expirado, a tela dizia "Este código já foi usado" e mostrava a conta `...null`. Agora mostra "Este código expirou".
2. **O "teto mensal" somava o gasto de todo o período.** Os alertas também nunca rearmavam no mês seguinte. Agora o realizado conta só o mês corrente, e os alertas são marcados por mês.

**Riscos e melhorias (não alterados, para decisão do dono)**

- **Limite diário (D17) furado nos marketplaces.** O cliente do iFood e similares só é identificado depois, em `enviarWa`, e ali o limite não é checado.
- **Estoque preso.** Brinde escolhido que nunca foi vinculado ao WhatsApp, ou que expirou, não devolve o estoque reservado.
- **Escolha depois do vencimento.** `escolherBrinde` não confere se o QR venceu (status `escolhendo` após a validade).
- **Força bruta no token.** O token tem cerca de 30 bits. No backend real é preciso limitar tentativas por IP.
- **Vínculo por wa.me.** Quem tem o papel do QR fica com o brinde. É aceitável (D07), mas vale registrar.
- **Código morto.** Em `render()`, `p.id===0` nunca acontece.

## 2. Como é feito lá fora

| Referência | O que faz | O que aproveitamos |
|---|---|---|
| Gatsby + Smile.io (EUA, Shopify) | Confere menções no Instagram (story, post, reels) e seguidas pelo @ do cliente, e dá pontos ou cupom | Conferência automática pelo @, sem print |
| Storify (Shopify) | Dá cupom ou reembolso parcial para quem posta story marcando a loja | Story marcando = ação principal |
| KittyChat (restaurantes) | Direct automático quando a loja é marcada. Pode exigir "seguir" e conferir antes de revelar o cupom | Resposta automática no Direct + botão "Já segui" |
| Swayze (Nova York) | App de vantagens em bares por story marcando o local | Recompensa no próximo consumo |
| This Is Not A Sushi Bar (Milão, 2018) | Desconto proporcional ao número de seguidores (ação de lançamento) | Bônus por faixa de alcance |

**Como funciona por baixo, na API oficial da Meta**

- **Story marcando a loja:** chega um webhook `messages` com anexo `story_mention`. Só chega se a conta do cliente for pública, ou privada mas seguindo a loja. A mídia **não pode ser guardada**, só o link CDN, que para de funcionar quando o story some.
- **Seguir e seguidores:** a User Profile API (`GET /{IGSID}`) traz `is_user_follow_business` e `follower_count`. Só funciona depois que o cliente interagiu com a loja (marcou em story ou mandou Direct). Não existe webhook de "seguiu", então a conferência acontece na próxima interação.
- **Post no feed:** chega pelo webhook `mentions` (menção na legenda). Marcação na foto não gera webhook.
- **Requisitos:** conta profissional do Instagram e app aprovado pela Meta com permissões de mensagens.

**Regras e leis**

- **Instagram:** as diretrizes proíbem oferecer *dinheiro* em troca de curtidas, seguidores ou comentários. Desconto em produto é zona cinzenta. Há relato (não confirmado na página da Meta) de que, desde 2025, a Meta desencoraja exigir "seguir" como condição. Também é proibido pedir para marcar quem não está na foto.
- **CONAR (guia de influenciadores, edição de maio de 2026):** post motivado por benefício deve deixar o incentivo claro (#publi, #publicidade), visível sem clicar em "mais".
- **FTC (EUA, Endorsement Guides 2023):** post ou avaliação incentivados exigem aviso claro. É a mesma linha do CONAR.
- **Lei 5.768/1971:** o desconto varia **por regra pública e sem sorte**, então segue fora do regime de autorização (D01).
- **LGPD:** o @ é pedido com consentimento, para uma finalidade só, e o vínculo é 1 @ para 1 WhatsApp.

## 3. O que foi implementado: "Poste e ganhe" (D20 a D31), versão que evita banimento

**Como funciona para o cliente**

1. Compra no cardápio próprio. No cupom sai: "POSTE E GANHE ATÉ 20% OFF. Tire foto do seu pedido, poste no story e marque @recheadinha".
2. Na página, cadastra o WhatsApp, o **@ do Instagram** e o nº do pedido, e dá a autorização (LGPD).
3. **Tira a foto do item** (ex.: a batata recheada) na própria página. O sistema aplica uma moldura com o nome da loja e o aviso "Ganhei desconto da @recheadinha por este post #publi".
4. Toca em **Postar no story**. O celular abre o compartilhamento: Instagram → Story. O cliente adiciona a figurinha **Menção** com @recheadinha. Há um botão "Copiar @", e se o aparelho não compartilhar direto, a foto é salva.
5. **O sistema faz o resto sozinho:**
   - a Meta avisa a marcação (webhook `story_mention`);
   - a API informa o @ e os seguidores;
   - o sistema calcula o %, guarda o cupom na conta e avisa pelo WhatsApp e pelo Direct.

   Se o cliente postou antes de se cadastrar, o story vale por 24 h.
6. **Botão "Abrir @recheadinha no Instagram para seguir"** (opcional). Usa o link universal `https://www.instagram.com/recheadinha/`, que abre o app no perfil, e o cliente toca em Seguir. **Seguir não vale desconto** e aparece no painel só como métrica.

**Desconto variável, sem sorte (padrão, editável no painel)**

| Ação | % | Regra |
|---|---|---|
| Story com a foto do item marcando @recheadinha | +12% | base: libera o desconto |
| Post no feed com a foto do item + #publi | +5% | bônus opcional |
| Bônus 1.000+ / 5.000+ seguidores | +2% / +3% | lido pela API |
| Seguir a loja | 0% | só convite (D23) |

O máximo é 20% e R$ 15, com pedido mínimo de R$ 30. O desconto vale 15 dias, sobre os produtos, e o cliente pode ganhar 1 a cada 7 dias. Cada pedido participa uma vez. O desconto nunca diminui depois de liberado.

**Painel da loja:**
- % por ação, faixas de seguidores e limites;
- moldura com aviso de publicidade;
- chave "aceitar pedidos de marketplace", desligada por padrão;
- lista de participações com link do story e "Anular", disponível por 24 h para story que não mostra o item;
- log dos eventos da Meta.

## Como não ser banido (o que o sistema respeita)

| Plataforma | Regra | Como foi resolvido |
|---|---|---|
| Instagram | Não trocar vantagem por seguidores ou curtidas. As regras de promoção (2025) não aceitam "seguir" como requisito | Seguir vale 0% e não é condição. Só um botão que abre o perfil (D23) |
| Instagram | Marcação deve ser verdadeira | A foto é do pedido. Nada de "marque amigos". A loja pode anular story sem o item (D29) |
| Instagram | Direct automático só até 24 h após a última mensagem/marcação do cliente. Message tags não podem levar promoção | O sistema bloqueia o Direct fora da janela e manda o aviso pelo WhatsApp (D28) |
| Instagram | Só API oficial, app revisado, sem guardar mídia do story | Webhooks + User Profile API. Nada de robô, senha ou raspagem de perfil. Guarda só o link (D22, D24) |
| WhatsApp | API não oficial (que imita o WhatsApp Web) leva a banimento do número | O sistema real deve usar a WhatsApp Business Platform (Cloud API). Lembretes fora de 24 h só com modelo aprovado (Marketing) e opt-in (D31) |
| iFood e outros | Contratos costumam vetar desviar o cliente para outro canal | Só pedidos do cardápio próprio participam, por padrão (D30) |
| CONAR / FTC | Post incentivado precisa deixar claro o benefício | Moldura com #publi e aviso. Hashtag conferida no feed (D25, D29) |
| Lei 5.768/71 | Sorte exige autorização | % por regra pública, sem sorte (D01, D20) |

Ponto em aberto: a documentação da Meta não diz explicitamente se uma **marcação em story** abre a janela de 24 h (para respostas a story, abre). O protótipo assume que sim. Vale testar no app real antes de lançar; se não abrir, o aviso vai só pelo WhatsApp.

**Para o backend real (FastAPI)**

- `POST /instagram/participar`
- `POST /webhooks/instagram`, conferindo `X-Hub-Signature-256`
- job periódico
- tabelas `ig_conta`, `ig_participacao` e `ig_prova` (só o link)
- cupom tipo `desconto` na carteira

## 4. Textos prontos (redação)

**Cupom impresso**
> POSTE E GANHE ATÉ 20% OFF
> Tire foto do seu pedido, poste no story e marque @recheadinha. Cadastre seu @ em recheadinha.delivery/instagram

**Moldura da foto**
> RECHEADINHA · Ganhei desconto da @recheadinha por este post #publi

**Página**
> Comprou com a gente? Tire uma foto do seu pedido, poste no story marcando a gente e ganhe até 20% de desconto no próximo pedido pelo nosso cardápio. 🧡 A foto precisa mostrar o seu pedido. A conferência é automática: não precisa mandar print.

**Direct (automático)**
> Oi! 💛 Vimos que você marcou a @recheadinha. Para ganhar seu desconto, cadastre seu @ e o código do pedido em recheadinha.delivery/instagram (vale por 24 h).
>
> Quase lá! 🙌 Para liberar seu desconto, poste um story com a foto do seu pedido e marque @recheadinha com a figurinha Menção.
>
> Obrigado pelo carinho! 🧡 Seu desconto de 14% já está no seu WhatsApp.

**WhatsApp**
> 📸 Valeu pelo post, @cliente! Seu desconto de *14%* está guardado na sua conta da Recheadinha e entra sozinho no próximo pedido pelo cardápio.
> _Vale para pedidos a partir de R$ 30,00 no nosso cardápio, até R$ 15,00 de desconto. Até 06/11/2026._

## 5. Antes de lançar

- Advogado: confirmar "desconto por regras" fora da Lei 5.768 e validar o regulamento curto (com as cláusulas que o Instagram exige: isenção do Instagram e aviso de que a promoção não é dele).
- Contratar a WhatsApp Business Platform (API oficial) e aprovar os modelos de lembrete.
- Testar no app real se a marcação em story abre a janela de 24 h do Direct.
- Criar o app na Meta, pedir as permissões de mensagens e passar pela revisão do app.
- Fazer o piloto só com pedidos próprios e medir: % que posta, % que usa o desconto, retorno em 15 e 30 dias e custo por pedido de retorno.

## Fontes

- [Meta/Instagram promotion rules (ShortStack)](https://www.shortstack.com/help/facebook-promotion-guidelines/) · [Plexus 2025](https://www.plexus.co/resources/blog/trade-promotions/social-media/terms-and-conditions/instagram)
- [Janela de 24 h (Meta)](https://developers.facebook.com/docs/messenger-platform/send-messages) · [Política da plataforma](https://developers.facebook.com/documentation/business-messaging/messenger-platform/policy)
- [Web Share API (MDN)](https://developer.mozilla.org/docs/Web/API/Navigator/share)
- [Instagram User Profile API](https://developers.facebook.com/docs/instagram-platform/instagram-api-with-instagram-login/messaging-api/user-profile)
- [Story Mention (Instagram Messaging)](https://developers.facebook.com/docs/messenger-platform/instagram/features/story-mention)
- [Webhooks for Instagram Messaging](https://developers.facebook.com/docs/instagram-messaging/webhooks)
- [Instagram Help: apps de curtidas e seguidores](https://help.instagram.com/263751177667145) · [Instagram Help: promoções](https://help.instagram.com/179379842258600)
- [Plexus: promoções no Instagram](https://www.plexus.co/learn/trade-promotions/social-media/terms-and-conditions/instagram) · [ShortStack: regras de concursos](https://www.shortstack.com/blog/contest-rules-for-facebook-instagram-pinterest-and-more/)
- [Smile x Gatsby](https://news.smile.io/98810-smile-x-gatsby-reward-verified-instagram-engagement) · [Gatsby](https://open.store/shopify-apps/gatsby) · [Storify](https://apps.shopify.com/storify-sh) · [KittyChat](https://www.kittychat.ai/en/restaurants/) · [Swayze](https://apps.apple.com/py/app/swayze-discover-share/id6479729407) · [This Is Not A Sushi Bar](https://www.thedrinksbusiness.com/2018/10/this-restaurant-in-milan-offers-discounts-based-on-instagram-followers/)
- [CONAR atualiza guia (Meio & Mensagem)](https://www.meioemensagem.com.br/midia/conar-atualiza-guia-para-influenciadores-digitais) · [Propmark](https://propmark.com.br/digital/conar-atualiza-guia-de-publicidade-com-influenciadores-e-inclui-regras-sobre-ia/)
- [FTC Endorsement Guides 2023 (DWT)](https://www.dwt.com/insights/2023/07/ftc-advertising-endorsement-and-testimonial-guides)
