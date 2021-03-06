
//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** @author  John Miller
 *  @version 1.3
 *  @date    Tue May 29 14:45:32 EDT 2012
 *  @see     LICENSE (MIT style license file).
 */

package scalation.analytics.clusterer

import scalation.linalgebra.{MatrixD, VectorD, VectorI}

//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** The `Clusterer` trait provides a common framework for several clustering
 *  algorithms.
 */
trait Clusterer
{
    private var _name: Array [String] = null   // optional names for clusters
    protected var clustered = false

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Return the centroids. Should only be called after `cluster ()`. 
     */
    def centroids (): MatrixD

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Given a set of points/vectors, put them in clusters, returning the cluster
     *  assignment vector.  A basic goal is to minimize the sum of the distances
     *  between points within each cluster.
     */
    def cluster (): Array [Int]

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Given a new point/vector y, determine which cluster it belongs to.
     *  @param y  the vector to classify
     */
    def classify (y: VectorD): Int

    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute a distance metric (distance squared) between vectors/points 'u' and 'v'.
     *  @param u  the first vector/point
     *  @param v  the second vector/point
     */
    def distance (u: VectorD, v: VectorD): Double = 
    {
        (u - v).normSq       // squared Euclidean norm used for efficiency, may use other norms
    } // distance

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Return the sizes of the centroids. Should only be called after 
     *  `cluster ()`. 
     */
    def csize (): VectorI

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Set the names for the clusters.
     *  @param n  the array of names
     */
    def name_ (n: Array [String])
    {
        _name = n   
    } // name_

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Get the name of the i-th cluster.
     *  @param y  the vector to classify
     */
    def getName (i: Int): String = 
    {
        if (_name != null && i < _name.length) _name(i) else "unknown"
    } // getName

    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the sum of squared errors (distance sqaured from centroid for all points)
     */
    def sse (x: MatrixD): Double =
    {
        //x.range1.view.map (i => distance (x(i), cent(clustr(i)))).sum
        var sum    = 0.0
        val cent   = centroids ()
        val clustr = cluster ()
        for (i <- x.range1) sum += distance (x(i), cent(clustr(i)))
        sum
    } // sse

} // Clusterer trait

