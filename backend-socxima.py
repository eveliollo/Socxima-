import os
import requests
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from web3 import Web3
from solana.rpc.api import Client as SolanaClient
from solders.pubkey import Pubkey

app = FastAPI(title="SOCXIMA Multichain Core & AI Engine")

# Habilitar CORS para evitar bloqueos del navegador o del celular
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Configuración de Nodos de Blockchain Reales
w3_eth = Web3(Web3.HTTPProvider("https://cloudflare-eth.com"))
sol_client = SolanaClient("https://api.mainnet-beta.solana.com")

# MODELOS DE DATOS (Pydantic)
class BalanceRequest(BaseModel):
    eth_address: str = None
    sol_address: str = None

class ChatRequest(BaseModel):
    eth_address: str = None
    sol_address: str = None
    mensaje_usuario: str

# FUNCIÓN AUXILIAR PARA CONSULTAR BLOQUES EN TIEMPO REAL
def obtener_balances_internos(eth_addr, sol_addr):
    balances = {"ETH": 0.0, "SOL": 0.0}
    if eth_addr and eth_addr.startswith("0x"):
        try:
            addr = w3_eth.to_checksum_address(eth_addr)
            balances["ETH"] = float(w3_eth.from_wei(w3_eth.eth.get_balance(addr), 'ether'))
        except: pass
    if sol_addr and len(sol_addr) > 30:
        try:
            pubkey = Pubkey.from_string(sol_addr)
            balances["SOL"] = sol_client.get_balance(pubkey).value / 1000000000
        except: pass
    return balances

# 1. ENDPOINT PARA COMPROBAR BALANCES Y GRÁFICOS
@app.post("/api/balances")
async def api_balances(req: BalanceRequest):
    data = obtener_balances_internos(req.eth_address, req.sol_address)
    return {"status": "SUCCESS", "networks": {"ethereum": {"balance_real": data["ETH"]}, "solana": {"balance_real": data["SOL"]}}}

# 2. ENDPOINT PARA EL CHAT CON INTELIGENCIA ARTIFICIAL REAL
@app.post("/api/chat")
async def api_chat(req: ChatRequest):
    # Obtener datos reales de los balances para dárselos a la IA
    saldos = obtener_balances_internos(req.eth_address, req.sol_address)
    
    # Prompt del sistema para moldear la personalidad de tu IA (SOCXIMA-BOT)
    system_prompt = (
        f"Eres SOCXIMA IA, un asistente experto en finanzas descentralizadas y análisis multichain. "
        f"Actualmente, el usuario tiene los siguientes saldos reales en sus billeteras conectadas: "
        f"Ethereum: {saldos['ETH']} ETH, Solana: {saldos['SOL']} SOL. "
        f"Responde de forma clara, técnica pero entendible, y basada estrictamente en estos datos de la blockchain si te pregunta por sus fondos."
    )

    # REQUISITO: Tener Ollama corriendo localmente con el comando: ollama run llama3
    # Si usas otra API (como OpenAI o Gemini), aquí cambiarías la URL y los Headers.
    ollama_url = "http://localhost:11434/api/generate"
    
    payload_ia = {
        "model": "llama3",
        "prompt": f"{system_prompt}\n\nUsuario dice: {req.mensaje_usuario}\nSOCXIMA IA:",
        "stream": False
    }

    try:
        respuesta_ollama = requests.post(ollama_url, json=payload_ia, timeout=15)
        if respuesta_ollama.status_code == 200:
            texto_ia = respuesta_ollama.json().get("response", "")
            return {"status": "SUCCESS", "respuesta": texto_ia}
        else:
            return {"status": "ERROR", "respuesta": "La IA local devolvió un código de error."}
    except Exception as e:
        # Fallback por si Ollama está apagado durante tus pruebas
        return {
            "status": "OFFLINE_MODE", 
            "respuesta": f"Hola! Veo tus billeteras (ETH: {saldos['ETH']}, SOL: {saldos['SOL']}), pero mi motor cognitivo local Llama3 está desconectado en el puerto 11434. Inicia Ollama para darte un análisis completo."
        }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
