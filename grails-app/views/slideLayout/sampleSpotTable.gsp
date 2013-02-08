<g:if test="${sampleProperty != 'sample'}">
    <g:render template="colorLegend" model="${[sampleProperty: sampleProperty]}"></g:render>
</g:if>
<g:else>
    <g:render template="sampleLegend" model="${[layoutId: slideLayout.id]}"></g:render>
</g:else>
    <g:render template="spotTooltip"/>

<g:set var="spot" value="${0}"/>
<g:set var="spotList" value="${spots.toList()}"/>

<g:if test="${slideLayout.blocksPerRow}">
    <g:set var="blocksPerRow" value="${Math.min(slideLayout.blocksPerRow, slideLayout.numberOfBlocks)}"/>
</g:if>
<g:else>
    <g:set var="blocksPerRow" value="${12}"/>
</g:else>

<!-- Figure out how many tabs we need with n blocks per tab -->
<g:set var="fullTabsNeeded" value="${(int) (slideLayout.numberOfBlocks / blocksPerRow)}"/>
<g:set var="blocksInLastTab" value="${slideLayout.numberOfBlocks % blocksPerRow}" />
<g:if test="${blocksInLastTab != 0}">
    <g:set var="tabsNeeded" value="${++fullTabsNeeded}"></g:set>
</g:if>
<g:else><g:set var="tabsNeeded" value="${fullTabsNeeded}"/></g:else>

<script type="text/javascript">
    $(document).ready(function() {
        $(function() {$("#blockTabs").tabs() });
        registerHandlers("blockTable1");
    });
</script>

<g:jprogressDialog message="Updating layout information..." progressId="update${slideLayout.id}" trigger="layoutUpdateButton"/>

<div class="message" id="message" role="status" style="padding:0px;margin:0px;margin-bottom:10px;">${flash.message?:"Select cells to change the layout"}</div>

<g:if test="${sampleProperty == 'sample'}" >
    <div class="errors">Warning: Colors are not unique for samples! Make sure you know what you are doing!</div>
</g:if>

<g:formRemote onSuccess="window.onbeforeunload = null;unsavedChanges=false" name="spotPropertiesForm" update="message" url="[controller: 'slideLayout', action: 'updateSpotProperty']">
    <div class="buttons" style="margin-top:5px; margin-bottom:10px;">
        <input type="submit" value="Save changes" name="layoutUpdateButton"/>
        Selection Mode: <g:select name="selectionMode" from="${['normal', 'whole rows', 'whole columns']}" onchange="updateSelectionMode(this.value);"/>
    </div>
    <input name="spotProperty" type="hidden" value="${sampleProperty}"/>
    <input name="slideLayout" type="hidden" value="${slideLayout.id}"/>

<div id = "blockTabs" style="overflow: auto;">
 <ul>
 <g:each var="i" in="${1..tabsNeeded}">
  <g:set var="tab" value="${((i-1) * blocksPerRow)+1}"/>
    <g:if test="${blocksInLastTab != 0 && i == tabsNeeded}"><g:set var="upperBound" value="${blocksInLastTab}"/></g:if>
    <g:else><g:set var="upperBound" value="${blocksPerRow}"/></g:else>
    <li><a href="#blockTabs-${i}" onclick="registerHandlers('blockTable${i}');">Blocks ${tab}..${tab+upperBound-1}</a></li>
 </g:each>
 </ul>

 <g:each var="i" in="${1..tabsNeeded}">
    <g:set var="tab" value="${((i-1) * blocksPerRow)+1}"/>
    <g:if test="${blocksInLastTab != 0 && i == tabsNeeded}"><g:set var="upperBound" value="${blocksInLastTab}"/></g:if>
    <g:else><g:set var="upperBound" value="${blocksPerRow}"/></g:else>

    <div id="blockTabs-${i}">
    <table id="blockTable${i}" style="border: 1px solid;">
        <thead>
        <tr>
            <th>Block</th>
            <g:each in="${tab..(tab+upperBound-1)}" var="block">
                <th colspan="${slideLayout.columnsPerBlock}">${block}</th>
            </g:each>
        </tr>

        <tr align="center">
            <th>Column</th>
            <g:each in="${tab..(tab+upperBound-1)}">
                <g:each in="${1..(slideLayout.columnsPerBlock)}" var="col">
                    <th style="width:25px;">${col}</th>
                </g:each>
            </g:each>
        </tr>
        </thead>

        <tbody>
        <g:each in="${1..(slideLayout.rowsPerBlock)}" var="row">
            <tr id="r${row+1}">
                <td>${row}</td>
                <g:each in="${tab..(tab+upperBound-1)}" var="block">
                    <g:each in="${1..(slideLayout.columnsPerBlock)}" var="col">
                        <g:if test="${spot < spotList.size() && spotList.get(spot).row == row && spotList.get(spot).col == col && spotList.get(spot).block == block }">
                            <td style="border: 1px solid; background-color:${spotList.get(spot)?.properties[sampleProperty]?spotList.get(spot).properties[sampleProperty].color?:'#e0e0e0':'#ffffff'};"><input name="${spotList.get(spot).id}" type="hidden" value=""></td>
                            <g:set var="spot" value="${++spot}"/>
                        </g:if>
                        <g:else>
                            <td style="border: 1px solid"></td>
                        </g:else>
                    </g:each>
                </g:each>
            </tr>
        </g:each>
        </tbody>
    </table>
    </div>
 </g:each>
