package lab6

import javafx.collections.FXCollections
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.event.EventHandler
import javafx.event.ActionEvent
import java.lang.Exception

class InterfaceSection(title: String) {
    val sectionPane: GridPane = GridPane()
    var lastRow: Int = 1

    var inputFields = mutableMapOf<String, TextField>()

    init {
        sectionPane.hgap = 3.0;
        sectionPane.vgap = 4.0;
        val sectionTitle = Label(title)
        sectionPane.add(sectionTitle, 0, 0, 2, 1)
    }

    fun addInput(labelText: String, defaultVal: String = ""): TextField {
        val inputLabel = Label(labelText)
        val inputField = TextField(defaultVal)
        inputField.maxWidth = 60.0
        sectionPane.add(inputLabel, 0, lastRow)
        sectionPane.add(inputField, 1, lastRow)
        inputFields[labelText] = inputField
        lastRow++
        return inputField
    }

    fun addLabel(labelText: String) {
        sectionPane.add(Label(labelText), 0, lastRow, 2, 1)
        lastRow++
    }

    fun addButton(buttonText: String, handler: EventHandler<ActionEvent>): Button {
        val button = Button(buttonText)
        button.onAction = handler
        sectionPane.add(button, 0, lastRow, 2, 1)
        lastRow++
        return button
    }

    fun <T> addComboBox(comboBox: ComboBox<T>) {
        sectionPane.add(comboBox, 0, lastRow, 2, 1)
        lastRow++
    }

    fun <T> addComboBox(items: Array<T>, defaultVal: T? = null): ComboBox<T> {
        val itemsCol = FXCollections.observableArrayList<T>(*items)
        val comboBox = ComboBox(itemsCol)
        if (defaultVal != null)
            comboBox.value = defaultVal
        sectionPane.add(comboBox, 0, lastRow, 2, 1)
        lastRow++
        return comboBox
    }

    fun getValue(labelText: String): String {
        val input = inputFields[labelText]
        if (input != null)
            return input.text
        throw Exception("No such input")
    }
}