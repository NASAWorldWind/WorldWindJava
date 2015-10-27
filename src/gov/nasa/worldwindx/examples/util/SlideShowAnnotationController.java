/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples.util;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.*;
import gov.nasa.worldwind.util.*;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.IOException;
import java.net.URL;

/**
 * @author dcollins
 * @version $Id: SlideShowAnnotationController.java 1171 2013-02-11 21:45:02Z dcollins $
 */
@SuppressWarnings( {"TypeParameterExplicitlyExtendsObject"})
public class SlideShowAnnotationController extends DialogAnnotationController
{
    public static final String BUFFERED_IMAGE_CACHE_SIZE = "gov.nasa.worldwind.avkey.BufferedImageCacheSize";
    public static final String BUFFERED_IMAGE_CACHE_NAME = java.awt.image.BufferedImage.class.getName();

    protected static final long SLIDESHOW_UPDATE_DELAY_MILLIS = 2000;
    protected static final long DEFAULT_BUFFERED_IMAGE_CACHE_SIZE = 30000000;
    protected static java.awt.Dimension SMALL_IMAGE_PREFERRED_SIZE = new java.awt.Dimension(320, 240);
    protected static java.awt.Dimension LARGE_IMAGE_PREFERRED_SIZE = new java.awt.Dimension(600, 450);

    protected int index;
    protected String state;
    protected java.util.List<Object> imageSources;
    // Concurrent task components.
    protected Thread readThread;
    protected javax.swing.Timer updateTimer;

    public SlideShowAnnotationController(WorldWindow worldWindow, SlideShowAnnotation annotation,
        Iterable<?> imageSources)
    {
        super(worldWindow, annotation);

        this.state = AVKey.STOP;
        this.index = -1;
        this.imageSources = new java.util.ArrayList<Object>();

        if (imageSources != null)
        {
            for (Object source : imageSources)
            {
                if (source != null)
                    this.imageSources.add(source);
            }
        }

        if (!WorldWind.getMemoryCacheSet().containsCache(BUFFERED_IMAGE_CACHE_NAME))
        {
            long size = Configuration.getLongValue(BUFFERED_IMAGE_CACHE_SIZE, DEFAULT_BUFFERED_IMAGE_CACHE_SIZE);
            MemoryCache cache = new BasicMemoryCache((long) (0.85 * size), size);
            WorldWind.getMemoryCacheSet().addCache(BUFFERED_IMAGE_CACHE_NAME, cache);
        }

        this.initializeSlideShow();
    }

    public SlideShowAnnotationController(WorldWindow worldWindow, SlideShowAnnotation annotation)
    {
        this(worldWindow, annotation, null);
    }

    protected void initializeSlideShow()
    {
        SlideShowAnnotation annotation = (SlideShowAnnotation) this.getAnnotation();

        // Set the image preferred size.
        this.setPreferredImageSize(SMALL_IMAGE_PREFERRED_SIZE);

        if (this.imageSources.size() <= 1)
        {
            annotation.getPlayButton().getAttributes().setVisible(false);
            annotation.getPreviousButton().getAttributes().setVisible(false);
            annotation.getNextButton().getAttributes().setVisible(false);
            annotation.getBeginButton().getAttributes().setVisible(false);
            annotation.getEndButton().getAttributes().setVisible(false);
        }

        if (!this.imageSources.isEmpty())
        {
            // Load the first image.
            this.doGoToImage(0);
        }
    }

    public java.util.List<? extends Object> getImageSources()
    {
        return java.util.Collections.unmodifiableList(this.imageSources);
    }

    public void setImageSources(Iterable<? extends Object> imageSources)
    {
        this.imageSources.clear();

        if (imageSources != null)
        {
            for (Object source : imageSources)
            {
                if (source != null)
                    this.imageSources.add(source);
            }
        }
    }

    public String getState()
    {
        return this.state;
    }

    public int getIndex()
    {
        return this.index;
    }

    @SuppressWarnings( {"StringEquality"})
    public void goToImage(int index)
    {
        if (this.getAnnotation() == null)
            return;

        if (this.getState() == AVKey.PLAY)
        {
            this.stopSlideShow();
        }

        this.doGoToImage(index);
    }

