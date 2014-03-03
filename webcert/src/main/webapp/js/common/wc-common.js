"use strict";

/**
 * Common module used in both WC main application as well as in a certificate's module app pages.
 * Since this js will be used/loaded from different contextpaths, all templates are inlined. PLEASE keep source
 * formatting in this file as-is, otherwise the inline templates will be hard to follow.
 */
var common = angular.module('wc.common', []);

common.factory('statService', [ '$http', '$log', '$timeout', '$rootScope', function($http, $log, $timeout, $rootScope) {

    var timeOutPromise = undefined;
    var msPollingInterval = 10* 1000;
    /*
     * get stats from server
     */
    function _refreshStat() {
        $log.debug("_getStat");
        $http.get('/moduleapi/stat/').success(function(data) {
            $log.debug("_getStat success - data:" + data);
            $rootScope.$broadcast('wc-stat-update', data);
            timeOutPromise = $timeout(_refreshStat, msPollingInterval);
        }).error(function(data, status, headers, config) {
            $log.error("_getStat error " + status);
            timeOutPromise = $timeout(_refreshStat, msPollingInterval);
        });
    }

    function _startPolling() {
        _refreshStat();
        $log.debug("statService -> Start polling");
    }
    function _stopPolling() {
        if (timeOutPromise) {
            $timeout.cancel(timeOutPromise);
            $log.debug("statService -> Stop polling");
        }
    }

    // Return public API for the service
    return {
        startPolling : _startPolling,
        stopPolling : _stopPolling
    }
} ]);

