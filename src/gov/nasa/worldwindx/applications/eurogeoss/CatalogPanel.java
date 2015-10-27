/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.eurogeoss;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.*;
import java.util.logging.Level;

/**
 * @author dcollins
 * @version $Id: CatalogPanel.java 1586 2013-09-06 18:03:47Z dcollins $
 */
public class CatalogPanel extends JPanel implements ActionListener
{
    protected static final String SEARCH_BUTTON_TEXT = "Search";
    protected static final String CANCEL_BUTTON_TEXT = "Cancel";
    protected static final String MORE_BUTTON_TEXT = "More";

    protected String serviceUrl;
    protected WorldWindow wwd;
    protected ExecutorService searchService;
    protected Future searchFuture;
    protected GetRecordsRequest lastRequest;
    protected GetRecordsResponse lastResponse;

    protected JPanel searchPanel;
    protected JTextField searchField;
    protected JProgressBar searchProgress;
    protected JButton searchButton;
    protected JPanel recordsPanel;
    protected JPanel recordsScrollPanel;
    protected JScrollPane recordsScrollPane;
    protected JPanel morePanel;
    protected JButton moreButton;

    public CatalogPanel(String serviceUrl, WorldWindow wwd)
    {
        if (serviceUrl == null)
        {
            String msg = Logging.getMessage("nullValue.ServiceIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (wwd == null)
        {
            String msg = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.serviceUrl = serviceUrl;
        this.wwd = wwd;
        this.searchService = Executors.newSingleThreadExecutor();

        this.searchPanel = new JPanel();
        this.searchField = new JTextField();
        this.searchField.addActionListener(this);
        this.searchProgress = new JProgressBar();
        this.searchProgress.setIndeterminate(true);
        this.searchButton = new JButton(SEARCH_BUTTON_TEXT);
        this.searchButton.addActionListener(this);
        this.recordsPanel = new JPanel();
        this.recordsScrollPanel = new JPanel();
        this.recordsScrollPane = new JScrollPane(this.recordsScrollPanel);
        this.morePanel = new JPanel();
        this.morePanel.setVisible(false);
        this.moreButton = new JButton(MORE_BUTTON_TEXT);
        this.moreButton.addActionListener(this);

        this.searchPanel.setLayout(new BoxLayout(this.searchPanel, BoxLayout.X_AXIS));
        this.searchPanel.add(this.searchField);
        this.searchPanel.add(this.searchProgress);
        this.searchPanel.add(Box.createHorizontalStrut(5));
        this.searchPanel.add(this.searchButton);
        this.searchProgress.setVisible(false);
        this.recordsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // top, left, bottom, right
        this.recordsPanel.setLayout(new GridLayout(0, 1, 0, 5)); // nrows, ncols, hgap, vgap
        this.recordsScrollPane.setBorder(BorderFactory.createEmptyBorder()); // suppress the scroll pane default border
        this.recordsScrollPanel.setLayout(new BorderLayout(0, 10)); // hgap, vgap
        this.recordsScrollPanel.add(BorderLayout.NORTH, this.recordsPanel);
        this.recordsScrollPanel.add(BorderLayout.CENTER, this.morePanel);
        this.morePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.morePanel.add(this.moreButton);
        this.setPreferredSize(new Dimension(500, 0));
        this.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9)); // top, left, bottom, right
        this.setLayout(new BorderLayout(0, 20)); // hgap, vgap
        this.add(BorderLayout.NORTH, this.searchPanel);
        this.add(BorderLayout.CENTER, this.recordsScrollPane);
        this.validate();
    }

    public String getServiceUrl()
    {
        return this.serviceUrl;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {
        if ((actionEvent.getSource() == this.searchButton || actionEvent.getSource() == this.searchField)
            && this.searchButton.getText().equals(SEARCH_BUTTON_TEXT))
        {
            GetRecordsRequest request = new GetRecordsRequest();
            request.setSearchText(this.searchField.getText().trim());
            this.sendGetRecordsRequest(request, false); // overwrite existing records
        }
        else if (actionEvent.getSource() == this.searchButton && this.searchButton.getText().equals(CANCEL_BUTTON_TEXT))
        {
            this.searchFuture.cancel(true); // Cancel the search task, interrupting it if it's already running.
        }
        else if (actionEvent.getSource() == this.moreButton)
        {
            GetRecordsRequest nextRequest = new GetRecordsRequest(this.lastRequest);
            nextRequest.setStartPosition(this.lastResponse.getNextRecord());
            this.sendGetRecordsRequest(nextRequest, true); // append to existing records
        }
    }

    protected void sendGetRecordsRequest(GetRecordsRequest request, boolean append)
    {
        this.lastRequest = request;

        this.searchField.setVisible(false);
        this.searchProgress.setVisible(true);
        this.searchButton.setText(CANCEL_BUTTON_TEXT);
        this.moreButton.setEnabled(false);
        this.validate();

        GetRecordsTask task = new GetRecordsTask(request, append);
        this.searchFuture = this.searchService.submit(task);

        this.searchService.submit(new Runnable()
        {
            @Override
            public void run()
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        searchField.setVisible(true);
                        searchProgress.setVisible(false);
                        searchButton.setText(SEARCH_BUTTON_TEXT);
                        moreButton.setEnabled(true);
                        validate();
                    }
                });
            }
        });
    }

    protected void handleGetRecordsResponse(GetRecordsResponse response, boolean append)
    {
        this.lastResponse = response;

        if (!append)
        {
            this.recordsPanel.removeAll();
        }

        if (response.getRecords().size() > 0)
        {
            for (Record record : response.getRecords())
            {
                this.recordsPanel.add(new RecordPanel(record, this.wwd));
            }
        }
        else
        {
            JOptionPane.showMessageDialog(CatalogPanel.this, "No results", null,
                JOptionPane.INFORMATION_MESSAGE);
        }

        this.morePanel.setVisible(response.nextRecord != 0); // 0 indicates all records have been returned.
        this.validate();
    }

    protected class GetRecordsTask implements Runnable
    {
        protected GetRecordsRequest request;
        protected boolean append;

        public GetRecordsTask(GetRecordsRequest request, boolean append)
        {
            this.request = request;
            this.append = append;
        }

        @Override
        public void run()
        {
            try
            {
                CatalogConnection conn = new CatalogConnection(serviceUrl);
                final GetRecordsResponse response = conn.getRecords(request);

                if (Thread.interrupted())
                    return;

                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        handleGetRecordsResponse(response, append);
                    }
                });
            }
            catch (Exception e)
            {
                Logging.logger().log(Level.SEVERE, "Unable to search catalog " + serviceUrl, e);

                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        JOptionPane.showMessageDialog(CatalogPanel.this, "Unable to search catalog", null,
                            JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        }
    }
}