    @SuppressWarnings( {"StringEquality"})
    public void startSlideShow()
    {
        if (this.getAnnotation() == null)
            return;

        if (this.hasNextIndex() && this.getState() == AVKey.STOP)
        {
            this.state = AVKey.PLAY;
            SlideShowAnnotation slideShowAnnotation = (SlideShowAnnotation) this.getAnnotation();
            slideShowAnnotation.setPlayButtonState(AVKey.PAUSE);
            this.startSlideShowUpdate();
        }
    }

    @SuppressWarnings( {"StringEquality"})
    public void stopSlideShow()
    {
        if (this.getAnnotation() == null)
            return;

        if (this.getState() == AVKey.PLAY)
        {
            this.state = AVKey.STOP;
            SlideShowAnnotation slideShowAnnotation = (SlideShowAnnotation) this.getAnnotation();
            slideShowAnnotation.setPlayButtonState(AVKey.PLAY);
            this.stopSlideShowUpdate();
        }
    }

    public void stopRetrievalTasks()
    {
        this.stopImageRetrieval();
    }

    public java.awt.Dimension getPreferredImageSize()
    {
        if (this.getAnnotation() == null)
            return null;

        SlideShowAnnotation slideShowAnnotation = (SlideShowAnnotation) this.getAnnotation();
        return slideShowAnnotation.getImageAnnotation().getAttributes().getSize();
    }

    public void setPreferredImageSize(java.awt.Dimension size)
    {
        if (this.getAnnotation() == null)
            return;

        SlideShowAnnotation slideShowAnnotation = (SlideShowAnnotation) this.getAnnotation();
        slideShowAnnotation.getImageAnnotation().getAttributes().setSize(size);
    }

    protected boolean hasPreviousIndex()
    {
        // The slide show loops, so there's always a previous index.
        return true;
    }

    protected boolean hasNextIndex()
    {
        // The slide show loops, so there's always a next index.
        return true;
    }

    protected int getPreviousIndex()
    {
        int maxIndex = this.imageSources.size() - 1;
        return (this.index > 0) ? (this.index - 1) : maxIndex;
    }

    protected int getNextIndex()
    {
        int maxIndex = this.imageSources.size() - 1;
        return (this.index < maxIndex) ? (this.index + 1) : 0;
    }

    protected void doGoToImage(int index)
    {
        int maxIndex = this.imageSources.size() - 1;
        if (index < 0 || index > maxIndex)
            return;

        if (index == this.index)
            return;

        this.retrieveAndSetImage(this.imageSources.get(index), index);
    }

    protected void doSetImage(PowerOfTwoPaddedImage image, int index)
    {
        int length = this.imageSources.size();
        Object imageSource = this.imageSources.get(index);
        String title = this.createTitle(imageSource);
        String positionText = this.createPositionText(index, length);

        this.index = index;
        SlideShowAnnotation slideShowAnnotation = (SlideShowAnnotation) this.getAnnotation();
        slideShowAnnotation.getTitleLabel().setText(title);
        slideShowAnnotation.getPositionLabel().setText(positionText);
        slideShowAnnotation.getImageAnnotation().setImageSource(image.getPowerOfTwoImage(),
            image.getOriginalWidth(), image.getOriginalHeight());

        // Update next and previous button states.
        slideShowAnnotation.getBeginButton().setEnabled(this.hasPreviousIndex());
        slideShowAnnotation.getPreviousButton().setEnabled(this.hasPreviousIndex());
        slideShowAnnotation.getNextButton().setEnabled(this.hasNextIndex());
        slideShowAnnotation.getEndButton().setEnabled(this.hasNextIndex());

        this.getWorldWindow().redraw();
    }

    //**************************************************************//
    //********************  Action Listener  ***********************//
    //**************************************************************//

    @SuppressWarnings( {"StringEquality"})
    public void onActionPerformed(java.awt.event.ActionEvent e)
    {
        super.onActionPerformed(e);

        if (e.getActionCommand() == AVKey.PLAY)
        {
            this.playPressed(e);
        }
        else if (e.getActionCommand() == AVKey.PREVIOUS)
        {
            this.previousPressed(e);
        }
        else if (e.getActionCommand() == AVKey.NEXT)
        {
            this.nextPressed(e);
        }
        else if (e.getActionCommand() == AVKey.BEGIN)
        {
            this.beginPressed(e);
        }
        else if (e.getActionCommand() == AVKey.END)
        {
            this.endPressed(e);
        }
        else if (e.getActionCommand() == AVKey.RESIZE)
        {
            this.resizePressed(e);
        }
    }

