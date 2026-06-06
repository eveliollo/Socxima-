package com.example.util

data class AIModel(
    val id: String,
    val name: String,
    val series: String,
    val category: String,
    val sizeGb: Double,
    val quantization: String,
    val contextWindow: Int,
    val tokensPerSecond: Int,
    val license: String,
    val description: String
)

object ModelSpecs {
    val modelList: List<AIModel> by lazy {
        val baseModels = listOf(
            BaseModel("DeepSeek-R1-Distill", "DeepSeek", "Reasoning / CoT", 80, "MIT", "Reasoning model leveraging reinforcement learning, specialized in deep math & logic."),
            BaseModel("Llama-3.1-Instruct", "Meta", "General Chat", 40, "Llama 3.1 Community", "State-of-the-art open large language model optimized for dialogue use cases."),
            BaseModel("Gemma-2-Instruct", "Google", "General Chat", 42, "Gemma", "High efficiency model utilizing sliding window attention with stellar performance."),
            BaseModel("Mistral-Nemo-Instruct", "Mistral", "General Chat", 38, "Apache 2.0", "Co-developed with NVIDIA. Ideal for multi-turn roleplay and instruction following."),
            BaseModel("Phi-3.5-mini-instruct", "Microsoft", "Lightweight", 52, "MIT", "Extremely lightweight model trained on highly curated educational textbooks and sites."),
            BaseModel("Qwen-2.5-Coder-Instruct", "Alibaba", "Coding / Dev", 45, "Apache 2.0", "Exceptional developer assistant optimized for code-generation and debugging."),
            BaseModel("Qwen-2.5-Math-Instruct", "Alibaba", "Reasoning / CoT", 41, "Apache 2.0", "The ultimate open math model, solving complex math olympiad problems."),
            BaseModel("TinyLlama-1.1B-Chat", "TinyLlama", "Lightweight", 62, "MIT", "An active pretraining project looking to pack maximum power in only 1.1 billion parameters."),
            BaseModel("Phi-3-Medium-Instruct", "Microsoft", "General Chat", 35, "MIT", "Excellent medium-scale reasoning model focusing on syntactic and programming tasks."),
            BaseModel("Yi-1.5-Chat", "01.AI", "General Chat", 30, "Apache 2.0", "Dual-language model showcasing extreme sequence understanding and creative text composition."),
            BaseModel("OpenHermes-2.5-Mistral", "Hermes", "Fine-tuned", 44, "Apache 2.0", "Custom fine-tune of Mistral-7B focusing on high assistant empathy and creative reasoning."),
            BaseModel("Command-R-Instruct", "Cohere", "RAG / Chat", 28, "C-UDA CC-BY-NC", "Enterprise-grade agent specialized in multi-step Retrieval-Augmented Generation (RAG)."),
            BaseModel("Falcon-7B-Instruct", "TII", "General Chat", 22, "Apache 2.0", "Pioneering decoder-only model utilizing multi-query attention for faster server outputs."),
            BaseModel("StarCoder-2-Instruct", "BigCode", "Coding / Dev", 40, "OpenRAIL-M", "Optimized programming model spanning across 80+ software development languages.")
        )

        val quantizations = listOf(
            QuantOption("Q2_K_S", 0.35, "Very low accuracy, extremely compact", "Low spacing"),
            QuantOption("Q3_K_M", 0.48, "Lightweight compromise, balanced RAM usage", "Medium speed"),
            QuantOption("Q4_K_M", 0.58, "The industry standard. Near-zero quality loss", "Optimal speed"),
            QuantOption("Q5_K_M", 0.68, "Excellent high-fidelity quantization", "Moderate speed"),
            QuantOption("Q6_K", 0.78, "Extremely minimal loss, recommended for high RAM", "Stable speed"),
            QuantOption("Q8_0", 0.92, "Almost indistinguishable from FP16 defaults", "High-spec required"),
            QuantOption("FP16", 1.80, "No quantization. Uncompressed original weights", "Maximum GPU/CPU load")
        )

        val list = mutableListOf<AIModel>()
        var globalIndex = 1

        // Dynamically synthesize 100 perfectly formatted entries
        // using combination matrices of real open weight architectures and quantization variants.
        loop@ for (base in baseModels) {
            for (quant in quantizations) {
                if (globalIndex > 100) break@loop

                val modifier = when (quant.name) {
                    "Q2_K_S" -> 1.45
                    "Q3_K_M" -> 1.22
                    "Q4_K_M" -> 1.00
                    "Q5_K_M" -> 0.85
                    "Q6_K" -> 0.72
                    "Q8_0" -> 0.52
                    else -> 0.32
                }

                val finalSize = base.baseSizeGb * (quant.sizeModifier / 1.0)
                val finalTokens = (base.baseTokensPerSec * modifier).toInt().coerceAtLeast(4)
                val roundedSize = Math.round(finalSize * 10.0) / 10.0

                list.add(
                    AIModel(
                        id = "m-${globalIndex.toString().padStart(3, '0')}",
                        name = "${base.name} [${quant.name}]",
                        series = base.series,
                        category = base.category,
                        sizeGb = if (roundedSize < 0.2) 0.8 else roundedSize,
                        quantization = quant.name,
                        contextWindow = when (base.series) {
                            "DeepSeek" -> 16384
                            "Meta" -> 8192
                            "Google" -> 8192
                            "Mistral" -> 32768
                            "Microsoft" -> 128000
                            else -> 4096
                        },
                        tokensPerSecond = finalTokens,
                        license = base.license,
                        description = "${base.description} Pre-packed with ${quant.description} quant format."
                    )
                )
                globalIndex++
            }
        }

        // Fill remaining spaces up to exactly 100 if needed (e.g., custom quantized edge architectures)
        val sizes = listOf(14, 32, 70)
        var fillerIndex = 1
        while (list.size < 100) {
            val size = sizes[fillerIndex % sizes.size]
            val quantName = "Q4_K_M"
            list.add(
                AIModel(
                    id = "m-${globalIndex.toString().padStart(3, '0')}",
                    name = "Llama-3-Instruct-${size}B [${quantName}]",
                    series = "Meta",
                    category = "Reasoning / CoT",
                    sizeGb = size * 0.58,
                    quantization = quantName,
                    contextWindow = 8192,
                    tokensPerSecond = (45 / (size * 0.1)).toInt(),
                    license = "Llama 3 Community",
                    description = "Heavy flagship $size billion parameter model pre-quantized in Q4 precision."
                )
            )
            fillerIndex++
            globalIndex++
        }

        list
    }

    private data class BaseModel(
        val name: String,
        val series: String,
        val category: String,
        val baseTokensPerSec: Int,
        val license: String,
        val description: String,
        val baseSizeGb: Double = when {
            name.contains("1.1B") -> 1.1
            name.contains("mini") || name.contains("Coder-Instruct") || name.contains("7B") -> 4.2
            name.contains("Instruct") && name.contains("2-") -> 1.8
            name.contains("Medium") -> 8.2
            else -> 6.8
        }
    )

    private data class QuantOption(
        val name: String,
        val sizeModifier: Double,
        val description: String,
        val performance: String
    )
}
