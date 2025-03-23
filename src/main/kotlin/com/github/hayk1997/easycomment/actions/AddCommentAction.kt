package com.github.hayk1997.easycomment.actions

import com.github.hayk1997.easycomment.CommentTable
import com.github.hayk1997.easycomment.DatabaseConfig
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.fileEditor.FileDocumentManager
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class AddCommentAction : AnAction() {
    init {
        DatabaseConfig.connect()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor: Editor? = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR)
        val project = e.project
        if (editor != null && project != null) {
            val document = editor.document
            val caretModel = editor.caretModel
            val logicalPosition = caretModel.logicalPosition
            val lineNumber = logicalPosition.line
            val virtualFile: VirtualFile? = FileDocumentManager.getInstance().getFile(document)

            if (virtualFile != null) {
                val comment = Messages.showInputDialog(
                    project,
                    "Enter your comment:",
                    "Add Short Comment",
                    Messages.getQuestionIcon()
                )

                if (!comment.isNullOrEmpty()) {
                    saveComment(virtualFile.path, lineNumber, comment)
                }
            }
        }
    }

    private fun saveComment(filePath: String, lineNumber: Int, comment: String) {
        transaction {
            CommentTable.insert {
                it[CommentTable.filePath] = filePath
                it[CommentTable.lineNumber] = lineNumber
                it[CommentTable.comment] = comment
            }
        }
    }
}
