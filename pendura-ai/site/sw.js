// Pendura Aí — modo offline.
// Guarda o app no celular. Com internet, sempre busca a versão nova; sem internet, abre a cópia guardada.
const CACHE = 'pendura-ai-v1';
const ARQUIVOS = ['./', './index.html', './manifest.webmanifest', './icone-192.png', './icone-512.png', './icone-maskable-512.png'];

self.addEventListener('install', e => {
  e.waitUntil(caches.open(CACHE).then(c => c.addAll(ARQUIVOS)).then(() => self.skipWaiting()));
});
self.addEventListener('activate', e => {
  e.waitUntil(caches.keys().then(ks => Promise.all(ks.filter(k => k !== CACHE).map(k => caches.delete(k)))).then(() => self.clients.claim()));
});
self.addEventListener('fetch', e => {
  const req = e.request;
  const url = new URL(req.url);
  if (req.method !== 'GET' || url.origin !== self.location.origin) return;   // planilha do Google e outros sites: não mexe
  const ehPagina = req.mode === 'navigate';
  e.respondWith((async () => {
    const cache = await caches.open(CACHE);
    try {
      // rede primeiro (com limite de 4 s), para sempre pegar a versão nova
      const resp = await Promise.race([fetch(req), new Promise((_, rej) => setTimeout(() => rej(new Error('lento')), 4000))]);
      if (resp && resp.ok) cache.put(ehPagina ? './' : req, resp.clone());
      return resp;
    } catch (err) {
      const salvo = await cache.match(ehPagina ? './' : req, { ignoreSearch: true }) || (ehPagina && await cache.match('./index.html'));
      if (salvo) return salvo;
      return new Response('Sem internet e o app ainda não foi guardado neste celular. Abra uma vez com internet.', { status: 503, headers: { 'Content-Type': 'text/plain; charset=utf-8' } });
    }
  })());
});