common.directive("wcHeader", ['$rootScope','$location','$modal','$window','statService','User', function($rootScope,$location,$modal,$window,statService, User) {

    return {
        restrict : "A",
        replace : true,
        scope : {
          defaultActive: "@"
        },
        controller: function($scope, $element, $attrs) {
          //Expose "now" as a model property for the template to render as todays date
          $scope.today = new Date();
          $scope.statService = statService;
          $scope.statService.startPolling();
          $scope.user = User.userContext;

          $scope.stat = {
                  userStat: {},
                  unitStat:{}
                  }

          $scope.$on("wc-stat-update", function(event, message){
              $scope.stat = message;
            });

          $scope.menuDefs = [
             {
				       link :'/web/dashboard#/unhandled-qa',
				       label:'Frågor och svar',
				       requires_doctor: false,
				       statNumberId : "stat-unitstat-unhandled-question-count",
                       getStat: function() { return $scope.stat.unitStat.unhandledQuestions || ""}
				     },
				     {
				       link :'/web/dashboard#/unsigned',
				       label:'Osignerade intyg',
				       requires_doctor: false,
				       statNumberId : "stat-unitstat-unsigned-certs-count",
                       getStat: function() { return $scope.stat.unitStat.unsignedCerts || ""}
				     },
				     {
				       link :'/web/dashboard#/support/about',
				       label:'Om Webcert',
				       requires_doctor: false,
                       getStat: function() { return ""}
				     }
			    ];

 					var writeCertMenuDef = {
			       link :'/web/dashboard#/create/index',
			       label:'Sök/skriv intyg',
			       requires_doctor: false,
	                   getStat: function() { return ""}
			     };

          if (eval($scope.user.lakare) == true) {
              $scope.menuDefs.splice(0, 0, writeCertMenuDef);
          }
          else {
              $scope.menuDefs.splice(2, 0, writeCertMenuDef);
          }

          $scope.isActive = function (page) {
          	if (!page) {return false;}

          	page = page.substr(page.lastIndexOf('/') + 1);
        		if (angular.isString($scope.defaultActive)) {
        			if (page == $scope.defaultActive) {
        				return true;
        			}
          	}

          	var currentRoute = $location.path().substr($location.path().lastIndexOf('/') + 1);
            return page === currentRoute;
          };

            $scope.getLogoutUrl = function () {
                if ($scope.user.authenticationScheme == "urn:inera:webcert:fake") {
                    return "/logout";
                }
                else {
                    return "/saml/logout";
                }
            }

          $scope.openChangeCareUnitDialog = function() {

              var msgbox = $modal.open({
                template :
                    '<div class="modal-header">'+
                      '<button class="close">×</button>'+
                      '<h3>Vilken vårdenhet vill du logga in på?</h3>'+
                    '</div>'+
                    '<div class="modal-body">'+
                      '<table class="table table-striped table-qa table-links" ng-repeat="vg in vardgivare">'+
                        '<tr>'+
                          '<th>{{vg.namn}}</th>'+
                          '<th>Ohanterade frågor och svar</th>'+
                          '<th>Osignerade intyg</th>'+
                        '</tr>'+
                        '<tr ng-repeat="enhet in vg.vardenheter">'+
                          '<td>'+
                            '<button class="btn btn-link" data-ng-click="selectVardenhet(enhet)">{{enhet.namn}}</a>'+
                          '</td>'+
                          '<td>'+
                            '0'+
                          '</td>'+
                          '<td>'+
                            '0'+
                          '</td>'+
                        '</tr>'+
                        '</table>'+
                        '<div class="alert alert-error" data-ng-show="error">Tekniskt fel. Kunde inte byta vårdenhet.</div>'+
                      '</div>',
                controller : function($scope, $modalInstance, vardgivare) {
                  $scope.vardgivare = vardgivare;
                  $scope.error = false;

                  $scope.selectVardenhet = function(enhet) {
                    $scope.error = false;
                    User.setValdVardenhet(enhet, function() {
                      // We updated the user context on the server. Reload page for changes to show.
                      $window.location.reload();
                    }, function() {
                      // TODO: better error handling
                      $scope.error = true;
                    });

                  }
                },
                resolve : {
                  vardgivare : function() {
                    return angular.copy(User.getVardenhetSelectionList());
                  }
                }
              });

              /*msgbox.result.then(function(result) {
                if (callback) {
                  callback(result)
                }
              }, function() {
              });*/

          }

        },
        template:
        	'<div>'
        		+'<div class="row-fluid header">'
	        		+'<div class="span6">'
	        			+'<div class="row-fluid">'
		        			+'<div class="span12 headerbox">'
			        			+'<span class="headerbox-logo pull-left"><a href="/web/start"><img alt="Till startsidan" src="/img/webcert_logo.png"/></a></span>'
			          		+'<span class="headerbox-date pull-left">'
			        				+'{{today | date:"shortDate"}}'
			        			+'</span>'
	        				+'</div>'
        				+'</div>'
        			+'</div>'
	        		+'<div class="span6 headerbox-user">'
		      			+'<div class="row-fluid">'
			      			+'<div class="span12">'
			        			+'<div class="headerbox-user-profile pull-right" ng-show="user.namn.length">'
			                        +'<span ng-switch="user.lakare">'
			                        +'<strong ng-switch-when="true">Läkare</strong>'
			                        +'<strong ng-switch-default>Admin</strong>'
			                        +'</span>'
			        				+' - <span class="logged-in">{{user.namn}}</span><br>'
			        				+'<a ng-href="{{getLogoutUrl()}}">Logga ut</a>'
	        					+'</div>'
			        			+'<div class="headerbox-avatar pull-right">'
			        				+'<img src="/img/avatar.png"/>'
			        			+'</div>'
				      			+'<div class="pull-right location">'
                      +'<button type="button" class="btn" data-ng-click="openChangeCareUnitDialog()">Byt vårdenhet</button> '
				      				+'<span>{{user.valdVardgivare.namn}} - {{user.valdVardenhet.namn}}</span><br>'
				      			+'</div>'
	            		+'</div>'
	        			+'</div>'
	        		+'</div>'
	    			+'</div>'
	    			+'<div class="row-fluid">'
	    				+'<div class="span12">'
	    					+'<div class="navbar">'
				    	  	+'<div class="navbar-inner">'
				    				+'<div class="container">'
				    		  		+'<a class="btn btn-navbar" data-toggle="collapse" data-target=".navbar-responsive-collapse">'
				    						+'<span class="icon-bar"></span>'
				    						+'<span class="icon-bar"></span>'
				    						+'<span class="icon-bar"></span>'
				    					+'</a>'
				    					+'<div class="nav-collapse collapse navbar-responsive-collapse">'
				    						+'<ul class="nav">'
			    								+'<li ng-class="{active: isActive(menu.link)}" ng-repeat="menu in menuDefs">'
		    										+'<a ng-href="{{menu.link}}" ng-show="(menu.requires_doctor && isDoctor) || !menu.requires_doctor">{{menu.label}}'
		    										+'<span id="{{menu.statNumberId}}" ng-if="menu.getStat()>0" class="stat-circle stat-circle-active"'
		    											+'title="Vårdenheten har {{menu.getStat()}} ohanterade frågor och svar.">{{menu.getStat()}}</span></a>'
		    									+'</li>'
		            				+'</ul>'
		            			+'</div><!-- /.nav-collapse -->'
		            		+'</div>'
				    		  +'</div><!-- /navbar-inner -->'
				    	  +'</div>'
	          	+'</div>'
	    			+'</div>'
	    		+'</div>'
    };
} ]);


