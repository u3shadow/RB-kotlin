package com.u3coding

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import java.util.regex.Pattern
import javax.print.Doc

/**
 * Created by u3 on 17-6-14.
 */
class DeleteAction(project: Project?,file: PsiFile?,document1: Document?,mClass: PsiClass?) : WriteCommandAction.Simple<Any>(project,file) {
    var tod  = ArrayList<Int>()
    var s1 = ArrayList<String>()
    var mFactory : PsiElementFactory? = null
    var mClass: PsiClass? = null
    var nameidmap = LinkedHashMap<String,String>()
    var tcmap = LinkedHashMap<Int,String>()
    var document : Document? = null
    var file : PsiFile? = null
    init {
        mFactory = JavaPsiFacade.getElementFactory(project as Project)
        this.mClass = mClass
        document = document1
        s1 = document!!.getText().split("\n") as ArrayList<String>
        this.file = file
    }
    override fun run() {
        deleteImport()
        replaceAnnotationAndGetIdName()
        deleteAnnotationAndGetIdName()
        deleteButterKnife()
        for ((key, value) in tcmap) {
            val deleteStart = document!!.getLineStartOffset(key)
            val deleteEnd = document!!.getLineEndOffset(key)
            document!!.replaceString(deleteStart, deleteEnd, "\t" + value + ";")
        }
        for (i in tod.indices) {
            val deleteStart = document!!.getLineStartOffset(tod[i])
            val deleteEnd = document!!.getLineEndOffset(tod[i])
            document!!.deleteString(deleteStart, deleteEnd)
        }
        val manager = PsiDocumentManager.getInstance(project)
        manager.commitDocument(document as Document)
        createFindViewByIdCode()

    }
      fun deleteImport() {
        //delete import
        val is1 = "import butterknife.Bind;"
        val is3 = "import butterknife.InjectView;"
        val is2 = "import butterknife.ButterKnife;"
        val is4 = "import butterknife.BindView;"
        for (i in s1.indices){
            when(s1[i]){
                is1,is2,is3,is4 -> tod.add(i)
            }
        }
    }
    fun replaceAnnotationAndGetIdName(){
        val pattern = "^@(BindView|InjectView|Bind)\\(R.id.*\\)$"
        var r = Pattern.compile(pattern)
        for (j in s1.indices){
            var m = r.matcher(s1[j])
            if (m.find()){
                var id = s1[j].substring(s1[j].indexOf("(")+1,s1[j].indexOf(")"))
                var s2 = s1[j].substring(s1[j].indexOf(")")+1).trim()
                var name = s2.split(" ")[1]
                name = name.substring(0,name.length-1)
                val type = s2.split(" ")[0]
                tcmap.put(j,type + " "+name)
                name = name+" = "+"("+type+")"
                nameidmap.put(name,id)
                System.out.print(type + "--"+name)
            }
        }
    }

    private fun deleteAnnotationAndGetIdName() {
        val pattern = "^@(BindView|InjectView|Bind)\\(R.id.*\\)$"
        val r = Pattern.compile(pattern)
        for (i in s1.indices) {
            val m = r.matcher(s1[i].trim { it <= ' ' })
            s1[i] = s1[i].trim { it <= ' ' }
            if (m.find()) {
                val id = s1[i].substring(s1[i].indexOf("(") + 1, s1[i].length - 1)
                val s2 = s1[i + 1].trim { it <= ' ' }.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val name = s2[1].substring(0, s2[1].length - 1) + " = " + "(" + s2[0] + ")"
                nameidmap.put(name, id)
                tod.add(i)
            }
        }
    }

    private fun deleteButterKnife() {
        //delete butterknife use
        for (i in s1.indices) {
            if (s1[i].trim { it <= ' ' }.indexOf("ButterKnife") == 0) {
                tod.add(i)
            }
        }
    }
    private fun createFindViewByIdCode(){
        var code  = ArrayList<String>()
        for ((key,value) in nameidmap){
            var codes = key+"findViewById("+value+");"
            code.add(codes)
        }
       var findAndWriter : FindVIewByIdWriter=  FindVIewByIdWriter(project, file, mClass, code, mFactory)
        findAndWriter.execute()
    }
}