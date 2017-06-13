v.1.0.2 Maintenance release with small bugfixes, upgrade to grails 2.5.4. Due to the shutdown of the NanoCAN web server, the MIRACLE demo server is now hosted at https://dockerhts.compbio.sdu.dk/ as part of a larger high-throughput screening platform.

v.1.0.0 Maintenance release with changes under the hood: Upgrade to grails 2.5.3, which includes Java 8 compatibility. Added support for [docker deployment](). The HtsBackend version used here is now on par with the public release version 0.9 of [SAVANAH](https://github.com/NanoCAN/SAVANAH). 

v.0.9.2 Maintenance release with smaller bugfixes

v.0.9.1 Maintenance release with bugfixes:

- Pagination was not working properly when filters were active
- When creating a slide layout from plates and experiment filter was active only plates were shown where the layout is linked to an experiment. This is not ideal since plates themselves can also be assigned to a different experiment. This has been fixed. 
- A problem in Rmiracle was fixed that caused loading readouts to fail
- The shiny callback can now be set in the miracle config file as ```shiny.baseUrl = "http://localhost:8080/spotExport"```. This can be important if shiny server and MIRACLE are hosted on the same IP since loopbacks are usually not allowed on the public IP. 
- When using a SQL database as backend, the time for adding spots to the database can now be significantly increased through SQL batch operations. To enable this add the following lines to the config file
```
jdbc.groovySql = "true"
jdbc.batchSize = 500 //number of spots per batch
```

v.0.9 Maintenance release with small improvements:

- Several small bug fixes and code improvements
- New fields added to Antibody, e.g. vendor and catalogue nr.
- Slides now have a PMT low / high setting
- Filenames with UTF-8 symbols such as µ caused problems in some setups. To avoid this, files are from now on stored using uniquely generated ids. 
- Fixes to the OpenSeaDragon library that generates the zoomable images. 
- *New feature*: Cell lines created in MIRACLE can now be linked to cell lines managed in [OpenLabFramework](https://github.com/NanoCAN/OpenLabFramework). Therefore two new lines need to be added to the configuration file:
  - ```openlabframework.rest.url = "https://localhost/OpenLabFramework```
  Change the URL to a running OpenLabFramework instance that supports https, e.g. https://130.225.157.237/OpenLabFramework.
  - ```openlabframework.appAccessToken = "xxx"```
  See [OpenLabFramework guide](https://github.com/NanoCAN/OpenLabFramework/wiki/AppAccessTokens) to learn how to obtain an app access token.
- Fixed problems with creating plate layout copies
- Renamed NumberOfCellsSeeded to Amount of protein / Number of cells seeded
- *New feature*: Copying of slide layouts and slides
- A warning note is now shown if a slide contains block shifts
- Block shifts can now be deleted
- *New feature*: A javascript based heatmap has been implemented to reduce MIRACLE's dependency on R for data management. Since the new heatmap is interactive ,individual samples can be quickly identified by moving the mouse cursor to the corresponding spot. Note that the heatmap for the block shifts still relies on R shiny since the block shifts are corrected on the R side.

v.0.8 First public release, together with the MIRACLE [ECCB paper](http://www.ncbi.nlm.nih.gov/pubmed/25161257)