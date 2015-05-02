utilModule = angular.module 'wa.util'

utilModule.factory 'Encoding', ->

  encode = (input) ->
    btoa encodeURIComponent input

  decode = (input) ->
    decodeURIComponent atob input

  {
    encode: encode
    decode: decode
  }