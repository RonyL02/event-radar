package com.col.eventradar.models

import java.time.LocalDateTime

data class Comment(val content: String, val username: String, val time: LocalDateTime)
