package processor

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.pow
import kotlin.math.round

data class MyMatrix(val rows: Int, val columns: Int) {
    private val matr = MutableList(rows) {MutableList(columns) {0.0} }

    companion object {
        fun createMatrix(pNumeral: String = ""): MyMatrix {
            print("Enter size of ${if (pNumeral != "") "$pNumeral " else pNumeral}matrix: ")
            val vIndexses = readLine()!!.split(" ").map(String::toInt)
            val rezM= MyMatrix(vIndexses[0],vIndexses[1])
            println("Enter ${if (pNumeral != "") "$pNumeral " else pNumeral}matrix:")
            rezM.fill()
            return rezM
        }
    }

    fun fill() {
        for (iRow in 0 until rows) {
            val strElement = readLine()!!.split(" ").map { it.toDouble() }
            for (iColumn in 0 until columns) matr[iRow][iColumn] = strElement[iColumn]
        }
    }

    // сравнение формата/размера матриц
    private fun eqFormat(vOtherMatrix: MyMatrix) = rows == vOtherMatrix.rows && columns == vOtherMatrix.columns

    // сложение матриц
    fun plusMatrix(vOtherMatrix: MyMatrix) {
        if (!this.eqFormat(vOtherMatrix)) {
            throw IllegalArgumentException("The operation cannot be performed.")
        }
        for (iRow in 0 until rows) for (iColumn in 0 until columns) {
            matr[iRow][iColumn] += vOtherMatrix.matr[iRow][iColumn]
        }
    }

    // печать матрицы
    fun printMatrix() {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.DOWN
        df.decimalFormatSymbols = DecimalFormatSymbols(Locale.ROOT)
        println("The result is:")
        for (iRow in 0 until rows) println(matr[iRow].joinToString(" "))
//        for (iRow in 0 until rows) println(matr[iRow].joinToString(" "){ if (round(it*100) == 0.0) "0" else df.format(it) })
        println()
    }

    // умножение на скаляр
    fun multiplicationScal(pKoef: Double): MyMatrix {
        val rezM = MyMatrix(rows,columns)
        for (iRow in 0 until rows) for (iColumn in 0 until columns) {
            rezM.matr[iRow][iColumn] = matr[iRow][iColumn] * pKoef
        }
        return rezM
    }

    // умножение на матрицу
    fun multiplicationMatrix(vOtherMatrix: MyMatrix): MyMatrix {
        if (columns != vOtherMatrix.rows) throw IllegalArgumentException("The operation cannot be performed.")
        val rezM = MyMatrix(rows, vOtherMatrix.columns)
        for (outRow in 0 until rezM.rows) for (outColumn in 0 until rezM.columns) {
            for (iColumn in 0 until columns) {
                rezM.matr[outRow][outColumn] += matr[outRow][iColumn] * vOtherMatrix.matr[iColumn][outColumn]
            }
        }
        return rezM
    }

    // транспонирование
    fun transpositionMatrixMain(): MyMatrix {
        val rezM = MyMatrix(columns, rows)
        for (iRow in 0 until rows) for (iColumn in 0 until columns) {
            rezM.matr[iColumn][iRow] = matr[iRow][iColumn]
        }
        return rezM
    }

    fun transpositionMatrixSide(): MyMatrix {
        val rezM = MyMatrix(columns, rows)
        for (iRow in 0 until rows) for (iColumn in 0 until columns) {
            rezM.matr[iColumn][iRow] = matr[rows - 1 - iRow][columns - 1 -iColumn]
        }
        return rezM
    }

    fun transpositionMatrixVert(): MyMatrix {
        val rezM = MyMatrix(rows, columns)
        for (iRow in 0 until rows) for (iColumn in 0 until columns) {
            rezM.matr[iRow][iColumn] = matr[iRow][columns - 1 - iColumn]
        }
        return rezM
    }

    fun transpositionMatrixHoriz(): MyMatrix {
        val rezM = MyMatrix(rows, columns)
        for (iRow in 0 until rows) for (iColumn in 0 until columns) {
            rezM.matr[iRow][iColumn] = matr[rows - 1 - iRow][iColumn]
        }
        return rezM
    }

