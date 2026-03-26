package com.FrameHopper.app.ui.utils

import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.scene.control.Button
import javafx.scene.control.TableView
import javafx.scene.control.TextField

object SearchUtils {
    @JvmStatic
    fun <T> handleSearch(
        searchButton: Button,
        searchField: TextField,
        table: TableView<T>,
        list: ObservableList<T>,
        filter: (ObservableList<T>, String) -> FilteredList<T>
    ) {
        val query = searchField.text.trim()
        if(query.isEmpty() && searchButton.text != "X") return

        if(searchButton.text == "\uD83D\uDD0D") {
            searchButton.text = "X"
            table.items = filter(list, query)
        } else {
            searchButton.text = "\uD83D\uDD0D"
            table.items = list
            searchField.clear()
        }
    }
}