# klarna-demo

Weather data demo for Klarna.

## Usage

FIXME: explanation

    $ java -jar klarna-demo-0.1.0-standalone.jar [args]

## Options

FIXME: listing of options this app accepts.

## Examples

...

## Steps taken

1. `lein new app klarna-demo`
1. `rm -rf CHANGELOG.md doc/ resources/ test/`
1. Create git repo and make initial commit
1. Open `src/klarna_demo/core.clj`
1. How do we make an HTTP call? [clj-http](https://github.com/dakrone/clj-http)
   * `[clj-http "2.2.0"]`
1. How do we decode JSON? [cheshire](https://github.com/dakrone/cheshire)
   * `[cheshire "5.6.1"]`
1. Start a REPL
1. Get location categories
1. Turn them into JSON
1. Get locations
1. Locations by category
1. Extract `get-data`
1. Return `:results` from `get-data`
1. Paging in `get-data`
1. Get weather (metric units!)
1. How do we handle no results in `get-data`?
1. Schema!
   * `[prismatic-schema "1.1.1"]`
1. Extract `(s/defn get-json ...)`
1. `(s/set-fn-validation! true)`
1. Group weather data by station
1. Dates

## License

Copyright © 2016 Josh Glover

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
