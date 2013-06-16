package edu.umass.cs.iesl.harshal.lexicons

import java.io.{FileOutputStream, OutputStreamWriter, File}
import Utils._

/**
 * @author harshal
 * @date: 6/16/13
 */
object ProcessTitlesWithParenthesis {
  val parenthesis = """\(.+?\)"""
  def apply(file:File){
    val parentDir = file.getParent
    val tempFile = new File(parentDir+"/"+System.currentTimeMillis())
    val parenFile = createOutFile(parentDir,file.getAbsolutePath,"paren")
    val parenFileWriter = new OutputStreamWriter(new FileOutputStream(parenFile),"UTF-8")
    val tempFileWriter = new OutputStreamWriter(new FileOutputStream(tempFile),"UTF-8")
    for (title <- io.Source.fromFile(file).getLines()){
      parenthesis.r.findFirstIn(title) match {
        case Some(_) => {
          val strippedTitle = title.replaceAll("""\s+"""+parenthesis,"");
          parenFileWriter.write(strippedTitle+"\n")
        }
        case None => { tempFileWriter.write(title+"\n") }
      }
    }
    parenFileWriter.close()
    tempFileWriter.close()
    assert(file.delete())
    tempFile.renameTo(new File(file.getAbsolutePath))
  }
}
