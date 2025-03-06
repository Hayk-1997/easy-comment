package com.github.hayk1997.easycomment.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.fileEditor.FileDocumentManager
import java.io.File

class AddCommentAction : AnAction() {
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

                if (comment != null && comment.isNotEmpty()) {
                    saveComment(virtualFile, lineNumber, comment)
                }
            }
        }
    }

    private fun saveComment(file: VirtualFile, lineNumber: Int, comment: String) {
        val commentFile = File(file.path + ".comments")
        commentFile.appendText("Line $lineNumber: $comment\n")
    }
}
