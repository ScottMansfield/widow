controllers = angular.module 'wa.controllers'

controllers.controller 'HomeController', ($scope, $http) ->

  $scope.foo = "default"

  $http.get('REST/test/ping')
    .success (data, status, headers, config) ->
      $scope.foo = data
    .error (data, status, headers, config) ->
      $scope.foo = "Failure: status " + status
