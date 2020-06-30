package siliconsloth.miniruler.timelineviewer

import java.awt.*
import java.lang.Exception
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class TimelineViewer(inputPath: String): JFrame("MiniRuler Timeline Viewer"), TimelinePane.SelectionListener {
    val searchField: JTextField
    val timelinePane: TimelinePane
    val infoPanel: InfoPanel

    val blacklist = listOf("Ordering", "PossibleOrdering", "PlanningRules.kt:57", "PlanningRules.kt:66")

    init {
        val loader = TimelineLoader(inputPath)

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        defaultCloseOperation = EXIT_ON_CLOSE
        layout = BorderLayout()

        val leftPanel = JPanel(BorderLayout())

        searchField = JTextField()
        searchField.document.addDocumentListener(SearchFieldListener())
        leftPanel.add(searchField, BorderLayout.PAGE_START)

        timelinePane = TimelinePane(loader.tracks.values.toList().filter {
            when (it.owner) {
                is Fact -> it.owner.factClass !in blacklist
                is Match -> it.owner.rule !in blacklist
                else -> true
            }
        }, loader.maxTime)
        timelinePane.addSelectionListener(this)

        val scrollPane = InteractiveScrollPane(timelinePane)
        // Ensure that track labels are repainted in their correct positions every time the timeline pane moves.
        scrollPane.viewport.scrollMode = JViewport.SIMPLE_SCROLL_MODE
        scrollPane.addMouseListener(timelinePane)
        scrollPane.addMouseMotionListener(timelinePane)
        timelinePane.scrollPane = scrollPane

        leftPanel.add(scrollPane)

        infoPanel = InfoPanel(timelinePane)
        val infoScrollPane = JScrollPane(infoPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)

        add(JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, infoScrollPane))
        pack()
    }

    inner class SearchFieldListener: DocumentListener {
        override fun insertUpdate(e: DocumentEvent) {
            timelinePane.updateFilter(searchField.text)
        }

        override fun removeUpdate(e: DocumentEvent) {
            timelinePane.updateFilter(searchField.text)
        }

        override fun changedUpdate(e: DocumentEvent) {
        }
    }

    override fun periodSelected(period: Track.Period<*>?) {
        infoPanel.period = period
    }
}

fun main() {
    val viewer = TimelineViewer("timeline.jtl")
    viewer.isVisible = true
}