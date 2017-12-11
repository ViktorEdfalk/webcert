/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

angular.module('webcert').directive('wcEnhetArendenFilter', [
    '$rootScope', '$log', '$cookies',
    'webcert.enhetArendenProxy',
    'webcert.enhetArendenModel', 'webcert.enhetArendenFilterModel', 'webcert.vardenhetFilterModel',
    function($rootScope, $log, $cookies,
        enhetArendenProxy,
        enhetArendenModel, enhetArendenFilterModel, vardenhetFilterModel) {
        'use strict';

        return {
            restrict: 'E',
            transclude: false,
            replace: false,
            scope: {

            },
            templateUrl: '/app/views/fragorOchSvar/wcEnhetArendenFilter/wcEnhetArendenFilter.directive.html',
            controller: function($scope) {

                this.$onInit = function(){

                    // Load filter form (first page load)
                    enhetArendenFilterModel.reset();
                    initLakareList(enhetArendenModel.enhetId);

                    $scope.enhetArendenFilterModel = enhetArendenFilterModel;

                    $scope.hasUnhandledArenden = function(){
                        return vardenhetFilterModel.units[0].fragaSvar;
                    };

                    $scope.hasNoArenden = function(){
                        return vardenhetFilterModel.units[0].fragaSvar === 0;
                    };

                    $scope.toggleFilter = function(){
                        enhetArendenFilterModel.viewState.filterFormCollapsed = !enhetArendenFilterModel.viewState.filterFormCollapsed;
                    };

                    function updateArendenList(){
                        $rootScope.$broadcast('enhetArendenList.requestListUpdate', {startFrom: 0});
                    }

                    $scope.resetFilterForm = function() {
                        enhetArendenFilterModel.reset();
                        updateArendenList();
                    };

                    $scope.filterList = function() {
                        enhetArendenFilterModel.filteredYet = true;
                        updateArendenList();
                    };

                };

                function initLakareList(unitId) {
                    enhetArendenFilterModel.viewState.loadingLakare = true;
                    var lakareUnitId = unitId === enhetArendenModel.ALL_UNITS ? undefined : unitId;
                    enhetArendenProxy.getArendenLakareList(lakareUnitId, function(list) {
                        enhetArendenFilterModel.viewState.loadingLakare = false;
                        enhetArendenFilterModel.lakareList = list;
                        if (list && (list.length > 0)) {
                            enhetArendenFilterModel.lakareList.unshift(enhetArendenFilterModel.lakareListEmptyChoice);
                            enhetArendenFilterModel.filterForm.lakareSelector = enhetArendenFilterModel.lakareList[0];
                        }
                    }, function() {
                        enhetArendenFilterModel.viewState.loadingLakare = false;
                        enhetArendenFilterModel.lakareList = [];
                        enhetArendenFilterModel.lakareList.push({
                            hsaId: undefined,
                            name: '<Kunde inte hämta lista>'
                        });
                    });
                }

                // Broadcast by statService on poll
                $scope.$on('statService.stat-update', function(event, message) {
                    var unitStats = message;
                    if (!enhetArendenFilterModel.viewState.filteredYet && unitStats.fragaSvarValdEnhet === 0) {
                        enhetArendenFilterModel.viewState.filterFormCollapsed = false;
                    }
                });

                // Broadcast by vardenhet filter directive on load and selection
                $scope.$on('wcVardenhetFilter.unitSelected', function(event, unit) {

                    // If we change enhet then we probably don't want the same filter criterias
                    if ($cookies.getObject('enhetsId') && $cookies.getObject('enhetsId') !== unit.id) {
                        enhetArendenFilterModel.reset();
                    }
                    enhetArendenFilterModel.viewState.filteredYet = false; // so proper info message is displayed if no items are found
                    enhetArendenFilterModel.viewState.filterFormCollapsed = true; // collapse filter form so it isn't in the way

                    initLakareList(unit.id); // Update lakare list for filter form
                });

                this.$onInit();
            }
        };
    }]);
