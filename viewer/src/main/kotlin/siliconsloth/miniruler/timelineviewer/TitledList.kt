package siliconsloth.miniruler.timelineviewer

import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.*

/**
 * A list of facts or matches in the info panel, consisting of a column of text areas below a title.
 * Double clicking on a text area will select the period referenced by that section of the text in the timeline view.
 */
class TitledList(title: String, val timelinePane: TimelinePane): JPanel() {
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

    fun setEntries(entries: List<InfoListing>) {
        listPanel.removeAll()
        for (entry in entries) {
            listPanel.add(ListEntry(entry))
        }
        isVisible = entries.isNotEmpty()
    }

    inner class ListEntry(val listing: InfoListing): JTextArea(listing.listing.joinToString("") { it.first }), MouseListener {
        init {
            configureTextArea(this)
            addMouseListener(this)
        }

        override fun mouseClicked(e: MouseEvent) {
            if (SwingUtilities.isLeftMouseButton(e) && e.clickCount == 2) {
                var relPos = caretPosition
                for ((section, period) in listing.listing) {
                    if (relPos > section.length) {
                        relPos -= section.length
                    } else {
                        if (period != null) {
                            timelinePane.selectPeriod(period)
                        }
                        break
                    }
                }
            }
        }

        override fun mouseReleased(e: MouseEvent) {
        }

        override fun mouseEntered(e: MouseEvent) {
        }

        override fun mouseExited(e: MouseEvent) {
        }

        override fun mousePressed(e: MouseEvent) {
        }
    }
}