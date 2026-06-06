# SOCXIMA IA

<div align="center">

```
███████╗ ██████╗  ██████╗██╗  ██╗██╗███╗   ███╗ █████╗     ██╗ █████╗
██╔════╝██╔═══██╗██╔════╝╚██╗██╔╝██║████╗ ████║██╔══██╗    ██║██╔══██╗
███████╗██║   ██║██║      ╚███╔╝ ██║██╔████╔██║███████║    ██║███████║
╚════██║██║   ██║██║      ██╔██╗ ██║██║╚██╔╝██║██╔══██║    ██║██╔══██║
███████║╚██████╔╝╚██████╗██╔╝ ██╗██║██║ ╚═╝ ██║██║  ██║    ██║██║  ██║
╚══════╝ ╚═════╝  ╚═════╝╚═╝  ╚═╝╚═╝╚═╝     ╚═╝╚═╝  ╚═╝    ╚═╝╚═╝  ╚═╝
```

**Red de 100 Agentes IA + Multichain Blockchain en Tiempo Real**

![Version](https://img.shields.io/badge/version-2.0.0--beta-cyan?style=flat-square)
![License](https://img.shields.io/badge/license-MIT-green?style=flat-square)
![Node](https://img.shields.io/badge/node-%3E%3D18.0-brightgreen?style=flat-square)
![Chains](https://img.shields.io/badge/chains-8-purple?style=flat-square)
![Agents](https://img.shields.io/badge/agents%20IA-100-blue?style=flat-square)

[🌐 Demo en Vivo](https://eveliollo.github.io) · [📦 Repositorio](https://github.com/eveliollo) · [🐛 Reportar Bug](https://github.com/eveliollo/issues)

</div>

---

## ¿Qué es SOCXIMA IA?

**SOCXIMA IA** es una plataforma open source que conecta **100 agentes de inteligencia artificial** con **8 redes blockchain simultáneas**, mostrando actividad en tiempo real. Combina IA local, datos reales de blockchain y un dashboard visual interactivo.

```
[ 100 Agentes IA ] ──→ [ SOCXIMA IA Core ] ──→ [ Dashboard Visual ]
                              │
                    ┌─────────┼─────────┐
                   ETH       SOL       BNB
                   POL       ADA       ARB
                   AVAX     BASE      ...
```

---

## ✨ Características

| Función | Descripción | Estado |
|---|---|---|
| 🤖 100 Agentes IA | LLaMA, Mistral, Gemma, Falcon y más | ✅ Activo |
| ⛓️ 8 Blockchains | ETH, SOL, BNB, POL, ADA, ARB, AVAX, BASE | ✅ Activo |
| 💰 Precios Reales | CoinGecko API pública, actualiza cada 60s | ✅ Activo |
| 👛 Balance Wallets | Consulta directa a nodos RPC públicos | ✅ Activo |
| 📊 TX en Vivo | Feed de transacciones en tiempo real | ✅ Activo |
| 🔗 WebSocket | Backend Node.js ↔ Dashboard HTML | ✅ Activo |
| 🧠 IA Local | Compatible con Ollama / llama.cpp | ✅ Activo |
| 📱 Android | App nativa disponible | 🔄 Beta |

---

## 🚀 Instalación Rápida

### Requisitos
- Node.js >= 18
- npm >= 9
- (Opcional) [Ollama](https://ollama.ai) para IA local

### Pasos

```bash
# 1. Clonar el repo
git clone https://github.com/eveliollo/SOCXIMA-IA.git
cd SOCXIMA-IA

# 2. Instalar dependencias
npm install

# 3. Configurar variables (opcional)
cp .env.example .env
# Edita .env con tu ETHERSCAN_KEY y wallets

# 4. Arrancar SOCXIMA IA
node socxima-core.js
```

Abre tu navegador en **http://localhost:3000** 🎉

---

## 🧠 IA Local con Ollama

Para activar los agentes IA locales:

```bash
# Instalar Ollama
curl -fsSL https://ollama.ai/install.sh | sh

# Descargar modelo (elige uno)
ollama pull mistral        # 4GB — recomendado
ollama pull llama3         # 4.7GB
ollama pull phi3           # 2.3GB — ligero

# Ollama arranca automáticamente en localhost:11434
```

SOCXIMA IA lo detecta automáticamente al iniciar.

---

## 📱 Android (Termux)

```bash
# Instalar Termux desde F-Droid
pkg update && pkg install nodejs git

git clone https://github.com/eveliollo/SOCXIMA-IA.git
cd SOCXIMA-IA && npm install
node socxima-core.js
```

Abre `http://localhost:3000` en tu navegador móvil.

---

## 🌐 Blockchains Soportadas

| Red | Símbolo | RPC Público | TPS |
|---|---|---|---|
| Ethereum | ETH | cloudflare-eth.com | ~15 |
| Solana | SOL | api.mainnet-beta.solana.com | ~65,000 |
| BNB Chain | BNB | bsc-dataseed.binance.org | ~300 |
| Polygon | POL | polygon-rpc.com | ~7,000 |
| Avalanche | AVAX | api.avax.network | ~4,500 |
| Arbitrum | ARB | arb1.arbitrum.io/rpc | ~40,000 |
| Optimism | OP | mainnet.optimism.io | ~2,000 |
| Base | BASE | mainnet.base.org | ~2,000 |

---

## 🤖 Agentes IA Incluidos

<details>
<summary>Ver los 100 agentes (click para expandir)</summary>

LLaMA-3-70B, Mistral-7B, Mixtral-8x7B, Phi-3-Mini, Gemma-7B, Falcon-180B, DBRX, Command-R+, DeepSeek-V2, Qwen-72B, Yi-34B, StarCoder2, CodeLlama, WizardLM2, Vicuna-13B, Orca2, OpenHermes, Zephyr-7B, Starling-7B, Openchat-3.5, TinyLlama, NeuralChat, Nous-Hermes, SolarPro, Platypus2, Airoboros, Synthia, Jackalope, Pygmalion, Samantha, MegaTron-LM, Dolly-v2, RedPajama, Bloom-176B, FLAN-T5-XXL, GPT-NeoX-20B, OPT-66B, Cerebras-GPT, MPT-30B, Mosaic-7B, Replit-Code, WizardCoder, DeepCoder, TabNine-OSS, Codeium-OSS, SuperCOT, Alpaca-LoRA, Baize, Guanaco, Koala, ChatGLM3, InternLM, Baichuan2, AquilaChat, TigerBot, LLaVA, MiniGPT4, InstructBLIP, Otter, CogVLM, Whisper-Large, SeamlessM4T, NLLB-200, mBART, mT5, Stable-Diffusion-XL, Kandinsky3, DeepFloyd-IF, Würstchen, AltDiffusion, AudioCraft, MusicGen, Bark, VALL-E, VoiceBox, SAM, GroundingDINO, YOLOv9, RT-DETR, EfficientSAM, ControlNet, IP-Adapter, DreamBooth, LoRA-Trainer, Textual-Inv, LangChain, LlamaIndex, Haystack, DSPy, Semantic-Kernel, AutoGPT, BabyAGI, CrewAI, MetaGPT, OpenDevin, AgentGPT, SuperAGI, Flowise, Dify, n8n-AI, Ollama

</details>

---

## 📁 Estructura del Proyecto

```
SOCXIMA-IA/
├── socxima-core.js        # Backend Node.js principal
├── socxima-dashboard.html # Dashboard visual (o index.html)
├── package.json           # Dependencias npm
├── .env.example           # Variables de entorno
├── app/                   # App Android nativa
├── assets/.aistudio/      # Assets de IA
└── README.md              # Este archivo
```

---

## ⚙️ Variables de Entorno

```env
# .env
ETHERSCAN_KEY=     # API key de etherscan.io (gratis)
MY_ETH_WALLET=     # Tu wallet ETH para monitoreo
MY_SOL_WALLET=     # Tu wallet Solana
MY_BNB_WALLET=     # Tu wallet BNB
HTTP_PORT=3000     # Puerto del dashboard
WS_PORT=8765       # Puerto WebSocket
```

---

## 🛠️ Tecnologías

- **Backend:** Node.js, Web3.js, @solana/web3.js, Axios, WS
- **Frontend:** HTML5 Canvas, WebSocket API, CoinGecko API
- **IA Local:** Ollama, llama.cpp, TensorFlow (Python)
- **Blockchain:** JSON-RPC, Etherscan API, Solana RPC
- **Android:** Gradle, Kotlin

---

## 🤝 Contribuir

1. Fork el repositorio
2. Crea tu rama: `git checkout -b feature/nueva-funcion`
3. Commit: `git commit -m 'Agrega nueva función'`
4. Push: `git push origin feature/nueva-funcion`
5. Abre un Pull Request

---

## 📜 Licencia

MIT © [eveliollo](https://github.com/eveliollo) — Libre para usar, modificar y distribuir.

---

<div align="center">

**SOCXIMA IA** — Construido con 💚 y código abierto

*"La inteligencia artificial al servicio de la descentralización"*

</div>
