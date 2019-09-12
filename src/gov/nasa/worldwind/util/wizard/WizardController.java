/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util.wizard;

import gov.nasa.worldwind.util.Logging;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * @author dcollins
 * @version $Id: WizardController.java 1171 2013-02-11 21:45:02Z dcollins $
 */
class WizardController implements ActionListener
{
    private Wizard wizard;

    public WizardController(Wizard wizard)
    {
        if (wizard == null)
        {
            String message = "Wizard is null";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        
        this.wizard = wizard;
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e != null && e.getActionCommand() != null)
        {
            String actionCommand = e.getActionCommand();
            if (actionCommand.equals(Wizard.CANCEL_BUTTON_ACTION_COMMAND))
                cancelButtonPressed();
            else if (actionCommand.equals(Wizard.BACK_BUTTON_ACTION_COMMAND))
                backButtonPressed();
            else if (actionCommand.equals(Wizard.NEXT_BUTTON_ACTION_COMMAND))
                nextButtonPressed();
            else if (actionCommand.equals(Wizard.DIALOG_CLOSE_ACTION_COMMAND))
                dialogClosed();
        }
    }

    private void backButtonPressed()
    {
        WizardModel model = this.wizard.getModel();
        if (model != null && model.getCurrentPanel() != null)
        {
            WizardPanelDescriptor descriptor = model.getCurrentPanel();
            Object backPanelDescriptor = descriptor.getBackPanelDescriptor();
            this.wizard.setCurrentPanelDescriptor(backPanelDescriptor);
        }
    }

    private void nextButtonPressed()
    {
        WizardModel model = this.wizard.getModel();
        if (model != null && model.getCurrentPanel() != null)
        {
            WizardPanelDescriptor descriptor = model.getCurrentPanel();
            Object nextPanelDescriptor = descriptor.getNextPanelDescriptor();
            if (nextPanelDescriptor != null && nextPanelDescriptor instanceof Wizard.FinishIdentifier)
            {
                this.wizard.close(Wizard.FINISH_RETURN_CODE);
            }
            else
            {
                this.wizard.setCurrentPanelDescriptor(nextPanelDescriptor);
            }
        }
    }

    private void cancelButtonPressed()
    {
        this.wizard.close(Wizard.CANCEL_RETURN_CODE);
    }

    private void dialogClosed()
    {
        this.wizard.close(Wizard.CLOSED_RETURN_CODE);
    }

    void resetButtonsToPanelRules()
    {
        WizardModel model = this.wizard.getModel();
        if (model != null)
        {
            model.setCancelButtonText("Cancel");
            model.setCancelButtonIcon(null);

            model.setBackButtonText("<Back");
            model.setBackButtonIcon(null);

            WizardPanelDescriptor descriptor = model.getCurrentPanel();
            
            if (descriptor != null && descriptor.getBackPanelDescriptor() != null)
                model.setBackButtonEnabled(Boolean.TRUE);
            else
                model.setBackButtonEnabled(Boolean.FALSE);

            if (descriptor != null && descriptor.getNextPanelDescriptor() != null)
                model.setNextButtonEnabled(Boolean.TRUE);
            else
                model.setNextButtonEnabled(Boolean.FALSE);

            if (descriptor != null
                && descriptor.getNextPanelDescriptor() != null
                && descriptor.getNextPanelDescriptor() instanceof Wizard.FinishIdentifier)
            {
                model.setNextButtonText("Finish");
                model.setNextButtonIcon(null);
            }
            else
            {
                model.setNextButtonText("Next>");
                model.setNextButtonIcon(null);
            }
        }
    }
}
