/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Controller for logic related to listing questions and answers
 */
angular.module('webcert').controller('webcert.enhetArendenCtrl',
    ['$rootScope', '$scope', '$cookies',
        'common.enhetArendenCommonService',
        'webcert.enhetArendenModel', 'webcert.vardenhetFilterModel',
        function($rootScope, $scope, $cookies,
            enhetArendenCommonService,
            enhetArendenModel, vardenhetFilterModel) {
            'use strict';

            $scope.enhetArendenModel = enhetArendenModel;
            $scope.vardenhetFilterModel = vardenhetFilterModel;

            $scope.$on('wcChangeActiveUnitDialog.vardenhetSelected', function(){
                // When changing active unit, reset selected enhet on 'Ej hanterade ärenden' for units with multiple under units
                enhetArendenModel.reset();
            });

            var unbindLocationChange = $rootScope.$on('$locationChangeStart', function($event, newUrl, currentUrl) {
                enhetArendenCommonService.checkQAonlyDialog($scope, $event, newUrl, currentUrl, unbindLocationChange);
            });
            $scope.$on('$destroy', unbindLocationChange);

       }]);