    protected void playPressed(java.awt.event.ActionEvent e)
    {
        if (e == null)
            return;

        if (this.getAnnotation() == null)
            return;

        this.onPlayPressed(e);
    }

    protected void previousPressed(java.awt.event.ActionEvent e)
    {
        if (e == null)
            return;

        if (this.getAnnotation() == null)
            return;

        this.onPreviousPressed(e);
    }

    protected void nextPressed(java.awt.event.ActionEvent e)
    {
        if (e == null)
            return;

        if (this.getAnnotation() == null)
            return;

        this.onNextPressed(e);
    }

    protected void beginPressed(java.awt.event.ActionEvent e)
    {
        if (e == null)
            return;

        if (this.getAnnotation() == null)
            return;

        this.onBeginPressed(e);
    }

    protected void endPressed(java.awt.event.ActionEvent e)
    {
        if (e == null)
            return;

        if (this.getAnnotation() == null)
            return;

        this.onEndPressed(e);
    }

    protected void resizePressed(java.awt.event.ActionEvent e)
    {
        if (e == null)
            return;

        if (this.getAnnotation() == null)
            return;

        this.onResizePressed(e);
    }

    @SuppressWarnings( {"UnusedDeclaration", "StringEquality"})
    protected void onPlayPressed(java.awt.event.ActionEvent e)
    {
        String state = this.getState();
        if (state == null)
            return;

        if (state == AVKey.PLAY)
        {
            this.stopSlideShow();
        }
        else if (state == AVKey.STOP)
        {
            this.startSlideShow();
        }
    }

    @SuppressWarnings( {"UnusedDeclaration"})
    protected void onPreviousPressed(java.awt.event.ActionEvent e)
    {
        if (!this.hasPreviousIndex())
            return;

        int newIndex = this.getPreviousIndex();
        this.goToImage(newIndex);
    }

    @SuppressWarnings( {"UnusedDeclaration"})
    protected void onNextPressed(java.awt.event.ActionEvent e)
    {
        if (!this.hasNextIndex())
            return;

        int newIndex = this.getNextIndex();
        this.goToImage(newIndex);
    }

    @SuppressWarnings( {"UnusedDeclaration"})
    protected void onBeginPressed(java.awt.event.ActionEvent e)
    {
        this.goToImage(0);
    }

    @SuppressWarnings( {"UnusedDeclaration"})
    protected void onEndPressed(java.awt.event.ActionEvent e)
    {
        int maxIndex = this.imageSources.size() - 1;
        if (maxIndex < 0)
            return;

        this.goToImage(maxIndex);
    }

    @SuppressWarnings( {"UnusedDeclaration"})
    protected void onResizePressed(java.awt.event.ActionEvent e)
    {
        if (this.getAnnotation() == null)
            return;

        java.awt.Dimension preferredSize = this.getPreferredImageSize();
        if (preferredSize.equals(SMALL_IMAGE_PREFERRED_SIZE))
        {
            this.setPreferredImageSize(LARGE_IMAGE_PREFERRED_SIZE);
            SlideShowAnnotation slideShowAnnotation = (SlideShowAnnotation) this.getAnnotation();
            slideShowAnnotation.setSizeButtonState(SlideShowAnnotation.DECREASE);
        }
        else
        {
            this.setPreferredImageSize(SMALL_IMAGE_PREFERRED_SIZE);
            SlideShowAnnotation slideShowAnnotation = (SlideShowAnnotation) this.getAnnotation();
            slideShowAnnotation.setSizeButtonState(SlideShowAnnotation.INCREASE);
        }
    }

    //**************************************************************//
    //********************  Image Load Thread  *********************//
    //**************************************************************//

    protected void retrieveAndSetImage(Object source, int index)
    {
        PowerOfTwoPaddedImage image = this.getImage(source);
        if (image != null)
        {
            this.doSetImage(image, index);
            return;
        }

        this.startImageRetrieval(source, index);
    }

