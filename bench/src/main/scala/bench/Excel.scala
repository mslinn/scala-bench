package bench

import org.apache.poi.hssf.usermodel.{HSSFCell, HSSFFont, HSSFRow, HSSFSheet, HSSFWorkbook}
import org.apache.poi.ss.usermodel.{CellStyle, FillPatternType, IndexedColors}

/** See https://poi.apache.org/components/spreadsheet/how-to.html */
object ExcelDemo extends App with Excel {
  cellManager.nextRow()
  cellManager.newHeaderCell("Test Name")
  cellManager.newHeaderCell("Time (seconds)")

  cellManager.nextRow()
  cellManager.newDataCell("m.Array")
  cellManager.newDataCell(20)

  writeFile(".")
}

object CellManager {
  private def defineHeaderStyle(implicit workbook: HSSFWorkbook): CellStyle = {
    val headerStyle: CellStyle = workbook.createCellStyle
    headerStyle.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex)
    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)

    val font: HSSFFont = workbook.createFont
    //font.setFontName("Arial")
    //font.setFontHeightInPoints(12.toShort)
    font.setBold(true)
    font.setColor(IndexedColors.WHITE.getIndex)
    headerStyle.setFont(font)
    headerStyle
  }

  private def defineDataStyle(implicit workbook: HSSFWorkbook): CellStyle = {
    val dataCellStyle = workbook.createCellStyle
    dataCellStyle.setWrapText(true)
    dataCellStyle
  }

  implicit class RichHSSFRow(row: HSSFRow) {
    def newCell(style: CellStyle, columnNumber: Int, value: Double): HSSFCell = {
      val cell: HSSFCell = row.createCell(columnNumber)
      cell.setCellValue(value)
      cell.setCellStyle(style)
      cell
    }

    def newCell(style: CellStyle, columnNumber: Int, value: String): HSSFCell = {
      val cell: HSSFCell = row.createCell(columnNumber)
      cell.setCellValue(value)
      cell.setCellStyle(style)
      cell
    }
  }
}

class CellManager(sheet: HSSFSheet)
                 (implicit workbook: HSSFWorkbook) {
  import bench.CellManager._

  val headerCellStyle: CellStyle = defineHeaderStyle
  val dataCellStyle: CellStyle   = defineDataStyle

  var columnNumber = 0
  var rowNumber = 0
  var row: HSSFRow = _

  def nextRow(): HSSFRow = {
    columnNumber = 0
    rowNumber = rowNumber + 1
    row = sheet.createRow(rowNumber)
    row
  }

  def newHeaderCell(value: String): HSSFCell = {
    val cell = row.newCell(headerCellStyle, columnNumber, value)
    columnNumber = columnNumber + 1
    cell
  }

  def newDataCell(value: String): HSSFCell = {
    val cell = row.newCell(dataCellStyle, columnNumber, value)
    columnNumber = columnNumber + 1
    cell
  }

  def newDataCell(value: Double): HSSFCell = {
    val cell = row.newCell(dataCellStyle, columnNumber, value)
    columnNumber = columnNumber + 1
    cell
  }
}

trait Excel {
  import java.io.{File, FileOutputStream}
  import java.util.Date

  implicit val workbook: HSSFWorkbook = new HSSFWorkbook

  val sheet: HSSFSheet = workbook.createSheet("ScalaBench")
  sheet.setColumnWidth(0, 6000)
  sheet.setColumnWidth(1, 4000)
  val cellManager = new CellManager(sheet)


  def writeFile(pathStr: String)
                       (implicit workbook: HSSFWorkbook): Unit = {
    val df = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
    val fileName: String = s"scala-bench-${ df.format(new Date) }.xls"
    val outputStream = new FileOutputStream(new File(pathStr, fileName))
    workbook.write(outputStream)
  }
}
