package com.dalfarra.avifviewer.editor

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.FileEditorStateLevel
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.ui.AsyncProcessIcon // IntelliJ's built-in animated spinning loader wheel
import java.awt.CardLayout
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.SwingConstants
import javax.swing.SwingUtilities

class AvifFileEditor(private val project: Project, private val file: VirtualFile) : UserDataHolderBase(), FileEditor {

    // 1. Manage multiple panels safely inside the same tab space using a CardLayout switcher
    private val cardLayout = CardLayout()
    private val mainPanel = JBPanel<JBPanel<*>>(cardLayout)

    private val LOADING_CARD = "LOADING"
    private val BROWSER_CARD = "BROWSER"

    private var browser: JBCefBrowser? = null

    init {
        setupLoadingScreen()
        initializeBrowserAsync()
    }

    // 2. Build a responsive, centered loading view using native IntelliJ UI elements
    private fun setupLoadingScreen() {
        val loadingPanel = JBPanel<JBPanel<*>>(GridBagLayout()).apply {
            // Uses a native spinning loader vector wheel designed by JetBrains
            val progressIcon = AsyncProcessIcon("AvifLoading")
            val loadingLabel = JBLabel("Initializing image engine...", SwingConstants.CENTER)

            add(progressIcon)
            add(loadingLabel)
        }

        mainPanel.add(loadingPanel, LOADING_CARD)
        cardLayout.show(mainPanel, LOADING_CARD) // Keep this front and center initially
    }

    // 3. Move the heavy browser initialization onto a safe, secondary background pool
    private fun initializeBrowserAsync() {
        Thread {
            try {
                // Instantiating the browser instance takes time as it initializes cef_server native assets
                val newBrowser = JBCefBrowser()
                newBrowser.loadURL("file://${file.path}")

                // 4. Safely push UI changes back onto Java's main window loop when compilation is ready
                SwingUtilities.invokeLater {
                    browser = newBrowser

                    // Attach the fully prepared Chromium surface panel to the active tab layout
                    mainPanel.add(newBrowser.component, BROWSER_CARD)
                    cardLayout.show(mainPanel, BROWSER_CARD)

                    // Force the screen panel canvas to update its sizing definitions instantly
                    mainPanel.revalidate()
                    mainPanel.repaint()
                }
            } catch (e: Exception) {
                SwingUtilities.invokeLater {
                    val errorPanel = JBLabel("Failed to initialize system browser: ${e.localizedMessage}", SwingConstants.CENTER)
                    mainPanel.add(errorPanel, "ERROR")
                    cardLayout.show(mainPanel, "ERROR")
                }
            }
        }.start()
    }

    // Bind layout target focuses to our multi-layered switcher canvas
    override fun getComponent(): JComponent = mainPanel
    override fun getPreferredFocusedComponent(): JComponent = mainPanel

    override fun getFile(): VirtualFile = file
    override fun getName(): String = file.name

    override fun getState(level: FileEditorStateLevel): FileEditorState = FileEditorState.INSTANCE
    override fun setState(state: FileEditorState) {}
    override fun isModified(): Boolean = false
    override fun isValid(): Boolean = file.isValid
    override fun addPropertyChangeListener(listener: java.beans.PropertyChangeListener) {}
    override fun removePropertyChangeListener(listener: java.beans.PropertyChangeListener) {}

    override fun dispose() {
        // Clean up memory and terminate the engine process when the user clicks the "X" on the tab header
        browser?.dispose()
    }
}
