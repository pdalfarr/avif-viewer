package com.dalfarra.avifviewer.editor

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class AvifFileEditorProvider : FileEditorProvider {

    // Tells IntelliJ to activate this plugin whenever someone opens an .avif file
    override fun accept(project: Project, file: VirtualFile): Boolean {
        return file.extension?.lowercase() == "avif"
    }

    // Creates the actual Tab object
    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        return AvifFileEditor(project, file)
    }

    // Identifies this specific editor type uniquely in cache definitions
    override fun getEditorTypeId(): String = "AvifViewerEditor"

    // PLACE_BEFORE puts this view format upfront instead of text editors
    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.PLACE_BEFORE_DEFAULT_EDITOR
}
