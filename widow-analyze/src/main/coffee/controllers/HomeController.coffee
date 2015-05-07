controllers = angular.module 'wa.controllers'

controllers.controller 'HomeController', ($scope, $http, Encoding) ->

  $scope.pages = [ ]
  $scope.status = "Loading..."

  $http.get('REST/pages')
    .success (data, status, headers, config) ->
      $scope.status = "Done."
      $scope.pages = ({page: x, page64: Encoding.encode(x)} for x in data.pages)

    .error (data, status, headers, config) ->
      $scope.status = "Failed to load pages: status " + status
      console.log JSON.stringify data
