controllers = angular.module 'wa.controllers'

controllers.controller 'HomeController', ($scope, $http, Encoding) ->

  $scope.pages = [ ]
  $scope.status = "Loading..."

  $scope.encode = Encoding.encode

  $scope.formatTime = (time) ->
    new Date(Number(time)).toString()

  $http.get('REST/pages')
    .success (data, status, headers, config) ->
      $scope.status = "Done."
      $scope.pages = data.pages

    .error (data, status, headers, config) ->
      $scope.status = "Failed to load pages: status " + status
      console.log JSON.stringify data
