package org.makeacake

import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import kotlin.system.exitProcess

object Gui {
    private val window = JFrame("EnigmaIRC")
    private val mainPanel: Box = Box.createVerticalBox()

    private val messageBox: Box = Box.createVerticalBox()
    private val messageBoxScrollbar = JScrollPane(messageBox)

    private val inputFieldPanel: Box = Box.createHorizontalBox()
    private val inputField = JTextField()

    private val COMPONENT_BORDER_COLOR: Color = Color.decode("#614376")

    var isMinimized: Boolean = false
        private set

    fun init() {
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
        window.setSize(800, 500)
        window.setResizable(false)
        window.addWindowStateListener(object : WindowAdapter() {
            override fun windowStateChanged(e: WindowEvent) {
                isMinimized = (e.getNewState() and JFrame.ICONIFIED) == JFrame.ICONIFIED
            }
        }) // Window minimized event
        applyAppIcon(window)

        mainPanel.preferredSize = Dimension(800, 500)
        mainPanel.setBorder(EmptyBorder(10, 10, 10, 10)) // Window padding
        setBackgroundColor(mainPanel, "#2a1f33")

        messageBoxScrollbar.preferredSize = Dimension(780, 450)
        messageBoxScrollbar.getHorizontalScrollBar().preferredSize = Dimension(800, 8)
        messageBoxScrollbar.getVerticalScrollBar().preferredSize = Dimension(8, 500)
        messageBox.setBorder(EmptyBorder(7, 7, 7, 7)) // Message Box padding
        setBackgroundColor(messageBox, "#4b3c57")
        messageBoxScrollbar.setBorder(LineBorder(COMPONENT_BORDER_COLOR))

        inputFieldPanel.setBorder(EmptyBorder(5, 0, 0, 0)) // Top padding
        inputFieldPanel.add(inputField)
        inputField.preferredSize = Dimension(800, 30) // 25px to input field & 5px free space
        inputField.setForeground(Color.WHITE)
        inputField.setCaretColor(Color.WHITE) // Input text color
        setBackgroundColor(inputField, "#27202e")
        inputField.setBorder(LineBorder(COMPONENT_BORDER_COLOR))


        inputField.addActionListener(ActionListener { obj: ActionEvent? -> inputAction(obj) })

        mainPanel.add(messageBoxScrollbar)
        mainPanel.add(inputFieldPanel)
        window.add(mainPanel)
        show()
    }

    /**
     * Pack all components and show window ad center of screen
     */
    fun show() {
        window.pack()
        window.setLocationRelativeTo(null)
        window.setVisible(true)
    }

    /**
     * Show new message
     * @param formattedMessage Formatted message
     */
    fun showNewMessage(formattedMessage: String?, type: MessageType) {
        val message = JLabel(formattedMessage)
        when (type) {
            MessageType.SYSTEM_GOOD -> message.setForeground(Color(0, 245, 0))
            MessageType.SYSTEM_INFO -> message.setForeground(Color(245, 245, 0))
            MessageType.SYSTEM_ERROR -> message.setForeground(Color(245, 0, 0))
            MessageType.SELF_USER_MESSAGE -> message.setForeground(Color.decode("#cbb6dc"))
            MessageType.USER_MESSAGE -> message.setForeground(Color.WHITE)
            MessageType.USER_SESSION -> message.setForeground(Color.decode("#ffb148"))
        }
        message.setFont(Font(Font.SANS_SERIF, Font.BOLD, 15))

        messageBox.add(message)
        scrollDown()
        updateWindow()
    }

    /**
     * Show welcome message.
     * Used instead of SYSTEM_GOOD due to increased font size
     */
    fun showWelcomeMessage() {
        val message = JLabel("Welcome to EnigmaIRC!")
        message.setForeground(Color(0, 245, 0))
        message.setFont(Font(Font.SANS_SERIF, Font.BOLD, 18))
        message.setBorder(EmptyBorder(0, 0, 10, 0))

        messageBox.add(message)
        updateWindow()
    }

    /**
     * Block input and exit the program after 2 minutes
     * Used for critical errors, implying the inability to further work with the program
     */
    fun breakInput() {
        inputField.isVisible = false
        updateWindow()
        try {
            Thread.sleep((1000 * 120).toLong())
        } catch (e: InterruptedException) {
        }
        exitProcess(0)
    }

    /**
     * Scroll down messageBox. Only for vertical scroll
     */
    fun scrollDown() {
        val newScroll = messageBoxScrollbar.getVerticalScrollBar()
        newScroll.setValue(messageBoxScrollbar.getVerticalScrollBar().getMaximum())
        messageBoxScrollbar.setVerticalScrollBar(newScroll)
    }

    /**
     * Revalidate all elements and repaint window
     */
    private fun updateWindow() {
        window.revalidate()
        window.repaint()
    }

    /**
     * Handle inputField "Enter" button
     * @param e Event
     */
    private fun inputAction(e: ActionEvent?) {
        val input = inputField.getText()
        when (input) {
            "!!clear" -> messageBox.removeAll()
            "!!exit" -> {
                // Send exit message
                exitProcess(0)
            }

            else -> {
                DataParser.handleOutputMessage(inputField.getText())
            }
        }

        inputField.text = ""
        scrollDown()
        updateWindow() // Update
    }

    fun applyAppIcon(frame: JFrame) {
        try {
            Main::class.java.classLoader.getResourceAsStream("icon.png").use { iconStream ->
                frame.iconImage = ImageIO.read(iconStream)
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    /**
     * Change JComponent background color
     * @param component JComponent
     * @param hex Hex-color (# and 6 symbols)
     */
    private fun setBackgroundColor(component: JComponent, hex: String) {
        component.setOpaque(true)
        component.setBackground(Color.decode(hex))
    }

    enum class MessageType {
        SELF_USER_MESSAGE, USER_MESSAGE, SYSTEM_GOOD, SYSTEM_INFO, SYSTEM_ERROR, USER_SESSION
    }
}