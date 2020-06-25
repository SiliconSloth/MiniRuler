package siliconsloth.miniruler.timelineviewer

import javax.swing.*

class TitledList(title: String): JPanel() {
    val titleLabel = JLabel(title)

    val listPanel = JPanel().also {
        it.layout = BoxLayout(it, BoxLayout.PAGE_AXIS)
    }

    init {
        layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
        add(titleLabel)
        add(listPanel)
    }

    fun setTitle(value: String) {
        titleLabel.text = value
    }

    fun setEntries(entries: List<String>) {
        listPanel.removeAll()
        for (entry in entries) {
            listPanel.add(makeTextArea(entry))
        }
        isVisible = entries.isNotEmpty()
    }
}