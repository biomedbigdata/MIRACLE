package rppascanner

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartFile

class SlideController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def slideService

    def index() {
        redirect(action: "list", params: params)
    }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [slideInstanceList: Slide.list(params), slideInstanceTotal: Slide.count()]
    }

    def create() {
        [slideInstance: new Slide(params)]
    }

    def save() {

        if(request instanceof MultipartHttpServletRequest)
        {
            MultipartHttpServletRequest mpr = (MultipartHttpServletRequest)request;

            CommonsMultipartFile resultFile = (CommonsMultipartFile) mpr.getFile("resultFile.input");
            CommonsMultipartFile resultImage = (CommonsMultipartFile) mpr.getFile("resultImage.input");

            params["resultFile.id"] = slideService.createResultFile(resultFile).id
            params["resultImage.id"] = slideService.createResultFile(resultImage).id
        }
        else
        {
            flash.message = 'request is not of type MultipartHttpServletRequest'
            redirect(action: "create", params: params)
        }

        def slideInstance = new Slide(params)
        if (!slideInstance.save(flush: true)) {
            render(view: "create", model: [slideInstance: slideInstance])
            return
        }

		flash.message = message(code: 'default.created.message', args: [message(code: 'slide.label', default: 'Slide'), slideInstance.id])
        redirect(action: "show", id: slideInstance.id)
    }

    def show() {
        def slideInstance = Slide.get(params.id)
        if (!slideInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'slide.label', default: 'Slide'), params.id])
            redirect(action: "list")
            return
        }

        [slideInstance: slideInstance]
    }

    def edit() {
        def slideInstance = Slide.get(params.id)
        if (!slideInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'slide.label', default: 'Slide'), params.id])
            redirect(action: "list")
            return
        }

        [slideInstance: slideInstance]
    }

    def update() {
        def slideInstance = Slide.get(params.id)
        if (!slideInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'slide.label', default: 'Slide'), params.id])
            redirect(action: "list")
            return
        }

        if (params.version) {
            def version = params.version.toLong()
            if (slideInstance.version > version) {
                slideInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'slide.label', default: 'Slide')] as Object[],
                          "Another user has updated this Slide while you were editing")
                render(view: "edit", model: [slideInstance: slideInstance])
                return
            }
        }

        slideInstance.properties = params

        if (!slideInstance.save(flush: true)) {
            render(view: "edit", model: [slideInstance: slideInstance])
            return
        }

		flash.message = message(code: 'default.updated.message', args: [message(code: 'slide.label', default: 'Slide'), slideInstance.id])
        redirect(action: "show", id: slideInstance.id)
    }

    def delete() {
        def slideInstance = Slide.get(params.id)
        if (!slideInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'slide.label', default: 'Slide'), params.id])
            redirect(action: "list")
            return
        }

        try {
            slideInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'slide.label', default: 'Slide'), params.id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'slide.label', default: 'Slide'), params.id])
            redirect(action: "show", id: params.id)
        }
    }

    def addSpots = {

        def slideInstance = Slide.get(params.id)

        [slideInstance: slideInstance, configs: ResultFileConfig.list(), sheets: slideService.getSheets(slideInstance)]
    }

    def processResultFile = {

        def slideInstance = Slide.get(params.id)

        if(!slideInstance.resultFile)
        {
            flash.message = "no result file found for ${slideInstance}"
            redirect(action:  "show", params:  params)
        }

        else if (!params.sheet)
        {
            flash.message = "please select a sheet."
            redirect(action: "addSpots", params: params)
        }

        else if (!params.config)
        {
            flash.message = "please select a column map config to assign property columns correctly."
            redirect(action: "addSpots", params: params)
        }

        else
        {
            slideService.processResultFile(slideInstance, params.sheet, ResultFileConfig.get(params.config))

            if(slideInstance.save() )
            {
                flash.message = "spots have been added successfully"
                redirect(action: "show", id: slideInstance.id)
            }

            else
            {
                flash.message = "spots could not be added"
                redirect(action: "addSpots", id: slideInstance.id)
            }
        }
    }
}
