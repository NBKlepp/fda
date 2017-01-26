
//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** @author  John Miller, Hao Peng, Zhe Jin
 *  @version 1.2
 *  @date    Mon Jul 27 01:27:00 EDT 2015
 *  @see     LICENSE (MIT style license file).
 */

package scalation.analytics.classifier

import scala.math.abs

import scalation.linalgebra.{MatriI, MatrixI, VectorD, VectoI, VectorI}
import scalation.linalgebra.gen.HMatrix4
import scalation.relalgebra.Relation
import scalation.util.time

import BayesClassifier.me_default

//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** The `AugNaiveBayes` class implements an Integer-Based Tree Augmented Naive Bayes
 *  Classifier,  which is a commonly used such classifier for discrete input data.
 *  The classifier is trained using a data matrix 'x' and a classification vector 'y'.
 *  Each data vector in the matrix is classified into one of 'k' classes numbered
 *  0, ..., k-1.  Prior probabilities are calculated based on the population of
 *  each class in the training-set.  Relative posterior probabilities are computed
 *  by multiplying these by values computed using conditional probabilities.  The
 *  classifier supports limited dependency between features/variables.
 *-----------------------------------------------------------------------------
 *  @param x      the integer-valued data vectors stored as rows of a matrix
 *  @param y      the class vector, where y(l) = class for row l of the matrix, x(l)
 *  @param fn     the names for all features/variables
 *  @param k      the number of classes
 *  @param cn     the names for all classes
 *  @param vc     the value count (number of distinct values) for each feature
 *  @param me     use m-estimates (me == 0 => regular MLE estimates)
 *  @param thres  the correlation threshold between 2 features for possible parent-child relationship
 */
