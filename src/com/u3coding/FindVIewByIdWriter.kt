package com.u3coding

import com.intellij.openapi.command.WriteCommandAction.Simple
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.search.EverythingGlobalScope

/**
 * Created by u3 on 17-6-26.
 */
public class FindVIewByIdWriter:Simple<Any>{
    internal var mClass: PsiClass?
    private val mFactory: PsiElementFactory?
    internal var code: List<String>
    internal var mProject: Project
    constructor(project: Project, file: PsiFile?, psiClass: PsiClass?, code: List<String>, mFactory: PsiElementFactory?):super(project,file){
        mClass = psiClass
        this.code = code
        this.mFactory = mFactory
        mProject = project
    }
    override fun run() {
        generateInjects(mProject)
    }

    protected fun generateInjects(mProject: Project) {
        try {
            val activityClass = JavaPsiFacade.getInstance(mProject).findClass(
                    "android.app.Activity", EverythingGlobalScope(mProject))
            val fragmentClass = JavaPsiFacade.getInstance(mProject).findClass(
                    "android.app.Fragment", EverythingGlobalScope(mProject))
            val supportFragmentClass = JavaPsiFacade.getInstance(mProject).findClass(
                    "android.support.v4.app.Fragment", EverythingGlobalScope(mProject))

            // Check for Activity class

            val onCreate = mClass!!.findMethodsByName("onCreate", false)[0]
            for (statement in onCreate.body!!.statements) {
                // Search for setContentView()
                if (statement.firstChild is PsiMethodCallExpression) {
                    val methodExpression = (statement.firstChild as PsiMethodCallExpression)
                            .methodExpression
                    // Insert ButterKnife.inject()/ButterKnife.bind() after setContentView()
                    if (methodExpression.text == "setContentView") {
                        for (i in code.indices.reversed()) {
                            onCreate.body!!.addAfter(mFactory!!.createStatementFromText(
                                    code[i] + "\n", mClass), statement)
                        }
                        break
                    }
                }
            }
            // Check for Fragment class
        } catch (e: Exception) {

            val onCreateView = mClass!!.findMethodsByName("onCreateView", false)[0]
            for (statement in onCreateView.body!!.statements) {
                val returnValue = statement.text
                if (returnValue.contains("R.layout")) {
                    val viewName = returnValue.trim { it <= ' ' }.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1] + "."
                    for (i in code.indices) {
                        val buffer = StringBuffer(code[i])
                        val num = buffer.indexOf(")")
                        buffer.insert(num + 1, viewName)
                        try {
                            statement.addAfter(mFactory!!.createStatementFromText(
                                    buffer.toString(), mClass), statement)
                        } catch (e1: Exception) {
                        }

                    }
                    break
                }
            }
            e.printStackTrace()
        }

    }
}