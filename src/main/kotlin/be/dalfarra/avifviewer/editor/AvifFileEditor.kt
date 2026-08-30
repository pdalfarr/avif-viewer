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

    // Gestion de l'affichage global de l'éditeur (Barre d'outils en haut, Contenu au centre)
    private val rootWrapper = JBPanel<JBPanel<*>>(BorderLayout())

    // Gestion du basculement entre l'écran de chargement et le navigateur JCEF
    private val cardLayout = CardLayout()
    private val contentContainer = JBPanel<JBPanel<*>>(cardLayout)

    private val LOADING_CARD = "LOADING"
    private val BROWSER_CARD = "BROWSER"

    private var browser: JBCefBrowser? = null

    init {
        // 1. On construit la barre d'outils supérieure et on l'ajoute au Nord
        rootWrapper.add(createToolbar(), BorderLayout.NORTH)

        // 2. On prépare la zone de contenu centrale
        setupLoadingScreen()
        rootWrapper.add(contentContainer, BorderLayout.CENTER)

        // 3. Initialisation asynchrone de Chromium
        initializeBrowserAsync()
    }

    /**
     * Crée une barre d'outils native reprenant le style de l'éditeur d'images d'IntelliJ
     */
    private fun createToolbar(): JComponent {
        val actionGroup = DefaultActionGroup().apply {
            // Bouton Grille transparente (style damier)
            add(createDummyAction("Toggle Grid", AllIcons.Modules.UnmarkWebroot))

            addSeparator() // Ligne de séparation verticale

            // Boutons de contrôle de zoom
            add(createDummyAction("Zoom In", AllIcons.General.Add))       // Icône "+"
            add(createDummyAction("Zoom Out", AllIcons.General.Remove))   // Icône "-"
            add(createDummyAction("Actual Size (1:1)", AllIcons.General.ActualZoom))
            add(createDummyAction("Fit to Screen", AllIcons.General.FitContent))

            addSeparator()

            // Outil pipette
            add(createDummyAction("Color Picker", AllIcons.General.ContextHelp))
        }

        // On demande à l'ActionManager de transformer notre groupe en une barre d'outils Swing utilisable
        val toolbar = ActionManager.getInstance().createActionToolbar(
            "AvifViewerToolbar",
            actionGroup,
            true // true = Orientation horizontale
        )

        // Indique à la barre d'outils sur quel composant elle agit
        toolbar.targetComponent = rootWrapper
        return toolbar.component
    }

    /**
     * Helper pour générer des actions de boutons rapidement
     */
    private fun createDummyAction(text: String, icon: javax.swing.Icon): AnAction {
        return object : AnAction(text, text, icon) {
            override fun actionPerformed(e: AnActionEvent) {
                // Pour le moment, affiche une simple info de clic
                println("Action cliquée : $text")
                // TODO: Vous pourrez brancher ici des fonctions executeJavaScript() sur votre 'browser' !
            }
        }
    }

    private fun setupLoadingScreen() {
        val loadingPanel = JBPanel<JBPanel<*>>(GridBagLayout()).apply {
            add(AsyncProcessIcon("AvifLoading"))
            add(JBLabel("Initialisation du moteur d'image...", SwingConstants.CENTER))
        }
        contentContainer.add(loadingPanel, LOADING_CARD)
        cardLayout.show(contentContainer, LOADING_CARD)
    }

    private fun initializeBrowserAsync() {
        Thread {
            try {
                val newBrowser = JBCefBrowser()
                newBrowser.loadURL("file://${file.path}")

                SwingUtilities.invokeLater {
                    browser = newBrowser
                    contentContainer.add(newBrowser.component, BROWSER_CARD)
                    cardLayout.show(contentContainer, BROWSER_CARD)

                    contentContainer.revalidate()
                    contentContainer.repaint()
                }
            } catch (e: Exception) {
                SwingUtilities.invokeLater {
                    val errorPanel = JBLabel("Erreur : ${e.localizedMessage}", SwingConstants.CENTER)
                    contentContainer.add(errorPanel, "ERROR")
                    cardLayout.show(contentContainer, "ERROR")
                }
            }
        }.start()
    }

    // --- MISE À JOUR : On retourne le rootWrapper qui contient désormais la barre d'outils ---
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
