waModals = angular.module 'wa.modals'

waModals.controller 'RawContentModalController', ($scope, $modalInstance, $http, $timeout, contentID) ->
  $scope.contentID = contentID

  $http.get("/REST/pages/rawContent/#{contentID}")
    .success (data) ->
      rawContent = data.rawContent
      console.log("Before: #{rawContent}")

      # standardize newlines
      # tabs become 4 spaces because that's the right way to do tabs
      rawContent = rawContent.replace(/(\r\n|\r|\n)/, "\r\n").replace(/\t/, "    ").replace("        ", "    ")
      console.log("After: #{rawContent}")

      $scope.rawContent = rawContent

      $timeout ->
        $("#rawPageContents").each (_, elem) ->
          hljs.highlightBlock elem

  $scope.ok = ->
    $modalInstance.close('success')
