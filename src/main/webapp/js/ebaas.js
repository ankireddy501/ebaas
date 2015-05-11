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
        }).when('/home', {
            templateUrl : 'landing.html'
         }).when('/signup', {
            templateUrl : 'signup.html',
            controller  : 'signupController'
        }).when('/login', {
            templateUrl : 'login.html',
            controller  : 'loginController'
        }).when('/application/:id', {
            templateUrl : 'application.html',
            controller  : 'applicationController'
        }).when('/appUserRegistration', {
            templateUrl : 'application/registration.html',
            controller  : 'appUserRegistrationController'
        }).when('/appAuth', {
            templateUrl : 'application/authentication.html',
            controller  : 'appAuthController'
        }).
        otherwise({
            redirectTo: '/home'
        });
});
ebbas.controller('appAuthController', ['$rootScope','$scope', function($rootScope, $scope){


}]);
ebbas.controller('appUserRegistrationController', ['$rootScope','$scope', function($rootScope, $scope){
    $scope.fields = [
                     {name:"firstName",type:'String',mandatory:'false'},
                     {name:"lastName",type:'String',mandatory:'false'},
                     {name:"userName",type:'String',mandatory:'true'},
                     {name:"email", type:'email',mandatory:'true'},
                     {name:"mobile",type:'mobile',mandatory:'false'},
                     {name:"password",type:'passowrd',mandatory:'true'}
                    ];

}]);
ebbas.controller('loginController', ['userService','$rootScope','$scope', '$location', function(service, $rootScope, $scope,$location){
    $scope.user = {};
    $scope.isAuthenticated = function(){
        if($rootScope.Authorization){
            return true;
        }
        return false;

    }
    $scope.login = function(){
        service.authenticate(function(r, e) {
            if (e) {
                alert(e);
            }
            $rootScope.Authorization = r;
            $location.path('listApp');
        }, $scope.user);
    }

}]);
ebbas.controller('signupController', ['userService','$rootScope','$scope','$location', function(service, $rootScope, $scope,$location){
    $scope.user = {};
    $scope.signup = function(){
        service.createUser(function(r, e) {
            if (e) {
                alert(e);
            }
            $rootScope.Authorization = r;
            $location.path('listApp');
        }, $scope.user);
    }
}]);

ebbas.controller('addAppController',['applicationService','$rootScope','$scope','$location', function(service, $rootScope, $scope, $location){
        $scope.application = {};
        $scope.cancel = function(){
            $location.path('listApp');
        };
        $scope.save = function(){
            $rootScope.addApp = false;
            service.createApplication(function(r, e) {
                if (e) {
                    alert(e);
                }
                $location.path('listApp');
            }, $scope.application);
        }
}]);
ebbas.controller('applicationController',['applicationService','$rootScope','$scope',function(service, $rootScope, $scope){
    $scope.application = {id:"dummy", name:'billboard application', description:'Place for finding the ad space'};
}]);

ebbas.controller('listAppController',['applicationService','$rootScope','$scope','$location',function(service, $rootScope, $scope, $location){
     service.application( function(r, e){
        if(e){
            alert(" Failed to get the applications")
        }else{
            $scope.applications = r;
        }
    });
    $scope.add = function(){
        $location.path('addApp');
    }
    $scope.delete = function(applicationId){
        service.deleteApplication(function(r, e) {
            if (e) {
                alert(e);
            }
        },applicationId);
    }

}]);

ebbas.service('applicationService',['$rootScope', '$http', function($rootScope, http){

   var applicationsUrl = 'rest/application';

    this.application = function(updateStatus){
        http.defaults.headers.common.Authorization = $rootScope.Authorization;
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
        http.defaults.headers.common.Authorization = $rootScope.Authorization;
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

ebbas.service('userService',['$http', function(http){

    this.createUser = function(updateStatus, person){
        var userUrl = 'rest/person';
        var promisses = http.post(userUrl, person);
        promisses.success(function(data, status, headers, config){
            $("#login").hide();
            $("#signup").hide();
            $("#signout").show();
            updateStatus(data.Authorization);
        });
        promisses.error(function(err){
            updateStatus(undefined,err)
        });
    }

    this.authenticate = function(updateStatus, person){
        var authenticateUrl = 'rest/person';
        authenticateUrl = authenticateUrl + '/' + person.email+ '/'+ person.password
        var promisses = http.post(authenticateUrl, person);
        promisses.success(function(data, status, headers, config){
            $("#login").hide();
            $("#signup").hide();
            $("#signout").show();
            updateStatus(data.Authorization);
        });
        promisses.error(function(err){
            updateStatus(undefined,err)
        });
    }
}])
