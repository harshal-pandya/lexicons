package edu.umass.cs.iesl.harshal.lexicons

/**
 * @author harshal
 * @date: 6/16/13
 */
object Utils {
  def createOutFile(dir:String,f:String,prefix:String="",extention:String="txt") = {
    dir+"/"+f.split("/").last.split("""\.""")(0)+"-"+prefix+"."+extention
  }
}
