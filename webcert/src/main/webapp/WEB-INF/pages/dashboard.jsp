<%--

    Copyright (C) 2013 Inera AB (http://www.inera.se)

    This file is part of Inera Certificate Web (http://code.google.com/p/inera-certificate-web).

    Inera Certificate Web is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    Inera Certificate Web is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>

<!DOCTYPE html>
<html lang="sv" xmlns:ng="http://angularjs.org" id="ng-app" ng-app="wcDashBoardApp">
<head>
<meta http-equiv="X-UA-Compatible" content="IE=Edge" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="ROBOTS" content="nofollow, noindex" />

<title><spring:message code="application.name" /></title>

<link rel="icon" href="<c:url value="/favicon.ico" />" type="image/vnd.microsoft.icon" />

<link rel="stylesheet" href="<c:url value="/css/bootstrap/2.3.2/bootstrap.css"/>">
<link rel="stylesheet" href="<c:url value="/css/bootstrap-responsive-modified.css"/>">
<link rel="stylesheet" href="<c:url value="/css/inera-webcert.css"/>">

<SCRIPT LANGUAGE="VBScript">
    Function ControlExists(objectID)
    on error resume next
    ControlExists = IsObject(CreateObject(objectID))
    End Function
</SCRIPT>


<script type="text/javascript">
    /**
     Global JS config/constants for this app, to be used by scripts
     **/
    var MODULE_CONFIG = {
        USERCONTEXT : <sec:authentication property="principal.asJson" htmlEscape="false"/>
    }
</script>

</head>

<body>

  <%-- Web-cert top navigation bar --%>
  <div id="wcHeader" wc-header></div>
  <div class="container-fluid">
    <%-- No script to show at least something when javascript is off --%>
    <noscript>
      <h1>
        <span><spring:message code="error.noscript.title" /></span>
      </h1>
      <div class="alert alert-error">
        <spring:message code="error.noscript.text" />
      </div>
    </noscript>

    <%-- ng-view that holds dynamic content managed by angular app --%>
    <div id="view" ng-view></div>
  </div>

  <script type="text/javascript" src="<c:url value="/js/vendor/angular/1.2.13/angular.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/js/vendor/angular/1.2.13/angular-cookies.min.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/js/vendor/angular/1.2.13/angular-route.min.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/js/vendor/angular/1.2.13/angular-sanitize.min.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/js/vendor/angular/1.2.13/i18n/angular-locale_sv-se.js"/>"></script>
  <script type="text/javascript" src='<c:url value="/js/vendor/ui-bootstrap/0.7.0/ui-bootstrap-tpls-0.7.0.js"/>'></script>

  <%-- Application files --%>
  <script type="text/javascript" src="<c:url value="/js/app/dashboard/app.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/js/app/dashboard/controllers.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/js/app/dashboard/UnhandledQAController.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/js/app/dashboard/services.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/js/app/dashboard/filters.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/js/app/dashboard/directives.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/js/app/dashboard/messages.js"/>"></script>

  <%-- Dependencies to common components --%>
  <script type="text/javascript" src="<c:url value="/js/common/wc-utils.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/js/common/wc-common-fragasvar-module.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/js/common/wc-message-module.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/js/common/wc-common.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/js/common/wc-common-message-resources.js"/>"></script>

  <script type="text/javascript" src="<c:url value="/siths.jsp"/>"></script>
</body>
</html>
