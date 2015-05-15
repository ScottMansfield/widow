controllers = angular.module 'wa.controllers'

controllers.controller 'HomeController', ($scope, $http, Encoding) ->

  $scope.pages = { }
  $scope.status = "Loading..."

  $scope.encode = Encoding.encode

  $scope.formatTime = (time) ->
    new Date(Number(time)).toString()

  getPages = (last) ->
    $http.get('REST/pages', {
      params: {
        last: last
      }
    })
      .success (data, status, headers, config) ->
        $scope.status = "Done."
        $scope.pages = _.extend $scope.pages, data.pages

        console.log data.startKey

        if data.startKey
          getPages data.startKey

      .error (data, status, headers, config) ->
        $scope.status = "Failed to load pages: status " + status
        console.log JSON.stringify data

  getPages(null)