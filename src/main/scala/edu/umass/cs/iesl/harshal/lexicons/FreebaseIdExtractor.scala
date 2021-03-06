package edu.umass.cs.iesl.harshal.lexicons

import java.io.File

/**
 * @author harshal
 * @date: 5/19/13
 */
class FreebaseIdExtractor(freebaseFile:String) {

  assert(new File(freebaseFile).exists())

  private def runScript(typeName:String,outputFile:String,outputDir:File)={
    val grep = """grep -P "\t"""+typeName.trim+"""$" """+freebaseFile+" > "+outputFile+".txt"
    val pb = new ProcessBuilder("bash","-c",grep)
    if(outputDir!=null && outputDir.exists()) pb.directory(outputDir)
    val process = pb.start()
    process.waitFor()
  }

  def extractIdsToFiles(types:Iterable[String],outputFiles:Iterable[String]=Seq[String](),outputDir:File=null){
    val typesWithFiles =
      if (outputFiles.size!=0 && outputFiles.size==types.size) { types.zip(outputFiles) }
      else { types.zip(types.map(_.split("""/""").last)) }

    for ((typeName,file) <- typesWithFiles){
      assert(runScript(typeName,file,outputDir)==0,"Grep failed for type "+typeName)
    }
  }
}
