var ebbas = angular.module('ebaas',['ngRoute']);
// configure our routes
ebbas.config(function($routeProvider) {
    $routeProvider
        .when('/addApp', {
            templateUrl : 'addApp.html',
            controller  : 'addAppController'
        }) .when('/listApp', {
            templateUrl : 'listApp.html',
            controller  : 'listAppController'
        }). otherwise({
            redirectTo: '/listApp'
        });
});
ebbas.controller('loginController', ['$rootScope','$scope', function($rootScope, $scope){
            $scope.person = {};
            $rootScope.signup = false;
            $rootScope.addApp = false;
            $scope.login = function(){
                // invoke the server call for authentication
                $rootScope.authenticated=true;
            }
            $scope.forgot = function(){
                alert("user clicked on forgot");
            }

            $scope.registration = function(){
                $rootScope.signup = true;
            }
            $scope.signout = function(){
                $rootScope.authenticated=false;
            }

    }]);
ebbas.controller('signupController', ['$rootScope','$scope', function($rootScope, $scope){
        $scope.person = {};
        $scope.signup = function(){
            $rootScope.signup = false;
            alert($scope.organization);
            // invoke the server call for signup
            $rootScope.authenticated=true;
        }
        $scope.forgot = function(){
            alert("user clicked on forgot");
        }

        $scope.registration = function(){
            alert("user clicked on registration");
        }


    }]);

ebbas.controller('addAppController',['applicationService','$rootScope','$scope',function(service, $rootScope, $scope){
        $scope.application = {};
        $rootScope.addApp = false;
        $scope.addApp = function(){
            $rootScope.addApp = true;
        };
        $scope.cancel = function(){
            $rootScope.addApp = false;
        };
        $scope.save = function(){
            alert(JSON.stringify($scope.application));
            $rootScope.addApp = false;
            service.createApplication(function(r, e) {
                if (e) {
                    alert(e);
                }
            }, $scope.application);
        }
    }]);
ebbas.controller('listAppController',['applicationService','$scope',function(service, $scope){
    service.application( function(r, e){
        if(e){
            alert(" Failed to get the applications")
        }else{
            $scope.applications = r;
        }
    });
}]);

ebbas.service('applicationService',['$http', function(http){

    var applicationsUrl = 'rest/application';

    this.application = function(updateStatus){
        var promisses = http.get(applicationsUrl);
        promisses.success(function(response){
            console.log('sucess');
            updateStatus(response);
        });
        promisses.error(function(err){
            console.log("failure:"+err.errorText);
            updateStatus(undefined, err);
        });
    };

    this.createApplication = function(updateStatus, app) {
        var promisses = http.post(applicationsUrl, app);
        promisses.success(function(response){
            console.log('sucess');
            updateStatus(response);
        });
        promisses.error(function(err){
            console.log("failure:"+err.errorText);
            updateStatus(undefined, err);
        });
    };

}]);
