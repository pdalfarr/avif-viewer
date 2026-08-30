package be.dalfarra.avifviewer.editor

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileTypes.FileType
import javax.swing.Icon

class AvifFileType : FileType {
    override fun getName(): String = "AVIF"
    override fun getDescription(): String = "AVIF Image File"
    override fun getDefaultExtension(): String = "avif"

    // --- CORRECTIF : Utilise l'icône native et garantie d'IntelliJ pour les images/diagrammes ---
    override fun getIcon(): Icon = AllIcons.FileTypes.Image

    override fun isBinary(): Boolean = true
}