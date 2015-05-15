controllers = angular.module 'wa.controllers'

controllers.controller 'PageSummaryController', ($scope, $stateParams, $http, Encoding) ->

  $scope.pageSummary = "loading..."

  $scope.pageID = $stateParams.id
  $scope.pageUrl = Encoding.decode $stateParams.id

  $http.get("REST/pages/#{$stateParams.id}")
    .success (data, status, headers, config) ->
      times = (visit['TIME_ACCESSED'] for visit in data.visits)

      $scope.timesAccessed = ({
        display: new Date(Number(time)).toString(),
        timestamp: time
      } for time in times)

      doCharts data.visits

    .error (data, status, headers, config) ->
      $scope.pageSummary = "Error. Code: #{status}"
      console.log data

  ################
  # Charting!
  ################

  doCharts = (visits) ->
    responseTimes = ([
      new Date(visit['TIME_ACCESSED'])
      visit['LOAD_TIME_MILLIS']
    ] for visit in visits)

    # Increase the y-axis range of the charts
    vMin = 1000000000
    vMax = -1

    for time in responseTimes
      if time[1] < vMin then vMin = time[1]
      if time[1] > vMax then vMax = time[1]

    extra = (vMax - vMin) * 0.2
    vMax += extra
    vMin -= extra

    # Load the Visualization API and the chart package.
    google.load 'visualization', '1.0', {
      'packages': ['line']
      'callback': -> loadCallback()
    }

    # Set a callback to run when the Google Visualization API is loaded.
    loadCallback = ->

      # Create the data table.
      data = new google.visualization.DataTable()

      data.addColumn 'date', 'Time'
      data.addColumn 'number', 'Load Time'

      data.addRows responseTimes

      # Set chart options
      options = {
        chart: {
          title: 'Latency over time'
          subtitle: 'in milliseconds'
        }
        vAxis: {
          minValue: vMin
          maxValue: vMax
        }
        chartArea: {
          backgroundColor: '#f0f0f0'
        }
        width: 800
        height: 400
        pointSize: 5
      }

      # Instantiate and draw our chart, passing in some options.
      chart = new google.charts.Line document.getElementById 'chart_div'
      chart.draw data, options