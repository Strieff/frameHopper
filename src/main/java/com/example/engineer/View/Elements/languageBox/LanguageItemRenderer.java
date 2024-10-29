package com.example.engineer.View.Elements.languageBox;

import javax.swing.*;
import java.awt.*;

public class LanguageItemRenderer extends JLabel implements ListCellRenderer<LanguageItem> {
    @Override
    public Component getListCellRendererComponent(
            JList<? extends LanguageItem> list,
            LanguageItem value,
            int index,
            boolean isSelected,
            boolean cellHasFocus
    ) {
        if (value != null) {
            setText(value.getLanguage());
            setIcon(value.getFlag());
        }

        // Handle selection styling
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        setOpaque(true); // This is important to show the background color
        return this;
    }
}
