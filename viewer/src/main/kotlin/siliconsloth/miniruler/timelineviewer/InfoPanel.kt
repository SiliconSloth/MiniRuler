package siliconsloth.miniruler.timelineviewer

import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.BoxLayout
import javax.swing.JPanel

class InfoPanel: JPanel() {
    val nameField = makeTextArea()

    val bindingList = TitledList("")
    val inserterList = TitledList("")
    val maintainerList = TitledList("")
    val deleterList = TitledList("")

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

        bindingList.setEntries(value?.track?.getBindings(value) ?: listOf())
        inserterList.setEntries(value?.track?.getInserts(value) ?: listOf())
        maintainerList.setEntries(value?.track?.getMaintains(value) ?: listOf())
        deleterList.setEntries(value?.track?.getDeletes(value) ?: listOf())

        nameField.revalidate()
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

        add(panel, BorderLayout.PAGE_START)
    }
}