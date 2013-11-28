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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"
         trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html>
<html lang="sv">
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=Edge"/>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <meta name="ROBOTS" content="nofollow, noindex"/>

    <title>Webcert test inloggning</title>

    <link rel="icon" href="<c:url value="/favicon.ico" />" type="image/vnd.microsoft.icon"/>

    <link rel="stylesheet" href="<c:url value="/css/bootstrap/2.3.2/bootstrap.css"/>">
    <style type="text/css">
        textarea {
            font-family: Consolas, Lucida Console, monospace;
            font-size: 0.7em;
        }
    </style>

    <script type="text/javascript">
        //L�gg till fler templates i arrayen + i options f�r att ut�ka antalet inloggingar

        var loginArr = [
            //Markus Gran Testanv�ndare  @ V�rdEnhet1A
            {
                "fornamn" : "Markus",
                "efternamn" : "Gran",
                "hsaId" : "TST5565594230-106J",
                "enhetId" : "IFV1239877878-103F",
                "lakare" : true
            },

            //Markus Gran Testanv�ndare  @ V�rdEnhet2A
            {
                "fornamn" : "Markus",
                "efternamn" : "Gran",
                "hsaId" : "TST5565594230-106J",
                "enhetId" : "IFV1239877878-103H",
                "lakare" : true
            },
            //Markus Gran Testanv�ndare  @ V�rdEnhetA
            {
                "fornamn" : "Markus",
                "efternamn" : "Gran",
                "hsaId" : "TST5565594230-106J",
                "enhetId" : "IFV1239877878-103D",
                "lakare" : true

            },

            //L�kare med flera enheter&mottagningar
            {
                "fornamn" : "Eva",
                "efternamn" : "Holgersson",
                "hsaId" : "eva",
                "enhetId" : "centrum-vast",
                "lakare" : true

            },
            //Admin personal med flera enheter&mottagningar
            {
                "fornamn" : "Adam",
                "efternamn" : "Admin",
                "hsaId" : "adam",
                "enhetId" : "centrum-vast",
                "lakare" : false
            },
            //Admin personal med 3 enheter och mottagningar
            {
                "fornamn" : "Adamo",
                "efternamn" : "Admin",
                "hsaId" : "adamo",
                "enhetId" : "centrum-vast",
                "lakare" : false
            },
            //Admin personal med 3 enheter och mottagningar
            {
                "fornamn" : "Adamo",
                "efternamn" : "Admin",
                "hsaId" : "adamo",
                "enhetId" : "centrum-ost",
                "lakare" : false
            },

            {
                "fornamn" : "Test",
                "efternamn" : "Testsson",
                "hsaId" : "fitness1",
                "enhetId" : "vardenhet-fit-1",
                "lakare" : false
            },
            //FitNesse Admin personal med 1 enhet
            {
                "fornamn" : "fit",
                "efternamn" : "nesse",
                "hsaId" : "fitness2",
                "enhetId" : "vardenhet-fit-2",
                "lakare" : false
            },
            {
                "fornamn" : "Han",
                "efternamn" : "Solo",
                "hsaId" : "hansolo",
                "enhetId" : "centrum-norr",
                "lakare" : false

            }
        ];

        function updateJsonInput() {
            var jsonEl = document.getElementById("userJson");
            var jsonElView = document.getElementById("userJsonDisplay");
            var selector = document.getElementById("jsonSelect");
            //jsonEl.value = escape(JSON.stringify(loginArr[selector.selectedIndex], undefined, 2));
            jsonElView.value = JSON.stringify(loginArr[selector.selectedIndex], undefined, 1);
            jsonEl.value = escape(JSON.stringify(loginArr[selector.selectedIndex], undefined, 1));
        }
    </script>
</head>
<body onLoad="updateJsonInput()">
<form id="loginForm" action="/fake" method="POST" class="form-inline">
    <div class="container">

        <div id="content-container">
            <div class="content row">


                <h1>Testinloggningar Webcert</h1>

                <p class="well">Templatelista till v�nster - Manuella �ndringar kan g�ras i jsonstrukturen - detta
                    omvandlas
                    till inloggad userContext</p>

                <div class="form-group span6">

                    <h4>Mallar</h4>
                    <select id="jsonSelect" name="jsonSelect" onChange="updateJsonInput()" size="10" style="width: 100%">
                        <option value="0" selected>Markus Gran (L�kare V�rdEnhet1A)</option>
                        <option value="1" selected>Markus Gran (L�kare V�rdEnhet2A)</option>
                        <option value="2" selected>Markus Gran (L�kare V�rdEnhetA)</option>
                        <option value="3">Eva Holgersson (L�kare Centrum V�st)</option>
                        <option value="4">Adam Admin (Administrat�r Centrum V�st)</option>
                        <option value="5">Adamo Admin (Administrat�r Centrum V�st)</option>
                        <option value="6">Adamo Admin (Administrat�r Centrum �st)</option>
                        <option value="7">Fitnesse Admin (Administrat�r Vardenhet Fitnesse 1)</option>
                        <option value="8">Fitnesse Admin-1 (Administrat�r Vardenhet Fitnesse 2)</option>
                        <option value="9">Han Solo (Administrat�r, Centrum Norr)</option>
                    </select>
                    </p>

                    <input id="loginBtn" type="submit" value="Logga in" class="btn btn-primary btn-default"
                           style="width: 100%">

                </div>

                <div class="form-group span6">
                    <p>
                    <h4>Inloggningsprofil</h4>
                    <input type="hidden" id="userJson" name="userjson"/>
                    <textarea id="userJsonDisplay" name="userJsonDisplay" class="field"
                              style="height: 200px; width: 50%">
                    </textarea>
                </div>


            </div>
        </div>
    </div>


</form>

</body>
</html>
