package com.u3coding

import com.intellij.codeInsight.generation.actions.BaseGenerateAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.util.PsiUtilBase

/**
 * Created by u3 on 17-6-14.
 */
class  MainAction : BaseGenerateAction(null) {
    override fun actionPerformed(e: AnActionEvent?) {
        var project = e!!.getData(PlatformDataKeys.PROJECT)
        var editor = e!!.getData(PlatformDataKeys.EDITOR)
        var file = PsiUtilBase.getPsiFileInEditor(editor as Editor,project as Project)
        var mFactory = JavaPsiFacade.getElementFactory(project as Project)
        var mClass  = getTargetClass(editor,file)
        val document : Document = editor!!.document
        val deleteAction = DeleteAction(project, file, document, mClass)
        deleteAction.execute()
    }

}
