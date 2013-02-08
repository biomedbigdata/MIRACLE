package org.nanocan.rppa.io

import groovy.sql.Sql
import org.nanocan.rppa.layout.LayoutSpot
import org.nanocan.rppa.layout.NoMatchingLayoutException

import org.nanocan.rppa.scanner.Slide
import org.nanocan.rppa.scanner.ResultFileConfig
import org.nanocan.rppa.scanner.Spot
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.StringUtils

/**
 * This service handles the extraction from spot information from an excel sheet using the excel-import plugin.
 * The spots are persisted in the database using groovy sql (for performance reasons)
 */
class SpotImportService {

    //dependencies
    def progressService
    def depositionService
    def dataSourceUnproxied
    def grailsApplication
    def xlsxImportService

    /**
     * Get name of sheets of an excel file
     * @param slideInstance
     * @return
     */
    def getSheets(def slideInstance)
    {
        def resultFile = slideInstance.resultFile
        def filePath = resultFile.filePath
        return xlsxImportService.getSheets(filePath)
    }

    def convertCSV2(String content)
    {
        content = StringUtils.replace(content, ".", "")
        content = StringUtils.replace(content, ",", ".")
        content = StringUtils.replace(content, ";", ",")

        return content
    }

    /**
     * Skip lines and read header, then parse it to array
     * @param content
     * @return
     */
    def extractHeader(def content, def skipLines)
    {
        Scanner scanner = new Scanner(content)

        //skipping lines

        for( int i = 0; i < skipLines; i++ )
        {
            if(scanner.hasNext()) scanner.nextLine()
        }

        //reading and parsing header
        def header = scanner.nextLine()

        scanner.close()

        header = header.split(',')
        header = Arrays.asList(header)

        return (header)
    }

    def createMatchingMap(ResultFileConfig resultFileCfg, List<String> header) {
        def matchingMap = [:]

        if (resultFileCfg) {

            for (String colName : header) {
                def trimmedColName = colName

                //remote leading and tailing quote
                if (colName.startsWith("\"") && colName.endsWith("\""))
                    trimmedColName = colName.substring(1, colName.length() - 1);


                switch (trimmedColName) {
                    case resultFileCfg.blockCol:
                        matchingMap.put(colName, "block")
                        break
                    case resultFileCfg.rowCol:
                        matchingMap.put(colName, "row")
                        break
                    case resultFileCfg.columnCol:
                        matchingMap.put(colName, "column")
                        break
                    case resultFileCfg.fgCol:
                        matchingMap.put(colName, "FG")
                        break
                    case resultFileCfg.bgCol:
                        matchingMap.put(colName, "BG")
                        break
                    case resultFileCfg.flagCol:
                        matchingMap.put(colName, "flag")
                        break
                    case resultFileCfg.diameterCol:
                        matchingMap.put(colName, "diameter")
                        break
                    case resultFileCfg.xCol:
                        matchingMap.put(colName, "X")
                        break
                    case resultFileCfg.yCol:
                        matchingMap.put(colName, "Y")
                        break
                }
            }
        }
        matchingMap
    }

    /**
     * Efficiently delete all spots that belong to a slide
     */
    def deleteSpots (slideInstanceId) {

        //config
        def groovySql = grailsApplication.config.rppa.jdbc.groovySql.toString().toBoolean()
        def slideInstance = Slide.get(slideInstanceId)

        if(groovySql)
        {
            def sql = Sql.newInstance(dataSourceUnproxied)
            sql.execute('delete from spot where slide_id = ?', slideInstanceId)
            sql.close()
        }

        else slideInstance.spots.clear()
    }

    /**
     * Import excel file using a fileinputstream
     * ResultFileConfig is a map of column letters to domain class properties, e.g. Signal is in column F, ...
     */
    def importSpotsFromFile(String filePath, int sheetIndex, int minNumOfCols) {

        def fileEnding = FilenameUtils.getExtension(filePath)

        if(fileEnding == "xlsx")
            return xlsxImportService.parseXLSXSheetToCSV(filePath, sheetIndex.toString(), minNumOfCols)
        else if(fileEnding == "xls")
            return xlsxImportService.parseXLSSheetToCSV(filePath, sheetIndex.toString(), minNumOfCols)
        else {
            return FileUtils.readFileToString(new File(filePath))
        }
    }

    //keep track of the progress, but update only every 1% to reduce the overhead
    def initializeProgressBar(spots, progressId){
        progressService.setProgressBarValue(progressId, 0)
        def numberOfSpots = spots.size()

        onePercent = (int) (numberOfSpots / 100)
        return onePercent
    }

    def updateProgressBar(nextStep, currentSpotIndex, progressId){
        if(currentSpotIndex == nextStep)
        {
            nextStep += onePercent
            progressService.setProgressBarValue(progressId, currentPercent++)
        }

        return nextStep
    }

    /*
     * Some global variables
     */

    //global variables for progress bar
    def currentPercent = 0
    def onePercent
    def nextStep

