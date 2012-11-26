package org.nanocan.rppa.layout

import org.springframework.security.access.annotation.Secured
import org.nanocan.savanah.experiment.Experiment
import org.nanocan.savanah.plates.PlateLayout
import org.nanocan.savanah.experiment.Project

@Secured(['ROLE_USER'])
class SpottingController {

    def plateImportService
    def springSecurityService

    def index() {
        redirect(action: "plateLayoutSpotting")
    }

    def plateLayoutSpottingFlow = {

        modelForPlateLayouts{
            action {
                [experiments: Experiment.list(), savanahProjects: Project.list(),
                        miracleProjects: org.nanocan.rppa.project.Project.list(),
                        savanahLayouts: PlateLayout.list(),
                        miracleLayouts: org.nanocan.rppa.layout.PlateLayout.list()]
            }
            on("success").to "selectPlateLayouts"
        }

        selectPlateLayouts{
            on("plateLayoutsOrdered").to "plateLayoutsOrdered"
        }

        plateLayoutsOrdered{
            action {
                if (!params["plateLayoutOrder"])
                {
                    flash.message = "No plate layouts have been selected"
                    noSelection()
                }
                else
                {
                    def selectedLayouts = params["plateLayoutOrder"].split("&").collect{it.split("=")[1]}
                    def layouts = [:]
                    boolean hasSavanahLayouts = false

                    selectedLayouts.each{
                        if(it.toString().startsWith("savanah")) hasSavanahLayouts = true
                        layouts = plateImportService.getPlateLayoutFromId(it, layouts)
                        layouts = layouts
                    }
                    flow.layouts = layouts
                    if(hasSavanahLayouts) matchSavanahProperties()
                    else spottingProperties()
                }
            }
            on("noSelection").to "modelForPlateLayouts"
            on("matchSavanahProperties").to "modelForMatchSavanahProperties"
            on("spottingProperties").to "spottingProperties"
        }

        modelForMatchSavanahProperties{
            action {
                def savanahLayouts = []
                flow.layouts.values().each {
                    if(it instanceof PlateLayout) savanahLayouts << it
                }
                def matchModel = plateImportService.createMatchListsForSavanahLayouts(savanahLayouts)
                matchModel.put("onthefly", true)
                matchModel.put("layouts", flow.layouts.keySet())
                matchModel
            }
            on("success").to "matchSavanahProperties"
        }

        matchSavanahProperties{
            on("continue").to "createSavanahMatchingMaps"
        }

        createSavanahMatchingMaps{
            action {
                def numberOfCellsSeededMap = [:]
                def cellLineMap = [:]
                def inducerMap = [:]
                def treatmentMap = [:]
                def sampleMap = [:]

                params.each{k,v ->
                    def indexOfSeparator = k.toString().indexOf('_') + 1

                    if(k.toString().startsWith("numberOfCellsSeeded")) numberOfCellsSeededMap.put(k.toString().substring(indexOfSeparator), NumberOfCellsSeeded.findByName(v))
                    else if(k.toString().startsWith("cellline")) cellLineMap.put(k.toString().substring(indexOfSeparator), CellLine.findByName(v))
                    else if(k.toString().startsWith("inducer")) inducerMap.put(k.toString().substring(indexOfSeparator), Inducer.findByName(v))
                    else if(k.toString().startsWith("treatment")) treatmentMap.put(k.toString().substring(indexOfSeparator), Treatment.findByName(v))
                    else if(k.toString().startsWith("sample")) sampleMap.put(k.toString().substring(indexOfSeparator), org.nanocan.rppa.rnai.Sample.findByName(v))
                }

                flow.numberOfCellsSeededMap = numberOfCellsSeededMap
                flow.cellLineMap = cellLineMap
                flow.inducerMap = inducerMap
                flow.treatmentMap = treatmentMap
                flow.sampleMap = sampleMap
            }
            on("success").to "spottingProperties"
        }

        spottingProperties{
            on("continue"){

                def extractions = [:]
                def excludedPlateExtractionsMap = [:]

                params.list("layouts").each{

                    def excludedPlateExtractions = []
                    for(int extraction in 1..params.int("numOfExtractions")){
                        def extractionExcluded = "Plate_"+it.toString()+"|Extraction_"+extraction+"|Field"
                        excludedPlateExtractions << params.boolean(extractionExcluded)
                        excludedPlateExtractionsMap.put(extractionExcluded, params.boolean(extractionExcluded))
                    }
                    extractions.put(it, excludedPlateExtractions)
                }
                flow.extractions = extractions
                flow.excludedPlateExtractions = excludedPlateExtractionsMap

                flow.title = params.title
                flow.xPerBlock = params.int("xPerBlock")
                flow.spottingOrientation = params.spottingOrientation
                flow.extractorOperationMode = params.extractorOperationMode
                flow.depositionPattern = params.depositionPattern
                flow.bottomLeftDilution = params.bottomLeftDilution
                flow.topLeftDilution = params.topLeftDilution
                flow.topRightDilution = params.topRightDilution
                flow.bottomRightDilution = params.bottomRightDilution

                if(!params.title || params.title == "")
                {
                    flash.message = "You have to give a title to this layout!"
                    error()
                }
                //check if title is taken
                else if(SlideLayout.findByTitle(params.title))
                {
                    flash.message = "Please select another title (this one already exists)."
                    error()
                }
                else success()
            }.to "spotPlateLayouts"
        }

        spotPlateLayouts{
            action {
                    def slideLayout
                    try{
                        slideLayout = plateImportService.importPlates(flow)
                    } catch(Exception e)
                    {
                        flash.message = "Import failed with exception: " + e.getMessage()
                        return spottingProperties()
                    }

                    slideLayout.lastUpdatedBy = springSecurityService.currentUser
                    slideLayout.createdBy = springSecurityService.currentUser

                    if (slideLayout.save(flush: true, failOnError: true)) {
                        flash.message = "Slide Layout successfully created"
                        redirect(controller: "slideLayout", action: "show", id: slideLayout.id)
                    }
                    else{
                        flash.message = "import succeeded, but persisting the slide layout failed: " + slideLayout.errors.toString()
                        spottingProperties()
                    }
            }
            on("spottingProperties").to "spottingProperties"
        }
    }
}
