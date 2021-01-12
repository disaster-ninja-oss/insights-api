# Insights Api
GraphQL API for calculating correlation between parameters in given polygon in wkt format and lists of numerators and denominators for x and y axes
##Test UI
Test UI is available on http://url/graphiql
##Requests
Get all Earth correlations:
```
{allStatistic(defaultParam: 1){fields}}
```
Get correlations in polygon in GeoJSON (single polygon, feature collection or feature) format and lists of numerators for x and y axes:
```
{polygonStatistic(polygonStatisticRequest:{
    polygon: "{\"type\":\"Polygon\",\"coordinates\":[[[27.2021484375,54.13347814286039],[26.9989013671875,53.82335438174398],[27.79541015625,53.70321053273598],[27.960205078125,53.90110181472825],[28.004150390625,54.081951104880396],[27.6470947265625,54.21707306629117],[27.2021484375,54.13347814286039]]]}",
    xNumeratorList: ["count", "area_km2"],
    yNumeratorList: ["population", "area_km2"]
  }){fields}
```
Fields description https://gist.github.com/Akiyamka/8ad19a8de3c955ac1f27f67281c12fdf#axis