controllers = angular.module 'wa.controllers'

controllers.controller 'PageVisitController', ($scope, $stateParams, $http, Encoding) ->

  $scope.visitInfo = { }
  $scope.pageUrl = Encoding.decode $stateParams.id
  $scope.visitTime = $stateParams.time

  $http.get("REST/pages/#{$stateParams.id}/#{$stateParams.time}")
    .success (data, status, headers, config) ->
      $scope.visitInfo = data.pageInfo
    .error (data, status, headers, config) ->
      console.log("Error: Status #{status} | data: #{data}")
      $scope.visitInfo = { }