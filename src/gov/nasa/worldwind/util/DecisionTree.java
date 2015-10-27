/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

/**
 * Traverses an implicit tree and makes decisions at tree nodes visited in depth-first order.
 * <p/>
 * Users provide an object at construction that implements the {@link gov.nasa.worldwind.util.DecisionTree.Controller}
 * interface. This controller provides methods to determine node inclusion, to terminate traversal, and to create
 * descendant nodes. It is carried to each node during traversal.
 * <p/>
 * Users also provide a user-defined context object to carry state and other information to each node. The context can
 * hold information to assist in decisions or to retains objects or information gathered during traversal.
 * <p/>
 * At the start of traversal a user-defined object is specified as the object defining the decision. It's the object
 * against which decisions are made at each node.
 * <p/>
 * See the source code of {@link gov.nasa.worldwind.util.SectorVisibilityTree} for example usage.
 *
 * @author tag
 * @version $Id: DecisionTree.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class DecisionTree<T, C> // T = type being managed. C = traversal context
{
    /**
     * Interface defined by the class user to control traversal.
     *
     * @param <T> the data type of objects associated with tree nodes -- the objects implicitly contained in the tree.
     * @param <C> the traversal context type.
     */
    public interface Controller<T, C>
    {
        /**
         * Indicates whether a node is active during traversal.
         *
         * @param o       the user-defined object specified at traversal start and examined at each tree nodes.
         * @param context the traversal context.
         *
         * @return true if the node is active, otherwise false.
         */
        public boolean isVisible(T o, C context);

        /**
         * Indicates whether traversal should contine or end at a node.
         *
         * @param o       the user-defined object specified at traversal start and examined at each tree nodes.
         * @param context the traversal context.
         *
         * @return true if the node is terminal, otherwise false. Traversal continues to descendants if false is
         *         returned. otherwise traversal of the node's branch of the tree stops.
         */
        public boolean isTerminal(T o, C context);

        /**
         * Create a cell's descendant nodes. Called in order to continue traversal down a branch of the tree from the
         * current node. The returned nodes are visited in the order returned.
         *
         * @param o       the user-defined object specified at traversal start and examined at each tree nodes.
         * @param context the traversal context.
         *
         * @return an array of descendant nodes.
         */
        public T[] split(T o, C context);
    }

    protected Controller<T, C> controller;

    /**
     * Construct a decision tree for a given item type and controller type.
     *
     * @param controller a user-defined object implementing the <code>Controller</code> interface providing the
     *                   traversal control methods.
     */
    public DecisionTree(Controller<T, C> controller)
    {
        this.controller = controller;
    }

    /**
     * Start tree traversal. The tree is visited in depth-first order.
     *
     * @param o       a user-defined object to examine at each tree node.
     * @param context the traversal context.
     */
    public void traverse(T o, C context)
    {
        if (!this.controller.isVisible(o, context))
            return;

        if (this.controller.isTerminal(o, context))
            return;

        for (T child : this.controller.split(o, context))
        {
            this.traverse(child, context);
        }
    }
//
//    public static void main(String[] args)
//    {
//        DecisionTree<Sector, Sector> tree = new DecisionTree<Sector, Sector>(new Controller<Sector, Sector>()
//        {
//            public boolean isVisible(Sector s, Sector context)
//            {
//                return s.intersects(context);
//            }
//
//            public boolean isTerminal(Sector s, Sector context)
//            {
//                return s.getDeltaLat().degrees < 1d;
//            }
//
//            public Sector[] split(Sector s, Sector context)
//            {
//                return s.subdivide();
//            }
//        });
//
//        int N = 10000;
//        long start = System.currentTimeMillis();
//        for (int i = 0; i < N; i++)
//            tree.traverse(Sector.FULL_SPHERE, Sector.fromDegrees(0, 40, 0, 40));
//        System.out.println((System.currentTimeMillis() - start) / (double) N + " ms");
//    }
}