common.directive("wcSpinner", ['$rootScope', function($rootScope) {
    return {
        restrict : "A",
        transclude : true,
        replace : true,
        scope : {
          label: "@",
          showSpinner: "=",
          showContent: "="
        },
        template :
            '<div>'
           +'  <div ng-show="showSpinner" class="wc-spinner">'
           +'    <img aria-labelledby="loading-message" src="/img/ajax-loader.gif"/>'
           +'    <p id="loading-message">'
           +'      <strong><span message key="{{ label }}"></span></strong>'
           +'    </p>'
           +'  </div>'
           +'  <div ng-show="showContent">'
           +'    <div ng-transclude></div>'
           +'  </div>'
           +'</div>'
    };
} ]);

/**
 * User service. Provides actions for controlling user context including which vardenhet user is working on.
 * TODO: Move all user services here
 */
common.factory('User', [ '$http', '$log',
  function ($http, $log) {
    return {

      reset : function () {
        this.userContext = null;
      },

      /**
       * Set user context from api
       * @param userContext
       */
      setUserContext : function(userContext) {
        this.userContext = userContext;
      },

      /**
       * returns valdVardenhet from user context
       * @returns valdVardenhet
       */
      getVardenhetSelectionList : function() {

        var ucVardgivare = angular.copy(this.userContext.vardgivare);

        var vardgivareList = [];

        angular.forEach(ucVardgivare, function (vardgivare, key) {
          this.push({"id":vardgivare.id, "namn": vardgivare.namn, "vardenheter":[]});
          angular.forEach(vardgivare.vardenheter, function (vardenhet, key) {
            this.push({"id":vardenhet.id, "namn": vardenhet.namn});
            angular.forEach(vardenhet.mottagningar, function (mottagning) {
              mottagning.namn = vardenhet.namn + ' - ' + mottagning.namn;
              this.push(mottagning);
            }, vardgivareList[key].vardenheter);
          }, vardgivareList[key].vardenheter);
        }, vardgivareList);

        return vardgivareList;
      },

      /**
       * Returns a list of the selected vardenhet and all its mottagningar
       * @returns {*}
       */
      getVardenhetFilterList : function(vardenhet){
        if(!vardenhet){
          $log.debug("getVardenhetFilterList: parameter vardenhet was omitted");
          return [];
        }

        var units = [];
        units.push(angular.copy(vardenhet));

        angular.forEach(vardenhet.mottagningar, function (mottagning, key) {
          mottagning.namn = vardenhet.namn + ' - ' + mottagning.namn;
          this.push(mottagning);
        }, units);

        return units;
      },

      /**
       * returns valdVardenhet from user context
       * @returns valdVardenhet
       */
      getValdVardenhet : function() {
        return this.userContext.valdVardenhet;
      },

      /**
       * setValdVardenhet. Tell server which vardenhet is active in user context
       * @param vardenhet - complete vardenhet object to send
       * @param onSuccess - success callback on successful call
       * @param onError - error callback on connection failure
       */
      setValdVardenhet : function (vardenhet, onSuccess, onError) {
        $log.debug('setValdVardenhet' + vardenhet.namn);

        var payload = vardenhet;

        var restPath = '/api/user/changeunit';
        $http.post(restPath, payload).success(function (data) {
          $log.debug('got callback data: ' + data);
          // TODO: do additional checks and error handling on returned context (data)
          onSuccess(data);
        }).error(function (data, status) {
              $log.error('error ' + status);
              onError(data);
            });
      }
    };
  }]);
