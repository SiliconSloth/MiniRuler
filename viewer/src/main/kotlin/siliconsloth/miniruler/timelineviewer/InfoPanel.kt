package siliconsloth.miniruler.timelineviewer

import java.awt.*
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.JTextArea

class InfoPanel(timelinePane: TimelinePane): JPanel() {
    val nameField = configureTextArea(JTextArea())

    val bindingList = TitledList("", timelinePane)
    val inserterList = TitledList("", timelinePane)
    val maintainerList = TitledList("", timelinePane)
    val deleterList = TitledList("", timelinePane)
    val triggeredList = TitledList("", timelinePane)

    var period: Track.Period<*>? = null
    set(value) {
        field = value

        @Suppress("UNCHECKED_CAST")
        value as Track.Period<Track.Event>?

        nameField.text = value?.track?.owner?.name ?: ""

        bindingList.setTitle(value?.track?.bindingsTitle ?: "")
        inserterList.setTitle(value?.track?.insertsTitle ?: "")
        maintainerList.setTitle(value?.track?.maintainsTitle ?: "")
        deleterList.setTitle(value?.track?.deletesTitle ?: "")
        triggeredList.setTitle(value?.track?.triggersTitle ?: "")

        bindingList.setEntries(value?.bindings ?: listOf())
        inserterList.setEntries(value?.inserts ?: listOf())
        maintainerList.setEntries(value?.maintains ?: listOf())
        deleterList.setEntries(value?.deletes ?: listOf())
        triggeredList.setEntries(value?.triggers ?: listOf())

        revalidate()
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(200, components.map { it.preferredSize.height }.sum())
    }

    init {
        layout = BorderLayout()
        preferredSize = Dimension(200, 1)

        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.PAGE_AXIS)

        panel.add(nameField)
        panel.add(bindingList)
        panel.add(inserterList)
        panel.add(maintainerList)
        panel.add(deleterList)
        panel.add(triggeredList)

        add(panel, BorderLayout.PAGE_START)
    }

    override fun scrollRectToVisible(aRect: Rectangle?) {
        // Stop JTextAreas moving the scrollbar around when they're updated.
    }
}