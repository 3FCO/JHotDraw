/*
 * @(#)SVGRect.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.samples.svg.figures;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import org.jhotdraw.draw.*;
import static org.jhotdraw.draw.AttributeKeys.FILL_COLOR;
import static org.jhotdraw.draw.AttributeKeys.STROKE_CAP;
import static org.jhotdraw.draw.AttributeKeys.STROKE_JOIN;
import static org.jhotdraw.draw.AttributeKeys.STROKE_MITER_LIMIT;
import static org.jhotdraw.draw.AttributeKeys.TRANSFORM;
import org.jhotdraw.draw.handle.BoundsOutlineHandle;
import org.jhotdraw.draw.handle.Handle;
import org.jhotdraw.draw.handle.ResizeHandleKit;
import org.jhotdraw.draw.handle.TransformHandleKit;
import org.jhotdraw.geom.Geom;
import org.jhotdraw.geom.GrowStroke;
import org.jhotdraw.samples.svg.Gradient;
import org.jhotdraw.samples.svg.SVGAttributeKeys;

import static org.jhotdraw.samples.svg.SVGAttributeKeys.*;

/**
 * SVGRect.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class SVGRectFigure extends SVGAttributedFigure implements SVGFigure {

    private static final long serialVersionUID = 1L;

    private static final String ARC_WIDTH_PROPERTY = "arcWidth";

    private static final String ARC_HEIGHT_PROPERTY = "arcHeight";
    /**
     * The variable acv is used for generating the locations of the control
     * points for the rounded rectangle using path.curveTo.
     */
    private static final double ACV;

    static {
        double angle = Math.PI / 4.0;
        double a = 1.0 - Math.cos(angle);
        double b = Math.tan(angle);
        double c = Math.sqrt(1.0 + b * b) - 1 + a;
        double cv = 4.0 / 3.0 * a * b / c;
        ACV = (1.0 - cv);
    }

    protected RoundRectangle2D.Double roundrect;
    /**
     * This is used to perform faster drawing.
     */
    private transient Shape cachedTransformedShape;
    /**
     * This is used to perform faster hit testing.
     */
    private transient Shape cachedHitShape;

    public SVGRectFigure() {
        this(0, 0, 0, 0);
    }

    public SVGRectFigure(double x, double y, double width, double height) {
        this(x, y, width, height, 0, 0);
    }

    public SVGRectFigure(double x, double y, double width, double height, double arcWidth, double arcHeight) {
        roundrect = new RoundRectangle2D.Double(x, y, width, height, arcWidth, arcHeight);
        SVGAttributeKeys.setDefaults(this);
        setConnectable(false);
    }

    /**
     * Drawing
     */
    @Override
    protected void drawFill(Graphics2D g) {
        if (getArcHeight() == 0d && getArcWidth() == 0d) {
            g.fill(roundrect.getBounds2D());
        } else {
            g.fill(roundrect);
        }
    }


    @Override
    protected void drawStroke(Graphics2D g) {
        if (roundrect.archeight == 0 && roundrect.arcwidth == 0) {
            g.draw(roundrect.getBounds2D());
        } else {
            drawPath(g);
        }
    }

    /**
     * Path for the round rectangle must be generated manually,
     * because the path of a Java RoundRectangle is drawn counterclockwise
     * whereas an SVG rect needs to be drawn clockwise.
     * @param g
     */
    private void drawPath(Graphics2D g) {
        Path2D.Double p = new Path2D.Double();
        double aw = roundrect.arcwidth / 2d;
        double ah = roundrect.archeight / 2d;
        p.moveTo((roundrect.x + aw), (float) roundrect.y);
        p.lineTo((roundrect.x + roundrect.width - aw), (float) roundrect.y);
        p.curveTo((roundrect.x + roundrect.width - aw * ACV), (float) roundrect.y,
                (roundrect.x + roundrect.width), (float) (roundrect.y + ah * ACV),
                (roundrect.x + roundrect.width), (roundrect.y + ah));
        p.lineTo((roundrect.x + roundrect.width), (roundrect.y + roundrect.height - ah));
        p.curveTo(
                (roundrect.x + roundrect.width), (roundrect.y + roundrect.height - ah * ACV),
                (roundrect.x + roundrect.width - aw * ACV), (roundrect.y + roundrect.height),
                (roundrect.x + roundrect.width - aw), (roundrect.y + roundrect.height));
        p.lineTo((roundrect.x + aw), (roundrect.y + roundrect.height));
        p.curveTo((roundrect.x + aw * ACV), (roundrect.y + roundrect.height),
                (roundrect.x), (roundrect.y + roundrect.height - ah * ACV),
                (float) roundrect.x, (roundrect.y + roundrect.height - ah));
        p.lineTo((float) roundrect.x, (roundrect.y + ah));
        p.curveTo((roundrect.x), (roundrect.y + ah * ACV),
                (roundrect.x + aw * ACV), (float) (roundrect.y),
                (float) (roundrect.x + aw), (float) (roundrect.y));
        p.closePath();
        g.draw(p);
    }

    /**
     * Shape and Bounds
     */
    public double getX() {
        return roundrect.x;
    }

    public double getY() {
        return roundrect.y;
    }

    public double getWidth() {
        return roundrect.width;
    }

    public double getHeight() {
        return roundrect.height;
    }

    public double getArcWidth() {
        return roundrect.arcwidth;
    }

    public double getArcHeight() {
        return roundrect.archeight;
    }
    public static String getArcWidthProperty() {
        return ARC_WIDTH_PROPERTY;
    }

    public static String getArcHeightProperty() {
        return ARC_HEIGHT_PROPERTY;
    }

    public void setArcWidth(double newValue) {
        double oldValue = roundrect.arcwidth;
        roundrect.arcwidth = newValue;
        firePropertyChange(ARC_WIDTH_PROPERTY, oldValue, newValue);
    }

    public void setArcHeight(double newValue) {
        double oldValue = roundrect.archeight;
        roundrect.archeight = newValue;
        firePropertyChange(ARC_HEIGHT_PROPERTY, oldValue, newValue);
    }

    public void setBothArcs(double width, double height) {
        setArcWidth(width);
        setArcHeight(height);
    }

    @Override
    public Rectangle2D.Double getBounds() {
        return (Rectangle2D.Double) roundrect.getBounds2D();
    }

    @Override
    public Rectangle2D.Double getDrawingArea() {
        Rectangle2D rx = getTransformedShape().getBounds2D();
        Rectangle2D.Double r = (rx instanceof Rectangle2D.Double) ? (Rectangle2D.Double) rx : new Rectangle2D.Double(rx.getX(), rx.getY(), rx.getWidth(), rx.getHeight());
        if (get(TRANSFORM) == null) {
            double g = SVGAttributeKeys.getPerpendicularHitGrowth(this, 1.0) * 2d + 1d;
            Geom.grow(r, g, g);
        } else {
            double strokeTotalWidth = AttributeKeys.getStrokeTotalWidth(this, 1.0);
            double width = strokeTotalWidth / 2d;
            if (get(STROKE_JOIN) == BasicStroke.JOIN_MITER) {
                width *= get(STROKE_MITER_LIMIT);
            }
            if (get(STROKE_CAP) != BasicStroke.CAP_BUTT) {
                width += strokeTotalWidth * 2;
            }
            width++;
            Geom.grow(r, width, width);
        }
        return r;
    }

    @Override
    public boolean contains(Point2D.Double p) {
        return getHitShape().contains(p);
    }

    @Override
    public void setBounds(Point2D.Double anchor, Point2D.Double lead) {
        invalidateTransformedShape();
        roundrect.x = Math.min(anchor.x, lead.x);
        roundrect.y = Math.min(anchor.y, lead.y);
        roundrect.width = Math.max(0.1, Math.abs(lead.x - anchor.x));
        roundrect.height = Math.max(0.1, Math.abs(lead.y - anchor.y));
        invalidate();
    }

    private void invalidateTransformedShape() {
        cachedTransformedShape = null;
        cachedHitShape = null;
    }

    private Shape getTransformedShape() {
        if (cachedTransformedShape == null) {
            if (getArcHeight() == 0 || getArcWidth() == 0) {
                cachedTransformedShape = roundrect.getBounds2D();
            } else {
                cachedTransformedShape = (Shape) roundrect.clone();
            }
            if (get(TRANSFORM) != null) {
                cachedTransformedShape = get(TRANSFORM).createTransformedShape(cachedTransformedShape);
            }
        }
        return cachedTransformedShape;
    }

    private Shape getHitShape() {
        if (cachedHitShape != null) {
            return cachedHitShape;
        }
        if (get(FILL_COLOR) != null || get(FILL_GRADIENT) != null) {
            return cachedHitShape = new GrowStroke(
                    (float) AttributeKeys.getStrokeTotalWidth(this, 1.0) / 2f,
                    (float) AttributeKeys.getStrokeTotalMiterLimit(this, 1.0)).createStrokedShape(getTransformedShape());
        } else {
             return cachedHitShape = AttributeKeys.getHitStroke(this, 1.0).createStrokedShape(getTransformedShape());
        }
    }

    @Override
    public void transform(AffineTransform tx) {
        invalidateTransformedShape();
        if (get(TRANSFORM) != null || (tx.getType() & (AffineTransform.TYPE_TRANSLATION)) != tx.getType()) {
            if (get(TRANSFORM) == null) {
                set(TRANSFORM, (AffineTransform) tx.clone());
            } else {
                AffineTransform t = TRANSFORM.getClone(this);
                t.preConcatenate(tx);
                set(TRANSFORM, t);
            }
        } else {
            executeTransform(tx);
        }
    }
    private void executeTransform(AffineTransform tx){
        Point2D.Double anchor = getStartPoint();
        Point2D.Double lead = getEndPoint();
        setBounds(
                (Point2D.Double) tx.transform(anchor, anchor),
                (Point2D.Double) tx.transform(lead, lead));
        if (get(FILL_GRADIENT) != null && !get(FILL_GRADIENT).isRelativeToFigureBounds()) {
            Gradient g = FILL_GRADIENT.getClone(this);
            g.transform(tx);
            set(FILL_GRADIENT, g);
        }
        if (get(STROKE_GRADIENT) != null && !get(STROKE_GRADIENT).isRelativeToFigureBounds()) {
            Gradient g = STROKE_GRADIENT.getClone(this);
            g.transform(tx);
            set(STROKE_GRADIENT, g);
        }
    }

    @Override
    public void restoreTransformTo(Object geometry) {
        invalidateTransformedShape();
        Object[] restoreData = (Object[]) geometry;
        roundrect = (RoundRectangle2D.Double) ((RoundRectangle2D.Double) restoreData[0]).clone();
        TRANSFORM.setClone(this, (AffineTransform) restoreData[1]);
        FILL_GRADIENT.setClone(this, (Gradient) restoreData[2]);
        STROKE_GRADIENT.setClone(this, (Gradient) restoreData[3]);
    }

    @Override
    public Object getTransformRestoreData() {
        return new Object[]{
            roundrect.clone(),
            TRANSFORM.getClone(this),
            FILL_GRADIENT.getClone(this),
            STROKE_GRADIENT.getClone(this)};
    }

    /**
     * Editing
     */
    @Override
    public Collection<Handle> createHandles(int detailLevel) {
        LinkedList<Handle> handles = new LinkedList<>();
        switch (detailLevel % 2) {
            case -1: // Mouse hover handles
                handles.add(new BoundsOutlineHandle(this, false, true));
                break;
            case 0:
                ResizeHandleKit.addResizeHandles(this, handles);
                handles.add(new SVGRectRadiusHandle(this));
                handles.add(new LinkHandle(this));
                break;
            case 1:
                TransformHandleKit.addTransformHandles(this, handles);
                break;
            default:
                assert false;
        }
        return handles;
    }

    /**
     * Cloning
     */
    @Override
    public SVGRectFigure clone() {
        SVGRectFigure that = (SVGRectFigure) super.clone();
        that.roundrect = (RoundRectangle2D.Double) this.roundrect.clone();
        that.cachedTransformedShape = null;
        that.cachedHitShape = null;
        return that;
    }

    @Override
    public boolean isEmpty() {
        Rectangle2D.Double b = getBounds();
        return b.width <= 0 || b.height <= 0;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        invalidateTransformedShape();
    }
}