    protected void doRetrieveAndSetImage(Object source, final int index)
    {
        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                if (updateTimer != null)
                {
                    updateTimer.stop();
                }

                getAnnotation().setBusy(true);
                getWorldWindow().redraw();
            }
        });

        final PowerOfTwoPaddedImage image = this.readImage(source);
        this.putImage(source, image);

        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                doSetImage(image, index);

                getAnnotation().setBusy(false);
                getWorldWindow().redraw();

                if (updateTimer != null)
                {
                    updateTimer.start();
                }
            }
        });
    }

    protected PowerOfTwoPaddedImage readImage(Object source)
    {
        try
        {
            if (source instanceof BufferedImage)
            {
                return PowerOfTwoPaddedImage.fromBufferedImage((BufferedImage) source);
            }
            else if (source instanceof String)
            {
                return PowerOfTwoPaddedImage.fromPath((String) source);
            }
            else if (source instanceof URL)
            {
                return PowerOfTwoPaddedImage.fromBufferedImage(ImageIO.read((URL) source));
            }
            else
            {
                String message = Logging.getMessage("generic.UnrecognizedSourceType", source);
                Logging.logger().severe(message);
            }
        }
        catch (IOException e)
        {
            String message = Logging.getMessage("generic.ExceptionAttemptingToReadFrom", source);
            Logging.logger().severe(message);
        }

        return null;
    }

    protected void startImageRetrieval(final Object source, final int index)
    {
        this.readThread = new Thread(new Runnable()
        {
            public void run()
            {
                doRetrieveAndSetImage(source, index);
            }
        });
        this.readThread.start();
    }

    protected void stopImageRetrieval()
    {
        if (this.readThread != null)
        {
            if (this.readThread.isAlive())
            {
                this.readThread.interrupt();
            }
        }

        this.readThread = null;
    }

    protected PowerOfTwoPaddedImage getImage(Object source)
    {
        return (PowerOfTwoPaddedImage) WorldWind.getMemoryCache(BUFFERED_IMAGE_CACHE_NAME).getObject(source);
    }

    protected boolean putImage(Object source, PowerOfTwoPaddedImage image)
    {
        long sizeInBytes = ImageUtil.computeSizeInBytes(image.getPowerOfTwoImage());
        MemoryCache cache = WorldWind.getMemoryCache(BUFFERED_IMAGE_CACHE_NAME);
        boolean addToCache = (sizeInBytes < cache.getCapacity());

        // If the image is too large for the cache, then do not add it to the cache.
        if (addToCache)
        {
            cache.add(source, image, sizeInBytes);
        }

        return addToCache;
    }

    //**************************************************************//
    //********************  Slideshow Update Timer  *******************//
    //**************************************************************//

    protected boolean nextSlideShowImage()
    {
        if (this.getAnnotation() == null)
            return false;

        if (this.hasNextIndex())
        {
            int newIndex = this.getNextIndex();
            this.doGoToImage(newIndex);
        }

        return this.hasNextIndex();
    }

    protected void onSlideShowUpdate()
    {
        if (!this.nextSlideShowImage())
        {
            this.stopSlideShow();
        }
    }

    protected void startSlideShowUpdate()
    {
        this.updateTimer = new javax.swing.Timer((int) SLIDESHOW_UPDATE_DELAY_MILLIS,
            new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent actionEvent)
                {
                    onSlideShowUpdate();
                }
            });
        // Coalesce timer events, so that an image load delay on the timer thread does not cause slide transition
        // events to bunch up.
        this.updateTimer.setCoalesce(true);
        this.updateTimer.start();
    }

    protected void stopSlideShowUpdate()
    {
        if (this.updateTimer != null)
        {
            this.updateTimer.stop();
        }

        this.updateTimer = null;
    }

    //**************************************************************//
    //********************  Utilities  *****************************//
    //**************************************************************//

    protected String createTitle(Object imageSource)
    {
        String imageName = this.getImageName(imageSource);
        return (imageName != null) ? imageName : "";
    }

    protected String createPositionText(int position, int length)
    {
        if (length <= 1)
            return "";

        StringBuilder sb = new StringBuilder();
        sb.append(position + 1).append(" of ").append(length);
        return sb.toString();
    }

    protected String getImageName(Object imageSource)
    {
        if (imageSource == null)
            return null;

        String s = imageSource.toString();
        s = WWIO.stripTrailingSeparator(s);

        int index = s.lastIndexOf("/");
        if (index == -1)
            index = s.lastIndexOf("\\");

        if (index != -1 && index < s.length() - 1)
        {
            s = s.substring(index + 1, s.length());
        }

        return s;
    }
}
