package com.dalfarra.avifviewer.editor

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

// Changed from "object" to "class"
class AvifFileType : FileType {
    override fun getName(): String = "AVIF"
    override fun getDescription(): String = "AVIF Image File"
    override fun getDefaultExtension(): String = "avif"
    override fun getIcon(): Icon = IconLoader.getIcon("/allicons/fileTypes/custom.svg", javaClass)
    override fun isBinary(): Boolean = true
}
