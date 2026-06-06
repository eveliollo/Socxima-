package com.example.util

data class LiveTx(
    val hash: String,
    val valueToken: Double,
    val valueUsd: Double
)

data class LiveChainInfo(
    val id: String,
    val name: String,
    val colorHex: String,
    val symbol: String,
    val blockNumber: String,
    val txCount: Int,
    val lastValue: String,
    val gasFee: String,
    val transactions: List<LiveTx>
)

data class TickerItem(
    val symbol: String,
    val price: Double,
    val percentChange: Double,
    val isUp: Boolean,
    val colorHex: String
)

data class LiveIpfsLog(
    val time: String,
    val cid: String,
    val message: String,
    val colorHex: String
)

data class LiveWalletInfo(
    val isConnected: Boolean,
    val address: String = "",
    val balanceEth: Double = 0.0,
    val activeNetwork: String = "",
    val estimatedUsd: Double = 0.0
)
