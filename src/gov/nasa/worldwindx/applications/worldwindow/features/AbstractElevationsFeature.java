/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.features;

import gov.nasa.worldwind.Disposable;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import gov.nasa.worldwind.wms.CapabilitiesRequest;
import gov.nasa.worldwindx.applications.worldwindow.core.*;
import gov.nasa.worldwindx.applications.worldwindow.util.Util;

import javax.swing.*;
import java.net.*;
import java.util.*;

/**
 * @author tag
 * @version $Id: AbstractElevationsFeature.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class AbstractElevationsFeature extends AbstractFeature implements NetworkActivitySignal.NetworkUser
{
    protected boolean on;
    protected List<ElevationModel> elevationModels;
    protected Thread createModelsThread;

    protected abstract void doCreateModels();

    protected AbstractElevationsFeature(String name, String featureID, String largeIconPath, Registry registry)
    {
        super(name, featureID, largeIconPath, registry);
    }

    @Override
    public void initialize(Controller controller)
    {
        super.initialize(controller);

        this.addToToolBar();
    }

    public boolean hasNetworkActivity()
    {
        return this.createModelsThread != null && this.createModelsThread.isAlive();
    }

    @Override
    public boolean isOn()
    {
        return this.on;
    }

    protected void setOn(boolean tf)
    {
        this.on = tf;
    }

    @Override
    public void turnOn(boolean tf)
    {
        if (tf == this.isOn())
            return;

        if (tf)
        {
            if (this.getElevationModels().size() == 0)
                this.createModels(); // also adds them to the layer manager
            else
                this.addModels(this.getElevationModels());
        }
        else
            this.removeModels();

        this.setOn(tf);
        this.controller.redraw();
    }

    public List<ElevationModel> getElevationModels()
    {
        return this.elevationModels != null ? this.elevationModels : new ArrayList<ElevationModel>();
    }

    protected void handleInterrupt()
    {
        if (Thread.currentThread().isInterrupted() && this.elevationModels != null)
        {
            Util.getLogger().info("Data retrieval cancelled");

            // Clean up so the user can try again later
            this.destroyElevationModels();
        }
    }

    protected void destroyElevationModels()
    {
        this.killPopulateLayerThread();

        if (this.elevationModels == null)
            return;

        for (ElevationModel em : this.elevationModels)
        {
            this.destroyElevationModel(em);
        }

        this.elevationModels.clear();
        this.elevationModels = null;
    }

    protected void destroyElevationModel(ElevationModel em)
    {
        this.removeModel(em);

        if (em instanceof Disposable)
            ((Disposable) em).dispose();
    }

    protected void removeModels()
    {
        for (ElevationModel em : this.getElevationModels())
        {
            this.removeModel(em);
        }
    }

    protected void addModels(List<ElevationModel> models)
    {
        for (ElevationModel em : models)
        {
            this.addModel(em);
        }
    }

    protected void removeModel(ElevationModel em)
    {
        if (em == null)
            return;

        ElevationModel parentModel = this.controller.getWWd().getModel().getGlobe().getElevationModel();

        if (parentModel instanceof CompoundElevationModel)
            ((CompoundElevationModel) parentModel).removeElevationModel(em);
    }

    protected void killPopulateLayerThread()
    {
        if (this.createModelsThread != null && this.createModelsThread.isAlive())
        {
            this.createModelsThread.interrupt();
            this.controller.getNetworkActivitySignal().removeNetworkUser(this);
            this.createModelsThread = null;
        }
    }

    protected void createModels()
    {
        if (this.elevationModels == null)
            this.elevationModels = new ArrayList<ElevationModel>();

        this.createModelsThread = new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    doCreateModels();
                }
                finally
                {
                    handleInterrupt();
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            controller.getNetworkActivitySignal().removeNetworkUser(AbstractElevationsFeature.this);
                            createModelsThread = null;
                            controller.redraw();
                        }
                    });
                }
            }
        });
        this.createModelsThread.setPriority(Thread.MIN_PRIORITY);
        this.createModelsThread.start();

        this.controller.getNetworkActivitySignal().addNetworkUser(AbstractElevationsFeature.this);
    }

    protected void addModel(ElevationModel em)
    {
        this.removeModel(em);
        this.doAddModel(em);

        if (this.elevationModels == null)
            this.elevationModels = new ArrayList<ElevationModel>();

        if (!this.getElevationModels().contains(em))
            this.getElevationModels().add(em);
    }

    protected void doAddModel(ElevationModel em)
    {
        ElevationModel globeEM = this.controller.getWWd().getModel().getGlobe().getElevationModel();
        if (!(globeEM instanceof CompoundElevationModel))
        {
            CompoundElevationModel cem = new CompoundElevationModel();
            cem.addElevationModel(globeEM);
            globeEM = cem;
            this.controller.getWWd().getModel().getGlobe().setElevationModel(globeEM);
        }

        ((CompoundElevationModel) globeEM).addElevationModel(em);
    }

    protected WMSCapabilities retrieveCapsDoc(String urlString)
    {
        try
        {
            CapabilitiesRequest request = new CapabilitiesRequest(new URI(urlString));

            return new WMSCapabilities(request);
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
