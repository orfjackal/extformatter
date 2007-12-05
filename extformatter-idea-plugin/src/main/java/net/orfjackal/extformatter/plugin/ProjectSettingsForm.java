/*
 * External Code Formatter
 * Copyright (c) 2007 Esko Luontola, www.orfjackal.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.orfjackal.extformatter.plugin;

import com.intellij.ui.DocumentAdapter;
import net.orfjackal.extformatter.settings.Settings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Esko Luontola
 * @since 4.12.2007
 */
public class ProjectSettingsForm {

    private static final Color NORMAL = Color.WHITE;
    private static final Color WARNING = new Color(255, 255, 204);
    private static final Color ERROR = new Color(255, 204, 204);

    private JCheckBox pluginEnabled;
    private JTextField eclipseExecutable;
    private JTextField eclipsePrefs;

    private JPanel rootComponent;
    private JLabel titleLabel;
    private JButton eclipseExecutableBrowse;
    private JButton eclipsePrefsBrowse;
    private JLabel eclipseExecutableLabel;
    private JLabel eclipsePrefsLabel;

    private final List<Popup> visiblePopups = new ArrayList<Popup>();

    public ProjectSettingsForm() {
        pluginEnabled.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateComponents();
            }
        });
        eclipseExecutable.getDocument().addDocumentListener(new DocumentAdapter() {
            protected void textChanged(DocumentEvent e) {
                updateComponents();
            }
        });
        eclipsePrefs.getDocument().addDocumentListener(new DocumentAdapter() {
            protected void textChanged(DocumentEvent e) {
                updateComponents();
            }
        });
        eclipseExecutableBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                browseForFile(eclipseExecutable);
            }
        });
        eclipsePrefsBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                browseForFile(eclipsePrefs);
            }
        });
        updateComponents();
    }

    private void browseForFile(@NotNull JTextField target) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileHidingEnabled(false);    // Eclipse's prefs file is in a hidden ".settings" directory

        File currentSelection = new File(target.getText());
        chooser.setCurrentDirectory(currentSelection);
        chooser.setSelectedFile(currentSelection);

        int result = chooser.showOpenDialog(rootComponent);
        if (result == JFileChooser.APPROVE_OPTION) {
            target.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void updateComponents() {
        hidePopups();
        JComponent[] affectedByPluginEnabled = new JComponent[]{
                titleLabel,
                eclipseExecutable,
                eclipseExecutableBrowse,
                eclipseExecutableLabel,
                eclipsePrefs,
                eclipsePrefsBrowse,
                eclipsePrefsLabel,
        };
        for (JComponent component : affectedByPluginEnabled) {
            component.setEnabled(pluginEnabled.isSelected());
        }
        if (notEmpty(eclipseExecutable) && fileExists(eclipseExecutable)) {
            ok(eclipseExecutable);
        }
        if (notEmpty(eclipsePrefs) && fileExists(eclipsePrefs)) {
            ok(eclipsePrefs);
        }
    }

    private boolean notEmpty(@NotNull JTextField field) {
        if (field.getText().isEmpty()) {
            field.setBackground(WARNING);
            showPopup(field, "Required field"); // TODO: localize
            return false;
        }
        return true;
    }

    private boolean fileExists(@NotNull JTextField field) {
        if (!new File(field.getText()).isFile()) {
            field.setBackground(ERROR);
            showPopup(field, "No such file"); // TODO: localize
            return false;
        }
        return true;
    }

    private void ok(@NotNull JTextField field) {
        field.setBackground(NORMAL);
    }

    private void showPopup(@NotNull JTextField parent, @NotNull String message) {
        if (!parent.isShowing() || !parent.isEnabled()) {
            return; // if getLocationOnScreen is called when the component is not showing, an exception is thrown
        }
        JToolTip tip = new JToolTip();
        tip.setTipText(message);
        Dimension tipSize = tip.getPreferredSize();

        Point location = parent.getLocationOnScreen();
        int x = (int) location.getX();
        int y = (int) (location.getY() - tipSize.getHeight());

        Popup popup = PopupFactory.getSharedInstance().getPopup(parent, tip, x, y);
        popup.show();
        visiblePopups.add(popup);
    }

    private void hidePopups() {
        for (Iterator<Popup> it = visiblePopups.iterator(); it.hasNext();) {
            Popup popup = it.next();
            popup.hide();
            it.remove();
        }
    }

    @NotNull
    public JPanel getRootComponent() {
        return rootComponent;
    }

    public void setData(@NotNull Settings data) {
        eclipseExecutable.setText(data.getEclipseExecutable());
        eclipsePrefs.setText(data.getEclipsePrefs());
        pluginEnabled.setSelected(data.isPluginEnabled());
        updateComponents();
    }

    public void getData(@NotNull Settings data) {
        data.setEclipseExecutable(eclipseExecutable.getText());
        data.setEclipsePrefs(eclipsePrefs.getText());
        data.setPluginEnabled(pluginEnabled.isSelected());
    }

    @SuppressWarnings({"ConstantConditions", "RedundantIfStatement"})
    public boolean isModified(@NotNull Settings data) {
        if (eclipseExecutable.getText() != null ? !eclipseExecutable.getText().equals(data.getEclipseExecutable()) : data.getEclipseExecutable() != null) {
            return true;
        }
        if (eclipsePrefs.getText() != null ? !eclipsePrefs.getText().equals(data.getEclipsePrefs()) : data.getEclipsePrefs() != null) {
            return true;
        }
        if (pluginEnabled.isSelected() != data.isPluginEnabled()) {
            return true;
        }
        return false;
    }
}