package edu.umass.cs.iesl.harshal.lexicons

import collection.immutable.{HashSet, HashMap}
import java.io.{FileOutputStream, OutputStreamWriter}

/**
 * @author harshal
 * @date: 5/16/13
 */
object Freebase {
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

  def createOutFile(f:String,prefix:String="") = {
    val splits = f.split("/")
    splits.take(splits.length-1).mkString("/")+"/"+prefix+splits(splits.length-1).split("""\.""")(0)+".lst"
  }

  def main(args:Array[String]){

    val idTitleF = args(0)
    val redirectsF = args(1)
    val fbWpF = args(2)
    val lexFs = args.takeRight(args.length-3)

    val id2title = loadId2Title(idTitleF)
    println("Done loading id2title..")
    val redirect = loadRedirects(redirectsF,title2Id(id2title))
    println("Done loading redirects..")
    val fb2Wiki = loadFbToWikiMap(fbWpF,redirect,id2title)
    println("Done loading fb2wiki..")
    val outLexs = for (f <- lexFs) yield {
      val out = createOutFile(f)
//      try{
//        val writer = new OutputStreamWriter(new FileOutputStream(out),"UTF-8")
//        for (line <- io.Source.fromFile(f).getLines()){
//          val id = line.split("\t")(0)
//          if(fb2Wiki.contains(id)){
//            writer.write(fb2Wiki(id)+"\n")
//          }
//        }
//        writer.close()
//      }
//      catch{
//        case e:Exception => {
//          println("Error while processing file "+f+" : "+e.getMessage)
//          println(e.getStackTraceString)
//        }
//      }
//      println("Done "+f+"...")
      out
    }
    val redirects = redirectSet(redirectsF)
    for (f <- outLexs){
      val out = createOutFile(f,"redirect.")
      try{
        val writer = new OutputStreamWriter(new FileOutputStream(out),"UTF-8")
        for (line <- io.Source.fromFile(f).getLines()){
          if (redirects.contains(line))
            writer.write(redirects(line).mkString("\n")+"\n")
        }
        writer.close()
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

}
