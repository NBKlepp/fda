
//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** @author  John Miller, Vishnu Gowda Harish
 *  @version 1.3
 *  @date    Wed Dec 14 13:00:24 EST 2016
 *  @see     LICENSE (MIT style license file).
 */

package scalation.linalgebra.bld

import java.io.{File, PrintWriter}

//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** The `BldRleMatrix` object is used to build matrix classes for various base types.
 *  > run-main scalation.linalgebra.bld.BldRleMatrix
 */
object BldRleMatrix extends App with BldParams
{
    println ("BldRleMatrix: generate code for RleMatrix classes")

    for (i <- 0 until kind.length-1) {        // do not generate `MatrixS`
        val VECTO     = kind(i)._1
        val RLEVECTOR = kind(i)._1.replace ("o", "or").replace("V", "RleV")
        val BASE      = kind(i)._2
        val VECTOR    = kind(i)._1.replace ("o", "or")
        val MATRI     = kind(i)._6
        val ZERO      = kind(i)._8
        val ONE       = kind(i)._9
        val BASE_LC   = BASE.toLowerCase
        val MATRIX    = { val m = MATRI.splitAt (MATRI.size-1); m._1 + "x" + m._2 }
        val RLEMATRIX = "Rle"+ MATRIX
        val IMPORT    = if (CUSTOM contains BASE) s"scalation.math.$BASE.{abs => ABS, max => MAX, min => MIN, _}"
                                              else "scala.math.{abs => ABS, max => MAX, min => MIN}"
        val IMPORT2   = if (BASE == "StrNum") "scalation.math.{StrO, oneIf}"
                        else if (CUSTOM contains BASE) s"scalation.math.{$BASE, oneIf}"
                        else s"scalation.math.{${BASE_LC}_exp, oneIf}"
        val TRIPLET   = "Triplet"+VECTO.charAt(5)

// Beginning of string holding code template -----------------------------------

        val code = raw"""
//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** @author   Vishnu Gowda Harish, Vinay Kumar Bingi, John Miller
 *  @version  1.0
 *  @date     Sat Oct 1 1:10:11 EDT 2016
 *  @see      LICENSE (MIT style license file).
 */

package scalation.linalgebra

import java.io.PrintWriter

import scala.io.Source.fromFile
import $IMPORT

import $IMPORT2
import scalation.math.ExtremeD.TOL
import scalation.util.{Error, PackageInfo, ReArray}

import $RLEMATRIX._

//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** The `$RLEMATRIX` class stores and operates on Numeric Matrices of type `$BASE`.
 *  Rather than storing the matrix as a 2 dimensional array, it is stored as an array
 *  of $RLEVECTOR's.
 *  @param d1  the first/row dimension
 *  @param d2  the second/column dimension
 *  @param v   the 1D array used to store matrix elements
 */
 class $RLEMATRIX (val d1: Int, 
                   val d2: Int, 
                   private var v: Array [$RLEVECTOR] = null,
                   val deferred: Boolean = false)
        extends $MATRI with Error with Serializable
{ 
    /** Dimension 1
     */
    lazy val dim1 = d1

    /** Dimension 2
     */
    lazy val dim2 = d2
    
    if (v == null) {
        v = Array.ofDim [$RLEVECTOR] (d2)
        for (i <- 0 until v.length) {
            val v1 = new ReArray [$TRIPLET] (1)
            v1(0) = new $TRIPLET ($ZERO, d1, 0)
            v(i) = new $RLEVECTOR (d1, v1)
        } // for   
    } // if
    
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Create an exact copy of 'this' m-by-n matrix.
     */
    def copy (): $RLEMATRIX = new $RLEMATRIX (dim1, dim2, v.clone())

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Create an m-by-n matrix with all elements intialized to zero.
     *  @param m  the number of rows
     *  @param n  the number of columns
     */
    def zero (mm: Int, nn: Int): $RLEMATRIX = $RLEMATRIX (new $MATRIX (mm, nn))
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Get 'this' matrix's element at the 'i, j'-th index position. 
     *  @param i  the row index
     *  @param j  the column index
     */
    def apply (i: Int, j: Int): $BASE = v(j)(i)
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Get 'this' matrix's vector at the 'i'-th index position ('i'-th row).
     *  @param i  the row index
     */
    def apply (i: Int): $RLEVECTOR = { $RLEVECTOR (for (j <- range2) yield  v(j)(i)) }
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Get a slice 'this' matrix row-wise on range 'ir' and column-wise on range 'jr'.
     *  Ex: b = a(2..4, 3..5)
     *  @param ir  the row range
     *  @param jr  the column range
     */
    def apply (ir: Range, jr: Range): $MATRI = slice (ir.start, ir.end, jr.start, jr.end)
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Get the underlying 1D array for 'this' matrix.
     */
    def apply (): Array [$RLEVECTOR] = v
   
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Set 'this' matrix's element at the 'i, j'-th index position to the scalar 'x'.
     *  @param i  the row index
     *  @param j  the column index
     *  @param x  the scalar value to assign
     */
    def update (i: Int, j: Int, x: $BASE) { v(j)(i) = x }
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Set 'this' matrix's row at the 'i'-th index position to the vector 'u'.
     *  @param i  the row index
     *  @param u  the vector value to assign
     */
    def update (i: Int, u: $VECTO) { for (j <- range2)  v(j)(i) = u(j) }
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Set a slice 'this' matrix row-wise on range ir and column-wise on range 'jr'.
     *  Ex: a(2..4, 3..5) = b
     *  @param ir  the row range
     *  @param jr  the column range
     *  @param b   the matrix to assign
     */ 
    def update (ir: Range, jr: Range, b: $MATRI)
    {
        if (b.isInstanceOf [$RLEMATRIX]) {
            val bb = b.asInstanceOf [$RLEMATRIX]
            for (i <- ir; j <- jr) this(i, j) = bb(i, j)
        } else {
            flaw ("update","must convert b to $RLEMATRIX first")
        } // if
    } // update
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Set all the elements in 'this' matrix to the scalar 'x'.
     *  @param x  the scalar value to assign
     */
    def set (x: $BASE) { for (i <- range1; j <- range2) v(i)(j) = x }
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Set 'this' matrix's 'i'-th row starting at column 'j' to the vector 'u'.
     *  @param i  the row index
     *  @param u  the vector value to assign
     *  @param j  the starting column index
     */
    def set (i: Int, u: $VECTO, j: Int = 0) { for (k <- 0 until u.dim) v(i)(k+j) = u(k) }
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Set all the values in 'this' matrix as copies of the values in 2D array 'u'.
     *  @param u  the 2D array of values to assign
     */
    def set (u: Array[Array[$BASE]]) 
    { 
        v = (for (j <- range2) yield $RLEVECTOR (for (i <- range1) yield u(j)(i))).toArray
    } // set
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Convert 'this' `$RLEMATRIX` into a `MatrixI`.
     */
    def toInt: MatrixI =
    {
        val c = new MatrixI (dim1, dim2)
        for (i <- range1) c(i) = this(i).toInt
        c
    } // toInt
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Convert 'this' matrix to a dense matrix.
     */
    def toDense: $MATRIX = 
    {
        val c = new $MATRIX (dim1, dim2)
        for (i <- range1) c(i) = this(i)
        c
    } // toDense

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Slice 'this' matrix row-wise 'from' to 'end'.
     *  @param from  the start row of the slice (inclusive)
     *  @param end   the end row of the slice (exclusive)
     */
    def slice (from: Int, end: Int): $RLEMATRIX =
    { 
        $RLEMATRIX (for (i <- range2) yield $RLEVECTOR (for (j <- from until end) yield v(i)(j)))       
    } // slice

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Slice 'this' matrix column-wise 'from' to 'end'.
     *  @param from  the start column of the slice (inclusive)
     *  @param end   the end column of the slice (exclusive)
     */
    def sliceCol (from: Int, end: Int): $RLEMATRIX = 
    {
        if (from >= end) return new $RLEMATRIX (0, 0)
        $RLEMATRIX (for (i <- from until end) yield v(i))
    } // sliceCol

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Slice 'this' matrix row-wise 'r_from' to 'r_end' and column-wise 'c_from' to 'c_end'.
     *  @param r_from  the start of the row slice
     *  @param r_end   the end of the row slice
     *  @param c_from  the start of the column slice
     *  @param c_end   the end of the column slice
     */
    def slice (r_from: Int, r_end: Int, c_from: Int, c_end: Int): $RLEMATRIX = 
    {       
        $RLEMATRIX (for (i <- c_from until c_end) yield $RLEVECTOR (for (j <- r_from until r_end) yield v(i)(j)))
    } // slice

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Slice 'this' matrix excluding the given row and/or column.
     *  @param row  the row to exclude
     *  @param col  the column to exclude
     */
    def sliceExclude (row: Int, col: Int): $RLEMATRIX =
    {
        $RLEMATRIX (for (i <- range2 if i != col) yield $RLEVECTOR (for (j <- range1 if j != row) yield v(i)(j))) 
    } // sliceExclude

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Select rows from 'this' matrix according to the given index/basis.
     *  @param rowIndex  the row index positions (e.g., (0, 2, 5))
     */
    def selectRows (rowIndex: Array [Int]): $RLEMATRIX =
    { 
        val c = new $RLEMATRIX (rowIndex.length, dim2)
        for (i <- c.range1) c(i) = this (rowIndex (i))
        c
    } // selectRows

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Get column 'col' from the matrix, returning it as a vector.
     *  @param col   the column to extract from the matrix
     */
    def col (col: Int): $RLEVECTOR =  v(col)

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Get column 'col' from the matrix, returning it as a vector.
     *  @param col   the column to extract from the matrix
     *  @param from  the position to start extracting from
     */
    def col (col: Int, from: Int = 0): $VECTOR =
    {
        val u = new $VECTOR (dim1 - from)
        for (i<- from until dim1) u(i-from) = v(col)(i)
        u
    } // col

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Set column 'col' of the matrix to a vector.
     *  @param col  the column to set
     *  @param u    the vector to assign to the column
     */
    def setCol (col: Int,u: $VECTO) 
    { 
        v (col) = if (u.isInstanceOf [$RLEVECTOR]) u.asInstanceOf [$RLEVECTOR] 
                  else $RLEVECTOR (u) 
    } // setCol

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Select columns from 'this' matrix according to the given index/basis.
     *  Ex: Can be used to divide a matrix into a basis and a non-basis.
     *  @param colIndex  the column index positions (e.g., (0, 2, 5))
     */
    def selectCols (colIndex: Array [Int]): $RLEMATRIX =
    {
        val c = new $RLEMATRIX (dim1, colIndex.length)
        for (j <- c.range2) c.v(j) = v(colIndex (j))
        c
    } // selectCols

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Transpose 'this' matrix (columns => rows).
     */
    def t: $RLEMATRIX = $RLEMATRIX (for (i <- range1) yield this(i))

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Concatenate (row) vector 'u' and 'this' matrix, i.e., prepend 'u' to 'this'.
     *  @param u  the vector to be prepended as the new first row in new matrix
     */
    def +: (u: $VECTO): $RLEMATRIX =
    {  
        if (u.dim != dim2) flaw ("+:", "vector does not match row dimension")
        $RLEMATRIX (for (i <- range2) yield 
            $RLEVECTOR (for (j <- 0 until (dim1 + 1)) yield if (j == 0) u(i) else v(i)(j-1)))
    } // +:

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Concatenate (column) vector 'u' and 'this' matrix, i.e., prepend 'u' to 'this'.
     *  @param u  the vector to be prepended as the new first column in new matrix
     */
    def +^: (u: $VECTO): $RLEMATRIX = 
    {
        val m = u match {
            case _: $RLEVECTOR => u.asInstanceOf [$RLEVECTOR] +: v
            case _             => $RLEVECTOR (for (i <- u.range) yield u(i)) +: v
        } // match     
        new $RLEMATRIX (dim1, dim2 + 1, m) 
    } // +^:
    
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Concatenate 'this' matrix and (row) vector 'u', i.e., append 'u' to 'this'.
     *  @param u  the vector to be appended as the new last row in new matrix
     */
    def :+ (u: $VECTO): $RLEMATRIX =
    {  
        if (u.dim != dim2) flaw (":+", "vector does not match row dimension")
        $RLEMATRIX (for (i <- range2) yield 
            $RLEVECTOR (for (j <- 0 until (dim1 + 1)) yield if (j < dim1) v(i)(j) else u(i)))
    } // :+
    
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Concatenate 'this' matrix and (column) vector 'u', i.e., append 'u' to 'this'.
     *  @param u  the vector to be appended as the new last column in new matrix
     */
    def :^+ (u: $VECTO): $RLEMATRIX =
    {
        val m = u match {
            case _: $RLEVECTOR => v :+ u.asInstanceOf [$RLEVECTOR]
            case _             => v :+ $RLEVECTOR (for (i <- u.range) yield u(i)) 
        } // match     
        new $RLEMATRIX (dim1, dim2 + 1, m) 
    } // :^+
    
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Concatenate (row-wise) 'this' matrix and matrix 'b'. FIX
     *  @param b  the matrix to be concatenated as the new last rows in new matrix
     */
    def ++ (b: $MATRI): $RLEMATRIX =
    {
        if (b.dim2 != dim2) flaw ("++", "matrix b does not match row dimension")
        val c = new $MATRIX (dim1 + b.dim1, dim2)
        for (i <- c.range1) c(i) = if (i < dim1) this(i) else b(i - dim1)
        $RLEMATRIX (c)
    } // ++
    
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Concatenate (column-wise) 'this' matrix and matrix 'b'.
     *  @param b  the matrix to be concatenated as the new last columns in new matrix
     */
    def ++^ (b: $MATRI): $RLEMATRIX =
    {
        if (b.dim1 != dim1) flaw ("++^", "matrix b does not match column dimension")       
        val m = b match {
        case _: $RLEMATRIX => v ++ b.asInstanceOf [$RLEMATRIX]()
        case _             => v ++ $RLEMATRIX (b.asInstanceOf [$MATRIX])()
        } // match
        new $RLEMATRIX (dim1, dim2 + dim2, m)
    } // ++^
   
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Add 'this' matrix and matrix 'b'.
     *  @param b  the matrix to add (requires leDimensions)
     */
    def + (b: $MATRI): $RLEMATRIX = 
    {
         val c = new $RLEMATRIX (dim1, dim2) 
         b match {
         case _: $RLEMATRIX => for (i <- range2) c.setCol (i, col(i) + b.col(i))                                                             
         case _             => for (i <- range1; j <- range2) c(i, j) = this(i, j) + b(i, j)                                                              
         } // match
         c
    } // +

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Add 'this' matrix and scalar 'x'.
     *  @param x  the scalar to add
     */
    def + (x: $BASE): $RLEMATRIX = 
    {
        val c = new $RLEMATRIX (dim1, dim2)
        for (i <- range2) c.v(i) = col(i) + x 
        c
    } // +

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Add 'this' matrix and vector 'u'.
     *  @param u  the matrix to add (requires leDimensions)
     */
    def + (u: $VECTO): $MATRI = 
    {
        val c = new $RLEMATRIX (dim1, dim2)
        for (i <- range2) c.v(i) = v(i) + u(i)
        c
    } // +

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Add in-place 'this' matrix and scalar 'x'.
     *  @param x  the scalar to add
     */
    def += (x: $BASE): $MATRI = 
    {
        for (i <- range2) v(i) += x
        this
    } // +=

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Add in-place 'this' matrix and vector 'u'.
     *  @param u  the vector to add
     */
    def += (u: $VECTO): $MATRI = 
    {
        for (i <- range2) v(i) += u(i)
        this
    } // +=
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Add in-place 'this' matrix and matrix 'b'.
     *  @param b  the matrix to add (requires 'leDimensions')
     */
    def += (b: $MATRI): $MATRI = 
    {
        b match {
        case _: $RLEMATRIX => for (i <- range2) col(i) += b.col(i)
        case _             => for (i <- range1; j <- range2) v(i)(j) += b(i, j) 
        } // match     
        this
    } // +=
    
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Get size of each column of 'this' RleMatrix
     */
    def csize: VectorI = VectorI (for (j <- range2) yield v(j).csize) 

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Divide 'this' matrix by scalar 'x'.
     *  @param x the scalar to divide by
     */
    def / (x: $BASE): $MATRI = 
    {
        val c = new $RLEMATRIX (dim1, dim2)
        for (i <- range2) c.v(i) = v(i) / x
        c
    } // /

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Divide in-place 'this' matrix by scalar 'x'.
     *  @param x  the scalar to divide by
     */
    def /= (x: $BASE): $MATRI = 
    {
        for (i <- range2) v(i) /= x
        this
    } // /=

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** From 'this' matrix subtract scalar 'x'.
     *  @param x the scalar to subtract
     */
    def - (x: $BASE): $MATRI = 
    {
        val c = new $RLEMATRIX (dim1, dim2)
        for (i <- range2) c.v(i) = v(i) - x
        c
    } // -

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** From 'this' matrix subtract vector 'u'.
     *  @param u the vector to subtract
     */
    def - (u: $VECTO): $RLEMATRIX = 
    {
        val c = new $RLEMATRIX (dim1, dim2)
        for (i <- range2) c.v(i) = v(i) - u(i)
        c
    } // -
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** From 'this' matrix subtract matrix 'b'.
     *  @param b the matrix to subtract
     */
    def - (b: $MATRI): $MATRI = 
    {
         val c = new $RLEMATRIX (dim1, dim2) 
         b match {
         case _: $RLEMATRIX => for (i <- range2) c.setCol (i, col(i) - b.col(i))                                                             
         case _             => for (i <- range1; j <- range2) c(i, j) = this(i, j) - b(i, j)                                                              
         } // match
         c
    } // -
    
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** From 'this' matrix subtract in-place scalar 'x'.
     *  @param x  the scalar to subtract
     */
    def -= (x: $BASE): $MATRI = 
    {
        for (i <- range2) v(i) -= x
        this
    } // -=

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** From 'this' matrix subtract in-place vector 'u'.
     *  @param u the vector to subtract
     */
    def -= (u: $VECTO): $MATRI = 
    {
        for (i <- range2) v(i) -= u(i)
        this
    } // -=

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** From 'this' matrix subtract in-place matrix 'b'.
     *  @param b  the matrix to subtract (requires 'leDimensions')
     */
    def -= (b: $MATRI): $MATRI = 
    {
        b match {
        case _: $RLEMATRIX => for (i <- range2) col(i) -= b.col(i)
        case _             => for (i <- range1; j <- range2) v(i)(j) -= b(i, j) 
        } // match     
        this
    } // -=

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Raise 'this' matrix to the 'p'th power (for some integer 'p' >= 2).
     *  FIX - make compatible with imple in BldMatrix
     *  @param p  the power to raise 'this' matrix to
     */  
    def ~^ (p: Int): $RLEMATRIX = 
    {
        if (p < 2)        flaw ("~^", "p must be an integer >= 2")
        if (dim1 != dim2) flaw ("~^", "Only defined on square matrices")

        var t    = p
        var res  = new $RLEMATRIX (dim1, dim2)
        var temp = new $RLEMATRIX (dim1, dim2)
        for (i <- range1; j <- range2) {
            temp(i, j) = this(i, j)
            res(i, j)  = if (i == j) $ONE else $ZERO
        } // for
        while (t > 0) {
            if (t % 2 == 1) res *= temp
            temp *= temp
            t = t / 2
        } // while
        res
    } // ~^

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Return the lower triangular of 'this' matrix (rest are zero).
     */ 
    def lowerT: $RLEMATRIX = 
    {
        var x = new $RLEMATRIX (dim1, dim2)
        for (i <- range1; j <- 0 to i) x(i, j) = this(i, j)
        x
    } // lowerT
    
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Use partial pivoting to find a maximal non-zero pivot and return its row
     *  index, i.e., find the maximum element '(k, i)' below the pivot '(i, i)'.
     *  @param a  the matrix to perform partial pivoting on
     *  @param i  the row and column index for the current pivot
     */
    private def partialPivoting (a: $RLEMATRIX, i: Int): Int =
    {
        var max  = a.v(i)(i)         // initially set to the pivot
        var kMax = i                 // initially the pivot row

        for (k <- i + 1 until a.dim1 if ABS (a.v(k)(i)) > max) {
            max  = ABS (a.v(k)(i))
            kMax = k
        } // for

        if (kMax == i) {
            flaw ("partialPivoting", "unable to find a non-zero pivot for row " + i)
        } // if
        kMax
    } // partialPivoting

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Solve for 'x' using back substitution in the equation 'u*x = y' where
     *  'this' matrix ('u') is upper triangular (see 'lud' above).
     *  @param y  the constant vector
     */
    def bsolve (y: $VECTO): $RLEVECTOR = 
    {
        var x = new $RLEVECTOR (dim2)
        for (i <- dim1 - 1 to 0 by -1) {
            var sum = $ZERO
            for (j <- i + 1 until dim2) sum += this(i, j) * x(j)
            x(i) = (y(i) - sum) / this(i, i)
        } // for
        x
    } // bsolve

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Return the upper triangular of 'this' matrix (rest are zero).
     */ 
    def upperT: $RLEMATRIX = 
    {
        var x = $RLEMATRIX (new $MATRIX (dim1, dim2))
        for (i <- range1; j <- i until dim2) x(i, j) = this(i, j)
        x
    } // upperT

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Create an 'm-by-n' identity matrix I (ones on main diagonal, zeros elsewhere).
     *  If 'n' is <= 0, set it to 'm' for a square identity matrix.
     *  FIX: store as a diagonal matrix.
     *  @param m  the row dimension of the matrix
     *  @param n  the column dimension of the matrix (defaults to 0 => square matrix)
     */
    def eye (m: Int, n: Int = 0): $RLEMATRIX =
    {
        val nn = if (n <= 0) m else n             // square matrix, if n <= 0
        val mn = if (m <= nn) m else nn           // length of main diagonal
        val c = new $RLEMATRIX (m, nn)
        for (i <- 0 until mn) c(i, i) = 1
        c
    } // eye
    
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Multiply 'this' matrix by (column) vector 'u'
     *  @param u  the vector to multiply by
     */
    def mul2 (u: $RLEVECTOR): $RLEVECTOR =
    {      
        val c = new $RLEVECTOR (dim1)
        for (i <- range1) {
            var sum = $ZERO
            for (k <- range2) sum += this(i, k) * u(k)
            c(i) = sum
        } // for
        c
    } // mul2
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Multiply 'this' matrix by (column) vector 'u'
     *  @param u  the vector to multiply by
     */
    def * (u: $VECTO): $VECTO = 
    {
        if (dim2 > u.dim) flaw ("*", "matrix * vector - vector dimension too small")

        u match {
        case _: $RLEVECTOR => mul2 (u.asInstanceOf [$RLEVECTOR])
        case _             => mul (this, u)
        } // match
    } // *

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Multiply 'this' matrix by scalar 'x'.
     *  @param x  the scalar to multiply by
     */
    def * (x: $BASE): $RLEMATRIX = 
    {
        val c = new $RLEMATRIX (dim1, dim2)
        for (i <- range2) c.v(i) = v(i) * x
        c
    } // *

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Multiply in-place 'this' matrix by matrix 'x'
     *  @param x  the matrix to multiply by 
     */
    def *= (x: $BASE): $RLEMATRIX = 
    {
        for (i <- range2) v(i) *= x
        this
    } // *=

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Multiply 'this' matrix by matrix 'b'.
     *  @param b  the matrix to multiply by (requires 'sameCrossDimensions')
     */
    def * (b: $MATRI): $MATRI =
    {
        b match {
        case _: $RLEMATRIX => t mdot b.asInstanceOf [$RLEMATRIX]
        case _             => toDense * b
        } // match
    } // *

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Multiply in-place 'this' matrix by matrix 'b'
     *  @param b  the matrix to multiply by (requires square and 'sameCrossDimensions')
     */
    def *= (b: $MATRI): $MATRI = 
    {
        if (! b.isSquare)   flaw ("*=", "matrix 'b' must be square")
        if (dim2 != b.dim1) flaw ("*=", "matrix *= matrix - incompatible cross dimensions")
        
        val temp = new $RLEMATRIX (b.dim1, b.dim2)
        for (i <- 0 until b.dim1; j <- 0 until b.dim2) temp(i, j) = b(i, j)
        for (i <- range1) {
            val row_i = new $VECTOR (dim2)
            for (j <- range2) row_i(j) = this(i, j)
            for (j <- 0 until b.dim2) {
                var sum = $ZERO
                for (k <- range2) sum += (row_i(k) * temp(k, j))
                this(i, j) = sum
            } // for
        } // for
        this
    } // *=

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Multiply 'this' matrix by vector 'u' to produce another matrix 'a_ij * u_j'.
     *  E.g., multiply a matrix by a diagonal matrix represented as a vector.
     *  @param u  the vector to multiply by
     */
    def ** (u: $VECTO): $MATRI = 
    {
        var c = new $RLEMATRIX (dim1,dim2)
        for (i <- range1; j <- range2) c(i, j) = this(i, j) * u(j)
        c
    } // **

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Multiply in-place 'this' matrix by vector 'u' to produce another matrix 'a_ij * u_j'.
     *  @param u  the vector to multiply by
     */
    def **= (u: $VECTO): $MATRI = 
    {
        for (i <- range1; j <- range2) this(i, j) = this(i, j) * u(j)
        this
    } // **=

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Clean values in 'this' matrix at or below the threshold 'thres' by setting
     *  them to zero.  Iterative algorithms give approximate values and if very close
     *  to zero, may throw off other calculations, e.g., in computing eigenvectors.
     *  @param thres     the cutoff threshold (a small value)
     *  @param relative  whether to use relative or absolute cutoff
     */
    def clean (thres: Double = TOL, relative: Boolean = true): $MATRI = 
    {
        val s = if (relative) mag else $ONE              // use matrix magnitude or 1
        for (i <- range1; j <- range2) if (ABS (this(i, j)) <= thres * s) this(i, j) = $ZERO
        this
    } // clean

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the determinant of 'this' matrix.  The value of the determinant
     *  indicates, among other things, whether there is a unique solution to a
     *  system of linear equations (a nonzero determinant).
     */ 
    def det: $BASE = 
    {
        if (dim1 != dim2) flaw ("det", "determinant only works on square matrices")
        
        var sum = $ZERO
        var b: $MATRIX = null
        for (j <- range2) {
            b = (this.toDense).sliceExclude (0, j)   // the submatrix that excludes row 0 and column j
            sum += (if (j % 2 == 0) this(0, j) * (if (b.dim1 == 1) b(0,0) else b.det)
                    else           -this(0, j) * (if (b.dim1 == 1) b(0,0) else b.det))
        } // for 
        sum
    } // det

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Form a matrix '[Ip, this, Iq]' where Ir is a 'r-by-r' identity matrix, by
     *  positioning the three matrices 'Ip', 'this' and 'Iq' along the diagonal.
     *  Fill the rest of matrix with zeros.
     *  @param p  the size of identity matrix Ip
     *  @param q  the size of identity matrix Iq
     */
    def diag (p: Int,q: Int = 0): $MATRI = 
    {
        if (! isSquare) flaw ("diag", "'this' matrix must be square")
        var c = new $RLEMATRIX (p + dim1 + q, p + dim2 + q)
        var Ip = eye (p)
        var Iq = eye (q)
        c = Ip diag (this) diag (Iq)
        c
    } // diag

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Combine 'this' matrix with matrix 'b', placing them along the diagonal and
     *  filling in the bottom left and top right regions with zeros; '[this, b]'.
     *  @param b  the matrix to combine with 'this' matrix
     */
    def diag (b: $MATRI): $RLEMATRIX = 
    {
        var c = new $RLEMATRIX (dim1 + b.dim1, dim2 + b.dim2)
        var x1 = new $RLEVECTOR (b.dim2)
        for (i <- range1) c(i) = this(i) ++ x1
        val x2 = new $RLEVECTOR (dim2)
        for (i <- 0 until b.dim1) c(i + dim1) = x2 ++ b(i)
        c
    } // diag

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the dot product of 'this' matrix and vector 'b', by first transposing
     *  'this' matrix and then multiplying by 'b' (i.e., 'a dot u = a.t * b').
     *  @param b  the vector to multiply by (requires same first dimensions)
     */
    def dot (b: $VECTO): $VECTO = 
    {
        if (dim1 != b.dim) flaw ("dot", "matrix dot vector - incompatible first dimensions")
        val c = new $VECTOR (dim2)
        val at = this.t
        for (i <- range2) {
            var sum = $ZERO
            for (j <- range1) sum += at(i, j) * b(j)
            c(i) = sum
        } // for
        c
    } // dot

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the dot product of 'this' matrix and matrix 'b'. Results in a Vector.
     *  @param b  the matrix to multiply by (requires same first dimensions)
     */
    def dot (b: $MATRI): $RLEVECTOR = 
    {
        if (dim1 != b.dim1) flaw ("dot", "incompatible")        
        $RLEVECTOR (for (j <- range2) yield  v(j) dot b.col(j))
    } // dot
    
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the dot product of 'this' matrix and matrix 'b'. Results in a Vector.
     *  @param b  the matrix to multiply by (requires same first dimensions)
     */
    def dot (b: $RLEMATRIX): $RLEVECTOR = 
    {
        if (dim1 != b.dim1) flaw ("dot", "incompatible")        
        $RLEVECTOR (for (j <- range2) yield  v(j) dot b.v(j))
    } // dot
    
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the matrix dot product of 'this' matrix and matrix 'b'.
     *  @param b  the matrix to multiply by (requires same first dimensions)
     */
    def mdot (b: $MATRI): $RLEMATRIX = 
    {   
        if (dim1 != b.dim1) flaw ("mdot", "incompatible first dimensions")
        val vv = Array.ofDim [$RLEVECTOR] (d2)   
        for (j <- b.range2) vv(j) = $RLEVECTOR (for (i <- range2) yield  v(i) dot b.col(j))
        new $RLEMATRIX (dim2, b.dim2, vv)
    } // mdot
    
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the matrix dot product of 'this' matrix and matrix 'b'.
     *  @param b  the matrix to multiply by (requires same first dimensions)
     */
    def mdot (b: $RLEMATRIX): $RLEMATRIX = 
    {   
        if (dim1 != b.dim1) flaw ("mdot", "incompatible first dimensions")
        val vv = Array.ofDim [$RLEVECTOR] (d2)   
        for (j <- b.range2) vv(j) = $RLEVECTOR (for (i <- range2) yield  v(i) dot b.v(j))
        new $RLEMATRIX (dim2, b.dim2, vv)
    } // mdot
  

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Get the 'k'th diagonal of 'this' matrix.
     *  @param k  how far above the main diagonal, e.g., (-1, 0, 1) for (sub, main, super)
     */
    def getDiag (k: Int = 0): $RLEVECTOR = 
    {
        var x = new $RLEVECTOR (1)
        var i, j = 0
        if (k >= 0) { i = 0;      j = k }
        else        { i = -1 * k; j = 0 }
        x(0) = this(i, j)
        i += 1; j += 1    
        while (i < dim1 && j < dim2) {
            x = x ++ this(i, j)
            i += 1; j += 1       
        } // while
        x
    } // getDiag

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Invert 'this' matrix (requires a square matrix) and use partial pivoting.
     */
    def inverse: $MATRI = 
    {
        var b = new $RLEMATRIX (dim1, dim2)
        var u = eye (dim1)
        for (i <- range1; j <- range2) b(i, j) = this(i, j)
        b = b ++^ u
        b.reduce_ip
        b.sliceCol (dim2, 2 * dim2)
    } // inverse

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Invert in-place 'this' matrix (requires a square matrix) and uses partial pivoting.
     *  Note: this method turns the original matrix into the identity matrix.
     *  The inverse is returned and is captured by assignment.
     */
    def inverse_ip: $MATRI = 
    {
        var b = this
        var u = eye (dim1)
        b = b ++^ u
        b.reduce_ip
        b.sliceCol (dim2, 2 * dim2)
    } // inverse_ip

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Check whether 'this' matrix is rectangular (all rows have the same number
     *  of columns).
     */
    def isRectangular: Boolean = 
    {
        for (j <- 0 until dim2) if (v(j).size != dim1) return false
        true
    } // isRectangular

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Factor 'this' matrix into the product of lower and upper triangular
     *  matrices '(l, u)' using the 'LU' Factorization algorithm.
     */
    def lud: Tuple2 [$RLEMATRIX, $RLEMATRIX] = 
    {
        val l = new $RLEMATRIX (dim1, dim2)
        val u = new $RLEMATRIX (dim1, dim2)
        for (i <- range1) l(i,i) = 1
        for (j <- range2) u(0, j) = this(0, j)
        for (i <- 1 until dim1) l(i,0) = this(i, 0) / u(0, 0)
        for (i <- 1 until dim1; j <- 1 until dim2) {
            var sum = $ZERO
            if (i > j) {    // find l(i, j)
                for (k <- 0 until j) sum += l(i, k) * u(k, j)
                l(i, j) = (this(i, j) - sum) / u(j, j)
            } // if
            if (i <= j) {   // find u(i, j)
                 for (k <- 0 until j) sum += l(i, k) * u(k, j)
                 u(i, j) = this(i, j) - sum
            } // if
        } // for
        Tuple2 (l, u)
    } // lud

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Factor in-place 'this' matrix into the product of lower and upper triangular
     *  matrices '(l, u)' using the 'LU' Factorization algorithm.
     */
    def lud_ip: ($RLEMATRIX, $RLEMATRIX) = 
    {
        var w = new $VECTOR (dim1)
        for (k <- range1) {
            for (j <- k+1 until dim2) w(j) = this(k, j)
            for (i <- k+1 until dim1) {
                var a = this(i, k) / this(k, k)
                this(i,k) = a
                for (j <- k+1 until dim2) this(i, j) = this(i, j) - (a * w(j))
            } // for
        } // for
        var l = new $RLEMATRIX (dim1, dim2)
        for (i <- range1; j <- 0 to i) {         
            if (i == j) l(i, j) = 1
            else if (i > j) {
                l(i, j) = this(i, j)
                this(i, j) = 0
            } // if
        } // for
        (l, this)
    } // lud_ip

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Find the maximum element in 'this' matrix.
     *  @param e  the ending row index (exclusive) for the search
     */
    def max (e: Int = dim1): $BASE = 
    {
        if (e <= 0) flaw ("max", "the ending index e can't be negative")
        var x = v(0)(0)
        for (j <- range2) x = MAX (x, v(j) max (e))
        x
    } // max

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Find the minimum element in 'this' matrix.
     *  @param e  the ending row index (exclusive) for the search
     */
    def min (e: Int = dim1): $BASE = 
    {
        if (e <= 0) flaw ("min", "the ending index e can't be negative")
        var x = v(0)(0)
        for (j <- range2) x = MIN (x, v(j) min (e))
        x
    } // min

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the (right) nullspace of 'this' 'm-by-n' matrix (requires 'n = m+1')
     *  by performing Gauss-Jordan reduction and extracting the negation of the
     *  last column augmented by 1.
     *  <p>
     *      nullspace (a) = set of orthogonal vectors v s.t. a * v = 0
     *  <p>
     *  The left nullspace of matrix 'a' is the same as the right nullspace of 'a.t'.
     *  FIX: need a more robust algorithm for computing nullspace (@see Fac_QR.scala).
     *  FIX: remove the 'n = m+1' restriction.
     *  @see http://ocw.mit.edu/courses/mathematics/18-06sc-linear-algebra-fall-2011/ax-b-and-the-four-subspaces
     *  @see /solving-ax-0-pivot-variables-special-solutions/MIT18_06SCF11_Ses1.7sum.pdf
     */ 
    def nullspace: $VECTO = 
    {
        if (dim2 != dim1 + 1) flaw ("nullspace", "requires n(columns) = n(rows) + 1")
        reduce.col (dim2 - 1) * -$ONE ++ $ONE
    } // nullspace

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute in-place the (right) nullspace of 'this' 'm-by-n' matrix (requires 'n = m+1')
     *  by performing Gauss-Jordan reduction and extracting the negation of the
     *  last column augmented by 1.
     *  <p>
     *      nullspace (a) = set of orthogonal vectors v s.t. a * v = 0
     *  <p>
     *  The left nullspace of matrix 'a' is the same as the right nullspace of 'a.t'.
     *  FIX: need a more robust algorithm for computing nullspace (@see Fac_QR.scala).
     *  FIX: remove the 'n = m+1' restriction.
     *  @see http://ocw.mit.edu/courses/mathematics/18-06sc-linear-algebra-fall-2011/ax-b-and-the-four-subspaces
     *  @see /solving-ax-0-pivot-variables-special-solutions/MIT18_06SCF11_Ses1.7sum.pdf
     */
    def nullspace_ip: $VECTO = 
    {
        if (dim2 != dim1 + 1) flaw ("nullspace_ip", "requires n(columns) = n(rows) + 1")
        reduce_ip ()
        col (dim2 - 1) * -$ONE ++ $ONE
    } // nullspace_ip
    
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Use Gauss-Jordan reduction on 'this' matrix to make the left part embed an
     *  identity matrix.  A constraint on this 'm-by-n' matrix is that 'n >= m'.
     *  It can be used to solve 'a * x = b': augment 'a' with 'b' and call reduce.
     *  Takes '[a | b]' to '[I | x]'.
     */
    def reduce: $RLEMATRIX = 
    {
        if (dim2 < dim1) flaw ("reduce", "requires n(columns) >= n(rows)")
            
        val b = new $RLEMATRIX (dim1, dim2)
        for (i <- range1; j <- range2) b(i, j) = this(i, j)
        for (i <- range1) {
            var x = b(i,i)
            if (x == 0) flaw ("reduce", "unable to find a non-zero pivot in row "+i)
            for (j <- range2) b(i, j) = b(i, j) / x
            for (k <- range1) {
                if (k != i && b(k, i) != 0) {
                    var mul = b(k, i)
                    for (l <- range2) b(k, l) -= mul * b(i, l)
                } // if
            } // for
        } // for
        b
    } // reduce

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Use Gauss-Jordan reduction in-place on 'this' matrix to make the left part
     *  embed an identity matrix.  A constraint on this 'm-by-n' matrix is that 'n >= m'.
     *  It can be used to solve 'a * x = b': augment 'a' with 'b' and call reduce.
     *  Takes '[a | b]' to '[I | x]'.
     */
    def reduce_ip ()
    {
        if (dim2 < dim1) flaw ("reduce", "requires n (columns) >= m (rows)")
        
        val b = this
        for (i <- range1) {
            var pivot = b(i,i)
            if (pivot =~ 0) {
                val k = partialPivoting (b, i)  // find the maxiumum element below pivot
                b.swap (i, k, i)                // in b, swap rows i and k from column i
                pivot = b.v(i)(i)               // reset the pivot
            } // if
            for (j <- range2) b(i, j) = b(i, j) / pivot
            for (k <- range1) {
                if (k != i && b(k, i) != 0) {
                    var mul = b(k, i)
                    for (l <- range2) b(k, l) -= mul * b(i, l)
                } // if
            } // for
        } // for
    } // reduce_ip

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Set the main diagonal of 'this' matrix to the scalar 'x'.
     *  @param x  the scalar to set the diagonal to
     */
    def setDiag (x: $BASE)
    {
        val diagRange = if (dim1 < dim2) 0 until dim1 else 0 until d2
        for (i <- diagRange) v(i)(i) = x
    } // setDiag

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Set the 'k'th diagonal of 'this' matrix to the vector 'u'.  Assumes 'dim2 >= dim1'.
     *  @param u  the vector to set the diagonal to
     *  @param k  how far above the main diagonal, e.g., (-1, 0, 1) for (sub, main, super)
     */
    def setDiag (u: $VECTO, k: Int = 0)
    {
        var (i, j, z) = (0, 0, 0)
        var dsize = MIN (dim1, dim2)
        if (k > 0) {
            i = 0; j = k
            dsize = MIN (dim1, dim2 - j)
        } // if
        if (k < 0) {
            i = -k; j = 0           
            dsize = MIN (dim1 - i, dim2)
        } // if
        
        if (dsize != u.size) flaw ("setDiag","Vector 'u' must contain " + dsize + " element(s)")
        
        while (i < dim1 && j < dim2) {
            this(i, j) = u(z)
            z += 1; i += 1; j += 1;
        } // while
    } // setDiag

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Solve for 'x' in the equation 'l*u*x = b'
     *  @param l  the lower triangular matrix
     *  @param u  the upper triangular matrix
     *  @param b  the constant vector
     */
    def solve (l: $MATRI,u: $MATRI, b: $VECTO): $RLEVECTOR = 
    {
        var y = new $RLEVECTOR (l.dim2)
        for (i <- 0 until y.dim) {
            var sum = $ZERO
            for (j <- 0 until i) sum += l(i, j) * y(j)
            y(i) = b(i) - sum
        } // for
        u.bsolve (y).asInstanceOf [$RLEVECTOR]
    } // solve

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Solve for 'x' in the equation 'l*u*x = b'
     *  @param lu  the lower and upper triangular matrices
     *  @param b   the constant vector
     */
    def solve (lu: Tuple2 [$MATRI, $MATRI], b: $VECTO): $RLEVECTOR = solve (lu._1, lu._2, b)

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Solve for 'x' in the equation 'l*u*x = b' where 'l = this'.  Requires
     *  'l' to be lower triangular.
     *  @param u  the upper triangular matrix
     *  @param b  the constant vector
     */
    def solve (u: $MATRI, b: $VECTO): $RLEVECTOR = solve (this, u, b)

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Solve for 'x' in the equation 'a*x = b' where 'a' is 'this' matrix.
     *  @param b  the constant vector.
     */ 
    def solve (b: $VECTO): $RLEVECTOR = solve (lud, b)
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the sum of 'this' matrix, i.e., the sum of its elements.
     */
    def sum: $BASE = 
    {
        var sum = $ZERO
        for (i <- range2) sum += v(i).sum
        sum
    } // sum

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the 'abs' sum of 'this' matrix, i.e., the sum of the absolute value
     *  of its elements.  This is useful for comparing matrices '(a - b).sumAbs'.
     */
    def sumAbs: $BASE = 
    {
        var sum = $ZERO
        for (i <- range2) sum += v(i).sumAbs
        sum
    } // sumAbs

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the sum of the lower triangular region of 'this' matrix.
     */
    def sumLower: $BASE = 
    {
        var sum = $ZERO
        for (i <- range2; j <- i + 1 until dim1) sum += v(i)(j)
        sum
    } // sumLower

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the trace of 'this' matrix, i.e., the sum of the elements on the
     *  main diagonal.  Should also equal the sum of the eigenvalues.
     *  @see Eigen.scala
     */
    def trace: $BASE = 
    {
        if ( ! isSquare) flaw ("trace", "trace only works on square matrices")
        var sum = $ZERO
        for (i <- range1) sum += v(i)(i)
        sum
    } // trace

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Write 'this' matrix to a CSV-formatted text file with name 'fileName'.
     *  @param fileName  the name of file to hold the data
     */
    def write (fileName: String)
    {
        val out = new PrintWriter (fileName)
        for (i <- range1) {
            for (j <- range2) {
                out.print (this(i, j))
                if (j < dim2 - 1) out.print (",")
            } // for
            out.println ()
        } // for
        out.close ()
    } // write

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Convert 'this' real ($BASE precision) matrix to a string.
     */
    override def toString: String = 
    {
        val sb = new StringBuilder ("\n$RLEMATRIX (")
        if (dim1 == 0) return sb.append (")").mkString
        for (j <- range2) {
            if (j == 0) sb.append ("\t"+v(j) + "\n")
            else        sb.append ("\t\t"+v(j) + "\n")
        } // for
        sb.replace (sb.length-3, sb.length, ")").mkString
    } // toString
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Override equals to determine whether 'this' vector equals vector 'b'.
     *  @param b  the vector to compare with this
     */
    override def equals (b: Any): Boolean =
    {
        b.isInstanceOf [$RLEMATRIX] && (v.deep equals b.asInstanceOf [$RLEMATRIX].v.deep)
    } // equals

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Must also override hashCode for 'this' vector to be compatible with equals.
     */
    override def hashCode: Int = v.deep.hashCode

} // $RLEMATRIX class

//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** The `$RLEMATRIX` companion object provides operations for `$RLEMATRIX` that don't require
 *  'this' (like static methods in Java).  It provides factory methods for building
 *  matrices from sequences or vectors.
 */
object $RLEMATRIX 
{
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     */
    def apply (x: $RLEVECTOR, xs: $RLEVECTOR*): $RLEMATRIX = 
    {     
        new $RLEMATRIX (xs.length + 1, x.dim, (x +: xs).toArray)    
    } // apply
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     */
    def apply (x: Seq [$RLEVECTOR]): $RLEMATRIX = 
    {
        new $RLEMATRIX (x(0).dim, x.length, x.toArray)
    } // apply
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     */
    def apply (u: Vector [$VECTO]): $RLEMATRIX = 
    {
        val u_dim = u(0).dim
        val x = new $RLEMATRIX (u_dim, u.length)
        for (j <- 0 until u.length) x.setCol (j, u(j)) 
        x
    } // apply

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     */
    def apply (x: $MATRIX): $RLEMATRIX = 
    {
        $RLEMATRIX (for ( j <- 0 until x.dim2) yield $RLEVECTOR (x.col(j)))
    } // apply
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     */
    def mul (at: $RLEMATRIX, u: $VECTO): $RLEVECTOR = 
    {
        $RLEVECTOR (for (i <- 0 until at.dim1) yield at(i) dot u) 
    } // mul

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     */
    def mul (a: $RLEMATRIX, u: $RLEVECTOR): $RLEVECTOR = 
    {
        $RLEVECTOR (for (i <- 0 until a.dim1) yield a(i) dot u)    
    } // mul
       
} // $RLEMATRIX object


//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** The `${RLEMATRIX}Test` object tests the operations provided by `$MATRIX` class.
 *  > run-main scalation.linalgebra.${RLEMATRIX}Test
 */
object ${RLEMATRIX}Test extends App with PackageInfo
{
    for (l <- 1 to 4) {
        println ("\n\tTest $RLEMATRIX on real matrices of dim " + l)
        val x = new $RLEMATRIX (l, l)
        val y = new $RLEMATRIX (l, l)
        x.set (2)
        y.set (3)
        println ("x + y = " + (x + y))
        println ("x - y = " + (x - y))
        println ("x * y = " + (x * y))
        println ("x * 4 = " + (x * 4))
    } // for

    println ("\n\tTest $RLEMATRIX on additional operations")

    val z   = $RLEMATRIX (new $MATRIX ((2, 2), 1, 2,
                                               3, 2))
    val t   = $RLEMATRIX (new $MATRIX ((3, 3), 1, 2, 3,
                                               4, 3, 2,
                                               1, 3, 1))
    val zz  = $RLEMATRIX (new $MATRIX ((3, 3), 3, 1, 0,
                                               1, 4, 2,
                                               0, 2, 5))
    val bz  = $VECTOR (5, 3, 6)
    val b   = $VECTOR (8, 7)
    val lu  = z.lud

    println ("z            = " + z)
    println ("z.t          = " + z.t)
    println ("z.lud        = " + lu)
    println ("z.solve      = " + z.solve (lu._1, lu._2, b))
    println ("zz.solve     = " + zz.solve (zz.lud, bz))
    println ("z.inverse    = " + z.inverse)
    println ("z.inverse_ip = " + z.inverse_ip ())
    println ("t.inverse    = " + t.inverse)
    println ("t.inverse_ip = " + t.inverse_ip ())
    println ("z.inv * b    = " + z.inverse * b)
    println ("z.det        = " + z.det)
    println ("z            = " + z)
    z *= z                             // in-place matrix multiplication
    println ("z squared = " + z)

    val w = $RLEMATRIX (new $MATRIX ((2, 3), 2,  3, 5, 
                                            -4,  2, 3))
    val v = $RLEMATRIX (new $MATRIX ((3, 2), 2, -4, 
                                             3,  2,
                                             5,  3))
    println ("w         = " + w)
    println ("v         = " + v)
    println ("w.reduce  = " + w.reduce)

    println ("right:    w.nullspace = " + w.nullspace)

    println ("check right nullspace = " + w * w.nullspace)
    println ("left:   v.t.nullspace = " + v.t.nullspace)

    for (row <- z) println ("row = " + row.deep)

    val aa = $RLEMATRIX (new $MATRIX ((3, 2), 1, 2,
                                              3, 4,
                                              5, 6))
    val bb = $RLEMATRIX (new $MATRIX ((2, 2), 1, 2,
                                              3, 4))
    val cc = $RLEMATRIX (new $MATRIX ((3, 2), 3, 4,
                                              5, 6,
                                              7, 8))

    println ("aa        = " + aa)
    println ("bb        = " + bb)
    println ("aa * bb   = " + aa * bb)
    println ("aa dot bz = " + (aa dot bz))
    println ("aa.t * bz = " + aa.t * bz)

    println ("aa dot cc   = " + (aa dot cc))
    println ("aa.t * cc   = " + aa.t * cc)

    println ("Dense aa" + aa.toDense)
    println ("Dense bb" + bb.toDense)
    aa *= bb
    println ("aa *= bb  = " + aa)

    val filename = getDataPath + "bb_matrix.csv"
    bb.write (filename)
    println ("bb_csv = " + $MATRIX (filename))

} // ${RLEMATRIX}Test object

"""

// Ending of string holding code template --------------------------------------

//      println (code)

        val writer = new PrintWriter (new File (DIR + _l + RLEMATRIX + ".scalaa"))
        writer.write (code)
        writer.close ()
    } // for

} // BldRleMatrix object

