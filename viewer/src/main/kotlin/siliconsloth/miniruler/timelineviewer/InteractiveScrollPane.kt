package siliconsloth.miniruler.timelineviewer

import java.awt.Dimension
import java.awt.Point
import java.awt.event.*
import javax.swing.JComponent
import javax.swing.JScrollBar
import javax.swing.JScrollPane
import kotlin.math.max

/**
 * A scroll pane that allows its view to be moved and resized along both dimenions by dragging with the mouse.
 */
class InteractiveScrollPane(content: JComponent): JScrollPane(content), MouseListener, MouseMotionListener, ComponentListener {

    init {
        addMouseListener(this)
        addMouseMotionListener(this)
        addComponentListener(this)
    }

    fun pan(dx: Int, dy: Int) {
        horizontalScrollBar.value -= dx
        verticalScrollBar.value -= dy
    }

    fun zoom(dx: Int, dy: Int, focusPos: Point) {
        // Desired change in scale, proportional to mouse displacement.
        val xScale = (dx / 100f) + 1
        val yScale = (dy / 100f) + 1

        // Scale the view size, making sure it is no smaller than the viewport.
        val s = viewport.view.size
        viewport.view.preferredSize = Dimension(max((s.width * xScale).toInt(), viewport.width),
                max((s.height * yScale).toInt(), viewport.height))

        viewport.revalidate()
        repaint()

        scaleBarFocus(horizontalScrollBar, xScale, focusPos.x)
        scaleBarFocus(verticalScrollBar, yScale, focusPos.y)
    }

    /**
     * Move a scrollbar such that the specified focus point will remain in roughly the same place in the viewport
     * after the given scaling operation is applied.
     */
    fun scaleBarFocus(scrollBar: JScrollBar, scale: Float, focusPos: Int) {
        val f = scrollBar.value + focusPos
        scrollBar.value = (f * scale - focusPos).toInt()
    }

    var dragStart: Point? = null
    var lastDrag: Point? = null
    var button1Down = false
    var button3Down = false

    override fun mousePressed(e: MouseEvent) {
        dragStart = e.point
        lastDrag = e.point
        if (e.button == MouseEvent.BUTTON1) {
            button1Down = true
        }
        if (e.button == MouseEvent.BUTTON3) {
            button3Down = true
        }
    }

    override fun mouseReleased(e: MouseEvent) {
        lastDrag = null
        if (e.button == MouseEvent.BUTTON1) {
            button1Down = false
        }
        if (e.button == MouseEvent.BUTTON3) {
            button3Down = false
        }
    }

    override fun mouseDragged(e: MouseEvent) {
        lastDrag?.let {
            val dx = e.x - it.x
            val dy = e.y - it.y
            if (button1Down) {
                pan(dx, dy)
            }
            if (button3Down) {
                zoom(dx, dy, dragStart!!)
            }
        }
        lastDrag = e.point
    }

    override fun componentResized(e: ComponentEvent?) {
        // Ensure that if the viewport is resized to be larger than the view, the view is resized to fill it.
        if (viewport.view.width < viewport.width || viewport.view.height < viewport.height) {
            viewport.view.preferredSize = Dimension(max(viewport.width, viewport.view.width),
                    max(viewport.height, viewport.view.height))
            viewport.revalidate()
        }
    }

    override fun mouseEntered(e: MouseEvent) {
    }

    override fun mouseClicked(e: MouseEvent) {
    }

    override fun mouseExited(e: MouseEvent) {
    }

    override fun mouseMoved(e: MouseEvent) {
    }

    override fun componentMoved(e: ComponentEvent?) {
    }

    override fun componentHidden(e: ComponentEvent?) {
    }

    override fun componentShown(e: ComponentEvent?) {
    }
}