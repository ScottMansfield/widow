controllers = angular.module 'wa.controllers'

controllers.controller 'PageSummaryController', ($scope, $stateParams, $http, Encoding) ->

  $scope.pageSummary = "loading..."

  $scope.pageID = $stateParams.id
  $scope.pageUrl = Encoding.decode $stateParams.id

  $http.get("REST/pages/#{$stateParams.id}")
    .success (data, status, headers, config) ->
      $scope.timesAccessed = ({
        display: new Date(Number(x)).toString(),
        timestamp: x
      } for x in data.timesAccessed)

    .error (data, status, headers, config) ->
      $scope.pageSummary = "Error. Code: #{status}"
      console.log data