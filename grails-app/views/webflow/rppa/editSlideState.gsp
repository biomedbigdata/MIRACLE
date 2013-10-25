<%@ page import="org.nanocan.rppa.scanner.Slide" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="webflow">
		<g:set var="entityName" value="${message(code: 'slide.label', default: 'Slide')}" />
		<title><g:message code="default.edit.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#edit-slide" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		
		<div id="edit-slide" class="content scaffold-edit" role="main">
			<h1><g:message code="default.edit.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<g:hasErrors bean="${slideInstance}">
			<ul class="errors" role="alert">
				<g:eachError bean="${slideInstance}" var="error">
				<li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
				</g:eachError>
			</ul>
			</g:hasErrors>
			<g:form method="post" enctype="multipart/form-data">
				<g:hiddenField name="id" value="${slideInstance?.id}" />
				<g:hiddenField name="version" value="${slideInstance?.version}" />
				<fieldset class="form">
					<g:render template="slideStateForm"/>
				</fieldset>
				<fieldset class="buttons">
					<g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" />
					<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" formnovalidate="" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure? Be aware: This will most likely take a while!')}');" />
				</fieldset>
			</g:form>
		</div>
	</body>
</html>