/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.util.cachecleaner;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.cache.*;
import gov.nasa.worldwindx.examples.util.FileStoreDataSet;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * The DataCacheViewer is a tool that allows the user to view and delete cached WorldWind files based on how old they
 * are.  The utility shows the various directories in the cache root, how large they each are, when they were last used,
 * and how many files exist in them that are older than a day, week, month or year. It also allows the user to delete
 * all files older than a specified number of days, weeks, months or years.
 *
 * @author tag
 * @version $Id: DataCacheViewer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
@SuppressWarnings("unchecked")
public class DataCacheViewer
{
    protected JPanel panel;
    protected CacheTable table;
    protected JButton delBtn;
    protected JSpinner ageSpinner;
    protected JComboBox ageUnit;
    protected JLabel deleteSizeLabel;

    public DataCacheViewer(File cacheRoot)
    {
        this.panel = new JPanel(new BorderLayout(5, 5));

        JLabel rootLabel = new JLabel("Cache Root: " + cacheRoot.getPath());
        rootLabel.setBorder(new EmptyBorder(10, 15, 10, 10));
        this.panel.add(rootLabel, BorderLayout.NORTH);

        this.table = new CacheTable();
        this.table.setDataSets(cacheRoot.getPath(), FileStoreDataSet.getDataSets(cacheRoot));
        JScrollPane sp = new JScrollPane(table);
        this.panel.add(sp, BorderLayout.CENTER);

        JPanel pa = new JPanel(new BorderLayout(10, 10));
        pa.add(new JLabel("Delete selected data older than"), BorderLayout.WEST);
        this.ageSpinner = new JSpinner(new SpinnerNumberModel(6, 0, 10000, 1));
        this.ageSpinner.setToolTipText("0 selects the entire dataset regardless of age");
        JPanel pas = new JPanel();
        pas.add(this.ageSpinner);
        pa.add(pas, BorderLayout.CENTER);
        this.ageUnit = new JComboBox(new String[] {"Hours", "Days", "Weeks", "Months", "Years"});
        this.ageUnit.setSelectedItem("Months");
        this.ageUnit.setEditable(false);
        pa.add(this.ageUnit, BorderLayout.EAST);

        JPanel pb = new JPanel(new BorderLayout(5, 10));
        this.deleteSizeLabel = new JLabel("Total to delete: 0 MB");
        pb.add(this.deleteSizeLabel, BorderLayout.WEST);
        this.delBtn = new JButton("Delete");
        this.delBtn.setEnabled(false);
        JButton quitButton = new JButton("Quit");
        JPanel pbb = new JPanel();
        pbb.add(this.delBtn);
        pb.add(pbb, BorderLayout.CENTER);
        pbb.add(quitButton);

        JPanel pc = new JPanel(new BorderLayout(5, 10));
        pc.add(pa, BorderLayout.WEST);
        pc.add(pb, BorderLayout.EAST);

        JPanel ctlPanel = new JPanel(new BorderLayout(10, 10));
        ctlPanel.setBorder(new EmptyBorder(10, 10, 20, 10));
        ctlPanel.add(pc, BorderLayout.CENTER);

        this.panel.add(ctlPanel, BorderLayout.SOUTH);

        this.ageUnit.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                update();
            }
        });

        this.ageSpinner.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                update();
            }
        });

        this.table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                update();
            }
        });

        this.delBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                panel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                Thread t = new Thread(new Runnable()
                {
                    public void run()
                    {
                        try
                        {
                            List<FileStoreDataSet> dataSets = table.getSelectedDataSets();
                            int age = Integer.parseInt(ageSpinner.getValue().toString());
                            String unit = getUnitKey();

                            for (FileStoreDataSet ds : dataSets)
                            {
                                ds.deleteOutOfScopeFiles(unit, age, false);
                                if (ds.getSize() == 0)
                                {
                                    table.deleteDataSet(ds);
                                    ds.delete(false);
                                }
                            }
                        }
                        finally
                        {
                            update();
                            SwingUtilities.invokeLater(new Runnable()
                            {
                                public void run()
                                {
                                    panel.setCursor(Cursor.getDefaultCursor());
                                }
                            });
                        }
                    }
                });
                t.start();
            }
        });

        quitButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                System.exit(0);
            }
        });
    }

    protected void update()
    {
        java.util.List<FileStoreDataSet> dataSets = this.table.getSelectedDataSets();
        int age = Integer.parseInt(this.ageSpinner.getValue().toString());

        if (dataSets.size() == 0)
        {
            this.deleteSizeLabel.setText("Total to delete: 0 MB");
            this.delBtn.setEnabled(false);
            return;
        }

        String unit = this.getUnitKey();

        long totalSize = 0;
        for (FileStoreDataSet ds : dataSets)
        {
            totalSize += ds.getOutOfScopeSize(unit, age);
        }

        Formatter formatter = new Formatter();
        formatter.format("%5.1f", ((float) totalSize) / 1e6);
        this.deleteSizeLabel.setText("Total to delete: " + formatter.toString() + " MB");

        this.delBtn.setEnabled(true);
    }

    protected String getUnitKey()
    {
        String unit = null;
        String unitString = (String) this.ageUnit.getSelectedItem();
        if (unitString.equals("Hours"))
            unit = FileStoreDataSet.HOUR;
        else if (unitString.equals("Days"))
            unit = FileStoreDataSet.DAY;
        else if (unitString.equals("Weeks"))
            unit = FileStoreDataSet.WEEK;
        else if (unitString.equals("Months"))
            unit = FileStoreDataSet.MONTH;
        else if (unitString.equals("Years"))
            unit = FileStoreDataSet.YEAR;

        return unit;
    }

    static
    {
        if (Configuration.isMacOS())
        {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "WorldWind Cache Cleaner");
        }
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                JFrame frame = new JFrame();
                frame.setPreferredSize(new Dimension(800, 300));
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                FileStore store = new BasicDataFileStore();
                File cacheRoot = store.getWriteLocation();
                DataCacheViewer viewerPanel = new DataCacheViewer(cacheRoot);
                frame.getContentPane().add(viewerPanel.panel, BorderLayout.CENTER);
                frame.pack();

                // Center the application on the screen.
                Dimension prefSize = frame.getPreferredSize();
                Dimension parentSize;
                java.awt.Point parentLocation = new java.awt.Point(0, 0);
                parentSize = Toolkit.getDefaultToolkit().getScreenSize();
                int x = parentLocation.x + (parentSize.width - prefSize.width) / 2;
                int y = parentLocation.y + (parentSize.height - prefSize.height) / 2;
                frame.setLocation(x, y);
                frame.setVisible(true);
            }
        });
    }
}
