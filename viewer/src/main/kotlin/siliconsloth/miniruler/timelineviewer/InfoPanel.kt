package siliconsloth.miniruler.timelineviewer

import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JPanel
import javax.swing.JTextArea

class InfoPanel: JPanel(BorderLayout()) {
    val nameField = JTextArea().apply {
        isEditable = false
        lineWrap = true
        minimumSize = Dimension(0,0)
    }

    var period: Track.Period<*>? = null
    set(value) {
        field = value
        nameField.text = value?.track?.owner?.name ?: ""
        nameField.revalidate()
    }

    init {
        preferredSize = Dimension(200, 1)
        add(nameField, BorderLayout.PAGE_START)
    }
}