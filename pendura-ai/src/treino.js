<script>
/* ===== Pendura Aí — Treino guiado (manual interativo) ===== */
(function(){
'use strict';
try{ localStorage.setItem(KEY+'_boasvindas','1'); }catch(e){}
const q=s=>document.querySelector(s);
const abaAtiva=n=>!document.getElementById('tab-'+n).classList.contains('hidden');
const admAtiva=a=>abaAtiva('admin') && !document.getElementById('adm-'+a).classList.contains('hidden');
const aberto=id=>{ const e=document.getElementById(id); return !!e && !e.classList.contains('hidden'); };
const foco=id=>document.activeElement===document.getElementById(id);
const val=id=>String((document.getElementById(id)||{}).value||'');
const digitos=id=>val(id).replace(/\D/g,'').length;
const TAB=n=>`.tab[data-tab="${n}"]`, CHIP=a=>`#admTabs [data-a="${a}"]`;
const marcas={}; const marca=n=>{ marcas[n]=(marcas[n]||0)+1; };
const novo=(c,n)=>(marcas[n]||0)>(c.m[n]||0);
function foto(){ return {prod:prodAtivos().length, cli:cliAtivos().length, vendas:ativas().length, pagos:ativas().filter(s=>s.paid).length, m:{...marcas}}; }

/* ---- No treino nada sai do celular ---- */
window.open=()=>{ marca('whats'); aviso('📱 No app de verdade, agora abriria o WhatsApp com a mensagem pronta.','info',5000); return null; };
window.baixar=(c,nome)=>{ marca('baixou'); aviso('📄 No app de verdade, a cópia "'+nome+'" seria salva no celular.','ok',5000); };
window.copiarTexto=async(t,ok)=>{ marca('copiou'); aviso('✔ '+(ok||'Copiado')+' (treino)','ok'); return true; };
window.fetch=()=>Promise.reject(new Error('treino'));

/* ---- Pré-requisitos da jornada ---- */
const PRE={
  produto:{ok:()=>prodAtivos().length>0, txt:'cadastrar pelo menos um produto', tarefa:'produto'},
  venda:{ok:()=>ativas().length>0, txt:'fazer uma venda', tarefa:'venda'},
  aberta:{ok:()=>ativas().some(s=>!s.paid&&s.date===hoje()), txt:'ter uma venda em aberto (fiado)', tarefa:'venda'},
  whats:{ok:()=>cliAtivos().some(c=>c.phone), txt:'cadastrar um cliente com WhatsApp', tarefa:'cliente'},
  pix:{ok:()=>pixConfigurado(), txt:'configurar o Pix da loja', tarefa:'pix'}
};
/* ---- Tarefas ---- */
const ir=(n,txt,extra)=>({tipo:'toque', alvo:TAB(n), ok:()=>abaAtiva(n), t:txt||('Toque na aba '+n.toUpperCase()+'.'), ...(extra||{})});
const chip=(a,nome)=>({tipo:'toque', alvo:CHIP(a), ok:()=>admAtiva(a), t:'Toque em '+nome+'.'});
const tocarCampo=(id,nome,extra)=>({tipo:'toque', alvo:'#'+id, ok:()=>foco(id)||val(id).length>0, t:'Toque no espaço '+nome+'.', ...(extra||{})});
const info=(alvo,t,small)=>({tipo:'info', alvo, t, small});
const TAREFAS=[
 {id:'conhecer', grupo:'Comece aqui', ic:'👀', titulo:'Conhecer o app', desc:'As partes do app, uma por uma', passos:[
   info('.tabs','Bem-vindo! Lá em cima ficam as ABAS. Cada aba é uma parte do app.','Vamos passar por todas. É só tocar onde a mãozinha mostrar.'),
   ir('rel','Toque em RELATÓRIO.',{small:'Aqui você vê quem está devendo.'}),
   ir('camp','Agora toque em CAMPANHA.',{small:'Aqui você manda avisos para os clientes.'}),
   ir('prod','Toque em PRODUTOS.',{small:'Aqui você cadastra o que vende.'}),
   ir('admin','Toque em ADMIN.',{small:'Aqui ficam as configurações. No app de verdade, o Admin pede usuário e senha.'}),
   ir('vendas','Para voltar, toque em VENDAS.',{small:'É a tela principal, onde você anota as vendas.'}),
   info('#btnMsg','Este botão lá embaixo monta o resumo do dia para mandar no WhatsApp.')
 ]},
 {id:'produto', grupo:'Comece aqui', ic:'📦', titulo:'Cadastrar um produto', desc:'O que você vende e o preço', sozinho:'Cadastre OUTRO produto, com um nome diferente.', passos:[
   ir('prod','Toque na aba PRODUTOS.'),
   tocarCampo('pNome','NOME'),
   {tipo:'digitar', alvo:'#pNome', ok:()=>val('pNome').trim().length>=2, t:'Escreva o nome do produto.', small:'Exemplo: Bolo de pote', ex:'Bolo de pote', ex2:'Pão de mel', s:'Escrever o nome'},
   tocarCampo('pPreco','PREÇO'),
   {tipo:'digitar', alvo:'#pPreco', ok:()=>(parsePreco(val('pPreco'))||0)>0, t:'Escreva o preço.', small:'Exemplo: 12,50. Embaixo aparece o valor que o app entendeu.', ex:'12,50', ex2:'8,00', s:'Escrever o preço'},
   {tipo:'toque', alvo:'#btnAddProd', ok:c=>prodAtivos().length>c.prod, t:'Toque em + ADICIONAR PRODUTO.'},
   info('#listaProd','Pronto! Seu produto está na lista.','Para mudar o preço depois, use EDITAR (no app de verdade pede senha do gerente).')
 ]},
 {id:'cliente', grupo:'Comece aqui', ic:'👤', titulo:'Cadastrar um cliente', desc:'Nome e WhatsApp de quem compra', sozinho:'Cadastre OUTRO cliente, com um nome diferente.', passos:[
   ir('admin','Toque na aba ADMIN.'),
   chip('cad','CLIENTES'),
   tocarCampo('cNome','NOME'),
   {tipo:'digitar', alvo:'#cNome', ok:()=>val('cNome').trim().length>=2, t:'Escreva o nome ou apelido do cliente.', small:'Exemplo: Dona Maria', ex:'Dona Maria', ex2:'Seu João', s:'Escrever o nome'},
   tocarCampo('cFone','WHATSAPP'),
   {tipo:'digitar', alvo:'#cFone', ok:()=>digitos('cFone')>=10, t:'Escreva o WhatsApp com DDD.', small:'Exemplo: 11 91234-5678', ex:'11 91234-5678', ex2:'11 98888-7777', s:'Escrever o WhatsApp'},
   {tipo:'toque', alvo:'#btnAddCli', ok:c=>cliAtivos().length>c.cli, t:'Toque em + ADICIONAR NOME.'},
   info('#listaCli','Cliente cadastrado! Com o WhatsApp dá para cobrar e mandar avisos para ele.','Dica: na hora da venda, um nome novo também é cadastrado sozinho.')
 ]},
 {id:'venda', grupo:'Comece aqui', ic:'📝', titulo:'Fazer uma venda', desc:'Anotar quem pegou o quê', pre:['produto'], passos:[
   ir('vendas','Toque na aba VENDAS.'),
   tocarCampo('vNome','NOME / APELIDO',{small:'Vai abrir a lista dos seus clientes.'}),
   {tipo:'digitar', alvo:'#vNome', permite:['#vNome','#acLista'], ok:()=>val('vNome').trim().length>=2, t:'Escreva o nome de quem comprou, ou toque num nome da lista.', small:'Exemplo: Dona Maria', ex:'Dona Maria', s:'Escrever quem comprou'},
   info('#vProd','Aqui fica o PRODUTO. Se for outro, toque aqui e escolha.'),
   {tipo:'toque', alvo:'.qtd-btn[data-q="2"]', ok:()=>val('vQtd')==='2', t:'A pessoa pegou 2? Toque no número 2.', small:'Se foi só 1, nem precisa mexer: o 1 já vem marcado.', s:'Escolher a quantidade 2'},
   info('#vPago','Se a pessoa JÁ PAGOU, marque esta caixinha. Desta vez deixe SEM marcar: vai ficar no fiado.'),
   {tipo:'toque', alvo:'#btnAddVenda', ok:c=>ativas().length>c.vendas, t:'Toque em + REGISTRAR VENDA.'},
   info('#listaVendas','Venda anotada! Ela aparece aqui embaixo, em vermelho: EM ABERTO.','Lá em cima, os totais do dia já mudaram.')
 ]},
 {id:'pago', grupo:'Comece aqui', ic:'✅', titulo:'Marcar que pagou', desc:'Quando a pessoa paga o fiado', pre:['aberta'], passos:[
   ir('vendas','Toque na aba VENDAS.'),
   {tipo:'toque', alvo:'#listaVendas .pay.open', ok:c=>ativas().filter(s=>s.paid).length>c.pagos, t:'Recebeu o dinheiro? Toque no EM ABERTO, em vermelho.', small:'Ele vira PAGO, em verde.', s:'Marcar a venda como paga'},
   info('.totals','Veja: o PAGO aumentou e o EM ABERTO diminuiu.','Tocou errado? No app de verdade, voltar para EM ABERTO pede senha do gerente.')
 ]},
 {id:'relatorio', grupo:'Receber e cobrar', ic:'📊', titulo:'Ver quem está devendo', desc:'O total de cada pessoa', pre:['venda'], passos:[
   ir('rel','Toque na aba RELATÓRIO.'),
   info('#relFiltros','Aqui você escolhe o período: o DIA, o MÊS ou TUDO.'),
   {tipo:'toque', alvo:'#relLista [data-act="relx"]', ok:()=>relAberto.size>0, t:'Toque no botão colorido ao lado do nome (DEVE ou QUITADO).', small:'Abre a lista de compras da pessoa.', s:'Abrir as compras de uma pessoa'},
   info('.rel-det','Aqui estão as compras dessa pessoa. Dá para marcar pago, trocar a pessoa ou excluir.','Trocar e excluir pedem senha do gerente no app de verdade.')
 ]},
 {id:'cobrar', grupo:'Receber e cobrar', ic:'📲', titulo:'Cobrar pelo WhatsApp', desc:'Mandar o extrato para a pessoa', pre:['aberta'], passos:[
   ir('rel','Toque na aba RELATÓRIO.'),
   {tipo:'toque', alvo:'#relLista [data-act="ext"]', ok:()=>aberto('modal'), t:'Toque no 📲 ao lado de quem está devendo.', s:'Abrir o extrato da pessoa'},
   info('#msgTxt','Esta é a mensagem de cobrança: tem as datas, o que pegou e quanto falta.','Se o Pix estiver configurado, o código Pix já vai junto.'),
   {tipo:'toque', alvo:'#btnWa', ok:c=>novo(c,'whats'), t:'Toque no botão verde WHATSAPP.', s:'Mandar pelo WhatsApp'},
   {tipo:'toque', alvo:'#btnClose', ok:()=>!aberto('modal'), t:'Toque em FECHAR.', s:'Fechar'}
 ]},
 {id:'pix', grupo:'Receber e cobrar', ic:'💳', titulo:'Configurar o Pix', desc:'Para receber pelo Pix', passos:[
   ir('admin','Toque na aba ADMIN.'),
   chip('pix','PIX'),
   info('#pixTipo','Aqui você escolhe o tipo da sua chave Pix. Vamos usar CELULAR.'),
   tocarCampo('pixChave','CHAVE PIX'),
   {tipo:'digitar', alvo:'#pixChave', ok:()=>digitos('pixChave')>=10, t:'Escreva a chave Pix: o celular com DDD.', small:'Exemplo: 11 91234-5678', ex:'11 91234-5678', s:'Escrever a chave'},
   tocarCampo('pixNome','NOME DE QUEM RECEBE'),
   {tipo:'digitar', alvo:'#pixNome', ok:()=>val('pixNome').trim().length>=3, t:'Escreva o nome de quem recebe, igual está no banco.', small:'Exemplo: Maria Silva', ex:'Maria Silva', s:'Escrever o nome'},
   tocarCampo('pixCidade','CIDADE'),
   {tipo:'digitar', alvo:'#pixCidade', ok:()=>pixConfigurado(), t:'Escreva a cidade.', small:'Exemplo: Sao Paulo', ex:'Sao Paulo', s:'Escrever a cidade'},
   info('#pixAtualTxt','Pronto! Aqui aparece, em verde, a chave que vai receber os pagamentos.')
 ]},
 {id:'qr', grupo:'Receber e cobrar', ic:'🔳', titulo:'Receber com QR Code', desc:'A pessoa paga na sua frente', pre:['pix','aberta'], passos:[
   ir('rel','Toque na aba RELATÓRIO.'),
   {tipo:'toque', alvo:'#relLista [data-act="qr"]', ok:()=>aberto('qrTela'), t:'Toque no 🔳 ao lado de quem está devendo.', small:'Abre o QR Code do Pix com o valor certo.', s:'Abrir o QR Code'},
   info('#qrBox','A pessoa abre o app do banco, escolhe Pix › Ler QR Code e aponta para cá. O valor já vem preenchido.'),
   {tipo:'toque', alvo:'#qrFechar', ok:()=>!aberto('qrTela'), t:'Toque em FECHAR.', s:'Fechar'}
 ]},
 {id:'resumo', grupo:'Receber e cobrar', ic:'📋', titulo:'Resumo do dia', desc:'O que vendeu, pagos e fiados', pre:['venda'], passos:[
   ir('vendas','Toque na aba VENDAS.'),
   {tipo:'toque', alvo:'#btnMsg', ok:()=>aberto('modal'), t:'Toque em MONTAR MENSAGEM DO DIA, lá embaixo.', s:'Montar o resumo'},
   info('#msgTxt','Este é o resumo: o que vendeu, quem pagou e quem ficou devendo.'),
   {tipo:'toque', alvo:'#btnCopy', ok:c=>novo(c,'copiou'), t:'Toque em COPIAR.', small:'Depois é só colar no WhatsApp.', s:'Copiar'},
   {tipo:'toque', alvo:'#btnClose', ok:()=>!aberto('modal'), t:'Toque em FECHAR.', s:'Fechar'}
 ]},
 {id:'campanha', grupo:'Divulgar', ic:'📣', titulo:'Avisar os clientes', desc:'"Hoje tem…" para todo mundo', pre:['produto','whats'], antes:()=>{ camp().env=[]; save(); }, passos:[
   ir('prod','Toque na aba PRODUTOS.'),
   {tipo:'toque', alvo:'#listaProd .dia-flag', ok:()=>produtosDoDia().length>0, t:'Marque a caixinha PRODUTO DO DIA no que você tem hoje.', s:'Marcar o produto do dia'},
   ir('camp','Agora toque na aba CAMPANHA.'),
   {tipo:'toque', alvo:'#campExemplo', ok:()=>!!(camp().texto||'').trim(), t:'Toque em ✨ USAR UM EXEMPLO.', small:'Se aparecer uma pergunta, toque em OK. Depois você pode mudar o texto.', s:'Escrever a mensagem'},
   info('#campPrevia','Veja a prévia: o nome do cliente e o produto do dia entram sozinhos na mensagem.'),
   {tipo:'toque', alvo:'#campTodos', ok:()=>camp().sel.length>0, t:'Toque em MARCAR TODOS.', s:'Escolher quem recebe'},
   {tipo:'toque', alvo:'#campUmAUm', ok:()=>aberto('campProx'), t:'Toque em 📲 ENVIAR UM A UM.', s:'Começar o envio'},
   {tipo:'toque', alvo:'#campAbrir', ok:c=>novo(c,'whats'), t:'Toque em ABRIR WHATSAPP.', small:'No celular de verdade, abre a conversa com a mensagem pronta. Você manda, volta e passa para o próximo.', s:'Mandar para o primeiro'}
 ]},
 {id:'aparencia', grupo:'Configurações (Admin)', ic:'🎨', titulo:'Nome e logo da empresa', desc:'Deixar o app com a sua cara', passos:[
   ir('admin','Toque na aba ADMIN.'),
   chip('apar','APARÊNCIA'),
   tocarCampo('aNome','NOME DO NEGÓCIO'),
   {tipo:'digitar', alvo:'#aNome', ok:()=>val('aNome').trim().length>=2, t:'Escreva o nome da sua empresa.', small:'Ele aparece lá em cima, no topo do app.', ex:'Minha Loja', s:'Escrever o nome da empresa'},
   info('#logoUrl','Aqui entra a LOGO: escolha uma foto do celular ou cole o link do Google Drive.','No Drive: compartilhe como "qualquer pessoa com o link" e cole aqui.'),
   info('#corBrand','E aqui você muda as cores, se quiser.')
 ]},
 {id:'planilha', grupo:'Configurações (Admin)', ic:'☁️', titulo:'A planilha do Google', desc:'Onde as vendas ficam guardadas', passos:[
   ir('admin','Toque na aba ADMIN.'),
   chip('gs','PLANILHA'),
   info('#gsPainel','Aqui mostra se a planilha do Google está ligada.','No treino ela não está. No app de verdade aparece "☁ Planilha em dia".'),
   info('#gsUrl','Este é o link da planilha. Quem liga é o responsável da empresa, uma vez só.'),
   info('#gsRestaurar','Celular novo ou trocado? Este botão traz todas as vendas, clientes e produtos da planilha.'),
   info('#gsToken','Este é o CÓDIGO DE SEGURANÇA da empresa.','Nunca mande para quem não é da empresa.')
 ]},
 {id:'backup', grupo:'Configurações (Admin)', ic:'💾', titulo:'Guardar uma cópia', desc:'Para não perder nada', passos:[
   ir('admin','Toque na aba ADMIN.'),
   chip('bkp','BACKUP'),
   info('#ultBackup','Aqui mostra quando foi salva a última cópia.'),
   {tipo:'toque', alvo:'#btnExportTxt', ok:c=>novo(c,'baixou'), t:'Toque em SALVAR TXT.', small:'O app salva uma cópia das vendas no celular. Mande para você mesmo no WhatsApp para guardar.', s:'Salvar a cópia'},
   info('#btnImport','Se trocar de celular, é aqui que a cópia volta para o app.')
 ]},
 {id:'usuarios', grupo:'Configurações (Admin)', ic:'🔑', titulo:'Usuários e senhas', desc:'Quem pode fazer o quê', passos:[
   ir('admin','Toque na aba ADMIN.'),
   chip('seg','SEGURANÇA'),
   info('#usuNome','Este é o nome de quem usa este celular. Toda venda fica marcada com esse nome.'),
   info('.perm','Esta tabela mostra o que cada pessoa pode fazer.','As coisas importantes pedem usuário e senha do gerente ou do dono. As senhas ficam só na planilha.'),
   info('#btnCfgWa','Para colocar outra pessoa da empresa no app, mande o LINK DE CONFIGURAÇÃO por aqui.')
 ]}
];
const T=Object.fromEntries(TAREFAS.map(t=>[t.id,t]));
const JORNADA=TAREFAS.map(t=>t.id);

/* ---- Progresso e voz ---- */
const PKEY='penduraTreino_progresso';
let prog={feitos:{},voz:true};
try{ prog=Object.assign(prog, JSON.parse(localStorage.getItem(PKEY))||{}); }catch(e){}
const salvarProg=()=>{ try{ localStorage.setItem(PKEY, JSON.stringify(prog)); }catch(e){} };
function falar(txt){
  if(!prog.voz || !('speechSynthesis' in window)) return;
  try{ speechSynthesis.cancel(); const u=new SpeechSynthesisUtterance(String(txt).replace(/<[^>]+>/g,' ').replace(/[^\p{L}\p{N}\s.,!?:;%$-]/gu,' ')); u.lang='pt-BR'; u.rate=0.92;
    const v=speechSynthesis.getVoices().find(v=>/pt[-_]BR/i.test(v.lang)); if(v) u.voice=v; speechSynthesis.speak(u); }catch(e){}
}

/* ---- Interface ---- */
const faixa=document.createElement('div'); faixa.className='tr-faixa'; faixa.textContent='🎓 MODO TREINO — pode errar à vontade: nada aqui vai para a planilha';
document.body.insertBefore(faixa, document.body.firstChild);
const escuro=document.createElement('div'); escuro.className='tr-escuro'; escuro.hidden=true;
const spot=document.createElement('div'); spot.className='tr-spot'; spot.hidden=true;
const mao=document.createElement('div'); mao.className='tr-mao'; mao.textContent='👆'; mao.hidden=true;
const card=document.createElement('div'); card.className='tr-card embaixo'; card.hidden=true; card.setAttribute('role','dialog'); card.setAttribute('aria-live','polite');
const abrirBt=document.createElement('button'); abrirBt.className='tr-abrir'; abrirBt.textContent='🎓 Treino'; abrirBt.hidden=true;
document.body.append(escuro,spot,mao,card,abrirBt);
abrirBt.onclick=()=>menu();

let st=null;   // {tarefa, i, modo:'guiado'|'sozinho', c, feitos:Set, depois, jornada}
function el(passo){ if(!passo||!passo.alvo) return null; const e=typeof passo.alvo==='function'?passo.alvo():q(passo.alvo); return e; }
function permitidos(passo){ if(passo.tipo==='info') return []; return (passo.permite||[passo.alvo]).map(s=>typeof s==='function'?s():q(s)).filter(Boolean); }
function dentroTreino(t){ return t&&t.closest&&t.closest('.tr-card,.tr-menu,.tr-festa,.tr-abrir'); }

// bloqueia toques fora do lugar certo (só no modo guiado)
function bloquear(e){
  if(!st||st.modo!=='guiado'||dentroTreino(e.target)) return;
  const passo=st.tarefa.passos[st.i]; if(!passo) return;
  const ok=permitidos(passo).some(x=>x===e.target||x.contains(e.target));
  if(ok) return;
  e.preventDefault(); e.stopPropagation();
  if(e.type==='click') errou();
}
['pointerdown','mousedown','touchstart','click'].forEach(t=>document.addEventListener(t,bloquear,{capture:true,passive:false}));
function errou(){
  card.classList.remove('treme'); void card.offsetWidth; card.classList.add('treme');
  const m=card.querySelector('.tr-msg'); if(m){ m.className='tr-msg mau'; m.textContent=st.tarefa.passos[st.i].tipo==='info'?'Leia e toque em ENTENDI ➜':'Quase! Toque onde a mãozinha está mostrando 👆'; }
  falar(m?m.textContent:'');
}

function posicionar(){
  if(!st){ escuro.hidden=spot.hidden=mao.hidden=true; return; }
  const passo=st.tarefa.passos[st.i];
  const mostrar = st.modo==='guiado' || st.ajuda;
  const e=passo?el(passo):null;
  if(!mostrar || !e){ spot.hidden=mao.hidden=true; escuro.hidden=!(mostrar&&passo&&!e); return; }
  const r=e.getBoundingClientRect();
  if(r.width===0&&r.height===0){ spot.hidden=mao.hidden=true; return; }
  escuro.hidden=true; spot.hidden=false;
  const p=8; Object.assign(spot.style,{left:(r.left-p)+'px',top:(r.top-p)+'px',width:(r.width+2*p)+'px',height:(r.height+2*p)+'px'});
  mao.hidden=passo.tipo==='info';
  const embaixo=r.bottom+60<innerHeight;
  Object.assign(mao.style,{left:(r.left+Math.min(r.width/2,140)-20)+'px', top:(embaixo?r.bottom+4:r.top-58)+'px'});
  mao.textContent=embaixo?'👆':'👇';
  const centro=(r.top+r.bottom)/2;
  card.classList.toggle('encima', centro>innerHeight*0.5); card.classList.toggle('embaixo', centro<=innerHeight*0.5);
}
function rolarAte(passo){ const e=el(passo); if(!e) return; const r=e.getBoundingClientRect(); if(r.top<60||r.bottom>innerHeight-220) e.scrollIntoView({block:'center'}); }

function desenhar(){
  const tf=st.tarefa, passos=tf.passos, passo=passos[st.i];
  if(st.modo==='guiado'){
    const tot=passos.length, pct=Math.round(st.i/tot*100);
    card.innerHTML=`<div class="tr-topo">${tf.ic} ${esc(tf.titulo)} <span class="tr-barra"><i style="width:${pct}%"></i></span> ${st.i+1}/${tot}</div>
      <div class="tr-txt">${esc(passo.t)}${passo.small?`<small>${esc(passo.small)}</small>`:''}</div>
      <div class="tr-msg"></div>
      <div class="tr-bts">${passo.tipo==='info'?'<button class="pri tr-entendi">Entendi ➜</button>':''}<button class="tr-ouvir">🔊 Ouvir</button><button class="tr-sair">Sair</button></div>`;
    falar(passo.t+'. '+(passo.small||''));
  }else{
    const lista=passos.filter(p=>p.tipo!=='info');
    card.innerHTML=`<div class="tr-topo">⭐ Agora sozinho: ${esc(tf.titulo)}</div>
      <div class="tr-txt" style="font-size:18px">${tf.sozinho?`<b>${esc(tf.sozinho)}</b><br>`:''}Faça sem a mãozinha. Se esquecer, toque em ME AJUDE.</div>
      <ul class="tr-lista">${lista.map(p=>{ const k=passos.indexOf(p), f=st.feitos.has(k), at=!f&&k===st.i;
        return `<li class="${f?'feito':''} ${at?'atual':''}"><span class="ck">${f?'✔':''}</span>${esc(p.s||p.t)}</li>`; }).join('')}</ul>
      <div class="tr-msg"></div>
      <div class="tr-bts"><button class="pri tr-ajuda">🙋 Me ajude</button><button class="tr-sair">Sair</button></div>`;
  }
  const b=s=>card.querySelector(s);
  if(b('.tr-entendi')) b('.tr-entendi').onclick=()=>avancar();
  if(b('.tr-ouvir')) b('.tr-ouvir').onclick=()=>{ const v=prog.voz; prog.voz=true; falar(passo.t+'. '+(passo.small||'')); prog.voz=v; };
  if(b('.tr-ajuda')) b('.tr-ajuda').onclick=()=>{ st.ajuda=true; const p=passos[st.i]; card.querySelector('.tr-msg').className='tr-msg'; card.querySelector('.tr-msg').textContent='👉 '+p.t; falar(p.t); rolarAte(p); };
  b('.tr-sair').onclick=()=>{ parar(); menu(); };
  card.hidden=false; rolarAte(passo); posicionar();
}
function avancar(){
  const tf=st.tarefa;
  if(st.modo==='guiado'){
    st.i++;
    if(st.i>=tf.passos.length) return concluir();
    const m=card.querySelector('.tr-msg'); if(m){ m.className='tr-msg bom'; m.textContent='✔ Muito bem!'; }
    setTimeout(()=>{ if(st) desenhar(); }, 450);
  }
}
// verificação contínua
setInterval(()=>{
  if(!st) return;
  posicionar();
  const tf=st.tarefa, passos=tf.passos;
  if(st.modo==='guiado'){
    const p=passos[st.i]; if(!p||p.tipo==='info'||st.travar) return;
    let ok=false; try{ ok=!!p.ok(st.c); }catch(e){}
    if(ok){ st.travar=true; const m=card.querySelector('.tr-msg'); if(m){ m.className='tr-msg bom'; m.textContent='✔ Muito bem!'; } if(navigator.vibrate) try{navigator.vibrate(60);}catch(e){}
      setTimeout(()=>{ if(!st) return; st.travar=false; st.i++; if(st.i>=passos.length) concluir(); else desenhar(); },650); }
  }else{
    // modo sozinho: marca até o passo mais adiantado que já está feito
    let mais=-1;
    passos.forEach((p,k)=>{ if(p.tipo==='info') return; let ok=false; try{ ok=!!p.ok(st.c); }catch(e){} if(ok&&k>=st.i) mais=Math.max(mais,k); });
    if(mais>=st.i){ for(let k=0;k<=mais;k++) if(passos[k].tipo!=='info') st.feitos.add(k);
      st.i=mais+1; while(st.i<passos.length&&passos[st.i].tipo==='info') st.i++;
      st.ajuda=false;
      if(st.i>=passos.length) return concluir();
      desenhar(); }
  }
},300);
addEventListener('resize',posicionar); addEventListener('scroll',posicionar,true);

function comecar(id, modo, extra){
  const tf=T[id]; if(!tf) return;
  fecharMenu();
  // pré-requisitos
  if(modo==='guiado'){
    for(const k of tf.pre||[]){
      if(!PRE[k].ok()){
        return festa('🧩','Antes, um passo',`Para "${tf.titulo}", você precisa primeiro ${PRE[k].txt}. Vamos fazer isso agora?`,
          [['Sim, vamos lá',()=>comecar(PRE[k].tarefa,'guiado',{depois:id, jornada:extra&&extra.jornada})],['Voltar ao menu',()=>menu()]]);
      }
    }
  }
  if(tf.antes) try{ tf.antes(); }catch(e){}
  st={tarefa:tf, i:0, modo, c:foto(), feitos:new Set(), depois:extra&&extra.depois, jornada:extra&&extra.jornada};
  if(modo==='sozinho'){ while(st.i<tf.passos.length&&tf.passos[st.i].tipo==='info') st.i++; falar('Agora sozinho: '+tf.titulo); }
  abrirBt.hidden=true;
  desenhar();
}
function parar(){ st=null; card.hidden=true; escuro.hidden=spot.hidden=mao.hidden=true; if('speechSynthesis' in window) try{speechSynthesis.cancel();}catch(e){} }
function concluir(){
  const tf=st.tarefa, modo=st.modo, depois=st.depois, jornada=st.jornada;
  prog.feitos[tf.id]= modo==='sozinho' ? 'sozinho' : (prog.feitos[tf.id]==='sozinho'?'sozinho':'guiado'); salvarProg();
  parar();
  const prox = depois ? T[depois] : (jornada ? T[JORNADA[JORNADA.indexOf(tf.id)+1]] : null);
  const continuar = prox ? [['Continuar: '+prox.titulo, ()=>comecar(prox.id,'guiado',{jornada})]] : [];
  if(modo==='guiado'){
    festa('🎉','Muito bem!',`Você aprendeu: ${tf.titulo}. Agora faça sozinho para não esquecer.`,
      [['⭐ Agora faço sozinho', ()=>comecar(tf.id,'sozinho',{depois, jornada})], ...continuar, ['Voltar ao menu',()=>menu()]]);
  }else{
    festa('⭐','Parabéns!',`Você fez sozinho: ${tf.titulo}.`, [...continuar, ['Voltar ao menu',()=>menu()], ['Usar o app livre',()=>{ abrirBt.hidden=false; }]]);
  }
}
function festa(ic,tit,txt,botoes){
  const f=document.createElement('div'); f.className='tr-festa';
  f.innerHTML=`<div class="bx"><div class="grande">${ic}</div><h2></h2><p></p><div class="bts"></div></div>`;
  f.querySelector('h2').textContent=tit; f.querySelector('p').textContent=txt;
  botoes.forEach(([rot,fn],k)=>{ const b=document.createElement('button'); b.className='tr-grande'+(k===0?'':' claro'); b.textContent=rot; b.onclick=()=>{ f.remove(); fn(); }; f.querySelector('.bts').appendChild(b); });
  document.body.appendChild(f); falar(tit+'. '+txt);
}
let menuEl=null;
function fecharMenu(){ if(menuEl){ menuEl.remove(); menuEl=null; } }
function menu(){
  parar(); fecharMenu(); abrirBt.hidden=true;
  const m=document.createElement('div'); m.className='tr-menu'; menuEl=m;
  const total=TAREFAS.length, feitas=TAREFAS.filter(t=>prog.feitos[t.id]).length;
  let html=`<div class="tr-cab"><img alt="Pendura Aí" src="${$('marcaLogo').src}"><h1>Treino guiado</h1>
    <p class="tr-sub">Aqui você aprende usando o app de verdade, sem medo de errar. A mãozinha 👆 mostra onde tocar e o app só segue quando você acertar.</p>
    <p class="tr-sub" style="font-weight:700">${feitas} de ${total} aprendidas</p></div>
    <button class="tr-grande" id="trJornada">▶ ${feitas?'Continuar a jornada':'Começar do começo'}</button>
    <button class="tr-grande azul" id="trVoz">${prog.voz?'🔊 Voz ligada (toque para desligar)':'🔇 Voz desligada (toque para ligar)'}</button>`;
  let grupo='';
  TAREFAS.forEach(t=>{
    if(t.grupo!==grupo){ grupo=t.grupo; html+=`<div class="tr-grupo">${esc(grupo)}</div>`; }
    const f=prog.feitos[t.id];
    html+=`<button class="tr-tarefa ${f?'feita':''}" data-t="${t.id}"><span class="ic">${t.ic}</span><span class="tx">${esc(t.titulo)}<small>${esc(t.desc)}</small></span><span class="st">${f==='sozinho'?'⭐':f?'✔':'›'}</span></button>`;
  });
  html+=`<button class="tr-grande claro" id="trLivre" style="margin-top:18px">Explorar o app livremente</button>
    <button class="tr-grande claro" id="trZerar">🗑 Começar o treino do zero</button>`;
  m.innerHTML=html; document.body.appendChild(m);
  m.querySelectorAll('[data-t]').forEach(b=>b.onclick=()=>comecar(b.dataset.t,'guiado'));
  m.querySelector('#trJornada').onclick=()=>{ const prox=TAREFAS.find(t=>!prog.feitos[t.id])||TAREFAS[0]; comecar(prox.id,'guiado',{jornada:true}); };
  m.querySelector('#trVoz').onclick=()=>{ prog.voz=!prog.voz; salvarProg(); menu(); if(prog.voz) falar('Voz ligada'); };
  m.querySelector('#trLivre').onclick=()=>{ fecharMenu(); abrirBt.hidden=false; };
  m.querySelector('#trZerar').onclick=()=>{ if(!confirm('Apagar tudo o que foi feito no treino e começar de novo?')) return;
    try{ localStorage.removeItem(KEY); localStorage.removeItem(PKEY); localStorage.removeItem(KEY+'_planilha'); }catch(e){} location.reload(); };
  falar('Treino guiado. Toque em começar do começo.');
}
window.TREINO={TAREFAS, PRE, comecar, menu, estado:()=>st};
setTimeout(menu, 50);
})();
</script>
