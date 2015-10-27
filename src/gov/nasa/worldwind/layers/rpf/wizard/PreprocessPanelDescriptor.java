/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.rpf.wizard;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.rpf.RPFTiledImageProcessor;
import gov.nasa.worldwind.layers.rpf.RPFFileIndex;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.wizard.DefaultPanelDescriptor;
import gov.nasa.worldwind.util.wizard.Wizard;
import gov.nasa.worldwind.util.wizard.WizardModel;
import gov.nasa.worldwind.Configuration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dcollins
 * @version $Id: PreprocessPanelDescriptor.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class PreprocessPanelDescriptor extends DefaultPanelDescriptor
{
    private ProgressPanel panelComponent;
    // Preprocessor logical components.
    private RPFTiledImageProcessor preprocessor;
    private Thread workerThread;
    // Preprocessing state display components.
    private int numSteps;
    private final AtomicInteger stepsTaken = new AtomicInteger(0);
    private final AtomicInteger stepsWithErrors = new AtomicInteger(0);
    private final ETRCalculator etrCalc = new ETRCalculator();

    public static final String IDENTIFIER = "gov.nasa.worldwind.rpf.wizard.PreprocessPanel";
    public static final String THREAD_POOL_SIZE = "gov.nasa.worldwind.rpf.wizard.ThreadPoolSize";
    public static final String STEPS_NEEDED_FOR_ESTIMATE = "gov.nasa.worldwind.rpf.wizard.StepsNeededForEstimate";

    private static final int DEFAULT_THREAD_POOL_SIZE = 3;
    private static final int DEFAULT_STEPS_NEEDED_FOR_ESTIMATE = 20;

    public PreprocessPanelDescriptor()
    {
        // Get preprocessor thread pool size, and num steps needed for ETR
        // from Configuration. Provide suitable defaults if these values
        // aren't specified.
        int threadPoolSize = Configuration.getIntegerValue(THREAD_POOL_SIZE, DEFAULT_THREAD_POOL_SIZE);
        int stepsNeededForEst = Configuration.getIntegerValue(STEPS_NEEDED_FOR_ESTIMATE,
            DEFAULT_STEPS_NEEDED_FOR_ESTIMATE);

        this.panelComponent = new ProgressPanel();
        this.preprocessor = new RPFTiledImageProcessor();
        this.preprocessor.setThreadPoolSize(threadPoolSize);
        this.preprocessor.addPropertyChangeListener(new PropertyEvents());
        this.etrCalc.setStepsNeededForEstimate(stepsNeededForEst);
        setPanelIdentifier(IDENTIFIER);
        setPanelComponent(this.panelComponent);
    }

    public Object getBackPanelDescriptor()
    {
        return null;
    }

    public Object getNextPanelDescriptor()
    {
        return Wizard.FINISH;
    }

    public void aboutToDisplayPanel()
    {
        this.panelComponent.getProgressBar().setMinimum(0);
        this.panelComponent.getProgressBar().setMaximum(0);
        this.panelComponent.getProgressBar().setValue(0);
        this.panelComponent.setProgressDescription1(" ");
        this.panelComponent.setProgressDescription2(" ");
    }

    public void displayingPanel()
    {
        WizardModel model = getWizardModel();
        final Iterable<FileSet> fileSetList = RPFWizardUtil.getFileSetList(model);
        final File selectedFile = RPFWizardUtil.getSelectedFile(model);
        if (fileSetList != null && selectedFile != null)
        {
            this.panelComponent.setTitle(RPFWizardUtil.makeLarger("Importing Imagery"));
            this.panelComponent.setDescription("");
            this.panelComponent.getProgressBar().setVisible(true);
            if (model != null)
            {
                model.setNextButtonEnabled(false);
            }

            startWorkerThread(new Runnable() {
                public void run() {
                    List<FileSet> selectedSets = new ArrayList<FileSet>();
                    for (FileSet set : fileSetList) {
                        if (set.isSelected()) {
                            selectedSets.add(set);
                        }
                    }
                    for (int i = 0; i < selectedSets.size(); i++) {
                        FileSet set = selectedSets.get(i);
                        preprocess(selectedFile, set, i + 1, selectedSets.size());
                    }
                    finished();
                }
            });
        }
        else
        {
            this.panelComponent.setTitle(RPFWizardUtil.makeLarger("No Imagery to Import"));
            this.panelComponent.setDescription("No Imagery");
            this.panelComponent.getProgressBar().setVisible(false);
        }
    }

    public void aboutToHidePanel()
    {
        Wizard wizard = getWizard();
        if (wizard != null && wizard.getReturnCode() == Wizard.FINISH_RETURN_CODE)
        {
            // "Finish" button pressed.
        }
        else
        {
            // "<Back" or "Cancel" button pressed, or window closed.
            if (this.preprocessor != null)
                this.preprocessor.stop();
        }
    }

    private void preprocess(File inFile, FileSet set, int setNumber, int numSets)
    {
        long startTime = System.currentTimeMillis();

        RPFFileIndex fileIndex = null;
        Layer layer = null;
        try
        {
            String descr = makeDescription(set, setNumber, numSets);
            if (inFile != null && set != null)
            {
                String subDescr = makeSubStepDescription(descr, "Processing Image Files");
                this.panelComponent.setDescription(RPFWizardUtil.makeBold(subDescr));

                fileIndex = this.preprocessor.makeFileIndex(inFile, set.getIdentifier(), set.getTitle(), set.getFiles());
                set.setProperty("filesProcessed", this.stepsTaken.intValue());
                set.setProperty("filesWithErrors", this.stepsWithErrors.intValue());
            }

            if (fileIndex != null)
            {
                String subDescr = makeSubStepDescription(descr, "Generating Overview Imagery");
                this.panelComponent.setDescription(RPFWizardUtil.makeBold(subDescr));

                layer = this.preprocessor.makeLayer(fileIndex);
            }
        }
        catch (Exception e)
        {
            String message = "Exception while preprocessing: " + (set != null ? set.getTitle() : "null");
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            layer = null;
        }

        WizardModel model = getWizardModel();
        if (layer != null && model != null)
        {
            List<Layer> layerList = RPFWizardUtil.getLayerList(model);
            if (layerList == null)
            {
                layerList = new ArrayList<Layer>();
                RPFWizardUtil.setLayerList(model, layerList);
            }

            layerList.add(layer);
        }

        long endTime = System.currentTimeMillis();
        String message = String.format("Preprocessor completed \'%s\' in %,d (millis)", (set != null ? set.getTitle() : "null"), endTime - startTime);
        Logging.logger().fine(message);
    }

    private void finished()
    {
        this.panelComponent.setTitle(RPFWizardUtil.makeLarger("Finished"));
        this.panelComponent.setDescription(makeFinishedDescription());
        this.panelComponent.getProgressBar().setMinimum(0);
        this.panelComponent.getProgressBar().setMaximum(0);
        this.panelComponent.getProgressBar().setValue(0);
        this.panelComponent.getProgressBar().setVisible(false);
        this.panelComponent.setProgressDescription1(" ");
        this.panelComponent.setProgressDescription2(" ");

        WizardModel model = getWizardModel();
        if (model != null)
        {
            model.setNextButtonEnabled(true);
        }
    }

    private class PropertyEvents implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt != null && evt.getPropertyName() != null) {
                if (evt.getPropertyName().equals(RPFTiledImageProcessor.BEGIN_SUB_TASK)) {
                    beginTask();
                } else if (evt.getPropertyName().equals(RPFTiledImageProcessor.END_SUB_TASK)) {
                    endTask();
                } else if (evt.getPropertyName().equals(RPFTiledImageProcessor.SUB_TASK_NUM_STEPS)) {
                    stepsForTask((Integer) evt.getNewValue());
                } else if (evt.getPropertyName().equals(RPFTiledImageProcessor.SUB_TASK_STEP_COMPLETE)) {
                    stepComplete(evt.getNewValue().toString(), true);
                } else if (evt.getPropertyName().equals(RPFTiledImageProcessor.SUB_TASK_STEP_FAILED)) {
                    stepComplete(evt.getNewValue().toString(), false);
                }
            }
        }
    }

    private void beginTask()
    {
        this.stepsTaken.set(0);
        this.stepsWithErrors.set(0);
        this.etrCalc.setStartTime(System.currentTimeMillis());
    }

    private void endTask()
    {
        this.panelComponent.setProgressDescription1(" ");
        this.panelComponent.setProgressDescription2(" ");
    }

    private void stepsForTask(int numSteps)
    {
        this.numSteps = numSteps;
    }

    private void stepComplete(String description, boolean success)
    {
        int n = this.stepsTaken.incrementAndGet();
        if (!success)
            this.stepsWithErrors.incrementAndGet();

        int numFiles = this.numSteps;
        this.etrCalc.setStep(n);
        this.etrCalc.setNumSteps(numFiles);
        long etr = this.etrCalc.getEstimatedTimeRemaining();

        StringBuilder sb = new StringBuilder();
        sb.append(description);
        int nErrors = this.stepsWithErrors.get();
        if (nErrors > 0)
        {
            if (sb.length() > 0)
                sb.append("; ");
            sb.append(formatFileCount(nErrors)).append(" with errors");
        }
        setProgressMessage(sb.toString());
        setProgress(n, numFiles, etr);
    }

    private void setProgress(int progressValue, int progressRange, long remainingMillis)
    {
        if (progressValue >= 0 && progressValue < progressRange)
        {
            this.panelComponent.getProgressBar().setValue(progressValue);
            this.panelComponent.getProgressBar().setMaximum(progressRange);
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%,d of %,d", progressValue, progressRange));
            if (remainingMillis > 0)
            {
                TimeFormatter tf = new TimeFormatter();
                if (sb.length() > 0)
                    sb.append(" - ");
                sb.append(tf.formatEstimate(remainingMillis));
            }
            this.panelComponent.setProgressDescription2(sb.toString());
        }
        else
        {
            this.panelComponent.getProgressBar().setValue(0);
            this.panelComponent.getProgressBar().setMaximum(0);
            this.panelComponent.setProgressDescription2(" ");
        }
    }

    private void setProgressMessage(String message)
    {
        this.panelComponent.setProgressDescription1(message);
    }

    private String formatFileCount(int n)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%,d", n));
        sb.append(" file");
        if (n != 1)
            sb.append("s");
        return sb.toString();
    }

    private String makeDescription(FileSet set, int value, int max)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Importing ");
        if (set != null && set.getTitle() != null)
        {
            sb.append("\'");
            sb.append(set.getTitle());
            sb.append("\'");
        }
        if (max > 1)
        {
            sb.append(" (").append(value).append(" of ").append(max).append(")");
        }
        return sb.toString();
    }

    private String makeSubStepDescription(String description, String subDescription)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<br>");
        sb.append(description);
        sb.append("<br><br><br>");
        sb.append(subDescription);
        return sb.toString();
    }

    private String makeFinishedDescription()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<br>");
        sb.append("<font size=\"+1\">");
        sb.append("Import Imagery Complete");
        sb.append("</font>");
        sb.append("<br><br>");

        WizardModel model = getWizardModel();
        Iterable<FileSet> fileSetList = RPFWizardUtil.getFileSetList(model);
        if (fileSetList != null)
        {
            for (FileSet set : fileSetList)
            {
                if (set != null && set.isSelected())
                {
                    sb.append("<b>");
                    sb.append(set.getTitle());
                    sb.append("</b>");

                    Integer filesProcessed = set.getIntegerProperty("filesProcessed");
                    Integer filesWithErrors = set.getIntegerProperty("filesWithErrors");
                    if (filesProcessed != null && filesWithErrors != null)
                    {
                        int numFilesOk = filesProcessed - filesWithErrors;
                        sb.append("<br>");
                        sb.append("<font size=\"-2\">");
                        sb.append(formatFileCount(numFilesOk)).append(" imported");
                        if (filesWithErrors > 0)
                        {
                            sb.append("; ");
                            sb.append("<font color=#990000>");
                            sb.append(formatFileCount(filesWithErrors)).append(" with errors");
                            sb.append("</font>");
                        }
                        sb.append("</font>");
                    }
                    sb.append("<br><br>");
                }
            }
        }

        sb.append("</html>");
        return sb.toString();
    }

    private void startWorkerThread(Runnable runnable)
    {
        killWorkerThread();
        this.workerThread = new Thread(runnable);
        this.workerThread.start();
    }

    private void killWorkerThread()
    {
        if (this.workerThread != null && this.workerThread.isAlive())
            this.workerThread.interrupt();
        this.workerThread = null;
    }
}
