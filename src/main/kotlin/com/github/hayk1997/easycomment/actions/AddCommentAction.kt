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
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.actionSystem.CommonDataKeys
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.swing.*
import java.awt.Dimension

data class GitHubUser(val login: String)

class AddCommentAction : AnAction() {
    init {
        DatabaseConfig.connect()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
        val project = e.project
        if (editor != null && project != null) {
            val document = editor.document
            val caretModel = editor.caretModel
            val logicalPosition = caretModel.logicalPosition
            val lineNumber = logicalPosition.line
            val virtualFile: VirtualFile? = FileDocumentManager.getInstance().getFile(document)

            if (virtualFile != null) {
                val authors = getGitUsers()
                val defaultAuthor = getGitUserName()
                val authorComboBox = JComboBox(authors.toTypedArray())
                authorComboBox.selectedItem = defaultAuthor
                authorComboBox.preferredSize = Dimension(200, 30)

                val panel = JPanel().apply { preferredSize = Dimension(200, 50) }
                panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
                panel.add(JLabel("Select Author:"))
                panel.add(authorComboBox)

                val result = JOptionPane.showConfirmDialog(
                    null,
                    panel,
                    "Select author",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                )

                if (result == Messages.OK) {
                    val selectedAuthor = authorComboBox.selectedItem as String
                    val comment = Messages.showInputDialog(
                        project,
                        "Enter your comment: ${authorComboBox.selectedItem}",
                        "Add Short Comment",
                        Messages.getQuestionIcon()
                    )

                    if (!comment.isNullOrEmpty()) {
                        saveComment(virtualFile.path, lineNumber, comment, selectedAuthor)
                    }
                }
            }
        }
    }

    private fun getGitUsers(): List<String> {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.github.com/repos/Hayk-1997/easy-comment/collaborators")
            .header("Authorization", "token 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")
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                val users: List<GitHubUser> = Gson().fromJson(body, object : TypeToken<List<GitHubUser>>() {}.type)
                users.map { it.login }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun getGitUserName(): String {
        return try {
            val process = Runtime.getRuntime().exec("git config --get user.name")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            reader.readLine().orEmpty()
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun saveComment(filePath: String, lineNumber: Int, comment: String, author: String) {
        transaction {
            CommentTable.insert {
                it[CommentTable.filePath] = filePath
                it[CommentTable.lineNumber] = lineNumber
                it[CommentTable.comment] = comment
                it[CommentTable.author] = author
            }
        }
    }
}