</div>
</g:formRemote>

<script type="text/javascript">
    var allTDs
    var selColor = "";
    var selName = "";
    var selId = "";
    var buttondown = -1;
    var cellstartr, cellstartc, cellendr, cellendc;
    var tableName
    var unsavedChanges = false;
    var selectionMode = "normal";
    var sampleProperty = "${sampleProperty.toString().capitalize()}";

    $("td").bind("mouseover", function(event) {
        if(true) {
            var spot = $(this);
            var timer = window.setTimeout(function() {
                var id = spot.find("input").attr("name");
                ${remoteFunction(controller: "slideLayout", action: "showSpotTooltip",
            params: "\'id=\' + id", update: "draggableSpotTooltip")}

                $("#draggableSpotTooltip").show();
            }, 800)
            spot.data('timerid', timer);
        }
    }).bind("mouseout", function() {
                if(true) {
                    var timerid = $(this).data('timerid');
                    if(timerid != null)
                    {
                        window.clearTimeout(timerid);
                    }
                    $("#draggableSpotTooltip").hide();
                }
            });
</script>

<r:script>

    function updateSelectionMode(newValue)
    {
        selectionMode = newValue;
    }

    function registerHandlers(tN) {
        tableName = tN
        allTDs = document.getElementById(tableName).getElementsByTagName("td")
        document.getElementById(tableName).onmousedown = mouseDownHandler
        document.getElementById(tableName).onmouseup = mouseUpHandler
        document.getElementById(tableName).onmouseover = mouseOverHandler
    }

    function mouseOverHandler(e) {

        if (buttondown != -1) {
            if (window.getSelection) window.getSelection().removeAllRanges()
            if (document.selection) document.selection.empty()
        }
    }

    function mouseDownHandler(e) {
        if (!e) e = window.event
        var daTarget
        if (document.all)
            daTarget = e.srcElement
        else
            daTarget = e.target
        cellstartr = daTarget.parentNode.id.substring(1)
        cellstartc = daTarget.cellIndex
        buttondown = e.button
    }
    function mouseUpHandler(e) {

        if (!selColor) {
            alert('Please select a ' + sampleProperty +  ' first!')
        }

        else {
            if (!e) e = window.event
            var daTarget
            if (document.all)
                daTarget = e.srcElement
            else
                daTarget = e.target
            cellendr = daTarget.parentNode.id.substring(1)
            cellendc = daTarget.cellIndex
            var rowstart = cellstartr
            var rowend = cellendr
            if (parseInt(cellendr) < parseInt(cellstartr)) {
                rowstart = cellendr
                rowend = cellstartr
            }
            var colstart = cellstartc
            var colend = cellendc
            if (parseInt(cellendc) < parseInt(cellstartc)) {
                colstart = cellendc
                colend = cellstartc
            }

            /*whole columns selection style */
            if(selectionMode == "whole columns")
            {
                rowstart = 1;
                rowend = ${slideLayout.rowsPerBlock}+1
            }

            /*whole rows selection mode */
            else if(selectionMode == "whole rows"){
                colstart = 1;
                colend = ${slideLayout.columnsPerBlock * (slideLayout.blocksPerRow?:12)}
            }
            //alert("rstart: " + rowstart + "|rend:" + rowend + "|cstart:" + colstart + "|cend:" + colend);

            for (var currRow = parseInt(rowstart); currRow <= parseInt(rowend); currRow++) {
                //alert("current row:" + currRow);
                for (var currCol = parseInt(colstart); currCol <= parseInt(colend); currCol++) {
                    //alert("current column:" + currCol);
                    if(currCol != 0) //protect row names from color changes
                    {
                        var cell = document.getElementById(tableName).rows[currRow].cells[currCol];
                        if(cell.style.backgroundColor != "")
                        {
                             cell.style.backgroundColor = selColor;
                             cell.firstChild.setAttribute("value", selId);
                             $('#message').html("Please save your changes!") ;
                            window.onbeforeunload = unloadPage;
                            unsavedChanges = true
                        }
                    }
                }
            }
            buttondown = -1
        }
    }

    function unloadPage(){
        return "If you leave without saving, your changes will be discarded! Are you sure?";
    }

</r:script>
