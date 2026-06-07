import sys
import json
from web3 import Web3
from solana.rpc.api import Client as SolanaClient
from solders.pubkey import Pubkey

class SOCXIMACoreEngine:
    def __init__(self):
        # Nodos RPC reales públicos de producción (Mainnet)
        self.nodo_ethereum = "https://cloudflare-eth.com"
        self.nodo_solana = "https://api.mainnet-beta.solana.com"
        
        # Inicialización de clientes reales
        self.w3_eth = Web3(Web3.HTTPProvider(self.nodo_ethereum))
        self.client_sol = SolanaClient(self.nodo_solana)

    def obtener_datos_produccion(self, eth_address=None, sol_address=None):
        payload = {
            "status": "OPERATIONAL",
            "networks": {},
            "errors": []
        }

        # --- PROCESAMIENTO REAL ETHEREUM / METAMASK ---
        if eth_address and eth_address != "0x0":
            try:
                if self.w3_eth.is_connected():
                    # Convierte a ChecksumAddress (estándar de seguridad real de Ethereum)
                    checksum_addr = self.w3_eth.to_checksum_address(eth_address)
                    balance_wei = self.w3_eth.eth.get_balance(checksum_addr)
                    balance_eth = float(self.w3_eth.from_wei(balance_wei, 'ether'))
                    
                    payload["networks"]["ethereum"] = {
                        "address": checksum_addr,
                        "balance_real": balance_eth,
                        "unit": "ETH"
                    }
            except Exception as e:
                payload["errors"].append(f"Error ETH: {str(e)}")

        # --- PROCESAMIENTO REAL SOLANA ---
        if sol_address and sol_address != "0x0":
            try:
                # Conversión de la cadena de texto Base58 a una Clave Pública Estructural de Solana
                pubkey_real = Pubkey.from_string(sol_address)
                response = self.client_sol.get_balance(pubkey_real)
                
                # Solana devuelve los datos en Lamports (1 SOL = 10^9 Lamports)
                balance_sol = response.value / 1000000000
                
                payload["networks"]["solana"] = {
                    "address": sol_address,
                    "balance_real": balance_sol,
                    "unit": "SOL"
                }
            except Exception as e:
                payload["errors"].append(f"Error SOL: {str(e)}")

        return payload

if __name__ == "__main__":
    # Recibe parámetros desde la UI o consola: python backend-socxima.py [wallet_eth] [wallet_sol]
    address_input_eth = sys.argv[1] if len(sys.argv) > 1 else None
    address_input_sol = sys.argv[2] if len(sys.argv) > 2 else None

    engine = SOCXIMACoreEngine()
    json_final = engine.obtener_datos_produccion(address_input_eth, address_input_sol)
    
    # Salida limpia en JSON directo a tu frontend
    sys.stdout.write(json.dumps(json_final, ensure_ascii=False) + "\n")
