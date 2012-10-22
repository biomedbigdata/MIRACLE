package org.nanocan.savanah

import org.nanocan.savanah.plates.Plate
import org.nanocan.savanah.extraction.ExtractionRowWiseIterator
import org.nanocan.savanah.extraction.ExtractionColumnWiseIterator
import org.nanocan.rppa.spotting.Spotter
import org.nanocan.rppa.spotting.LeftToRightSpotter
import org.nanocan.rppa.spotting.TopToBottomSpotter

class PlateImportService {

    def grailsApplication

    /*
    We are assuming an extraction head with 48 pins, e.g. 12 columns and 4 rows. We could now extract with these 48 pins 8 extractions
    from a 384 well plate, either row- or column-wise (extractorOperationMode).
    We can then spot these again either row- or column-wise (or left-to-right and top-to-bottom in spottingOrientation more precisely) using the spot384 method.
    Since we are mainly dealing with 96 well plates that are diluted into a 384 well plate, we can also extract using a smaller extraction head (24 pins with 6x2)
    and use the method spot96as384 to multiply each well by four. We can customized the dilution pattern as well.

     1      2   becomes     1a  1b  2a 2b
    13     14               1c  1d  2c 2d
                            13a 13b 14a 14b
                            13c 13d 14c 14d

    One more issue is with respect to deposition patterns. We could generate a layout where each layout spot corresponds to a spot on the slide
    (allowing us to add more plates to a single slide),
    but deposition patterns occur in a regular fashion and thus allow us to merge these layout spots into one.
     */
    def importPlates(plates, extractions, spottingOrientation, extractorOperationMode, depositionPattern, xPerBlock, bottomLeftDilution, topLeftDilution, topRightDilution, bottomRightDilution) {

        println xPerBlock

        def spotter

        //spot top-to-bottom or left-to-right
        if(spottingOrientation == "left-to-right") spotter = new LeftToRightSpotter(grailsApplication: grailsApplication, maxSpottingColumns: 2)
        else if(spottingOrientation == "top-to-bottom") spotter = new TopToBottomSpotter(grailsApplication: grailsApplication, maxSpottingRows: 2)

        println "spotter"

        //iterate over plates
        plates.each{

            println "iterating plate ${it}"

            //iterate over extractions
            def iterator
            def extractorRows = 4
            def extractorCols = 12

            def plate = Plate.findByName(it)
            println "Found plate ${plate}"

            //if we use a 384 well plate we can use the default options (48 pins), but in case of 96 well plates
            // we need to reduce the size of the virtual extraction head. We regain the lost pins by adding a dilution pattern during spotting with spot96as384.
            if(plate.plateType == "96-well")
            {
                println "Plate type is 96 well, adjusting extraction head"
                extractorRows = extractorRows / 2
                extractorCols = extractorCols / 2
            }

            //extract row- or column-wise
            if(extractorOperationMode == "row-wise") iterator = new ExtractionRowWiseIterator(plate: plate, extractorCols: extractorCols, extractorRows: extractorRows)
            else if(extractorOperationMode =="column-wise") iterator = new ExtractionColumnWiseIterator(plate: plate, extractorCols: extractorCols, extractorRows: extractorRows)

            println "iterator created for ${extractorOperationMode}: ${iterator}"

            while(iterator.hasNext())
            {

                //spot current extraction on virtual slide
                if(plate.format == "96-well")  {
                    println "adding 96-well plate to slide layout as 384 diluted."
                    spotter.spot96as384(iterator.next())
                }
                else if(plate.format == "384-well"){
                    println "adding 384-well plate."
                    spotter.spot384(iterator.next())
                }
                else{
                    throw new Exception("plate type unknown")
                    break;
                }
            }
            println "done with plate ${plate}."
        }

        def slideLayout = spotter.slideLayout
        slideLayout.blocksPerRow = 12
        slideLayout.columnsPerBlock = 2
        slideLayout.depositionPattern = depositionPattern
        slideLayout.numberOfBlocks = 48
        slideLayout.rowsPerBlock = spotter.currentSpottingRow
        slideLayout.title = "test layout"

        return(slideLayout)
    }
}