    /**
     * Main method
     */
    def processResultFile(def slideInstance, String sheetContent, def columnMap, int skipLines, String progressId)
    {
        Scanner scanner = new Scanner(sheetContent)

        //skip lines including header this time
        for(int i = 0; i < skipLines; i++)
        {
            if(scanner.hasNextLine()) scanner.nextLine()
        }

        def spots = []

        while(scanner.hasNextLine())
        {
            def currentLine = scanner.nextLine()

            currentLine = currentLine.split(',')

            try
            {
                def newSpot = [:]

                newSpot.BG = Double.valueOf(currentLine[columnMap.BG])
                newSpot.FG = Double.valueOf(currentLine[columnMap.FG])
                newSpot.block = Integer.valueOf(currentLine[columnMap.block])
                newSpot.row = Integer.valueOf(currentLine[columnMap.row])
                newSpot.col = Integer.valueOf(currentLine[columnMap.column])
                newSpot.x = Integer.valueOf(currentLine[columnMap.X])
                newSpot.y = Integer.valueOf(currentLine[columnMap.Y])
                newSpot.diameter = Double.valueOf(currentLine[columnMap.diameter] )
                newSpot.flag = Double.valueOf(currentLine[columnMap.flag] )

                spots << newSpot

            }catch(ArrayIndexOutOfBoundsException)
            {
                log.info "could not parse line, assuming the end is reached."
            }
        }

        //clean up
        scanner.close()

        nextStep = initializeProgressBar(spots, progressId)

        //create an sql instance for direct inserts via groovy sql
        def sql = Sql.newInstance(dataSourceUnproxied)

        //insert spots
        try{
            performSqlBatchInsert(sql, spots, slideInstance, progressId)
        }catch(NoMatchingLayoutException nmle)
        {
            return "No matching layout found for spot ${nmle.obj}"
        }catch(MissingMethodException e)
        {
            log.error e.getMessage();
        }catch(java.sql.BatchUpdateException bue){
            log.error bue.getMessage()
            return "Could not add spots: ${bue.getMessage()}"
        } finally{
            sql.close()
        }

        //clean up
        sql.close()

        //refresh slide because hibernate does not know about our changes
        slideInstance.refresh()

        return slideInstance
    }

    //use hibernate batch with prepared statements for max performance
    def performSqlBatchInsert(sql, spots, slideInstance, progressId){
        //config
        def groovySql = grailsApplication.config.rppa.jdbc.groovySql.toString().toBoolean()
        def batchSize = grailsApplication.config.rppa.jdbc.batchSize?:150

        //extract deposition pattern from layout
        def depositionList = depositionService.parseDepositions(slideInstance.layout.depositionPattern)
        int deposLength = depositionList.size()

        //sort rows in obj like this block -> column -> row
        spots.sort{ a,b -> (a.block <=> b.block) ?: (a.row <=> b.row) ?:(a.col <=> b.col)  }

        //get all layout spots in the correct order
        def layoutSpots = fetchOrderedLayoutSpots(slideInstance.layout.id)

        def layoutSpotIterator = layoutSpots.iterator()
        def currentLayoutSpot

        if(layoutSpotIterator.hasNext())
            currentLayoutSpot = layoutSpotIterator.next()

        else throw new NoMatchingLayoutException(obj: null)


        if(!groovySql){
            spots.eachWithIndex{ obj, currentSpotIndex ->

                int layoutColumn = mapToLayoutColumn(obj.col, deposLength)
                currentLayoutSpot = scrollThroughLayoutSpots(currentLayoutSpot, layoutSpotIterator, obj, layoutColumn)

                //add new spot
                def newSpot = new Spot(obj)
                newSpot.layoutSpot = currentLayoutSpot
                newSpot.slide = slideInstance
                newSpot.save()

                nextStep = updateProgressBar(nextStep, currentSpotIndex, progressId)
            }
        }

        else{
            sql.withBatch(batchSize, 'insert into spot (version, bg, fg, block, col, diameter, flag, layout_spot_id, row, slide_id, x, y, signal) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)'){ stmt ->

                spots.eachWithIndex{ obj, currentSpotIndex ->
                    int layoutColumn = mapToLayoutColumn(obj.col, deposLength)

                    //match a layout spot to this slide spot in a closure
                    currentLayoutSpot = scrollThroughLayoutSpots(currentLayoutSpot, layoutSpotIterator, obj, layoutColumn)

                    //add insert statement to batch
                    stmt.addBatch(0, obj.BG, obj.FG, obj.block, obj.col, obj.diameter, obj.flag, currentLayoutSpot.id, obj.row, slideInstance.id, obj.x, obj.y, obj.FG-obj.BG)

                    nextStep = updateProgressBar(nextStep, currentSpotIndex, progressId)
                }
            }
        }
    }

    public fetchOrderedLayoutSpots(layoutId) {
        def layoutSpots = LayoutSpot.withCriteria {
            eq("layout.id", layoutId)
            order('block')
            order('row')
            order('col')
        }
        return layoutSpots
    }

    public scrollThroughLayoutSpots(def currentLayoutSpot, def layoutSpotIterator, def obj, int layoutColumn) {

        //scroll forward
        while ((currentLayoutSpot.row < (obj.row as int) || currentLayoutSpot.block < (obj.block as int)
                || currentLayoutSpot.col < layoutColumn) && layoutSpotIterator.hasNext()) {
            currentLayoutSpot = layoutSpotIterator.next()
        }

        //check if we missed the right spot
        if(currentLayoutSpot.row == obj.row && currentLayoutSpot.block == obj.block && currentLayoutSpot.col == layoutColumn)
        {
            return currentLayoutSpot
        }

        else
        {
            throw new NoMatchingLayoutException(obj)
        }
    }

    def mapToLayoutColumn(col, deposLength) {
        //match layout column to actual column
        int layoutColumn = (((col as Integer) - 1) / deposLength)
        //we don't want to start with zero
        layoutColumn++
        return layoutColumn
    }
}