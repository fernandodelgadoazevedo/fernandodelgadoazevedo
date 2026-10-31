# App Android — Impressora Térmica POS-5820 (Bluetooth)

App instalável para Android que funciona como "driver" da impressora
**Thermal Label Printer POS-5820** (papel 58 mm, 5V/2A, Bluetooth, com sensor de etiqueta).

## Baixar e instalar

1. No celular, abra a página **Releases** deste repositório e baixe `POS5820-Impressora.apk`
   (tag `pos5820-latest`). Também fica disponível em *Actions > APK Impressora POS-5820 > Artifacts*.
2. Toque no arquivo e permita "Instalar apps desconhecidos" quando o Android pedir.
3. Android 5.0 ou superior.

## Primeiro uso

1. Ligue a impressora e pareie em **Configurações > Bluetooth** (PIN `0000` ou `1234`).
2. Abra o app, permita o acesso a "Dispositivos próximos" e toque em **Escolher / conectar**.
3. Toque em **Página de teste**.

## Recursos

- **Dois modos de impressão**
  - *Recibo / cupom (ESC/POS)* — bobina contínua de 58 mm. Opção para avançar até a próxima etiqueta (comando `GS FF`) quando usar rolo de etiquetas.
  - *Etiqueta com sensor (TSPL)* — tamanho da etiqueta (mm), gap ou marca preta, densidade e **calibração do sensor** (`GAPDETECT` / `BLINEDETECT`).
- Texto com acentos, tamanho de fonte, alinhamento e negrito (impresso como imagem, então sem problemas de código de página).
- QR code (links, PIX etc.) e código de barras CODE128.
- Imagens/logos com dithering.
- Pré-visualização antes de imprimir e número de cópias.
- **Compartilhar** texto ou imagem de outro app direto para a impressora.
- **Driver do sistema (Print Service)**: em *Configurações > Impressão*, ative "Térmica POS-5820 (Bluetooth)". A impressora passa a aparecer no menu **Imprimir** de qualquer app (PDF, navegador, galeria, Docs...).

## Qual modo usar?

A POS-5820 normalmente fala **ESC/POS**. Alguns lotes vendidos como "Label Printer" também aceitam **TSPL**.
Faça a página de teste nos dois modos: o que imprimir corretamente é o da sua impressora.
Se no modo Etiqueta sair tudo em negativo, marque "Inverter cores".

## Compilar

```bash
cd pos5820-app
./gradlew assembleDebug   # requer JDK 17 e Android SDK
```

O GitHub Actions (`.github/workflows/pos5820-apk.yml`) compila e publica o APK automaticamente a cada alteração.
