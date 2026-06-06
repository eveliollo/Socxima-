package com.example.util

data class Blockchain(
    val id: String,
    val name: String,
    val token: String,
    val blockTime: String,
    val averageFeeUsd: Double,
    val status: String,
    val protocol: String,
    val colorHex: String,
    val hashPrefix: String
) {
    fun generateTransactionHash(): String {
        return when (id) {
            "solana" -> {
                // Base58 Transaction Signature representation
                val chars = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
                (1..88).map { chars.random() }.joinToString("")
            }
            "bitcoin" -> {
                val chars = "0123456789abcdef"
                (1..64).map { chars.random() }.joinToString("")
            }
            "ethereum", "polygon", "avalanche", "bsc" -> {
                val chars = "0123456789abcdef"
                "0x" + (1..64).map { chars.random() }.joinToString("")
            }
            "cosmos" -> {
                val chars = "0123456789ABCDEF"
                (1..64).map { chars.random() }.joinToString("")
            }
            "polkadot" -> {
                val chars = "0123456789abcdef"
                "0x" + (1..64).map { chars.random() }.joinToString("")
            }
            "filecoin" -> {
                val chars = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
                "bafy" + (1..55).map { chars.random() }.joinToString("")
            }
            else -> "0x" + System.currentTimeMillis().toString(16)
        }
    }
}

object BlockchainSpecs {
    val chains = listOf(
        Blockchain(
            id = "bitcoin",
            name = "Bitcoin Mainnet",
            token = "BTC",
            blockTime = "10 min",
            averageFeeUsd = 1.25,
            status = "Local Daemon Active",
            protocol = "OP_RETURN Sella",
            colorHex = "#F7931A",
            hashPrefix = "txid: "
        ),
        Blockchain(
            id = "ethereum",
            name = "Ethereum Geth",
            token = "ETH",
            blockTime = "12 sec",
            averageFeeUsd = 0.85,
            status = "Optimized RPC Synced",
            protocol = "EIP-1559 Calldata",
            colorHex = "#627EEA",
            hashPrefix = "hash: "
        ),
        Blockchain(
            id = "solana",
            name = "Solana Validator",
            token = "SOL",
            blockTime = "400 ms",
            averageFeeUsd = 0.00025,
            status = "Local Genesis Peer",
            protocol = "Memo Program v2",
            colorHex = "#14F195",
            hashPrefix = "sig: "
        ),
        Blockchain(
            id = "polygon",
            name = "Polygon Heuristics",
            token = "POL",
            blockTime = "2.1 sec",
            averageFeeUsd = 0.005,
            status = "Active JSON-RPC API",
            protocol = "State Anchor Sella",
            colorHex = "#8247E5",
            hashPrefix = "tx: "
        ),
        Blockchain(
            id = "avalanche",
            name = "Avalanche C-Chain",
            token = "AVAX",
            blockTime = "1.5 sec",
            averageFeeUsd = 0.04,
            status = "AvalancheGo Core Subnet",
            protocol = "C-Chain Payload Log",
            colorHex = "#E84142",
            hashPrefix = "txn: "
        ),
        Blockchain(
            id = "bsc",
            name = "BNB Smart Chain",
            token = "BNB",
            blockTime = "3 sec",
            averageFeeUsd = 0.03,
            status = "BSC-Node Client Mode",
            protocol = "Contract ABI Call",
            colorHex = "#F3BA2F",
            hashPrefix = "hash: "
        ),
        Blockchain(
            id = "cosmos",
            name = "Cosmos Hub",
            token = "ATOM",
            blockTime = "5 sec",
            averageFeeUsd = 0.015,
            status = "Tendermint Engine Active",
            protocol = "IBC Tx Attribute Log",
            colorHex = "#2E3192",
            hashPrefix = "hash: "
        ),
        Blockchain(
            id = "polkadot",
            name = "Polkadot Relay",
            token = "DOT",
            blockTime = "6 sec",
            averageFeeUsd = 0.07,
            status = "Substrate Node Running",
            protocol = "System Remark Pallet",
            colorHex = "#E6007A",
            hashPrefix = "extrinsic: "
        ),
        Blockchain(
            id = "filecoin",
            name = "Filecoin Lotus",
            token = "FIL",
            blockTime = "30 sec",
            averageFeeUsd = 0.008,
            status = "Lotus Backed Client",
            protocol = "CID Sector Settlement",
            colorHex = "#0090FF",
            hashPrefix = "deal: "
        )
    )
}