class AugNaiveBayes (x: MatriI, y: VectoI, fn: Array [String], k: Int, cn: Array [String],
                     private var vc: VectoI = null, me: Int = me_default, thres: Double = 0.3)
      extends BayesClassifier (x, y, fn, k, cn)
{
    private val DEBUG  = false                          // debug flag
    private val cor    = calcCorrelation                // feature correlation matrix
    private val parent = new VectorI (n)                // vector holding the parent for each feature/variable
    private val vcp    = new VectorI(n)                 // value count for the parent

    private val popC  = new VectorI (k)                 // frequency counts for classes 0, ..., k-1
    private val probC = new VectorD (k)                 // probabilities for classes 0, ..., k-1
    private val popX  = new HMatrix4 [Int] (k, n)       // conditional frequency counts for variable/feature j: xj
    private val probX = new HMatrix4 [Double] (k, n)    // conditional probabilities for variable/feature j: xj

    if (vc == null) vc = vc_fromData                    // set to default for binary data (2)

    if (DEBUG) {
        println ("value count vc      = " + vc)
        println ("value count vcp     = " + vcp)
        println ("correlation matrix  = " + cor)
        println ("parent features parent = " + parent)
    } // if

    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Build a model, including ordering and selection of features.
     *  @param testStart  the begining of the test region
     *  @param testEnd    the end of the test region
     */
    def buildModel (testStart: Int= 0, testEnd: Int = 0): (Array [Boolean], DAG)=
    {
        computeParent()
        computeVcp ()

        popX.alloc (vc, vcp)
        probX.alloc (vc, vcp)
        val pp: Traversable [Array [Int]] = for (p <- parent) yield Array (p)
        (Array.fill (n)(true), new DAG(pp.toArray))
    } // buildModel

    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the parent of each feature based on the correlation matrix.
     *  Feature x_i is only a possible candidate for parent of feature x_j if
     *  i < j
     */
    def computeParent ()
    {
        parent(0) = -1                                       // feature 0 does not have a parent
        for (i <- 1 until n) {
            val correl = cor(i).map ((x: Double) => abs (x))
            parent(i) = if (correl.max () > thres) correl.argmax () else -1
        } // for
    } // computeParent

    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the value counts of each parent feature based on the parent vector.
     */
    def computeVcp ()
    {
        //set default value count to 1
        vcp.set(1)
        for (j <- 0 until n if (parent(j) > -1)) vcp(j) = vc(parent(j))
    } // computeVcp

    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Count the frequencies for 'y' having class 'i' and 'x' for cases 0, 1, ...
     *  Only the test region from 'testStart' to 'testEnd' is skipped, the rest is
     *  training data.
     *  @param testStart  starting index of test region (inclusive) used in cross-validation
     *  @param testEnd    ending index of test region (exclusive) used in cross-validation
     */
    def frequencies (testStart: Int, testEnd: Int)
    {
        for (l <- 0 until m if l < testStart || l >= testEnd) {    // l = lth row of data matrix x
            val i = y(l)                                           // get the class
            popC(i) += 1                                           // increment ith class
            for (j <- 0 until n) {
                if (parent(j) > -1) popX(i, j, x(l, j), x(l, parent(j))) += 1
                else             popX(i, j, x(l, j), 0) += 1
            } // for
        } // for

        if (DEBUG) {
            println ("popC = " + popC)                             // #(C = i)
            println ("popX = " + popX)                             // #(X_j = x & C = i)
        } // if
    } // frequencies

    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Train the classifier by computing the probabilities for C, and the
     *  conditional probabilities for X_j.
     *  @param testStart  starting index of test region (inclusive) used in cross-validation.
     *  @param testEnd    ending index of test region. (exclusive) used in cross-validation.
     */
    def train (testStart: Int = 0, testEnd: Int = 0)
    {
        frequencies (testStart, testEnd)                           // compute frequencies skipping test region

        for (i <- 0 until k) {                                     // for each class i
            val pci = popC(i).toDouble                             // population of class i
            probC(i) = pci / md                                    // probability of class i

            for (j <- 0 until n) {                                 // for each feature j
                val me_vc = me / vc(j).toDouble
                for (xj <- 0 until vc(j); xp <- 0 until vcp(j)) {  // for each value for feature j: xj, parent(j): xp
                    probX(i, j, xj, xp) = (popX(i, j, xj, xp) + me_vc) / (pci + me)
                } // for
            } // for
        } // for

        if (DEBUG) {
            println ("probC = " + probC)                           // P(C = i)
            println ("probX = " + probX)                           // P(X_j = x | C = i)
        } // if
    } // train

    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Count the frequencies for 'y' having class 'i' and 'x' for cases 0, 1, ...
     *  Only the test region from 'testStart' to 'testEnd' is skipped, the rest is
     *  training data.
     *  @param testStart  starting index of test region (inclusive) used in cross-validation
     *  @param testEnd    ending index of test region (exclusive) used in cross-validation
     */
    private def frequencies (itrain: Array[Int])
    {
        for (l <- itrain) {    // l = lth row of data matrix x
        val i = y(l)                                           // get the class
            popC(i) += 1                                           // increment ith class
            for (j <- 0 until n) {
                if (parent(j) > -1) popX(i, j, x(l, j), x(l, parent(j))) += 1
                else             popX(i, j, x(l, j), 0) += 1
            } // for
        } // for

        if (DEBUG) {
            println ("popC = " + popC)                             // #(C = i)
            println ("popX = " + popX)                             // #(X_j = x & C = i)
        } // if
    } // frequencies

    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Train the classifier by computing the probabilities for C, and the
     *  conditional probabilities for X_j.
     *  @param testStart  starting index of test region (inclusive) used in cross-validation.
     *  @param testEnd    ending index of test region. (exclusive) used in cross-validation.
     */
    override def train (itrain: Array[Int])
    {
        frequencies (itrain)                           // compute frequencies skipping test region
        for (i <- 0 until k) {                                     // for each class i
        val pci = popC(i).toDouble                             // population of class i
            probC(i) = pci / md                                    // probability of class i

            for (j <- 0 until n) {                                 // for each feature j
            val me_vc = me / vc(j).toDouble
                for (xj <- 0 until vc(j); xp <- 0 until vcp(j)) {  // for each value for feature j: xj, parent(j): xp
                    probX(i, j, xj, xp) = (popX(i, j, xj, xp) + me_vc) / (pci + me)
                } // for
            } // for
        } // for

        if (DEBUG) {
            println ("probC = " + probC)                           // P(C = i)
            println ("probX = " + probX)                           // P(X_j = x | C = i)
        } // if
    } // train

    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Given a discrete data vector 'z', classify it returning the class number
     *  (0, ..., k-1) with the highest relative posterior probability.
     *  Return the best class, its name and its real;tive probability.
     *  @param z  the data vector to classify
     */
    def classify (z: VectoI): (Int, String, Double) =
    {
        val prob = new VectorD (k)
        for (i <- 0 until k) {
            prob(i) = probC(i)                                               // P(C = i)
            for (j <- 0 until n) {
                prob(i) *= (if (parent(j) > -1) probX(i, j, z(j), z(parent(j)))    // P(X_j = z_j | C = i), parent
                            else             probX(i, j, z(j), 0))           // P(X_j = z_j | C = i), no parent
            } // for
        } // for
        if (DEBUG) println ("prob = " + prob)
        val best = prob.argmax ()                   // class with the highest relative posterior probability
        (best, cn(best), prob(best))                // return the best class, its name and its probability
    } // classify

    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Reset or re-initialize the frequency tables and the probability tables.
     */
    def reset()
    {
        popC.set (0)
        probC.set (0)
        popX.clear ()
        probX.clear ()
        popX.alloc (vc, vcp)
        probX.alloc (vc, vcp)
    } // reset

} // AugNaiveBayes class


//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** `AugNaiveBayes` is the companion object for the `AugNaiveBayes` class.
 */
