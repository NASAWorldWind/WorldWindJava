/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util.wizard;

import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * @author dcollins
 * @version $Id: Wizard.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Wizard
{
    // Logical Wizard components.
    private WizardModel model;
    private WizardController controller;
    private int returnCode;
    @SuppressWarnings({"FieldCanBeLocal"})
    private PropertyEvents propertyEvents;
    @SuppressWarnings({"FieldCanBeLocal"})
    private WindowEvents windowEvents;
    // Wizard UI components.
    private JDialog dialog;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private JButton backButton;
    private JButton nextButton;
    private JButton cancelButton;

    static final String BACK_BUTTON_ACTION_COMMAND = "wizard.BackButtonActionCommand";
    static final String NEXT_BUTTON_ACTION_COMMAND = "wizard.NextButtonActionCommand";
    static final String CANCEL_BUTTON_ACTION_COMMAND = "wizard.CancelButtonActionCommand";
    static final String DIALOG_CLOSE_ACTION_COMMAND = "wizard.DialogCloseActionCommand";

    public static final int FINISH_RETURN_CODE = 0;
    public static final int CANCEL_RETURN_CODE = 1;
    public static final int ERROR_RETURN_CODE = 2;
    public static final int CLOSED_RETURN_CODE = -1;

    public static final FinishIdentifier FINISH = new FinishIdentifier();

    static class FinishIdentifier
    {
        public static final String IDENTIFIER = "wizard.FinishIdentifier";
    }
    
    public Wizard()
    {
        this.dialog = new JDialog();
        init();
    }

    public Wizard(Dialog owner)
    {
        if (owner == null)
        {
            String message = "Dialog is null";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.dialog = new JDialog(owner);
        init();
    }

    public Wizard(Frame owner)
    {
        if (owner == null)
        {
            String message = "Frame is null";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.dialog = new JDialog(owner);
        init();
    }

    public WizardPanelDescriptor getWizardPanel(Object id)
    {
        return this.model.getWizardPanel(id);
    }

    public void registerWizardPanel(Object id, WizardPanelDescriptor panel)
    {
        if (id == null)
        {
            String message = Logging.getMessage("nullValue.ObjectIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (panel == null || panel.getPanelComponent() == null)
        {
            String message = "Panel or PanelComponent is null";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.cardPanel.add(panel.getPanelComponent(), id);
        panel.registerPanel(this);
        this.model.registerWizardPanel(id, panel);
    }

    public WizardPanelDescriptor getCurrentPanel()
    {
        return this.model.getCurrentPanel();
    }

    public void setCurrentPanelDescriptor(Object id)
    {
        if (id == null)
        {
            close(ERROR_RETURN_CODE);
            return;
        }

        WizardPanelDescriptor oldPanel = this.model.getCurrentPanel();
        if (oldPanel != null)
            oldPanel.aboutToHidePanel();

        if (!this.model.setCurrentPanel(id))
        {
            return;
        }

        WizardPanelDescriptor newPanel = this.model.getCurrentPanel();

        if (newPanel != null)
            newPanel.aboutToDisplayPanel();

        this.cardLayout.show(this.cardPanel, id.toString());

        if (newPanel != null)
            newPanel.displayingPanel();
    }

    public WizardModel getModel()
    {
        return this.model;
    }

    public int getReturnCode()
    {
        return this.returnCode;
    }

    public Window getOwner()
    {
        return this.dialog.getOwner();
    }

    public JDialog getDialog()
    {
        return this.dialog;
    }

    public boolean isModal()
    {
        return this.dialog.isModal();
    }

    public void setModal(boolean b)
    {
        this.dialog.setModal(b);
    }

    public String getTitle()
    {
        return this.dialog.getTitle();
    }

    public void setTitle(String title)
    {
        this.dialog.setTitle(title);
    }

    public int showModalDialog()
    {
        this.dialog.setModal(true);
        this.dialog.pack();
        this.dialog.setVisible(true);

        return this.returnCode;
    }

    void close(int code)
    {
        this.returnCode = code;

        WizardPanelDescriptor panel = this.model.getCurrentPanel();
        if (panel != null)
            panel.aboutToHidePanel();

        this.dialog.dispose();
    }

    public boolean isBackButtonEnabled()
    {
        Boolean b = this.model.isBackButtonEnabled();
        return b != null ? b : false;
    }

    public void setBackButtonEnabled(boolean b)
    {
        this.model.setBackButtonEnabled(b);
    }

    public boolean isNextButtonEnabled()
    {
        Boolean b = this.model.isNextButtonEnabled();
        return b != null ? b : false;
    }

    public void setNextButtonEnabled(boolean b)
    {
        this.model.setNextButtonEnabled(b);
    }

    public boolean isCancelButtonEnabled()
    {
        Boolean b = this.model.isCancelButtonEnabled();
        return b != null ? b : false;
    }

    public void setCancelButtonEnabled(boolean b)
    {
        this.model.setCancelButtonEnabled(b);
    }

    public void giveFocusToBackButton()
    {
        this.backButton.requestFocusInWindow();
    }

    public void giveFocusToNextButton()
    {
        this.nextButton.requestFocusInWindow();
    }

    public void giveFocusToCancelButton()
    {
        this.cancelButton.requestFocusInWindow();        
    }

    private void init()
    {
        // Initialize logical components.
        this.model = new WizardModel();
        this.controller = new WizardController(this);

        this.propertyEvents = new PropertyEvents();
        this.model.addPropertyChangeListener(this.propertyEvents);
        this.windowEvents = new WindowEvents();
        this.dialog.addWindowListener(this.windowEvents);

        // Initialize UI components.
        makeComponents();
        // Initialize UI layout.
        layoutComponents();

        this.controller.resetButtonsToPanelRules();
    }

    private void makeComponents()
    {
        this.cardPanel = new JPanel();
        this.cardLayout = new CardLayout();

        this.backButton = new JButton();
        this.nextButton = new JButton();
        this.cancelButton = new JButton();
        this.backButton.setActionCommand(BACK_BUTTON_ACTION_COMMAND);
        this.nextButton.setActionCommand(NEXT_BUTTON_ACTION_COMMAND);
        this.cancelButton.setActionCommand(CANCEL_BUTTON_ACTION_COMMAND);
        this.backButton.addActionListener(this.controller);
        this.nextButton.addActionListener(this.controller);
        this.cancelButton.addActionListener(this.controller);
        this.dialog.getRootPane().setDefaultButton(this.nextButton);
    }

    private void layoutComponents()
    {
        this.dialog.getContentPane().setLayout(new BorderLayout());

        this.cardPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        this.cardPanel.setLayout(this.cardLayout);
        this.dialog.getContentPane().add(cardPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        JSeparator separator = new JSeparator();
        buttonPanel.add(separator, BorderLayout.NORTH);
        Box buttonBox = new Box(BoxLayout.X_AXIS);
        buttonBox.setBorder(new EmptyBorder(5, 10, 5, 10));
        buttonBox.add(this.backButton);
        buttonBox.add(Box.createHorizontalStrut(10));
        buttonBox.add(this.nextButton);
        buttonBox.add(Box.createHorizontalStrut(30));
        buttonBox.add(this.cancelButton);
        buttonPanel.add(buttonBox, BorderLayout.EAST);
        this.dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setButtonText(JButton button, Object value)
    {
        if (button != null)
        {
            button.setText(value != null ? value.toString() : null);
        }
    }

    private void setButtonEnabled(JButton button, Object value)
    {
        if (button != null)
        {
            button.setEnabled(value != null && Boolean.parseBoolean(value.toString()));
        }
    }

    private void setButtonIcon(JButton button, Object value)
    {
        if (button != null)
        {
            button.setIcon((value != null && value instanceof Icon) ? (Icon) value : null);
        }
    }
    
    private class PropertyEvents implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt != null && evt.getPropertyName() != null) {
                String propertyName = evt.getPropertyName();
                Object newValue = evt.getNewValue();

                if (propertyName.equals(WizardModel.CURRENT_PANEL_DESCRIPTOR)) {
                    if (controller != null) {
                        controller.resetButtonsToPanelRules();
                    }
                } else if (propertyName.equals(WizardModel.BACK_BUTTON_TEXT)) {
                    setButtonText(backButton, newValue);
                } else if (propertyName.equals(WizardModel.NEXT_BUTTON_TEXT)) {
                    setButtonText(nextButton, newValue);
                } else if (propertyName.equals(WizardModel.CANCEL_BUTTON_TEXT)) {
                    setButtonText(cancelButton, newValue);
                } else if (propertyName.equals(WizardModel.BACK_BUTTON_ENABLED)) {
                    setButtonEnabled(backButton, newValue);
                } else if (propertyName.equals(WizardModel.NEXT_BUTTON_ENABLED)) {
                    setButtonEnabled(nextButton, newValue);
                } else if (propertyName.equals(WizardModel.CANCEL_BUTTON_ENABLED)) {
                    setButtonEnabled(cancelButton, newValue);
                } else if (propertyName.equals(WizardModel.BACK_BUTTON_ICON)) {
                    setButtonIcon(backButton, newValue);
                } else if (propertyName.equals(WizardModel.NEXT_BUTTON_ICON)) {
                    setButtonIcon(nextButton, newValue);
                } else if (propertyName.equals(WizardModel.CANCEL_BUTTON_ICON)) {
                    setButtonIcon(cancelButton, newValue);
                }
            }
        }
    }

    private class WindowEvents extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            // Simulate a button's ActionEvent for window closing.
            if (controller != null) {
                controller.actionPerformed(new ActionEvent(e.getSource(), e.getID(), DIALOG_CLOSE_ACTION_COMMAND));
            }
        }
    }
}
