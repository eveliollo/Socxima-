# backend-socxima.py
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import uvicorn

# Inicialización del núcleo del sistema
app = FastAPI(
    title="SOCXIMA IA",
    version="9.9.9-GOLDEN"
)

# Conexión abierta para que tu interfaz de GitHub Pages pueda leer los datos sin bloqueos
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Permite conexiones desde cualquier origen (como tu GitHub Pages)
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Ruta del estado del sistema que buscará tu interfaz
@app.get("/estado")
async def obtener_estado():
    return {
        "estado": "MOTOR ACTIVO 🟢",
        "core": {
            "system_id": "SOCXIMA-PRIME-ENGINE",
            "owner_and_creator": "EVELIO LLOVERA",
            "version": "9.9.9-GOLDEN",
            "ecosystem_type": "SUPREME_FINANCIAL_EMPIRE"
        },
        "agent_cluster": {
            "total_count": 44,
            "orchestrator": "Evelio-Llovera-AI-Orchestrator",
            "mode": "autonomous_hyperdrive"
        }
    }

if __name__ == "__main__":
    # Corre el servidor en el puerto 5000
    uvicorn.run(app, host="0.0.0.0", port=5000)
