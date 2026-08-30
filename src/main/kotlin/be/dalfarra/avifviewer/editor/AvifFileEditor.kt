package be.dalfarra.avifviewer.editor

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.FileEditorStateLevel
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.ui.AsyncProcessIcon
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.SwingConstants
import javax.swing.SwingUtilities

class AvifFileEditor(private val project: Project, private val file: VirtualFile) : UserDataHolderBase(), FileEditor {

    private val rootWrapper = JBPanel<JBPanel<*>>(BorderLayout())
    private val cardLayout = CardLayout()
    private val contentContainer = JBPanel<JBPanel<*>>(cardLayout)

    private val LOADING_CARD = "LOADING"
    private val BROWSER_CARD = "BROWSER"

    private var browser: JBCefBrowser? = null

    init {
        rootWrapper.add(createToolbar(), BorderLayout.NORTH)
        setupLoadingScreen()
        rootWrapper.add(contentContainer, BorderLayout.CENTER)
        initializeBrowserAsync()
    }

    private fun createToolbar(): JComponent {
        val actionGroup = DefaultActionGroup().apply {
            add(createAction("Toggle Grid", AllIcons.Modules.UnmarkWebroot) { toggleGrid() })
            addSeparator()
            add(createAction("Zoom In", AllIcons.General.Add) { zoomIn() })
            add(createAction("Zoom Out", AllIcons.General.Remove) { zoomOut() })
            add(createAction("Actual Size (1:1)", AllIcons.General.ActualZoom) { resetZoom() })
            add(createAction("Fit to Screen", AllIcons.General.FitContent) { fitToScreen() })
        }

        val toolbar = ActionManager.getInstance().createActionToolbar(
            "AvifViewerToolbar",
            actionGroup,
            true
        )
        toolbar.targetComponent = rootWrapper
        return toolbar.component
    }

    private fun createAction(text: String, icon: javax.swing.Icon, action: () -> Unit): AnAction {
        return object : AnAction(text, text, icon) {
            override fun actionPerformed(e: AnActionEvent) {
                action()
            }
        }
    }

    private fun setupLoadingScreen() {
        val loadingPanel = JBPanel<JBPanel<*>>(GridBagLayout()).apply {
            add(AsyncProcessIcon("AvifLoading"))
            add(JBLabel("Loading...", SwingConstants.CENTER))
        }
        contentContainer.add(loadingPanel, LOADING_CARD)
        cardLayout.show(contentContainer, LOADING_CARD)
    }

    private fun initializeBrowserAsync() {
        Thread {
            try {
                val newBrowser = JBCefBrowser()

                val customHtml = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <style>
                            * { box-sizing: border-box; }
                            html, body {
                                margin: 0;
                                padding: 0;
                                width: 100%;
                                height: 100%;
                                background-color: #0e0e0e;
                                overflow: auto;
                                display: flex;
                                justify-content: center;
                                align-items: center;
                            }
                            .checkerboard {
                                background-image: linear-gradient(45deg, #222 25%, transparent 25%), 
                                                  linear-gradient(-45deg, #222 25%, transparent 25%), 
                                                  linear-gradient(45deg, transparent 75%, #222 75%), 
                                                  linear-gradient(-45deg, transparent 75%, #222 75%);
                                background-size: 20px 20px;
                                background-position: 0 0, 0 10px, 10px -10px, -10px 0px;
                            }
                            #viewport {
                                display: flex;
                                justify-content: center;
                                align-items: center;
                                min-width: 100%;
                                min-height: 100%;
                            }
                            img {
                                display: block;
                                transform-origin: center center;
                                transition: transform 0.1s ease-out;
                                user-select: none;
                            }
                            .fit-screen {
                                max-width: 100vw;
                                max-height: 100vh;
                                object-fit: contain;
                            }
                        </style>
                    </head>
                    <body>
                        <div id="viewport">
                            <img id="avif-img" class="fit-screen" src="file://${file.path}" />
                        </div>
                        <script>
                            window.currentScale = 1.0;
                            const img = document.getElementById('avif-img');

                            window.viewerZoom = function(delta) {
                                img.classList.remove('fit-screen');
                                window.currentScale = Math.max(0.1, Math.min(10.0, window.currentScale + delta));
                                img.style.transform = 'scale(' + window.currentScale + ')';
                            };

                            window.viewerReset = function() {
                                img.classList.remove('fit-screen');
                                window.currentScale = 1.0;
                                img.style.transform = 'scale(1)';
                            };

                            window.viewerFit = function() {
                                window.currentScale = 1.0;
                                img.style.transform = 'scale(1)';
                                img.classList.add('fit-screen');
                            };

                            window.viewerToggleGrid = function() {
                                document.body.classList.toggle('checkerboard');
                            };
                        </script>
                    </body>
                    </html>
                """.trimIndent()

                newBrowser.loadHTML(customHtml)

                SwingUtilities.invokeLater {
                    browser = newBrowser
                    contentContainer.add(newBrowser.component, BROWSER_CARD)
                    cardLayout.show(contentContainer, BROWSER_CARD)
                    contentContainer.revalidate()
                    contentContainer.repaint()
                }
            } catch (e: Exception) {
                SwingUtilities.invokeLater {
                    val errorPanel = JBLabel("Error : ${e.localizedMessage}", SwingConstants.CENTER)
                    contentContainer.add(errorPanel, "ERROR")
                    cardLayout.show(contentContainer, "ERROR")
                }
            }
        }.start()
    }

    // --- ACTIONS ---

    private fun zoomIn() = executeJS("window.viewerZoom(0.25);")
    private fun zoomOut() = executeJS("window.viewerZoom(-0.25);")
    private fun resetZoom() = executeJS("window.viewerReset();")
    private fun fitToScreen() = executeJS("window.viewerFit();")
    private fun toggleGrid() = executeJS("window.viewerToggleGrid();")

    private fun executeJS(script: String) {
        val cefBrowser = browser?.cefBrowser ?: return
        val mainFrame = cefBrowser.mainFrame ?: return

        if (SwingUtilities.isEventDispatchThread()) {
            mainFrame.executeJavaScript(script, mainFrame.url, 0)
        } else {
            SwingUtilities.invokeLater {
                mainFrame.executeJavaScript(script, mainFrame.url, 0)
            }
        }
    }

    // --- FILE EDITOR LIFECYCLE ---

    override fun getComponent(): JComponent = rootWrapper
    override fun getPreferredFocusedComponent(): JComponent = rootWrapper
    override fun getFile(): VirtualFile = file
    override fun getName(): String = file.name
    override fun getState(level: FileEditorStateLevel): FileEditorState = FileEditorState.INSTANCE
    override fun setState(state: FileEditorState) {}
    override fun isModified(): Boolean = false
    override fun isValid(): Boolean = file.isValid
    override fun addPropertyChangeListener(listener: java.beans.PropertyChangeListener) {}
    override fun removePropertyChangeListener(listener: java.beans.PropertyChangeListener) {}

    override fun dispose() {
        browser?.dispose()
    }
}