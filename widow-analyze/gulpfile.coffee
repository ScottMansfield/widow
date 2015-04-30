
del        = require 'del'
gulp       = require 'gulp'
coffee     = require 'gulp-coffee'
jade       = require 'gulp-jade'
minifyCSS  = require 'gulp-minify-css'
minifyHTML = require 'gulp-minify-html'
ngAnnotate = require 'gulp-ng-annotate'
rename     = require 'gulp-rename'
sass       = require 'gulp-sass'
sourcemaps = require 'gulp-sourcemaps'
uglify     = require 'gulp-uglify'
gutil      = require 'gulp-util'

paths = {
  srcSass:   ['src/main/sass/**/*.sass', 'src/main/sass/**/*.scss']
  destCSS:   'www/css'

  srcCoffee: 'src/main/coffee/**/*.coffee'
  destJS:    'www/js'

  srcJade:   'src/main/jade/**/*.jade'
  destHTML:  'www'
}

gulp.task 'default', ['sass', 'coffee', 'jade']

gulp.task 'sass', (done) ->
  gulp.src paths.srcSass
    .pipe sass({errLogToConsole: true, sourceComments: 'normal'})
      .on 'error', gutil.log
    .pipe gulp.dest paths.destCSS
    .pipe minifyCSS keepSpecialComments: 0
    .pipe rename( (path) -> path.extName = '.min.css' )
    .pipe gulp.dest paths.destCSS
    .on 'end', done
  return undefined

gulp.task 'coffee', (done) ->
  gulp.src paths.srcCoffee
    .pipe sourcemaps.init()
    .pipe coffee({bare: true})
      .on 'error', gutil.log
    .pipe gulp.dest paths.destJS
    .pipe ngAnnotate()
    .pipe uglify()
    .pipe rename( (path) -> path.extname = '.min.js' )
    .pipe sourcemaps.write()
    .pipe gulp.dest paths.destJS
    .on 'end', done
  return undefined

gulp.task 'jade', (done) ->
  gulp.src paths.srcJade
    .pipe jade()
      .on 'error', gutil.log
    .pipe minifyHTML()
    .pipe gulp.dest paths.destHTML
    .on 'end', done
  return undefined

gulp.task 'watch', ->
  gulp.watch paths.srcSass, ['sass']
  gulp.watch paths.srcCoffee, ['coffee']
  gulp.watch paths.srcJade, ['jade']

# Clean up all compile output
gulp.task 'clean', ->
  del [
    paths.destCSS,
    paths.destJS,
    paths.destHTML + '/templates',
    paths.destHTML + '/index.html'
  ]

# Clean up bower libraries
gulp.task 'clean-all', ['clean'], ->
  del [
    'src/main/webapp/lib'
  ]