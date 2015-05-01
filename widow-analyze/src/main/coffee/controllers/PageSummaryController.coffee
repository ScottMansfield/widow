controllers = angular.module 'wa.controllers'

controllers.controller 'PageSummaryController', ($scope, $stateParams, $http) ->

  decode = (page64) ->
    decodeURIComponent atob page64

  $scope.pageSummary = "loading..."

  $scope.pageUrl = decode $stateParams.id

  $http.get("REST/pages/#{$stateParams.id}")
    .success (data, status, headers, config) ->
      $scope.pageSummary = JSON.stringify data
    .error (data, status, headers, config) ->
      $scope.pageSummary = "Error. Code: #{status}"
      console.log data