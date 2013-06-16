package edu.umass.cs.iesl.harshal.lexicons

import collection.immutable.{HashSet, HashMap}
import java.io._
import java.util.Properties
import collection.JavaConverters._
import Utils._

/**
 * @author harshal
 * @date: 5/16/13
 */
object Runner {

  val parenthesis = """\(.+?\)"""

  def processWikipedia(wpFile:String,titleIdFile:String,redirectFile:String){
    println("Extracting Title, Id and Redirects from wikipedia dump...")
    val writer1 = new OutputStreamWriter(new FileOutputStream(titleIdFile),"UTF-8")
    val writer2 = new OutputStreamWriter(new FileOutputStream(redirectFile),"UTF-8")
    val wdp = new WikipediaDumpProcessor(wpFile)
    while(wdp.hasNext){
      val page = wdp.next()
      writer1.write(page.title+"\t"+page.id+"\n")
      if(page.isRedirect) { writer2.write(page.title+"\t"+page.redirect.get+"\n") }
    }
    writer1.close()
    writer2.close()
  }

  def extractIdsFromFreebase(freebaseTypesFile:String,outputDir:String)={
    println("Extracting ids for freebase types...")
    val prop = new Properties()
    prop.load(new InputStreamReader(this.getClass.getResourceAsStream("/freebase_types.properties")))
    val types = prop.propertyNames().asScala.map( n => n.toString -> prop.getProperty(n.toString)).toMap
    val extractor = new FreebaseIdExtractor(freebaseTypesFile)
    extractor.extractIdsToFiles(types.values,types.keys,new File(outputDir))
    types.keys
  }

  def loadId2Title(f:String)={
    io.Source.fromFile(f,"UTF-8").getLines().foldLeft(HashMap[String,String]())((map,line)=>{
      val split = line.split("\t")
      map + (split(1) -> split(0))
    })
  }

  def title2Id(id2Title:HashMap[String,String])= id2Title map(_.swap)

  def loadFbToWikiMap(f:String,redirect:HashMap[String,String],id2Title:HashMap[String,String])={
    val map = collection.mutable.HashMap[String,String]()
    for (line <- io.Source.fromFile(f,"UTF-8").getLines()){
      val split = line.split("\t")
      if (redirect.contains(split(1))) map += (split(0)->redirect(split(1)))
      else if (id2Title.contains(split(1))) map += (split(0)->id2Title(split(1)))
      else println("Not found "+split(1))
    }
    map
  }


  def loadRedirects(f:String,title2id:HashMap[String,String])={
    io.Source.fromFile(f,"UTF-8").getLines().foldLeft(HashMap[String,String]())((map,line)=>{
      val split = line.split("\t")
      map + (title2id(split(0)) -> split(1))
    })
  }


  def redirectSet(f:String)={
    val map = collection.mutable.HashMap[String,Set[String]]()
    for (line <- io.Source.fromFile(f).getLines()){
      val split = line.split("\t")
      map(split(1)) = map.getOrElseUpdate(split(1),HashSet[String]()) + split(0)
    }
    map
  }

  def generateLexicons(idTitleF:String,redirectsF:String,
                       fb2WpF:String,fbIdTypeFs:Iterable[String],pathToResourcesFolder:String){
    val id2title = loadId2Title(idTitleF)
    println("Done loading id2title..")
    val redirect = loadRedirects(redirectsF,title2Id(id2title))
    println("Done loading redirects..")
    val fb2Wiki = loadFbToWikiMap(fb2WpF,redirect,id2title)
    println("Done loading fb2wiki..")
    val outLexs = for (f <- fbIdTypeFs) yield {
      val out = createOutFile(pathToResourcesFolder,f)
      val parenFile = createOutFile(pathToResourcesFolder,out,"paren")
      try{
        val writer = new OutputStreamWriter(new FileOutputStream(out),"UTF-8")
        val parenFileWriter = new OutputStreamWriter(new FileOutputStream(parenFile),"UTF-8")
        for (line <- io.Source.fromFile(f).getLines()){
          val id = line.split("\t")(0)
          if(fb2Wiki.contains(id)){
            val title = fb2Wiki(id)
            parenthesis.r.findFirstIn(title) match {
              case Some(_) => {
                val strippedTitle = title.replaceAll("""\s+"""+parenthesis,"");
                parenFileWriter.write(strippedTitle+"\n")
              }
              case None => { writer.write(title+"\n") }
            }
          }
        }
        writer.close()
        parenFileWriter.close()
      }
      catch{
        case e:Exception => {
          println("Error while processing file "+f+" : "+e.getMessage)
          println(e.getStackTraceString)
        }
      }
      println("Done "+f+"...")
      out
    }

    val redirects = redirectSet(redirectsF)
    for (f <- outLexs){
      val out = createOutFile(pathToResourcesFolder,f,"redirect.")
      val parenFile = createOutFile(pathToResourcesFolder,out,"paren")
      try{
        val writer = new OutputStreamWriter(new FileOutputStream(out),"UTF-8")
        val parenFileWriter = new OutputStreamWriter(new FileOutputStream(parenFile),"UTF-8")
        for (line <- io.Source.fromFile(f).getLines()){
          if (redirects.contains(line)){
            for (title <- redirects(line)){
              parenthesis.r.findFirstIn(title) match {
                case Some(_) => {
                  val strippedTitle = title.replaceAll("""\s+"""+parenthesis,"");
                  parenFileWriter.write(strippedTitle+"\n")
                }
                case None => { writer.write(title+"\n") }
              }
            }
          }
        }
        writer.close()
        parenFileWriter.close()
      }
      catch{
        case e:Exception => {
          println("Error while processing file "+f+" : "+e.getMessage)
          println(e.getStackTraceString)
        }
      }
      println("Done "+out+"...")
    }

  }

  def removeParen(files:Seq[String])={
    import java.io._
    val paren = """\s*\(.+?\)$"""
    for (f <- files){
      val writer = new OutputStreamWriter(new FileOutputStream(f+".txt"),"UTF-8")
      for (line <- io.Source.fromFile(f,"UTF-8").getLines()){
        writer.write(line.replaceAll(paren,"")+"\n")
      }
      writer.close()
    }
  }

  def main(args:Array[String]){

    val prop = new Properties()
    prop.load(this.getClass.getResourceAsStream("/config.properties"))

    val wikiDump = prop.getProperty("wikipediaDump")
    val title2IdFile = prop.getProperty("titleToIdFile")
    val redirectsFile = prop.getProperty("redirectsFile")
    processWikipedia(wikiDump,title2IdFile,redirectsFile)

    val fbTypesFile = prop.getProperty("freebaseTypes")
    val fbOutDir = prop.getProperty("freebaseOutputDir")
    val outFiles = extractIdsFromFreebase(fbTypesFile,fbOutDir)

    val fb2Wiki = prop.getProperty("freebaseToWikipedia")
    val lexOutputDir = prop.getProperty("pathToLexiconOutput")
    generateLexicons(title2IdFile,redirectsFile,fb2Wiki,outFiles,lexOutputDir)

  }

}
