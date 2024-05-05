package com.example.engineer.View.Elements;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class MultilineTableCellRenderer extends JTextArea implements TableCellRenderer {
    public MultilineTableCellRenderer() {
        setLineWrap(true);
        setWrapStyleWord(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setText((value == null) ? "" : value.toString());
        setSize(table.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);

        /*int fontHeight = this.getFontMetrics(this.getFont()).getHeight();
        int textLength = this.getText().length();
        int lines = textLength / this.getColumnWidth();
        if (lines == 0) {
            lines = 1;
        }
        int height = fontHeight * lines;
        table.setRowHeight(row, height);*/

        if (table.getRowHeight(row) != getPreferredSize().height) {
            table.setRowHeight(row, getPreferredSize().height);

        }

        return this;
    }
}
