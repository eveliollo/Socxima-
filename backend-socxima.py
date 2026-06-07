// 1. CONEXIÓN REAL A METAMASK
async function conectarMetaMask() {
    if (window.ethereum) {
        try {
            // Solicita acceso real a las cuentas del usuario
            const cuentas = await window.ethereum.request({ method: 'eth_requestAccounts' });
            const walletEVM = cuentas[0];
            document.getElementById('address-evm').innerText = walletEVM;
            console.log("MetaMask conectada con éxito: ", walletEVM);
            
            // Aquí puedes enviar esta dirección a tu backend-socxima.py para procesar datos reales
        } catch (error) {
            console.error("Usuario rechazó la conexión a MetaMask", error);
        }
    } else {
        alert("MetaMask no detectado. Instala la extensión para operar en producción.");
    }
}

// 2. CONEXIÓN REAL A PHANTOM (SOLANA)
async function conectarPhantom() {
    // Las billeteras de Solana se inyectan comúnmente en window.solana
    const isPhantomInstalled = window.solana && window.solana.isPhantom;
    
    if (isPhantomInstalled) {
        try {
            // Conexión real al proveedor de Solana
            const resp = await window.solana.connect();
            const walletSolana = resp.publicKey.toString();
            document.getElementById('address-solana').innerText = walletSolana;
            console.log("Phantom conectada con éxito. Clave Pública: ", walletSolana);
        } catch (error) {
            console.error("Usuario rechazó la conexión a Phantom", error);
        }
    } else {
        alert("Phantom Wallet no detectada. Instala la extensión para operar en Solana.");
    }
}
