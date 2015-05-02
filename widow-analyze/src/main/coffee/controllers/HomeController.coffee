controllers = angular.module 'wa.controllers'

controllers.controller 'HomeController', ($scope, $http, Encoding) ->

  $scope.pages = [{page: "loading..."}]

  $http.get('REST/pages')
    .success (data, status, headers, config) ->
      $scope.pages = ({page: x, page64: Encoding.encode(x)} for x in data.pages)

    .error (data, status, headers, config) ->
      $scope.pages = ["Failure: status " + status]
      console.log data
