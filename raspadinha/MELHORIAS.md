# Raspadinha: usabilidade, design e expansão sem custo

Data: 26/10/2026. Princípio: tudo gratuito, sem biblioteca paga e sem mudar o que foi decidido (D01 a D31). O backend continua FastAPI + PostgreSQL (D16). As mudanças são de experiência e de organização das telas.

## 1. O que já foi aplicado no protótipo

| Melhoria | Por quê | Referência |
|---|---|---|
| **Barra de navegação embaixo** na visão do cliente, com ícones | É a área que o polegar alcança. Os destinos principais ficam sempre à mão | Thumb zone (Hoober), guias de navegação inferior |
| **Botões de no mínimo 44 px**, quantidade com botão de 40 px | Menos toque errado, inclusive andando ou com uma mão só | Apple 44 pt, Google 48 dp, WCAG 2.2 |
| **Foco visível** ao navegar por teclado | Acessibilidade | WCAG 2.4.7 |
| **"Revelar sem raspar"** na raspadinha | Quem não consegue arrastar, usa leitor de tela ou teclado também participa. A raspagem continua sem decidir nada (D02) | Boas práticas de raspadinha acessível |
| **Comemoração**: confete curto + vibração | Momento de prazer na revelação e na escolha. Desliga sozinho com "reduzir movimento". A vibração só funciona no Android | `prefers-reduced-motion`, Vibration API |
| **Mensagens anunciadas ao leitor de tela** (erro = alerta) e avisos rápidos (toast) | Retorno claro sem tirar a pessoa do lugar | `role="status"` / `role="alert"` |
| **"🔁 Pedir de novo"** no cardápio | Atalho de recompra é o que mais traz pedido de volta | Estudos de apps de restaurante |
| **Passos 1-2-3 no Poste e ganhe** (Cadastro → Story → Desconto) | O cliente vê onde está e o que falta | Indicador de progresso |
| **Painel com cartões recolhíveis** (os menos usados já vêm fechados, e cada aparelho lembra o que deixou aberto) | Bem menos rolagem no celular | Revelação progressiva |
| **"Pronto para lançar?"** no topo do painel | Mostra o que falta antes de ligar. Detecta **termos de sorte** nos textos (D19) e lembrete sem "SAIR" (D13) | Checklist de onboarding |
| **"WhatsApp enviados no mês"** no painel | Acompanhar o custo (ver item 2.2) | Mudança de preço da Meta |
| `theme-color`, área segura do iPhone, barras de rolagem discretas | Acabamento de app | n/a |

Tudo foi testado num navegador automático em tela de celular (390 px), no claro e no escuro, sem erros e sem rolagem lateral.

## 2. Próximos passos de expansão (todos gratuitos)

### 2.1 Virar "app" sem loja de aplicativos (PWA)

- Arquivo `manifest.json` + *service worker*. O cliente instala o cardápio e a raspadinha na tela inicial, sem baixar nada da loja de apps, e as telas abrem mesmo com internet fraca.
- **Notificações push na web: grátis.** No Android funcionam direto. No iPhone, só com o app instalado na tela inicial (iOS 16.4+).
- **Hospedagem grátis** da parte estática no Cloudflare Pages (tráfego estático sem cobrança no plano grátis). O FastAPI continua onde está.

### 2.2 Canal de aviso pelo custo, não pelo hábito

Fornecedores relatam que, **desde 01/10/2026, a Meta cobra também as mensagens de atendimento do WhatsApp**, com uma franquia mensal citada de cerca de 1.000. É afirmação de terceiros: **confira na página oficial de preços da Meta**. Proposta de ordem de canais para lembretes e avisos:

1. **Push da web** (grátis, se o cliente instalou e aceitou);
2. **Direct do Instagram** (grátis, só dentro das 24 h, D28);
3. **WhatsApp** só para o essencial: confirmação do brinde e 1 lembrete (D13, D31).

### 2.3 Funil de métricas próprio (sem Google Analytics)

A auditoria já registra os eventos. Basta consultar no PostgreSQL:

- QR gerado → raspado → escolhido → usado;
- Instagram: cadastro → story → desconto usado;
- recompra em até 15 e 30 dias.

Não custa nada e evita mandar dados de clientes para terceiros (mais simples na LGPD).

### 2.4 Divulgação gratuita

- **Perfil da Empresa no Google**: botão "Pedir online" apontando para o cardápio próprio, e publicações com a campanha.
- **Bio do Instagram** com o link do Poste e ganhe.
- **QR de mesa ou balcão** para quem retira no local.

### 2.5 Qualidade contínua, de graça

- **Lighthouse** (já vem no Chrome) e **axe DevTools** (versão grátis): desempenho e acessibilidade a cada versão.
- **Testes automáticos** com Playwright, como os usados aqui: cada mudança roda o fluxo completo antes de publicar.

### 2.6 Expansão para outras lojas do saas.delivery

O módulo já é um plugin por empresa (D16). Para escalar:
- textos, cores e @ da loja como configuração por empresa (sem nada fixo "Recheadinha" no código);
- um "modelo de campanha" que a loja nova ativa em 1 clique, passando pelo checklist "Pronto para lançar?".

## Fontes

- [Thumb zone (Parachute Design)](https://parachutedesign.ca/blog/thumb-zone-ux/) · [Navegação inferior (Smashing)](https://shop.smashingmagazine.com/2016/11/the-golden-rules-of-mobile-navigation-design/) · [Navegação inferior 2025 (AppMySite)](https://blog.appmysite.com/bottom-navigation-bar-in-mobile-apps-heres-all-you-need-to-know/)
- [Alvo de toque e acessibilidade (Siteimprove)](https://career-prod2.siteimprove.com/blog/motor-impairments-and-mobile-ui-the-touch-target-problem/) · [WCAG 2.2 (Deque)](https://dequeuniversity.com/resources/wcag-2.2) · [Checklist WCAG 2.2](https://getwcag.com/en/wcag-2-2-checklist)
- [Fidelidade em restaurantes (Voucherify)](https://www.voucherify.io/blog/restaurant-loyalty-programs-best-practices-and-examples) · [Design de apps de restaurante 2026 (Per Diem)](https://www.tryperdiem.com/webinar-collections/the-app-design-principles-every-restaurant-needs-to-know-in-2026) · [Cordial](https://cordial.com/resources/6-data-driven-strategies-to-make-your-restaurant-loyalty-program-more-personal/)
- [Vibração e acessibilidade (W3C)](https://lists.w3.org/Archives/Public/public-device-apis-log/2024Jun/0129.html)
- [Preços do WhatsApp (Meta)](https://developers.facebook.com/documentation/business-messaging/whatsapp/pricing) · [Mudança de 01/10/2026 (meetergo)](https://help.meetergo.com/en/integrations/automation/whatsapp-pricing) · [Zoho](https://help.zoho.com/portal/ja/community/topic/whatsapp-message-pricing-changes-effective-october-1-2026)
- [Cloudflare Pages grátis (Flavio Copes)](https://flaviocopes.com/hosting-free-tiers/) · [limites (eastondev)](https://eastondev.com/blog/en/posts/dev/20260526-cloudflare-free-limits/)
