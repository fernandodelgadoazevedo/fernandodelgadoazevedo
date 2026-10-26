#!/usr/bin/env python3
"""Monta o Pendura Aí a partir de uma fonte só.

    python3 build.py

Gera, em site/ (a pasta que vai para o Cloudflare Pages ou outro site):
  index.html  o app de verdade (instala no celular, funciona sem internet)
  treino.html o treino guiado (dados separados, nada sai do celular)
e copia manifest, service worker, ícones e _headers.

Edite sempre src/: app.html (o app), treino.css e treino.js (só o treino).
"""
import pathlib
import shutil

AQUI = pathlib.Path(__file__).resolve().parent
SRC, SITE = AQUI / 'src', AQUI / 'site'

VARIANTES = {
    'index.html': {
        '@TITULO': 'Pendura Aí',
        '@KEY': 'controleVendas_v1',
        '@INSTALA': "'serviceWorker' in navigator && (location.protocol==='https:'||location.hostname==='localhost')",
        '<!--@HEAD_PWA-->': '<link rel="manifest" href="manifest.webmanifest">',
        '<!--@TREINO_CSS-->\n': '',
        '<!--@TREINO_JS-->\n': '',
    },
    'treino.html': {
        '@TITULO': 'Treino · Pendura Aí',
        '@KEY': 'penduraTreino_v1',
        '@INSTALA': 'false',
        '<!--@HEAD_PWA-->': '',
        '<!--@TREINO_CSS-->\n': (SRC / 'treino.css').read_text(encoding='utf-8'),
        '<!--@TREINO_JS-->\n': (SRC / 'treino.js').read_text(encoding='utf-8'),
    },
}


def montar(nome, trocas):
    html = (SRC / 'app.html').read_text(encoding='utf-8')
    for marca, valor in trocas.items():
        if html.count(marca) != 1:
            raise SystemExit(f'{nome}: a marca {marca!r} aparece {html.count(marca)} vez(es) em src/app.html (precisa ser 1)')
        html = html.replace(marca, valor)
    if '@' + 'TREINO' in html or '@' + 'KEY' in html:
        raise SystemExit(f'{nome}: sobrou marca sem trocar')
    return html


def main():
    SITE.mkdir(exist_ok=True)
    for nome, trocas in VARIANTES.items():
        (SITE / nome).write_text(montar(nome, trocas), encoding='utf-8')
    for arq in (SRC / 'pwa').iterdir():
        shutil.copy2(arq, SITE / arq.name)
    print('Pronto:', ', '.join(sorted(p.name for p in SITE.iterdir())))


if __name__ == '__main__':
    main()
