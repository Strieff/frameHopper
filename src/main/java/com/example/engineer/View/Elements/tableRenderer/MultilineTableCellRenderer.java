package com.example.engineer.View.Elements.tableRenderer;

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

        if (table.getRowHeight(row) != getPreferredSize().height) {
            table.setRowHeight(row, getPreferredSize().height);
        }

        if (isSelected) {
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        } else {
            setBackground(table.getBackground());
            setForeground(table.getForeground());
        }

        setBorder(hasFocus ? UIManager.getBorder("Table.focusCellHighlightBorder") : null);

        return this;
    }
}
