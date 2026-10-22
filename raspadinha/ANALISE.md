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

## 3. O que foi implementado: "Poste e ganhe" (D20 a D27)

**Como funciona para o cliente**

1. Compra (qualquer canal). No cupom sai "POSTE E GANHE ATÉ 20% OFF".
2. Na página, informa o WhatsApp, o **@ do Instagram** e o código do pedido, e dá a autorização.
3. Segue a loja e posta story com a foto do pedido marcando @recheadinha. O post no feed com #publi é opcional e aumenta o desconto.
4. **O sistema faz o resto sozinho:**
   - recebe o webhook;
   - descobre o @ pela API;
   - confere se o cliente segue e quantos seguidores tem;
   - calcula o %;
   - guarda o cupom na conta do WhatsApp;
   - avisa no WhatsApp e no Direct.

   Se faltar algo, responde no Direct dizendo o que falta. Se o cliente postou antes de se cadastrar, o post vale por 24 h.
5. No checkout do cardápio, o "seguir" é conferido de novo. Quem deixou de seguir tem o desconto pausado até voltar a seguir.

**Desconto variável, sem sorte (padrão, tudo editável no painel)**

| Ação | % | Obrigatória |
|---|---|---|
| Seguir @recheadinha | +5% | sim |
| Story com a foto do pedido marcando | +7% | sim |
| Post no feed marcando + #publi | +5% | não |
| Bônus 1.000+ seguidores | +2% | n/a |
| Bônus 5.000+ seguidores | +3% | n/a |

O máximo é 20% e R$ 15,00, com pedido mínimo de R$ 30. O desconto vale 15 dias, sobre os produtos (não o frete), e o cliente pode ganhar 1 a cada 7 dias. Cada pedido participa uma vez.

**Painel da loja (aba Instagram):** liga/desliga, perfil, % por ação, o que é obrigatório, faixas de seguidores, limites, hashtag e se acumula com o brinde. Mostra exemplos calculados, a lista de participações e o log dos eventos da Meta. O custo entra no gasto de marketing e no teto (só alerta).

**Cliente (aba Instagram):** regras claras, cadastro do @, acompanhamento e Direct. O simulador do Instagram existe só no protótipo.

**Para o backend real (FastAPI)**

- `POST /instagram/participar`
- `POST /webhooks/instagram`, com validação da assinatura `X-Hub-Signature-256`
- job periódico de reconferência
- tabelas `ig_conta`, `ig_participacao` e `ig_prova` (sem mídia)
- cupom tipo `desconto` na carteira já existente

## 4. Textos prontos (redação)

**Cupom impresso**
> POSTE E GANHE ATÉ 20% OFF
> Siga e marque @recheadinha no story com a foto do pedido. Cadastre seu @ em recheadinha.delivery/instagram

**Página**
> Comprou com a gente? Mostra pra galera e ganhe até 20% de desconto no próximo pedido pelo nosso cardápio. 🧡 Quanto mais você fizer, maior o desconto. Use #publi no post: assim fica claro para seus seguidores que você ganhou desconto. Não precisa mandar print.

**Direct (automático)**
> Oi! 💛 Vimos que você marcou a @recheadinha. Para ganhar seu desconto, cadastre seu @ e o código do pedido em recheadinha.delivery/instagram (vale por 24 h).
>
> Quase lá! 🙌 Para liberar seu desconto, falta: seguir @recheadinha. Quando fizer, responda "Já segui" aqui.
>
> Obrigado pelo carinho! 🧡 Seu desconto de 14% já está no seu WhatsApp.

**WhatsApp**
> 📸 Valeu pelo post, @cliente! Seu desconto de *14%* está guardado na sua conta da Recheadinha e entra sozinho no próximo pedido pelo cardápio.
> _Vale para pedidos a partir de R$ 30,00 no nosso cardápio, até R$ 15,00 de desconto. Até 06/11/2026._

## 5. Antes de lançar

- Advogado: confirmar "desconto por regras" fora da Lei 5.768 e validar o regulamento curto (com as cláusulas que o Instagram exige: isenção do Instagram e aviso de que a promoção não é dele).
- Decidir se "seguir" continua **obrigatório** ou vira **bônus** (menor risco com a Meta).
- Criar o app na Meta, pedir as permissões de mensagens e passar pela revisão do app.
- Fazer o piloto só com pedidos próprios e medir: % que posta, % que usa o desconto, retorno em 15 e 30 dias e custo por pedido de retorno.

## Fontes

- [Instagram User Profile API](https://developers.facebook.com/docs/instagram-platform/instagram-api-with-instagram-login/messaging-api/user-profile)
- [Story Mention (Instagram Messaging)](https://developers.facebook.com/docs/messenger-platform/instagram/features/story-mention)
- [Webhooks for Instagram Messaging](https://developers.facebook.com/docs/instagram-messaging/webhooks)
- [Instagram Help: apps de curtidas e seguidores](https://help.instagram.com/263751177667145) · [Instagram Help: promoções](https://help.instagram.com/179379842258600)
- [Plexus: promoções no Instagram](https://www.plexus.co/learn/trade-promotions/social-media/terms-and-conditions/instagram) · [ShortStack: regras de concursos](https://www.shortstack.com/blog/contest-rules-for-facebook-instagram-pinterest-and-more/)
- [Smile x Gatsby](https://news.smile.io/98810-smile-x-gatsby-reward-verified-instagram-engagement) · [Gatsby](https://open.store/shopify-apps/gatsby) · [Storify](https://apps.shopify.com/storify-sh) · [KittyChat](https://www.kittychat.ai/en/restaurants/) · [Swayze](https://apps.apple.com/py/app/swayze-discover-share/id6479729407) · [This Is Not A Sushi Bar](https://www.thedrinksbusiness.com/2018/10/this-restaurant-in-milan-offers-discounts-based-on-instagram-followers/)
- [CONAR atualiza guia (Meio & Mensagem)](https://www.meioemensagem.com.br/midia/conar-atualiza-guia-para-influenciadores-digitais) · [Propmark](https://propmark.com.br/digital/conar-atualiza-guia-de-publicidade-com-influenciadores-e-inclui-regras-sobre-ia/)
- [FTC Endorsement Guides 2023 (DWT)](https://www.dwt.com/insights/2023/07/ftc-advertising-endorsement-and-testimonial-guides)
