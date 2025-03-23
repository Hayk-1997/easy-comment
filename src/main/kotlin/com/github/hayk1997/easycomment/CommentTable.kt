package com.github.hayk1997.easycomment

import org.jetbrains.exposed.sql.Table

object CommentTable : Table("comments") {
    val id = integer("id").autoIncrement()
    val filePath = varchar("file_path", 255)
    val lineNumber = integer("line_number")
    val comment = text("comment")
    val author = varchar("author", 255)

    override val primaryKey = PrimaryKey(id)
}