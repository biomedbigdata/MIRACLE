<%@ page import="org.nanocan.rppa.project.Project" %>
<li class="dropdown" id="main.menu">
    <a class="dropdown-toggle" data-toggle="dropdown" href="#main.menu">
        <g:message code="default.menu.label" default="Projects"/>
        <b class="caret"></b>
    </a>
    <ul class="dropdown-menu">
        <li>
            <g:link class="list" controller="project" action="list">List Projects</g:link>
        </li>
        <li>
            <g:link class="create" controller="project" action="create">Create New Project</g:link>
        </li>
    </ul>
</li>
<li class="dropdown" id="plate.menu">
    <a class="dropdown-toggle" data-toggle="dropdown" href="#plate.menu">
        <g:message code="default.plateLayout.menu.label" args="['...']" default="Plate Layout"/>
        <b class="caret"></b>
    </a>
    <ul class="dropdown-menu">
        <li>
            <g:link class="list" controller="plateLayout" action="list">List Layouts</g:link>
        </li>
        <li>
            <g:link class="create" controller="plateLayout" action="create">Create New Layout</g:link>
        </li>
        <li>
            <g:link class="create" controller="plateImport" action="plateLayoutImport">Import Layout from SAVANAH</g:link>
        </li>
    </ul>
</li>
<li class="dropdown" id="browse.menu">
    <a class="dropdown-toggle" data-toggle="dropdown" href="#browse.menu">
        <g:message code="default.slideLayout.menu.label" args="['...']" default="Slide Layout"/>
        <b class="caret"></b>
    </a>
    <ul class="dropdown-menu">
        <li>
            <g:link class="list" controller="slideLayout" action="list">List Layouts</g:link>
        </li>
        <li>
            <g:link class="create" controller="slideLayout" action="create">Create New Layout</g:link>
        </li>
        <li>
            <g:link class="create" controller="plateImport" action="plateImport">Create Slide Layout from Plate Layouts</g:link>
        </li>
        <li class="divider"></li>
        <li>
            <g:link class="slide_layout" controller="cellLine" action="list">CellLines</g:link>
        </li>
        <li>
            <g:link class="slide_layout" controller="dilution" action="list">Dilution Factors</g:link>
        </li>
        <li>
            <g:link class="slide_layout" controller="inducer" action="list">Inducers</g:link>
        </li>
        <li>
            <g:link class="slide_layout" controller="lysisBuffer" action="list">Lysis Buffers</g:link>
        </li>
        <li>
            <g:link class="slide_layout" controller="spotType" action="list">Spot Types</g:link>
        </li>
        <li>
            <g:link class="slide_layout" controller="treatment" action="list">Treatments</g:link>
        </li>
        <li>
            <g:link class="slide_layout" controller="sample" action="list">Samples</g:link>
        </li>
    </ul>
</li>
<li class="dropdown" id="organize.menu">
    <a class="dropdown-toggle" data-toggle="dropdown" href="#organize.menu">
        <g:message code="default.slide.menu.label" default="Slide Data"/>
        <b class="caret"></b>
    </a>
    <ul class="dropdown-menu">
        <li>
            <g:link class="list" controller="slide" action="list">List Slide Results</g:link>
        </li>
        <li class="divider"></li>
        <li>
            <g:link class="slide" controller="antibody" action="list">Antibodies</g:link>
        </li>
        <li>
            <g:link class="slide" controller="resultFileConfig" action="list">Result File Config</g:link>
        </li>
    </ul>
</li>
<li class="divider-vertical"></li>
<li>
    <g:form class="navbar-search" url='[controller: "searchable", action: "index"]' id="searchableForm" name="searchableForm" method="get">
        <g:textField class="search-query" placeholder="Search" name="q" value="${params.q}"/>
    </g:form>

</li>
<li class="divider-vertical"></li>
<li><g:form class="navbar-form" name="projectForm" controller="project" action="updateSelectedProject">
    <g:hiddenField name="returnPage" value="${createLink(action:actionName, params:params, absolute: true)}"/>
    <g:select from="${Project.list()}" value="${session.projectSelected?:""}" optionKey="id" noSelection="['':'All projects']" name="projectSelect" onchange="\$('#projectForm').submit();"/>
</g:form></li>
<li class="divider-horizontal"></li>
</ul>
<ul class="nav nav-pills">
