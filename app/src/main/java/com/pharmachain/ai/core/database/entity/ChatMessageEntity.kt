package com.pharmachain.ai.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ChatMessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

@Entity(
    tableName = "chat_messages",
    indices = [Index("userId"), Index("timestamp")]
)
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val role: ChatMessageRole,
    val content: String,
    val timestamp: Long,
    val tokensUsed: Int = 0
)
