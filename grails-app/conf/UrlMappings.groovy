class UrlMappings {

	static mappings = {
		"/img/getTiles/$imageFolder/$imageLevel/$imageFile" (controller: "img", action: "getTiles")

        "/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"(controller: "index", action: "index")
		"500"(view:'/error')
        "500"(controller:  "r", action: "error", exception: org.rosuda.REngine.Rserve.RserveException)
	}
}
