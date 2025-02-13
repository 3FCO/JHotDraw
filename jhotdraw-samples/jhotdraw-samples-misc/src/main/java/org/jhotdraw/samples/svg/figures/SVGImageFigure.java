/*
 * @(#)SVGImage.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.samples.svg.figures;

import dk.sdu.mmmi.featuretracer.lib.FeatureEntryPoint;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.figure.ImageHolderFigure;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;

import static org.jhotdraw.draw.AttributeKeys.TRANSFORM;

import org.jhotdraw.draw.event.TransformRestoreEdit;
import org.jhotdraw.draw.handle.Handle;
import org.jhotdraw.geom.GrowStroke;
import org.jhotdraw.samples.svg.SVGAttributeKeys;

import static org.jhotdraw.samples.svg.SVGAttributeKeys.*;

import org.jhotdraw.util.*;

/**
 * SVGImage.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class SVGImageFigure extends SVGAttributedFigure implements SVGFigure, ImageHolderFigure {

    private static final long serialVersionUID = 1L;
    /**
     * This rectangle describes the bounds into which we draw the image.
     */
    private Rectangle2D.Double rectangle;
    /**
     * This is used to perform faster drawing.
     */
    private transient Shape cachedTransformedShape;
    /**
     * This is used to perform faster hit testing.
     */
    private transient Shape cachedHitShape;
    /**
     * The image data. This can be null, if the image was created from a
     * BufferedImage.
     */
    private byte[] imageData;
    /**
     * The buffered image. This can be null, if we haven't yet parsed the
     * imageData.
     */
    private BufferedImage bufferedImage;

    /**
     * Creates a new instance.
     */
    public SVGImageFigure() {
        this(0, 0, 0, 0);
    }

    @FeatureEntryPoint("Image")
    public SVGImageFigure(double x, double y, double width, double height) {
        rectangle = new Rectangle2D.Double(x, y, width, height);
        SVGAttributeKeys.setDefaults(this);
        setConnectable(false);
    }

    // DRAWING
    @Override
    public void draw(Graphics2D g) {
        double opacity = get(OPACITY);
        opacity = Math.min(Math.max(0d, opacity), 1d);
        if (opacity != 0d) {
            Composite savedComposite = g.getComposite();
            if (opacity != 1d) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) opacity));
            }
            BufferedImage image = getBufferedImage();
            if (image != null) {
                if (get(TRANSFORM) != null) {
                    // FIXME - We should cache the transformed image.
                    //         Drawing a transformed image appears to be very slow.
                    Graphics2D gx = (Graphics2D) g.create();
                    // Use same rendering hints like parent graphics
                    gx.setRenderingHints(g.getRenderingHints());
                    gx.transform(get(TRANSFORM));
                    gx.drawImage(image, (int) rectangle.x, (int) rectangle.y, (int) rectangle.width, (int) rectangle.height, null);
                    gx.dispose();
                } else {
                    g.drawImage(image, (int) rectangle.x, (int) rectangle.y, (int) rectangle.width, (int) rectangle.height, null);
                }
            } else {
                Shape shape = getTransformedShape();
                g.setColor(Color.red);
                g.setStroke(new BasicStroke());
                g.draw(shape);
            }
            if (opacity != 1d) {
                g.setComposite(savedComposite);
            }
        }
    }

    @Override
    protected void drawFill(Graphics2D g) {
    }

    @Override
    protected void drawStroke(Graphics2D g) {
    }

    // SHAPE AND BOUNDS
    public double getX() {
        return rectangle.x;
    }

    public double getY() {
        return rectangle.y;
    }

    public double getWidth() {
        return rectangle.width;
    }

    public double getHeight() {
        return rectangle.height;
    }

    @Override
    public Rectangle2D.Double getBounds() {
        return (Rectangle2D.Double) rectangle.clone();
    }

    @Override
    public Rectangle2D.Double getDrawingArea() {
        Rectangle2D rx = getTransformedShape().getBounds2D();
        return (rx instanceof Rectangle2D.Double) ? (Rectangle2D.Double) rx : new Rectangle2D.Double(rx.getX(), rx.getY(), rx.getWidth(), rx.getHeight());
    }

    /**
     * Checks if a Point2D.Double is inside the figure.
     */
    @Override
    public boolean contains(Point2D.Double p) {
        return getHitShape().contains(p);
    }

    @Override
    public void setBounds(Point2D.Double anchor, Point2D.Double lead) {
        invalidateTransformedShape();
        rectangle.x = Math.min(anchor.x, lead.x);
        rectangle.y = Math.min(anchor.y, lead.y);
        rectangle.width = Math.max(0.1, Math.abs(lead.x - anchor.x));
        rectangle.height = Math.max(0.1, Math.abs(lead.y - anchor.y));
    }

    private void invalidateTransformedShape() {
        cachedTransformedShape = null;
        cachedHitShape = null;
    }

    private Shape getTransformedShape() {
        if (cachedTransformedShape == null) {
            cachedTransformedShape = (Shape) rectangle.clone();
            if (get(TRANSFORM) != null) {
                cachedTransformedShape = get(TRANSFORM).createTransformedShape(cachedTransformedShape);
            }
        }
        return cachedTransformedShape;
    }

    private Shape getHitShape() {
        if (cachedHitShape == null) {
            cachedHitShape = new GrowStroke(
                    (float) AttributeKeys.getStrokeTotalWidth(this, 1.0) / 2f,
                    (float) AttributeKeys.getStrokeTotalMiterLimit(this, 1.0)).createStrokedShape(getTransformedShape());
        }
        return cachedHitShape;
    }

    /**
     * Transforms the figure.
     *
     * @param tx The transformation.
     */
    @Override
    public void transform(AffineTransform tx) {
        invalidateTransformedShape();
        if (get(TRANSFORM) != null
                || (tx.getType() & (AffineTransform.TYPE_TRANSLATION | AffineTransform.TYPE_MASK_SCALE)) != tx.getType()) {
            if (get(TRANSFORM) == null) {
                set(TRANSFORM, (AffineTransform) tx.clone());
            } else {
                AffineTransform t = TRANSFORM.getClone(this);
                t.preConcatenate(tx);
                set(TRANSFORM, t);
            }
        } else {
            Point2D.Double anchor = getStartPoint();
            Point2D.Double lead = getEndPoint();
            setBounds(
                    (Point2D.Double) tx.transform(anchor, anchor),
                    (Point2D.Double) tx.transform(lead, lead));
        }
    }

    // ATTRIBUTES
    @Override
    public void restoreTransformTo(Object geometry) {
        invalidateTransformedShape();
        Object[] o = (Object[]) geometry;
        rectangle = (Rectangle2D.Double) ((Rectangle2D.Double) o[0]).clone();
        if (o[1] == null) {
            set(TRANSFORM, null);
        } else {
            set(TRANSFORM, (AffineTransform) ((AffineTransform) o[1]).clone());
        }
    }

    @Override
    public Object getTransformRestoreData() {
        return new Object[]{
                rectangle.clone(),
                get(TRANSFORM)
        };
    }

    // EDITING
    @Override
    public Collection<Handle> createHandles(int detailLevel) {
        return HandleHelper.createHandles(detailLevel, this);
    }

    @Override
    public Collection<Action> getActions(Point2D.Double p) {
        final ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.samples.svg.Labels");
        LinkedList<Action> actions = new LinkedList<>();
        if (get(TRANSFORM) != null) {
            actions.add(new AbstractAction(labels.getString("edit.removeTransform.text")) {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(ActionEvent evt) {
                    willChange();
                    fireUndoableEditHappened(
                            TRANSFORM.setUndoable(SVGImageFigure.this, null));
                    changed();
                }
            });
        }

        if (bufferedImage != null) {
            if (rectangle.width != bufferedImage.getWidth()
                    || rectangle.height != bufferedImage.getHeight()) {
                actions.add(new AbstractAction(labels.getString("edit.setToImageSize.text")) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        actionsPerformed1();
                    }
                });
            }
            double imageRatio = bufferedImage.getHeight() / (double) bufferedImage.getWidth();
            double figureRatio = rectangle.height / rectangle.width;
            if (Math.abs(imageRatio - figureRatio) > 0.001) {
                actions.add(new AbstractAction(labels.getString("edit.adjustHeightToImageAspect.text")) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        actionsPerformed2();
                    }
                });
                actions.add(new AbstractAction(labels.getString("edit.adjustWidthToImageAspect.text")) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        actionsPerformed3();
                    }
                });
            }
        }
        return actions;
    }

    private void actionsPerformed1(){
        Object geometry = getTransformRestoreData();
        willChange();
        rectangle = new Rectangle2D.Double(
                rectangle.x - (bufferedImage.getWidth() - rectangle.width) / 2d,
                rectangle.y - (bufferedImage.getHeight() - rectangle.height) / 2d,
                bufferedImage.getWidth(),
                bufferedImage.getHeight());
        fireUndoableEditHappened(
                new TransformRestoreEdit(SVGImageFigure.this, geometry, getTransformRestoreData()));
        changed();
    }

    private void actionsPerformed2(){
        Object geometry = getTransformRestoreData();
        willChange();
        double newHeight = bufferedImage.getHeight() * rectangle.width / bufferedImage.getWidth();
        rectangle = new Rectangle2D.Double(rectangle.x, rectangle.y - (newHeight - rectangle.height) / 2d, rectangle.width, newHeight);
        fireUndoableEditHappened(
                new TransformRestoreEdit(SVGImageFigure.this, geometry, getTransformRestoreData()));
        changed();
    }
    private void actionsPerformed3(){
        Object geometry = getTransformRestoreData();
        willChange();
        double newWidth = bufferedImage.getWidth() * rectangle.height / bufferedImage.getHeight();
        rectangle = new Rectangle2D.Double(rectangle.x - (newWidth - rectangle.width) / 2d, rectangle.y, newWidth, rectangle.height);
        fireUndoableEditHappened(
                new TransformRestoreEdit(SVGImageFigure.this, geometry, getTransformRestoreData()));
        changed();

    }


    // CONNECTING
    // COMPOSITE FIGURES
    // CLONING
    @Override
    public SVGImageFigure clone() {
        SVGImageFigure copy = (SVGImageFigure) super.clone();
        copy.rectangle = (Rectangle2D.Double) this.rectangle.clone();
        copy.cachedTransformedShape = null;
        copy.cachedHitShape = null;
        return copy;
    }

    @Override
    public boolean isEmpty() {
        Rectangle2D.Double b = getBounds();
        return b.width <= 0 || b.height <= 0 || imageData == null && bufferedImage == null;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        invalidateTransformedShape();
    }

    /**
     * Sets the image.
     * <p>
     * Note: For performance reasons this method stores a reference to the
     * imageData array instead of cloning it. Do not modify the imageData
     * array after invoking this method.
     *
     * @param imageData     The image data. If this is null, a buffered image must
     *                      be provided.
     * @param bufferedImage An image constructed from the imageData. If this
     *                      is null, imageData must be provided.
     */
    @Override
    public void setImage(byte[] imageData, BufferedImage bufferedImage) {
        willChange();
        this.imageData = imageData;
        this.bufferedImage = bufferedImage;
        changed();
    }

    /**
     * Sets the image data.
     * This clears the buffered image.
     * <p>
     * Note: For performance reasons this method stores a reference to the
     * imageData array instead of cloning it. Do not modify the imageData
     * array after invoking this method.
     */
    public void setImageData(byte[] imageData) {
        willChange();
        this.imageData = imageData;
        this.bufferedImage = null;
        changed();
    }

    /**
     * Sets the buffered image.
     * This clears the image data.
     */
    @Override
    public void setBufferedImage(BufferedImage image) {
        willChange();
        this.imageData = null;
        this.bufferedImage = image;
        changed();
    }

    /**
     * Gets the buffered image. If necessary, this method creates the buffered
     * image from the image data.
     */
    @Override
    public BufferedImage getBufferedImage() {
        if (bufferedImage == null && imageData != null) {
            try {
                bufferedImage = ImageIO.read(new ByteArrayInputStream(imageData));
            } catch (IOException e) {
                e.printStackTrace();
                // If we can't create a buffered image from the image data,
                // there is no use to keep the image data and try again, so
                // we drop the image data.
                imageData = null;
            }
        }
        return bufferedImage;
    }

    /**
     * Gets the image data. If necessary, this method creates the image
     * data from the buffered image.
     * <p>
     * Note: For performance reasons this method returns a reference to
     * the internally used image data array instead of cloning it. Do not
     * modify this array.
     */
    @Override
    public byte[] getImageData() {
        if (bufferedImage != null && imageData == null) {
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "PNG", bout);
                bout.close();
                imageData = bout.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
                // If we can't create image data from the buffered image,
                // there is no use to keep the buffered image and try again, so
                // we drop the buffered image.
                bufferedImage = null;
            }
        }
        return imageData;
    }

    @Override
    public void loadImage(File file) throws IOException {
        try (InputStream in = Files.newInputStream(file.toPath())) {
            loadImage(in);
        } catch (IOException t) {
            ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
            IOException e = new IOException(labels.getFormatted("file.failedToLoadImage.message", file.getName()));
            e.initCause(t);
            throw e;
        }
    }

    @Override
    public void loadImage(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[512];
        int bytesRead;
        while ((bytesRead = in.read(buf)) > 0) {
            baos.write(buf, 0, bytesRead);
        }
        BufferedImage img;
        try {
            img = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
        } catch (IOException t) {
            img = null;
        }
        if (img == null) {
            ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
            throw new IOException(labels.getFormatted("file.failedToLoadImage.message", in.toString()));
        }
        imageData = baos.toByteArray();
        bufferedImage = img;
    }
}