object AugNaiveBayes
{
    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Create a `AugNaiveBayes object, passing 'x' and 'y' together in one table.
     *  @param xy     the data vectors along with their classifications stored as rows of a matrix
     *  @param fn     the names of the features
     *  @param k      the number of classes
     *  @param vc     the value count (number of distinct values) for each feature
     *  @param me     use m-estimates (me == 0 => regular MLE estimates)
     *  @param thres  the correlation threshold between 2 features for possible parent-child relationship
     */
    def apply (xy: MatriI, fn: Array [String], k: Int, cn: Array [String],
               vc: VectoI = null, me: Int = me_default, thres: Double = 0.3) =
    {
        new AugNaiveBayes (xy(0 until xy.dim1, 0 until xy.dim2 - 1), xy.col(xy.dim2 - 1), fn, k, cn,
                           vc, me, thres)
    } // apply

} // AugNaiveBayes object


//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** The `AugNaiveBayesTest` object is used to test the `AugNaiveBayes` class.
 *  Classify whether a car is more likely to be stolen (1) or not (1).
 *  @see www.inf.u-szeged.hu/~ormandi/ai2/06-AugNaiveBayes-example.pdf
 *  > run-main scalation.analytics.classifier.AugNaiveBayesTest
 */
object AugNaiveBayesTest extends App
{
    // x0: Color:   Red (1), Yellow (0)
    // x1: Type:    SUV (1), Sports (0)
    // x2: Origin:  Domestic (1), Imported (0)
    // features:                 x0 x1 x2
    val x = new MatrixI ((10, 3), 1, 0, 1,              // data matrix
                                  1, 0, 1,
                                  1, 0, 1,
                                  0, 0, 1,
                                  0, 0, 0,
                                  0, 1, 0,
                                  0, 1, 0,
                                  0, 1, 1,
                                  1, 1, 0,
                                  1, 0, 0)

    val y  = VectorI (1, 0, 1, 0, 1, 0, 1, 0, 0, 1)     // classification vector: 0(No), 1(Yes))
    val fn = Array ("Color", "Type", "Origin")          // feature/variable names
    val cn = Array ("No", "Yes")                        // class names

    println ("xy = " + (x :^+ y))
    println ("---------------------------------------------------------------")

    val anb = new AugNaiveBayes (x, y, fn, 2, cn)       // create the classifier

    // train the classifier ---------------------------------------------------
    anb.train ()

    // test sample ------------------------------------------------------------
    val z1 = VectorI (1, 0, 1)         // existing data vector to classify
    val z2 = VectorI (1, 1, 1)         // new data vector to classify
    println ("classify (" + z1 + ") = " + anb.classify (z1) + "\n")
    println ("classify (" + z2 + ") = " + anb.classify (z2) + "\n")

    // cross validate the classifier
    anb.crossValidate ()

} // AugNaiveBayesTest object


//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** The `AugNaiveBayesTest2` object is used to test the `AugNaiveBayes` class.
 *  Given whether a person is Fast and/or Strong, classify them as making C = 1
 *  or not making C = 0 the football team.
 *  > run-main scalation.analytics.classifier.AugNaiveBayesTest2
 */
object AugNaiveBayesTest2 extends App
{
    // training-set -----------------------------------------------------------
    // x0: Fast
    // x1: Strong
    // y:  Classification (No/0, Yes/1)
    // features:                  x0 x1  y
    val xy = new MatrixI ((10, 3), 1, 1, 1,
                                   1, 1, 1,
                                   1, 0, 1,
                                   1, 0, 1,
                                   1, 0, 0,
                                   0, 1, 0,
                                   0, 1, 0,
                                   0, 1, 1,
                                   0, 0, 0,
                                   0, 0, 0)

    val fn = Array ("Fast", "Strong")                  // feature names
    val cn = Array ("No", "Yes")                       // class names

    println ("xy = " + xy)
    println ("---------------------------------------------------------------")

    val anb = AugNaiveBayes (xy, fn, 2, cn, null, 0)   // create the classifier

    // train the classifier ---------------------------------------------------
    anb.train ()

    // test sample ------------------------------------------------------------
    val z = VectorI (1, 0)                             // new data vector to classify
    println ("classify (" + z + ") = " + anb.classify (z) + "\n")

    // cross validate the classifier
    anb.crossValidate ()

} // AugNaiveBayesTest2 object


//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** The `AugNaiveBayesTest3` object is used to test the `AugNaiveBayes` class.
  *  > run-main scalation.analytics.classifier.AugNaiveBayesTest3
  */
object AugNaiveBayesTest3 extends App
{
    val filename = BASE_DIR +  "breast-cancer.arff"
    var data = Relation(filename, -1, null)
    val xy = data.toMatriI2(null)
    val fn = data.colName.toArray
    val cn = Array ("p", "e")            // class names
    // val vc = VectorI (7, 5, 11, 3, 10, 5, 4, 3, 13, 3, 7, 5, 5, 10, 10, 3, 5, 4, 9, 10, 7, 8)
    //println ("xy = " + xy)
    println ("---------------------------------------------------------------")

    val anb =AugNaiveBayes(xy, fn, 2, cn,  null,0,0.3)               // create the classifier

    // train the classifier ---------------------------------------------------
    var testprint: (Array[Boolean], DAG) = null
    time {testprint = anb.buildModel()}
    println("parent = " + testprint)
    anb.train ()
    // cross validate the classifier
    anb.crossValidate ()

} // AugNaiveBayesTest3 object

