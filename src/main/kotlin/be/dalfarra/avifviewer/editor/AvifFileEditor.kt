package be.dalfarra.avifviewer.editor

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.FileEditorStateLevel
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.ui.AsyncProcessIcon
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.GridBagLayout
import javax.swing.*

class AvifFileEditor(private val project: Project, private val file: VirtualFile) : UserDataHolderBase(), FileEditor {

    private val LOG = Logger.getInstance(AvifFileEditor::class.java)

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
            add(createAction("Toggle Grid (g)", AllIcons.Modules.UnmarkWebroot) { toggleGrid() })
            addSeparator()
            add(createAction("Zoom In (↑ or +)", AllIcons.General.Add) { zoomIn() })
            add(createAction("Zoom Out (↓ or -)", AllIcons.General.Remove) { zoomOut() })
            add(createAction("Actual Size (/ or =)", AllIcons.General.ActualZoom) { resetZoom() })
            add(createAction("Fit to Screen (*)", AllIcons.General.FitContent) { fitToScreen() })
            addSeparator()
            add(createAction("Rotate Left (←)", AllIcons.Actions.Undo) { rotateLeft() })
            add(createAction("Rotate Right (→)", AllIcons.Actions.Redo) { rotateRight() })
        }

        val toolbar = ActionManager.getInstance().createActionToolbar(
            "AvifViewerToolbar",
            actionGroup,
            true
        )
        toolbar.targetComponent = rootWrapper
        return toolbar.component
    }

    private fun createAction(text: String, icon: Icon, action: () -> Unit): AnAction {
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

    fun createJcefFallbackPanel(): JComponent {
        // Determine OS-specific key representation
        val metaKey = if (SystemInfo.isMac) "Cmd" else "Ctrl"

        val uiDslPanel = panel {
            group("AVIF Viewer Requires JCEF Support") {
                row {
                    text(
                        """
                        Your current IDE environment does not have <a href='https://plugins.jetbrains.com/docs/intellij/embedded-browser-jcef.html'>JCEF Support</a>.<br/>
                        <br/>
                        Follow the steps below to enable functionality:
                        """.trimIndent()
                    )
                }

                // --- STEP 1 ---
                row {
                    text("<b>1. Switch to JetBrains Runtime with JCEF</b>")
                }
                indent {
                    row {
                        text("Press <shortcut>$metaKey + Shift + A</shortcut> to open <b>Find Action</b>.")
                    }
                    row {
                        text("Search for <code>Choose Boot Java Runtime for the IDE</code>.")
                    }
                    row {
                        text("Select a runtime with <b>JCEF support</b> and restart the IDE.")
                    }
                    row {
                        link("Open Boot Java Runtime Chooser") {
                            val actionManager = ActionManager.getInstance()
                            /*
                                                        val action = actionManager.getAction("ChooseBootJavaRuntimeAction")
                                                            ?: actionManager.getAction("SelectBootJavaRuntimeAction")
                                                            ?: actionManager.getAction("GotoAction")
                            */
                            val action = actionManager.getAction("GotoAction")
                            action?.let { targetAction ->
                                val event = AnActionEvent.createEvent(
                                    targetAction,
                                    // Create context that carries the active Project and initial text query
                                    SimpleDataContext.builder()
                                        .add(CommonDataKeys.PROJECT, project)
                                        .add(PlatformDataKeys.CONTEXT_COMPONENT, rootWrapper)
                                        .add(PlatformDataKeys.PREDEFINED_TEXT, "Runtime")
                                        .build(),
                                    null,
                                    ActionPlaces.ACTION_SEARCH,
                                    ActionUiKind.SEARCH_POPUP,
                                    null
                                )
                                try {
                                    LOG.info("Dispatching GotoAction event with context keys: ${AvifFileEditor::class.java.simpleName}")
                                    action.actionPerformed(event)
                                } catch (e: Exception) {
                                    LOG.error("Execution failed while invoking GotoAction", e)
                                }
                            }
                        }
                    }
                }

                // --- STEP 2 ---
                row {
                    text("<b>2. Check Registry Settings</b>")
                }
                indent {
                    row {
                        text("Open <b>Find Action</b> (<shortcut>$metaKey + Shift + A</shortcut>) and type <code>Registry...</code>.")
                    }
                    row {
                        text("Verify that <code>ide.browser.jcef.enabled</code> is checked.")
                    }
                    row {
                        link("Open Registry Settings") {
                            val actionManager = ActionManager.getInstance()
                            /*
                                                        val action = actionManager.getAction("ChooseBootJavaRuntimeAction")
                                                            ?: actionManager.getAction("SelectBootJavaRuntimeAction")
                                                            ?: actionManager.getAction("GotoAction")
                            */
                            val action = actionManager.getAction("GotoAction")
                            action?.let { targetAction ->
                                val event = AnActionEvent.createEvent(
                                    targetAction,
                                    // Create context that carries the active Project and initial text query
                                    SimpleDataContext.builder()
                                        .add(CommonDataKeys.PROJECT, project)
                                        .add(PlatformDataKeys.CONTEXT_COMPONENT, rootWrapper)
                                        .add(PlatformDataKeys.PREDEFINED_TEXT, "Registry")
                                        .build(),
                                    null,
                                    ActionPlaces.ACTION_SEARCH,
                                    ActionUiKind.SEARCH_POPUP,
                                    null
                                )
                                try {
                                    LOG.info("Dispatching GotoAction event with context keys: ${AvifFileEditor::class.java.simpleName}")
                                    action.actionPerformed(event)
                                } catch (e: Exception) {
                                    LOG.error("Execution failed while invoking GotoAction", e)
                                }
                            }
                        }
                    }                }

                // --- STEP 3 ---
                row {
                    text("<b>3. Check Environment Variables</b>")
                }
                indent {
                    row {
                        text(
                            "Ensure system variables like <code>IDEA_JDK</code> or <code>STUDIO_JDK</code> " +
                                    "are not overriding the default runtime with a custom non-JCEF JDK."
                        )
                    }
                }
            }
        }

        // Wrap in a GridBagLayout panel to center the content perfectly inside the editor area
        val centeringWrapper = JPanel(GridBagLayout()).apply {
            border = JBUI.Borders.empty(20)
            add(uiDslPanel)
        }

        val rootWrapper = JPanel(BorderLayout()).apply {
            add(centeringWrapper, BorderLayout.CENTER)
        }

        return rootWrapper
    }


    private fun showErrorCard() {
        SwingUtilities.invokeLater {
            val ERROR_CARD = "ERROR"
            val fallbackComponent = createJcefFallbackPanel()
            contentContainer.add(fallbackComponent, ERROR_CARD)
            cardLayout.show(contentContainer, ERROR_CARD)
        }
    }

    private fun initializeBrowserAsync() {
        if (!JBCefApp.isSupported()) {
            showErrorCard()
            return
        }

        Thread {
            try {
                val newBrowser = JBCefBrowser()
                val avifHtmlViewer = """
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
                                overflow: hidden;
                            }
                            /* Completely hide scrollbars across Chromium */
                            ::-webkit-scrollbar {
                                display: none;
                                width: 0px;
                                height: 0px;
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
                                width: 100%;
                                height: 100%;
                                display: flex;
                                justify-content: center;
                                align-items: center;
                                overflow: hidden;
                                position: relative;
                            }
                            img {
                                display: block;
                                transform-origin: center center;
                                transition: transform 0.15s ease-out;
                                user-select: none;
                                flex-shrink: 0;
                            }
                        </style>
                    </head>
                    <!-- disable mouse click -->
                    <body oncontextmenu="return false;">
                        <div id="viewport">
                            <img id="avif-img" src="file://${file.path}" />
                        </div>
                        <script>
                            window.currentScale = 1.0;
                            window.currentRotation = 0;
                            window.isFitMode = true;
                            const img = document.getElementById('avif-img');
                            const viewport = document.getElementById('viewport');

                            // === KEYBOARD SHORTCUTS LISTENER ===
                            window.addEventListener('keydown', (e) => {
                                switch (e.key) {
                                    case 'g':
                                    case 'G':
                                        e.preventDefault();
                                        window.viewerToggleGrid();
                                        break;
                                    case '+':
                                    case 'ArrowUp':
                                        e.preventDefault();
                                        window.viewerZoom(0.20);
                                        break;
                                    case '-':
                                    case 'ArrowDown':
                                        e.preventDefault();
                                        window.viewerZoom(-0.20);
                                        break;
                                    case '=':
                                    case '/':
                                        e.preventDefault();
                                        window.viewerReset();
                                        break;
                                    case '*':
                                        e.preventDefault();
                                        window.viewerFit();
                                        break;
                                    case 'ArrowLeft':
                                        e.preventDefault();
                                        window.viewerRotate(-90);
                                        break;
                                    case 'ArrowRight':
                                        e.preventDefault();
                                        window.viewerRotate(90);
                                        break;
                                }
                            });

                            function getNormalizedAngle() {
                                let angle = window.currentRotation % 360;
                                if (angle < 0) angle += 360;
                                return angle;
                            }

                            function applyTransforms() {
                                const imgW = img.naturalWidth;
                                const imgH = img.naturalHeight;

                                // Guard against executing math before image dimensions are decoded
                                if (!imgW || !imgH) return;

                                const rect = viewport.getBoundingClientRect();
                                const viewW = rect.width;
                                const viewH = rect.height;

                                if (window.isFitMode && viewW > 0 && viewH > 0) {
                                    const angle = getNormalizedAngle();
                                    const is90or270 = (angle === 90 || angle === 270);

                                    const targetW = is90or270 ? imgH : imgW;
                                    const targetH = is90or270 ? imgW : imgH;

                                    const scaleX = viewW / targetW;
                                    const scaleY = viewH / targetH;

                                    window.currentScale = Math.min(scaleX, scaleY);
                                }

                                img.style.transform = 'scale(' + window.currentScale + ') rotate(' + window.currentRotation + 'deg)';
                            }

                            window.viewerRotate = function(angleDelta) {
                                window.currentRotation = (window.currentRotation + angleDelta) % 360;
                                applyTransforms();
                            };

                            window.viewerZoom = function(delta) {
                                window.isFitMode = false;
                                window.currentScale = Math.max(0.1, Math.min(10.0, window.currentScale + delta));
                                applyTransforms();
                            };

                            window.viewerReset = function() {
                                window.isFitMode = false;
                                window.currentScale = 1.0;
                                applyTransforms();
                            };

                            window.viewerFit = function() {
                                window.isFitMode = true;
                                applyTransforms();
                            };

                            window.viewerToggleGrid = function() {
                                document.body.classList.toggle('checkerboard');
                            };

                            const resizeObserver = new ResizeObserver(() => {
                                if (window.isFitMode) applyTransforms();
                            });
                            resizeObserver.observe(viewport);

                            // Guarantee image decoding before triggering fit calculation
                            if (img.complete && img.naturalWidth !== 0) {
                                applyTransforms();
                            } else {
                                img.decode().then(() => {
                                    applyTransforms();
                                }).catch(() => {
                                    img.addEventListener('load', applyTransforms);
                                });
                            }
                        </script>
                    </body>
                    </html>
                """.trimIndent()

                newBrowser.loadHTML(avifHtmlViewer)

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

    private fun zoomIn() = executeJS("window.viewerZoom(0.20);")
    private fun zoomOut() = executeJS("window.viewerZoom(-0.20);")
    private fun resetZoom() = executeJS("window.viewerReset();")
    private fun fitToScreen() = executeJS("window.viewerFit();")
    private fun rotateLeft() = executeJS("window.viewerRotate(-90);")
    private fun rotateRight() = executeJS("window.viewerRotate(90);")
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