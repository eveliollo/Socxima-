import sys
import json
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from web3 import Web3
from solana.rpc.api import Client as SolanaClient
from solders.pubkey import Pubkey

app = FastAPI(title="SOCXIMA Engine API")

# Permite que tu Web interactúe con el Python sin bloqueos de seguridad
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

class BalanceRequest(BaseModel):
    eth_address: str = None
    sol_address: str = None

class SOCXIMACoreEngine:
    def __init__(self):
        self.nodo_ethereum = "https://cloudflare-eth.com"
        self.nodo_solana = "https://api.mainnet-beta.solana.com"
        self.w3_eth = Web3(Web3.HTTPProvider(self.nodo_ethereum))
        self.client_sol = SolanaClient(self.nodo_solana)

    def obtener_datos_produccion(self, eth_address=None, sol_address=None):
        payload = {"status": "OPERATIONAL", "networks": {}, "errors": []}

        # --- ETHEREUM REAL ---
        if eth_address and eth_address.startswith("0x"):
            try:
                if self.w3_eth.is_connected():
                    checksum_addr = self.w3_eth.to_checksum_address(eth_address)
                    balance_wei = self.w3_eth.eth.get_balance(checksum_addr)
                    payload["networks"]["ethereum"] = {
                        "balance_real": float(self.w3_eth.from_wei(balance_wei, 'ether')),
                        "unit": "ETH"
                    }
            except Exception as e:
                payload["errors"].append(f"Error ETH: {str(e)}")

        # --- SOLANA REAL ---
        if sol_address and len(sol_address) > 30:
            try:
                pubkey_real = Pubkey.from_string(sol_address)
                response = self.client_sol.get_balance(pubkey_real)
                payload["networks"]["solana"] = {
                    "balance_real": response.value / 1000000000,
                    "unit": "SOL"
                }
            except Exception as e:
                payload["errors"].append(f"Error SOL: {str(e)}")

        return payload

engine = SOCXIMACoreEngine()

@app.post("/api/balances")
async def api_obtener_balances(req: BalanceRequest):
    return engine.obtener_datos_produccion(req.eth_address, req.sol_address)

if __name__ == "__main__":
    import uvicorn
    # Corre el servidor real en el puerto 8000
    uvicorn.run(app, host="0.0.0.0", port=8000)