    private fun minorMatrix(pRow: Int, pColumn: Int, mtx: MutableList<MutableList<Double>>): MutableList<MutableList<Double>>
    {
        var tmpI = 0
        val tmpA = MutableList(mtx.size-1) {(MutableList(mtx.size-1) { 0.0 }) }
        for (i in mtx.indices) {
            if (i == pRow) continue
            var tmpJ = 0
            for (j in mtx[i].indices){
                if (j == pColumn) continue
                tmpA[tmpI][tmpJ] = mtx[i][j]
                tmpJ++
            }
            tmpI++
        }
        return tmpA
    }

    fun determinant(): Double {
        if (rows != columns) throw IllegalArgumentException("The operation cannot be performed.")
        if (matr.isEmpty()) return 0.0
        return determinantMatrix(matr)
    }
    private fun determinantMatrix(mtx: MutableList<MutableList<Double>>): Double {
        if (mtx.size == 1) return mtx[0][0]
        var rez = 0.0
        for (n in mtx.indices) {
            rez += mtx[0][n]*determinantMatrix(minorMatrix(0, n, mtx))*((-1.0).pow(n.toDouble()))
        }
        return rez
    }

    // союзная матрица
    private fun alliedMatrix(): MyMatrix {
        val rezM = MyMatrix(rows, columns)
        val tmpM = this.transpositionMatrixMain()
        for (i in tmpM.matr.indices) {
            for (j in tmpM.matr[i].indices){
                rezM.matr[i][j] = (-1.0).pow(i+j) * determinantMatrix(minorMatrix(i, j, tmpM.matr))
            }
        }
        return rezM
    }

    fun inverseMatrix(): MyMatrix?{
        val det = this.determinant()
        if (det == 0.0) {
            println("This matrix doesn't have an inverse.")
            return null
        }
        return this.alliedMatrix().multiplicationScal(1/det)
    }
}

object MatrixMenu {
    const val MAINMENU =
        "1. Add matrices\n" +
        "2. Multiply matrix by a constant\n" +
        "3. Multiply matrices\n" +
        "4. Transpose matrix\n" +
        "5. Calculate a determinant\n" +
        "6. Inverse matrix\n" +
        "0. Exit\n" +
        "Your choice: "

    const val TRANSMENU =
        "\n1. Main diagonal\n" +
        "2. Side diagonal\n" +
        "3. Vertical line\n" +
        "4. Horizontal line\n" +
        "Your choice: "

    fun exec(pMess: String): String {
        print(pMess)
        return readLine()!!
    }
}

fun main() {
    var mA: MyMatrix
    var mB: MyMatrix
    do {
        when (MatrixMenu.exec(MatrixMenu.MAINMENU)) {
            "0" -> break
            "1" -> {
                mA = MyMatrix.createMatrix("first")
                mB = MyMatrix.createMatrix("second")
                try {
                    mA.plusMatrix(mB)
                    mA.printMatrix()
                } catch (e: IllegalArgumentException) {
                    println(e.message)
                }
            }
            "2" -> {
                mA = MyMatrix.createMatrix()
                println("Enter constant: ")
                mA.multiplicationScal(readLine()!!.toDouble()).printMatrix()
            }
            "3" -> {
                mA = MyMatrix.createMatrix("first")
                mB = MyMatrix.createMatrix("second")
                try {
                    mA.multiplicationMatrix(mB).printMatrix()
                } catch (e: IllegalArgumentException) {
                    println(e.message)
                }
            }
            "4" -> {
                when (MatrixMenu.exec(MatrixMenu.TRANSMENU)){
                    "1" -> MyMatrix.createMatrix().transpositionMatrixMain().printMatrix()
                    "2" -> MyMatrix.createMatrix().transpositionMatrixSide().printMatrix()
                    "3" -> MyMatrix.createMatrix().transpositionMatrixVert().printMatrix()
                    "4" -> MyMatrix.createMatrix().transpositionMatrixHoriz().printMatrix()
                    else -> continue
                }
            }
            "5" -> println("The result is:\n${MyMatrix.createMatrix().determinant()}\n")
            "6" -> MyMatrix.createMatrix().inverseMatrix()?.printMatrix()
            else -> continue
        }
    } while (true)
}
